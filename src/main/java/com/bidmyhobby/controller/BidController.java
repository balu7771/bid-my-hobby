package com.bidmyhobby.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.bidmyhobby.service.BidService;
import com.bidmyhobby.service.ImageService;
import com.bidmyhobby.service.ModerationService;
import com.bidmyhobby.service.NotificationService;
import com.bidmyhobby.service.S3StorageService;

@RestController
@RequestMapping("/api/bid")
public class BidController {

    @Autowired
    private BidService bidService;
    
    @Autowired
    private S3StorageService s3StorageService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ImageService imageService;
    
    @Autowired
    private ModerationService moderationService;
    
    @Value("${admin.secret.key:default-admin-key}")
    private String adminSecretKey;
    
    // Store verification tokens temporarily (in a production app, use a database or cache)
    private final Map<String, String> verificationTokens = new HashMap<>();
    
    @Operation(summary = "Get all the catalog items from S3 bucket")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/allItems")
    public ResponseEntity<?> getAllItems() {
        try {
            List<Map<String, Object>> items = s3StorageService.listAllItems();
            return ResponseEntity.ok().body(items);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error retrieving items: " + e.getMessage());
        }
    }

    @Operation(summary = "Place a bid on an item")
    @ApiResponse(responseCode = "200", description = "Bid placed successfully")
    @PostMapping("/placeBid")
    public ResponseEntity<?> placeBid(@RequestBody Map<String, Object> bidRequest) {
        try {
            String itemId = (String) bidRequest.get("itemId");
            String email = (String) bidRequest.get("email");
            BigDecimal bidAmount = new BigDecimal(bidRequest.get("bidAmount").toString());
            String currency = (String) bidRequest.get("currency");
            
            // Default to USD if currency not provided
            if (currency == null || currency.isEmpty()) {
                currency = "USD";
            }
            
            // Get item metadata to check base price
            Map<String, Object> itemMetadata = s3StorageService.getItemMetadata(itemId);
            BigDecimal basePrice = BigDecimal.ZERO;
            String itemCurrency = "USD";
            
            if (itemMetadata.containsKey("basePrice")) {
                if (itemMetadata.get("basePrice") instanceof Number) {
                    basePrice = new BigDecimal(itemMetadata.get("basePrice").toString());
                }
                
                if (itemMetadata.containsKey("currency")) {
                    itemCurrency = (String) itemMetadata.get("currency");
                }
            }
            
            // Check if item is sold or deleted
            String status = (String) itemMetadata.get("status");
            if ("SOLD".equals(status) || "DELETED".equals(status)) {
                return ResponseEntity.badRequest().body(
                    "This item is no longer available for bidding");
            }
            
            // Check if bid amount is greater than base price
            // Note: In a real app, you would convert currencies if they don't match
            if (currency.equals(itemCurrency) && bidAmount.compareTo(basePrice) <= 0) {
                return ResponseEntity.badRequest().body(
                    "Bid amount must be greater than the base price of " + 
                    basePrice + " " + itemCurrency);
            }
            
            bidService.placeBid(itemId, email, bidAmount, currency);
            return ResponseEntity.ok().body("Bid placed successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error placing bid: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Upload a new hobby item")
    @ApiResponse(responseCode = "200", description = "Item uploaded successfully")
    @PostMapping("/uploadItem")
    public ResponseEntity<?> uploadItem(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("email") String email,
            @RequestParam("basePrice") BigDecimal basePrice,
            @RequestParam("currency") String currency) {
        try {
            String imageDescription = "";
            boolean isAppropriate = true;
            boolean isHobbyItem = true;
            
            try {
                // First, analyze the image using OpenAI's Vision API
                imageDescription = moderationService.analyzeImage(file);
                
                // Only proceed with moderation if we got a valid description
                if (imageDescription != null && !imageDescription.trim().isEmpty()) {
                    // Moderate the image description and check if it's appropriate
                    isAppropriate = moderationService.moderateText(imageDescription);
                    isHobbyItem = moderationService.isHobbyItem(imageDescription);
                }
            } catch (Exception e) {
                // Log the error but continue with upload
                System.err.println("Error during image moderation: " + e.getMessage());
                e.printStackTrace();
                // Set default description if analysis failed
                imageDescription = "Image analysis unavailable. Using user-provided description.";
            }
            
            if (!isAppropriate) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("The image contains inappropriate content and cannot be uploaded.");
            }
            
            if (!isHobbyItem) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("The image does not appear to be a hobby item. Only hobby items can be uploaded.");
            }
            
            // Add watermark to image with masked email
            String maskedEmail = imageService.maskEmail(email);
            byte[] watermarkedImage = imageService.addWatermark(file, "© " + maskedEmail);
            
            // Upload file to S3
            String itemId = s3StorageService.uploadFile(file, watermarkedImage, file.getContentType());
            
            // Save item metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("itemId", itemId);
            metadata.put("name", name);
            metadata.put("description", description);
            metadata.put("email", email);
            metadata.put("basePrice", basePrice);
            metadata.put("currency", currency);
            metadata.put("timestamp", System.currentTimeMillis());
            metadata.put("status", "ACTIVE");
            metadata.put("aiDescription", imageDescription);
            
            s3StorageService.saveItemMetadata(itemId, metadata);
            
            // Automatically subscribe the uploader (using email as userId)
            try {
                s3StorageService.saveUserSubscription(email, email, name);
            } catch (Exception e) {
                // Log but continue if subscription fails
                System.err.println("Failed to subscribe user: " + e.getMessage());
            }
            
            // Send confirmation email to creator (won't throw exception)
            notificationService.sendUploadConfirmation(email, name, itemId);
            
            // Send notifications to all subscribed users (won't throw exception)
            notificationService.notifyAboutNewItem(itemId, name, description);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Item uploaded successfully",
                "itemId", itemId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading item: " + e.getMessage());
        }
    }
    

