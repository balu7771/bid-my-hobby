package com.bidmyhobby.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.*;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.bucket.metadata-prefix}")
    private String metadataPrefix;

    @Value("${aws.s3.bucket.images-prefix}")
    private String imagesPrefix;

    @Value("${aws.s3.bucket.users-prefix:users/}")
    private String usersPrefix;
    
    @Value("${aws.s3.bucket.payments-prefix:payments/}")
    private String paymentsPrefix;
    
    @Value("${aws.s3.bucket.sales-prefix:sales/}")
    private String salesPrefix;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
        this.objectMapper = new ObjectMapper();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, null, null);
    }

    public String uploadFile(MultipartFile file, byte[] processedImageBytes, String contentType) throws IOException {
        String filename = imagesPrefix + System.currentTimeMillis() + "-" + file.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .contentType(contentType != null ? contentType : file.getContentType())
                .build();

        if (processedImageBytes != null) {
            s3Client.putObject(request, RequestBody.fromBytes(processedImageBytes));
        } else {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        }

        return filename;
    }

    public void saveBidMetadata(String itemId, String userId, double bidAmount, String currency) throws IOException {
        String metadataKey = metadataPrefix + "bids/" + itemId + "/" + UUID.randomUUID() + ".json";

        Map<String, Object> bidData = new HashMap<>();
        bidData.put("itemId", itemId);
        bidData.put("userId", userId);
        bidData.put("bidAmount", bidAmount);
        bidData.put("currency", currency);
        bidData.put("timestamp", System.currentTimeMillis());

        saveJsonToS3(metadataKey, bidData);
    }

    public void saveBidMetadata(String itemId, String userId, double bidAmount) throws IOException {
        // Default to USD for backward compatibility
        saveBidMetadata(itemId, userId, bidAmount, "USD");
    }

    public void saveItemMetadata(String itemId, Map<String, Object> metadata) throws IOException {
        String metadataKey = metadataPrefix + "items/" + itemId + ".json";
        saveJsonToS3(metadataKey, metadata);
    }

    public void saveUserSubscription(String userId, String email, String name) throws IOException {
        String userKey = usersPrefix + userId + ".json";

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("email", email);
        userData.put("name", name);
        userData.put("subscribed", true);
        userData.put("timestamp", System.currentTimeMillis());

        saveJsonToS3(userKey, userData);
    }

    private void saveJsonToS3(String key, Object data) throws IOException {
        String jsonContent = objectMapper.writeValueAsString(data);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/json")
                .build();

        s3Client.putObject(request, RequestBody.fromString(jsonContent));
    }

    public Map<String, Object> getItemMetadata(String itemId) throws IOException {
        try {
            // Try to find metadata by listing all metadata files
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(metadataPrefix + "items/")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            // First try with the exact itemId
            String metadataKey = metadataPrefix + "items/" + itemId + ".json";
            System.out.println("Looking for metadata with key: " + metadataKey);

            try {
                return getJsonFromS3(metadataKey);
            } catch (Exception e) {
                // If not found, try to find by matching the itemId in the metadata
                System.out.println("Metadata not found with direct key, searching in all metadata...");

                for (S3Object s3Object : listResponse.contents()) {
                    try {
                        Map<String, Object> metadata = getJsonFromS3(s3Object.key());
                        String storedItemId = (String) metadata.get("itemId");

                        // Check if this metadata matches our itemId
                        if (storedItemId != null) {
                            // Check for exact match or if the stored itemId contains our itemId
                            if (storedItemId.equals(itemId) ||
                                    (storedItemId.contains(itemId)) ||
                                    (itemId.contains(storedItemId.substring(storedItemId.lastIndexOf('/') + 1)))) {
                                System.out.println("Found matching metadata: " + s3Object.key() + " for itemId: " + itemId);
                                return metadata;
                            }
                        }
                    } catch (Exception ex) {
                        // Skip invalid metadata
                        continue;
                    }
                }

                // If we get here, no matching metadata was found
                throw new IOException("No matching metadata found for itemId: " + itemId);
            }
        } catch (Exception e) {
            System.err.println("Error getting metadata for item: " + itemId + ", Error: " + e.getMessage());
            // Return empty metadata if not found
            Map<String, Object> emptyMetadata = new HashMap<>();
            emptyMetadata.put("itemId", itemId);
            emptyMetadata.put("name", "Unknown Item");
            return emptyMetadata;
        }
    }

    public Map<String, Object> getUserById(String userId) throws IOException {
        String userKey = usersPrefix + userId + ".json";
        try {
            return getJsonFromS3(userKey);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Map<String, Object>> getSubscribedUsers() {
        List<Map<String, Object>> users = new ArrayList<>();

        try {
            // Check if bucket exists first
            try {
                HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.headBucket(headBucketRequest);
            } catch (NoSuchBucketException e) {
                // Create bucket if it doesn't exist
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.createBucket(createBucketRequest);

                // Return empty list since no users exist yet
                return users;
            }

            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(usersPrefix)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            for (S3Object s3Object : listResponse.contents()) {
                try {
                    Map<String, Object> userData = getJsonFromS3(s3Object.key());
                    if (Boolean.TRUE.equals(userData.get("subscribed"))) {
                        users.add(userData);
                    }
                } catch (Exception e) {
                    // Skip invalid user data
                }
            }
        } catch (Exception e) {
            // Return empty list if any error occurs
            System.err.println("Error getting subscribed users: " + e.getMessage());
        }

        return users;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getJsonFromS3(String key) throws IOException {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        String content = s3Client.getObjectAsBytes(getRequest).asUtf8String();
        return objectMapper.readValue(content, Map.class);
    }

    public List<Map<String, Object>> listAllItems() {
        List<Map<String, Object>> items = new ArrayList<>();

        try {
            // Check if bucket exists first
            try {
                HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.headBucket(headBucketRequest);
            } catch (NoSuchBucketException e) {
                // Create bucket if it doesn't exist
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.createBucket(createBucketRequest);

                // Return empty list since no items exist yet
                return items;
            }

            // First try to list item metadata
            ListObjectsV2Request metadataRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(metadataPrefix + "items/")
                    .build();

            ListObjectsV2Response metadataResponse = s3Client.listObjectsV2(metadataRequest);

            if (metadataResponse.contents().isEmpty()) {
                // If no metadata, list images directly
                ListObjectsV2Request imagesRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(imagesPrefix)
                        .build();

                ListObjectsV2Response imagesResponse = s3Client.listObjectsV2(imagesRequest);

                for (S3Object s3Object : imagesResponse.contents()) {
                    String key = s3Object.key();
                    String itemId = key.substring(imagesPrefix.length());

                    // Check if there's metadata for this item
                    try {
                        Map<String, Object> metadata = getItemMetadata(key);
                        // Skip if item is marked as deleted
                        if ("DELETED".equals(metadata.get("status"))) {
                            continue;
                        }
                    } catch (Exception e) {
                        // No metadata or error, continue with default
                    }

                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("itemId", itemId);
                    itemInfo.put("name", itemId.substring(itemId.indexOf('-') + 1));
                    itemInfo.put("size", s3Object.size());
                    itemInfo.put("lastModified", s3Object.lastModified().toString());
                    itemInfo.put("url", getPresignedUrl(key));

                    // Try to get bids for this item
                    try {
                        List<Map<String, Object>> bids = getBidsForItem(itemId);
                        itemInfo.put("bids", bids);
                    } catch (Exception e) {
                        itemInfo.put("bids", Collections.emptyList());
                    }

                    items.add(itemInfo);
                }
            } else {
                // Use metadata if available
                for (S3Object s3Object : metadataResponse.contents()) {
                    try {
                        Map<String, Object> itemMetadata = getJsonFromS3(s3Object.key());
                        String itemId = (String) itemMetadata.get("itemId");

                        // Skip items that are marked as deleted
                        if ("DELETED".equals(itemMetadata.get("status"))) {
                            System.out.println("Skipping deleted item: " + itemId);
                            continue;
                        }

                        // Check if the image file exists
                        try {
                            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(itemId)
                                    .build();
                            s3Client.headObject(headObjectRequest);
                        } catch (Exception e) {
                            // Image file doesn't exist, skip this item
                            System.out.println("Skipping item with missing image file: " + itemId);
                            continue;
                        }

                        // Add URL to the metadata
                        itemMetadata.put("url", getPresignedUrl(itemId));

                        // Get public bids for this item (with masked emails)
                        List<Map<String, Object>> bids = getPublicBidsForItem(itemId);
                        itemMetadata.put("bids", bids);
                        
                        // Add like count
                        itemMetadata.put("likes", getLikeCount(itemId));

                        items.add(itemMetadata);
                    } catch (Exception e) {
                        // Skip invalid item metadata
                        System.err.println("Error processing metadata: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing items from S3: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Gets all bids for an item with full details (including email)
     * This method is used by the creator after email verification
     */
    public List<Map<String, Object>> getBidsForItem(String itemId) {
        List<Map<String, Object>> bids = new ArrayList<>();

        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(metadataPrefix + "bids/" + itemId + "/")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            for (S3Object s3Object : listResponse.contents()) {
                try {
                    Map<String, Object> bidData = getJsonFromS3(s3Object.key());
                    
                    // Ensure the userId field contains the bidder's email
                    // This is important to prevent mixing with creator's email
                    if (bidData.containsKey("userId")) {
                        // The userId field already contains the bidder's email
                        // No need to modify it
                    } else {
                        // If userId is missing, mark it as unknown
                        bidData.put("userId", "Unknown");
                    }
                    
                    bids.add(bidData);
                } catch (Exception e) {
                    // Skip invalid bid data
                }
            }

            // Sort bids by amount (highest first)
            bids.sort((b1, b2) -> {
                Double amount1 = (Double) b1.get("bidAmount");
                Double amount2 = (Double) b2.get("bidAmount");
                return amount2.compareTo(amount1);
            });

        } catch (Exception e) {
            // Return empty list if any error occurs
            System.err.println("Error getting bids for item: " + itemId + ", " + e.getMessage());
        }

        return bids;
    }

    /**
     * Gets bids for an item with limited details (no email)
     * This method is used for public listing
     */
    public List<Map<String, Object>> getPublicBidsForItem(String itemId) {
        List<Map<String, Object>> bids = getBidsForItem(itemId);

        // Remove sensitive information (email) from each bid
        for (Map<String, Object> bid : bids) {
            // Replace email with masked version or remove it
            if (bid.containsKey("userId")) {
                String email = (String) bid.get("userId");
                bid.put("userId", maskEmail(email));
            }
        }

        return bids;
    }

    private String maskEmail(String email) {
        if (email == null) return null;

        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email; // Too short to mask

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        // Show first 2 chars and last char of username
        String maskedUsername = username.substring(0, 2) +
                "*".repeat(Math.max(0, username.length() - 3)) +
                username.substring(username.length() - 1);

        return maskedUsername + domain;
    }

    public void deleteItem(String itemId) throws IOException {
        try {
            System.out.println("Deleting item in S3StorageService: " + itemId);

            // Get item metadata to find the image file
            Map<String, Object> metadata = getItemMetadata(itemId);

            if (metadata == null || metadata.get("name").equals("Unknown Item")) {
                throw new IOException("Item not found: " + itemId);
            }

            // Mark item as deleted in metadata
            metadata.put("status", "DELETED");
            metadata.put("deletedAt", System.currentTimeMillis());

            // Handle case where itemId is the full path
            String metadataItemId = itemId;
            if (itemId.startsWith(imagesPrefix)) {
                metadataItemId = itemId.substring(imagesPrefix.length());
            }

            // Get the original itemId from metadata to ensure we're using the correct key
            String originalItemId = (String) metadata.get("itemId");
            System.out.println("Original itemId from metadata: " + originalItemId);

            // Save the updated metadata
            saveItemMetadata(originalItemId, metadata);
            System.out.println("Item marked as deleted successfully: " + originalItemId);

            // Actually delete the image file from S3
            try {
                DeleteObjectRequest deleteImageRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(originalItemId)
                        .build();
                s3Client.deleteObject(deleteImageRequest);
                System.out.println("Image file deleted from S3: " + originalItemId);
            } catch (Exception e) {
                System.err.println("Warning: Could not delete image file: " + e.getMessage());
                // Continue even if image deletion fails
            }
        } catch (Exception e) {
            System.err.println("Failed to delete item: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to delete item: " + e.getMessage(), e);
        }
    }

    private String getPresignedUrl(String key) {
        // Use direct URL for public objects
        return "https://" + bucketName + ".s3.ap-south-1.amazonaws.com/" + key;
    }
    
    public String getImageUrl(String key) {
        return getPresignedUrl(key);
    }

    /**
     * Deletes all items from the platform (admin only)
     * This is a powerful method that should be used with caution
     * @return The number of items deleted
     */
    public int deleteAllItems() throws IOException {
        int deletedCount = 0;

        try {
            // List all item metadata
            ListObjectsV2Request metadataRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(metadataPrefix + "items/")
                    .build();

            ListObjectsV2Response metadataResponse = s3Client.listObjectsV2(metadataRequest);

            // Process each item
            for (S3Object s3Object : metadataResponse.contents()) {
                try {
                    Map<String, Object> itemMetadata = getJsonFromS3(s3Object.key());
                    String itemId = (String) itemMetadata.get("itemId");

                    // Delete the item
                    deleteItem(itemId);
                    deletedCount++;
                } catch (Exception e) {
                    System.err.println("Error deleting item: " + e.getMessage());
                    // Continue with next item
                }
            }

            System.out.println("Successfully deleted " + deletedCount + " items");
        } catch (Exception e) {
            System.err.println("Error deleting all items: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to delete all items: " + e.getMessage(), e);
        }

        return deletedCount;
    }
    
    public void likeItem(String itemId, String email) throws IOException {
        String likeKey = metadataPrefix + "likes/" + itemId + "/" + email.replace("@", "_at_") + ".json";
        
        Map<String, Object> likeData = new HashMap<>();
        likeData.put("itemId", itemId);
        likeData.put("email", email);
        likeData.put("timestamp", System.currentTimeMillis());
        
        saveJsonToS3(likeKey, likeData);
    }
    
    public void unlikeItem(String itemId, String email) throws IOException {
        String likeKey = metadataPrefix + "likes/" + itemId + "/" + email.replace("@", "_at_") + ".json";
        
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(likeKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            // Like doesn't exist, ignore
        }
    }
    
    public int getLikeCount(String itemId) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(metadataPrefix + "likes/" + itemId + "/")
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            return listResponse.contents().size();
        } catch (Exception e) {
            return 0;
        }
    }
    
    public boolean isLikedByUser(String itemId, String email) {
        String likeKey = metadataPrefix + "likes/" + itemId + "/" + email.replace("@", "_at_") + ".json";
        
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(likeKey)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public List<Map<String, Object>> getItemsByCreator(String email) {
        List<Map<String, Object>> creatorItems = new ArrayList<>();
        
        try {
            ListObjectsV2Request metadataRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(metadataPrefix + "items/")
                    .build();
            
            ListObjectsV2Response metadataResponse = s3Client.listObjectsV2(metadataRequest);
            
            for (S3Object s3Object : metadataResponse.contents()) {
                try {
                    Map<String, Object> itemMetadata = getJsonFromS3(s3Object.key());
                    String creatorEmail = (String) itemMetadata.get("email");
                    
                    if (email.equals(creatorEmail)) {
                        String itemId = (String) itemMetadata.get("itemId");
                        itemMetadata.put("url", getPresignedUrl(itemId));
                        itemMetadata.put("likes", getLikeCount(itemId));
                        itemMetadata.put("bids", getBidsForItem(itemId));
                        creatorItems.add(itemMetadata);
                    }
                } catch (Exception e) {
                    // Skip invalid metadata
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting creator items: " + e.getMessage());
        }
        
        return creatorItems;
    }
    
    // Payment-related methods
    public void savePaymentData(String orderId, Map<String, Object> paymentData) throws IOException {
        String paymentKey = metadataPrefix + "payments/" + orderId + ".json";
        saveJsonToS3(paymentKey, paymentData);
    }
    
    public Map<String, Object> getPaymentData(String orderId) throws IOException {
        String paymentKey = metadataPrefix + "payments/" + orderId + ".json";
        try {
            return getJsonFromS3(paymentKey);
        } catch (Exception e) {
            return null;
        }
    }
    
    public void saveSaleData(String itemId, Map<String, Object> saleData) throws IOException {
        String saleKey = metadataPrefix + "sales/" + itemId + ".json";
        saveJsonToS3(saleKey, saleData);
    }
    
    public Map<String, Object> getSaleData(String itemId) throws IOException {
        String saleKey = metadataPrefix + "sales/" + itemId + ".json";
        try {
            return getJsonFromS3(saleKey);
        } catch (Exception e) {
            return null;
        }
    }

}