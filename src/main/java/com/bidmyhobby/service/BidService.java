package com.bidmyhobby.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class BidService {

    @Autowired
    private S3StorageService s3StorageService;
    
    @Autowired
    private NotificationService notificationService;

    public void placeBid(String itemId, String email, BigDecimal amount) {
        placeBid(itemId, email, amount, "USD");
    }
    
    public void placeBid(String itemId, String email, BigDecimal amount, String currency) {
        try {
            // Get item metadata to validate bid
            Map<String, Object> itemMetadata = s3StorageService.getItemMetadata(itemId);
            String itemName = (String) itemMetadata.get("name");
            
            // Check if item exists
            if (itemName == null || itemName.equals("Unknown Item")) {
                throw new RuntimeException("Item not found");
            }
            
            // Save bid metadata
            s3StorageService.saveBidMetadata(itemId, email, amount.doubleValue(), currency);
            
            // Send notification to item creator
            notificationService.notifyAboutNewBid(itemId, itemName, amount.doubleValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to place bid: " + e.getMessage(), e);
        }
    }
    
    public List<Map<String, Object>> getBidsForItem(String itemId) {
        try {
            return s3StorageService.getBidsForItem(itemId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get bids: " + e.getMessage(), e);
        }
    }
}