    @Operation(summary = "Get bids for an item (deprecated)")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping(value = "/getBids/{itemId:.+}", produces = "application/json")
    public ResponseEntity<?> getBidsDeprecated(@PathVariable(value = "itemId") String itemId) {
        try {
            List<Map<String, Object>> bids = bidService.getBidsForItem(itemId);
            return ResponseEntity.ok().body(bids);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(
                Map.of("error", "Error retrieving bids: " + e.getMessage()));
        }
    }

    @Operation(summary = "Subscribe to notifications")
    @ApiResponse(responseCode = "200", description = "Subscription successful")
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> subscriptionRequest) {
        try {
            String email = (String) subscriptionRequest.get("email");
            String name = (String) subscriptionRequest.get("name");
            
            s3StorageService.saveUserSubscription(email, email, name);
            
            // Send confirmation email
            notificationService.sendSubscriptionConfirmation(email);
            
            return ResponseEntity.ok().body("Subscription successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error subscribing: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Admin endpoint to delete all items (admin only)")
    @ApiResponse(responseCode = "200", description = "All items deleted successfully")
    @DeleteMapping("/admin/deleteAll")
    public ResponseEntity<?> deleteAllItems(@RequestHeader("Admin-Key") String adminKey) {
        // Security check using environment variable
        if (!adminSecretKey.equals(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Unauthorized access. Admin privileges required.");
        }
        
        try {
            int deletedCount = s3StorageService.deleteAllItems();
            return ResponseEntity.ok().body(Map.of(
                "message", "Platform reset successful",
                "itemsDeleted", deletedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error deleting all items: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Delete an item")
    @ApiResponse(responseCode = "200", description = "Item deleted successfully")
    @DeleteMapping("/deleteItem/{itemId}")
    public ResponseEntity<?> deleteItem(
            @PathVariable String itemId,
            @RequestParam("email") String email) {
        try {
            System.out.println("Deleting item: " + itemId + " by email: " + email);
            
            // Get item metadata
            Map<String, Object> metadata = s3StorageService.getItemMetadata(itemId);
            
            // Check if item exists
            if (metadata == null || metadata.get("name").equals("Unknown Item")) {
                System.out.println("Item not found: " + itemId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item not found");
            }
            
            // Check if email matches the creator's email
            String creatorEmail = (String) metadata.get("email");
            if (creatorEmail == null || !creatorEmail.equals(email)) {
                System.out.println("Permission denied. Creator: " + creatorEmail + ", Requester: " + email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only the creator can delete this item");
            }
            
            // Use the original itemId from metadata to delete
            String originalItemId = (String) metadata.get("itemId");
            System.out.println("Using original itemId from metadata: " + originalItemId);
            s3StorageService.deleteItem(originalItemId);
            
            return ResponseEntity.ok().body("Item deleted successfully");
        } catch (Exception e) {
            System.err.println("Error deleting item: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error deleting item: " + e.getMessage());
        }
    }

    @Operation(summary = "Get bids for an item")
    @ApiResponse(responseCode = "200", description = "Bids retrieved successfully")
    @GetMapping("/getBids")
    public ResponseEntity<?> getBids(@RequestParam String itemId, @RequestParam(required = false) String email) {
        try {
            // Get item metadata
            Map<String, Object> metadata = s3StorageService.getItemMetadata(itemId);
            
            // Check if item exists
            if (metadata.get("name").equals("Unknown Item")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item not found");
            }
            
            // Check if item is available for bidding
            String status = (String) metadata.get("status");
            if ("DELETED".equals(status)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("This item is no longer available");
            }
            
            List<Map<String, Object>> bids;
            
            // If email is provided and matches the creator's email, return full details
            if (email != null && email.equals(metadata.get("email"))) {
                // Get bids with full details (including emails)
                bids = s3StorageService.getBidsForItem(itemId);
            } else {
                // Get bids with public details (masked emails)
                bids = s3StorageService.getPublicBidsForItem(itemId);
            }
            
            // Return just the bids list
            return ResponseEntity.ok().body(bids);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving bids: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Get item details with bids")
    @ApiResponse(responseCode = "200", description = "Item details with bids retrieved successfully")
    @GetMapping("/getItemWithBids")
    public ResponseEntity<?> getItemWithBids(@RequestParam String itemId) {
        try {
            // Get item metadata
            Map<String, Object> metadata = s3StorageService.getItemMetadata(itemId);
            
            // Check if item exists
            if (metadata.get("name").equals("Unknown Item")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item not found");
            }
            
            // Check if item is available for bidding
            String status = (String) metadata.get("status");
            if ("DELETED".equals(status)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("This item is no longer available");
            }
            
            // Get bids with public details (masked emails)
            List<Map<String, Object>> bids = s3StorageService.getPublicBidsForItem(itemId);
            
            // Create response with item and bid details
            Map<String, Object> response = new HashMap<>();
            response.put("item", metadata);
            response.put("bids", bids);
            
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving item with bids: " + e.getMessage());
        }
    }
    
    // Keep the existing methods for backward compatibility and for creators to see full details
    @Operation(summary = "Request verification to view detailed bids on creator's item")
    @ApiResponse(responseCode = "200", description = "Verification email sent")
    @PostMapping("/requestBidAccess/{itemId}")
    public ResponseEntity<?> requestBidAccess(
            @PathVariable String itemId,
            @RequestParam("email") String email) {
        try {
            // Get item metadata
            Map<String, Object> metadata = s3StorageService.getItemMetadata(itemId);
            
            // Check if item exists
            if (metadata.get("name").equals("Unknown Item")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item not found");
            }
            
            // Check if email matches the creator's email
            String creatorEmail = (String) metadata.get("email");
            if (creatorEmail == null || !creatorEmail.equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only the creator can access detailed bid information");
            }
            
            // Generate verification token
            String token = UUID.randomUUID().toString();
            verificationTokens.put(token, itemId + ":" + email);
            
            // Send verification email
            notificationService.sendVerificationEmail(email, token);
            
            return ResponseEntity.ok().body("Verification email sent. Please check your inbox.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing request: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Verify email and get detailed bids for creator's item")
    @ApiResponse(responseCode = "200", description = "Detailed bids retrieved successfully")
    @GetMapping("/verifyAndGetBids/{token}")
    public ResponseEntity<?> verifyAndGetBids(@PathVariable String token) {
        try {
            // Verify token
            String value = verificationTokens.get(token);
            if (value == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired verification token");
            }
            
            // Parse token value
            String[] parts = value.split(":");
            String itemId = parts[0];
            String email = parts[1];
            
            // Remove token after use
            verificationTokens.remove(token);
            
            // Get item metadata
            Map<String, Object> metadata = s3StorageService.getItemMetadata(itemId);
            
            // Get bids with full details (including emails)
            List<Map<String, Object>> bids = s3StorageService.getBidsForItem(itemId);
            
            // Ensure we're not mixing emails - each bid should have its own bidder's email
            for (Map<String, Object> bid : bids) {
                // Make sure userId field contains the actual bidder's email, not the creator's email
                if (!bid.containsKey("userId") || bid.get("userId") == null) {
                    bid.put("userId", "Unknown");
                }
            }
            
            // Create response with item and bid details
            Map<String, Object> response = new HashMap<>();
            response.put("item", metadata);
            response.put("bids", bids);
            
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving bids: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Mark an item as sold")
    @ApiResponse(responseCode = "200", description = "Item marked as sold successfully")
    @PostMapping("/markAsSold/{itemId}")
    public ResponseEntity<?> markAsSold(
            @PathVariable String itemId,
            @RequestParam("email") String email) {
        try {
            System.out.println("Marking item as sold: " + itemId + " by email: " + email);
            
            // Get item metadata
            Map<String, Object> metadata = s3StorageService.getItemMetadata(itemId);
            
            // Check if item exists
            if (metadata == null || metadata.get("name").equals("Unknown Item")) {
                System.out.println("Item not found: " + itemId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item not found");
            }
            
            // Check if email matches the creator's email
            String creatorEmail = (String) metadata.get("email");
            if (creatorEmail == null || !creatorEmail.equals(email)) {
                System.out.println("Permission denied. Creator: " + creatorEmail + ", Requester: " + email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only the creator can mark this item as sold");
            }
            
            // Update status
            metadata.put("status", "SOLD");
            metadata.put("soldAt", System.currentTimeMillis());
            
            // Use the original itemId from metadata to save
            String originalItemId = (String) metadata.get("itemId");
            System.out.println("Using original itemId from metadata: " + originalItemId);
            s3StorageService.saveItemMetadata(originalItemId, metadata);
            
            return ResponseEntity.ok().body("Item marked as sold successfully");
        } catch (Exception e) {
            System.err.println("Error marking item as sold: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error marking item as sold: " + e.getMessage());
        }
    }
}