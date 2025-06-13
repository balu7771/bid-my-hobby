package com.bidmyhobby.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final SesClient sesClient;
    private final S3StorageService s3StorageService;
    
    @Value("${aws.ses.from-email}")
    private String fromEmail;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public NotificationService(SesClient sesClient, S3StorageService s3StorageService) {
        this.sesClient = sesClient;
        this.s3StorageService = s3StorageService;
    }

    public void notifyAboutNewItem(String itemId, String itemName, String description) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would have sent new item notification for: {}", itemName);
            return;
        }
        
        try {
            // Get all subscribed users
            List<Map<String, Object>> users = s3StorageService.getSubscribedUsers();
            
            for (Map<String, Object> user : users) {
                String email = (String) user.get("email");
                try {
                    sendEmail(email, 
                             "New Item Available for Bidding: " + itemName,
                             "A new item is available for bidding!\n\n" +
                             "Item: " + itemName + "\n" +
                             "Description: " + description + "\n\n" +
                             "Visit our site to place your bid!");
                } catch (Exception e) {
                    logger.error("Failed to send notification to {}: {}", email, e.getMessage());
                    // Continue with other notifications
                }
            }
        } catch (Exception e) {
            logger.error("Failed to send notifications: {}", e.getMessage());
            // Don't throw exception to prevent upload failure
        }
    }
    
    public void notifyAboutNewBid(String itemId, String itemName, double bidAmount) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would have sent bid notification for: {}", itemName);
            return;
        }
        
        try {
            // Get item creator
            Map<String, Object> itemMetadata = s3StorageService.getItemMetadata(itemId);
            String creatorEmail = (String) itemMetadata.get("email");
            
            if (creatorEmail != null) {
                try {
                    sendEmail(creatorEmail,
                             "New Bid on Your Item: " + itemName,
                             "Someone placed a new bid on your item!\n\n" +
                             "Item: " + itemName + "\n" +
                             "Bid Amount: $" + bidAmount + "\n\n" +
                             "Visit our site to see all bids!");
                } catch (Exception e) {
                    logger.error("Failed to send bid notification to {}: {}", creatorEmail, e.getMessage());
                    // Don't throw exception to prevent bid failure
                }
            }
        } catch (Exception e) {
            logger.error("Failed to send bid notification: {}", e.getMessage());
            // Don't throw exception to prevent bid failure
        }
    }
    
    public void sendUploadConfirmation(String email, String itemName, String itemId) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would have sent upload confirmation to: {}", email);
            return;
        }
        
        try {
            sendEmail(email,
                     "Your Item Has Been Successfully Uploaded: " + itemName,
                     "Thank you for uploading your item to Bid My Hobby!\n\n" +
                     "Item: " + itemName + "\n" +
                     "Item ID: " + itemId + "\n\n" +
                     "Your item is now available for bidding. You will receive notifications when bids are placed.\n\n" +
                     "You have been automatically subscribed to receive notifications about new items.");
            logger.info("Upload confirmation email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send upload confirmation to {}: {}", email, e.getMessage());
            // Don't throw exception to prevent upload failure
        }
    }
    
    private void sendEmail(String toEmail, String subject, String body) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder().toAddresses(toEmail).build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder().text(Content.builder().data(body).build()).build())
                    .build())
                .build();
                
            sesClient.sendEmail(request);
        } catch (Exception e) {
            logger.error("Error sending email to {}: {}", toEmail, e.getMessage());
            throw e; // Re-throw to be handled by calling methods
        }
    }
}