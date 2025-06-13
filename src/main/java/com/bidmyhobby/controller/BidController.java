package com.bidmyhobby.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bidmyhobby.service.BidService;
import com.bidmyhobby.service.ImageService;
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
    
    @Operation(summary = "Subscribe to notifications")
    @ApiResponse(responseCode = "200", description = "Subscription successful")
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> subscriptionRequest) {
        try {
            String email = (String) subscriptionRequest.get("email");
            String name = (String) subscriptionRequest.get("name");
            
            s3StorageService.saveUserSubscription(email, email, name);
            
            return ResponseEntity.ok().body("Subscription successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error subscribing: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Delete an item")
    @ApiResponse(responseCode = "200", description = "Item deleted successfully")
    @DeleteMapping("/deleteItem/{itemId}")
    public ResponseEntity<?> deleteItem(
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
                    .body("Only the creator can delete this item");
            }
            
            // Delete the item
            s3StorageService.deleteItem(itemId);
            
            return ResponseEntity.ok().body("Item deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting item: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Mark an item as sold")
    @ApiResponse(responseCode = "200", description = "Item marked as sold successfully")
    @PostMapping("/markAsSold/{itemId}")
    public ResponseEntity<?> markAsSold(
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
                    .body("Only the creator can mark this item as sold");
            }
            
            // Update status
            metadata.put("status", "SOLD");
            metadata.put("soldAt", System.currentTimeMillis());
            s3StorageService.saveItemMetadata(itemId, metadata);
            
            return ResponseEntity.ok().body("Item marked as sold successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error marking item as sold: " + e.getMessage());
        }
    }
}