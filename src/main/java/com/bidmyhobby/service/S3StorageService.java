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
            String metadataKey = metadataPrefix + "items/" + itemId + ".json";
            return getJsonFromS3(metadataKey);
        } catch (Exception e) {
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
                            continue;
                        }
                        
                        // Add URL to the metadata
                        itemMetadata.put("url", getPresignedUrl(itemId));
                        
                        // Get bids for this item
                        List<Map<String, Object>> bids = getBidsForItem(itemId);
                        itemMetadata.put("bids", bids);
                        
                        items.add(itemMetadata);
                    } catch (Exception e) {
                        // Skip invalid item metadata
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing items from S3: " + e.getMessage());
            e.printStackTrace();
        }
        
        return items;
    }
    
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
    
    public void deleteItem(String itemId) throws IOException {
        try {
            // Get item metadata to find the image file
            Map<String, Object> metadata = getItemMetadata(itemId);
            
            // Mark item as deleted in metadata
            metadata.put("status", "DELETED");
            metadata.put("deletedAt", System.currentTimeMillis());
            saveItemMetadata(itemId, metadata);
            
            // Delete the image file (optional - you can keep it but just mark as deleted in metadata)
            // DeleteObjectRequest deleteImageRequest = DeleteObjectRequest.builder()
            //     .bucket(bucketName)
            //     .key(itemId)
            //     .build();
            // s3Client.deleteObject(deleteImageRequest);
        } catch (Exception e) {
            throw new IOException("Failed to delete item: " + e.getMessage(), e);
        }
    }

    private String getPresignedUrl(String key) {
        // Use direct URL for public objects
        return "https://" + bucketName + ".s3.ap-south-1.amazonaws.com/" + key;
    }

}