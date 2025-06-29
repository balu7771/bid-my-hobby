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
                             "To see all bids and bidder details, please verify your email by visiting our site.");
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
    
    public void sendVerificationEmail(String email, String token) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would have sent verification email to: {}", email);
            return;
        }
        
        try {
            String verificationLink = "https://bidmyhobby.com/api/bid/verifyAndGetBids/" + token;
            
            sendEmail(email,
                     "Verify Email to View Bids on Your Item",
                     "Please verify your email to view all bids on your item.\n\n" +
                     "Click the link below to verify your email and view all bid details:\n" +
                     verificationLink + "\n\n" +
                     "This link will expire after use or within 24 hours.\n\n" +
                     "If you did not request this, please ignore this email.");
            
            logger.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
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

    public void sendSubscriptionConfirmation(String email) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would have sent subscription confirmation to: {}", email);
            return;
        }
        
        try {
            sendEmail(email,
                     "Subscription Confirmed - Bid My Hobby",
                     "Thank you for subscribing to Bid My Hobby notifications!\n\n" +
                     "You will now receive notifications about new items available for bidding.\n\n" +
                     "Happy bidding!");
            logger.info("Subscription confirmation email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send subscription confirmation to {}: {}", email, e.getMessage());
        }
    }
    
    // Payment notification methods
    public void notifyInitialPaymentComplete(String creatorEmail, String bidderEmail, String itemName, Map<String, Object> paymentData) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would have sent initial payment notification");
            return;
        }
        
        try {
            // Notify creator
            sendEmail(creatorEmail,
                     "Initial Payment Received - " + itemName,
                     "Great news! The bidder has made the initial payment for your item.\n\n" +
                     "Item: " + itemName + "\n" +
                     "Payment Amount: ₹" + paymentData.get("amount") + "\n" +
                     "Bidder: " + maskEmail(bidderEmail) + "\n\n" +
                     "Your item is now marked as 'Sale in Progress'. Please prepare for shipping.");
            
            // Notify bidder
            sendEmail(bidderEmail,
                     "Payment Confirmed - " + itemName,
                     "Your initial payment has been confirmed!\n\n" +
                     "Item: " + itemName + "\n" +
                     "Payment Amount: ₹" + paymentData.get("amount") + "\n\n" +
                     "The creator will prepare your item for shipping. You'll be notified when it's shipped.");
                     
        } catch (Exception e) {
            logger.error("Failed to send initial payment notifications: {}", e.getMessage());
        }
    }
    
    public void notifyShippingPaymentComplete(String creatorEmail, String bidderEmail, String itemName, Map<String, Object> paymentData) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would have sent shipping payment notification");
            return;
        }
        
        try {
            // Notify creator
            sendEmail(creatorEmail,
                     "Shipping Payment Received - " + itemName,
                     "The bidder has made the shipping payment.\n\n" +
                     "Item: " + itemName + "\n" +
                     "Payment Amount: ₹" + paymentData.get("amount") + "\n\n" +
                     "Please confirm delivery once the item reaches the bidder.");
            
            // Notify bidder
            sendEmail(bidderEmail,
                     "Shipping Payment Confirmed - " + itemName,
                     "Your shipping payment has been confirmed!\n\n" +
                     "Item: " + itemName + "\n" +
                     "Payment Amount: ₹" + paymentData.get("amount") + "\n\n" +
                     "Your item is on its way. You'll receive the final payment request upon delivery.");
                     
        } catch (Exception e) {
            logger.error("Failed to send shipping payment notifications: {}", e.getMessage());
        }
    }
    
    public void notifyFinalPaymentComplete(String creatorEmail, String bidderEmail, String itemName, Map<String, Object> paymentData) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would have sent final payment notification");
            return;
        }
        
        try {
            // Notify creator
            sendEmail(creatorEmail,
                     "Sale Completed - " + itemName,
                     "Congratulations! Your sale has been completed.\n\n" +
                     "Item: " + itemName + "\n" +
                     "Final Payment: ₹" + paymentData.get("amount") + "\n\n" +
                     "Thank you for using Bid My Hobby!");
            
            // Notify bidder
            sendEmail(bidderEmail,
                     "Purchase Complete - " + itemName,
                     "Congratulations on your purchase!\n\n" +
                     "Item: " + itemName + "\n" +
                     "Final Payment: ₹" + paymentData.get("amount") + "\n\n" +
                     "Enjoy your new item and thank you for using Bid My Hobby!");
                     
        } catch (Exception e) {
            logger.error("Failed to send final payment notifications: {}", e.getMessage());
        }
    }
    
    public void notifyCreatorPlatformFeePayment(String creatorEmail, String itemName, Map<String, Object> paymentData) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would have sent creator platform fee notification");
            return;
        }
        
        try {
            sendEmail(creatorEmail,
                     "Platform Fee Payment Confirmed - " + itemName,
                     "Thank you for your platform fee payment!\n\n" +
                     "Item: " + itemName + "\n" +
                     "Platform Fee: ₹" + paymentData.get("amount") + "\n\n" +
                     "Your item is now fully active on our platform and available for bidding.\n\n" +
                     "Thank you for using Bid My Hobby!");
                     
        } catch (Exception e) {
            logger.error("Failed to send creator platform fee notification: {}", e.getMessage());
        }
    }
    
    private String maskEmail(String email) {
        if (email == null) return null;
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        String maskedUsername = username.substring(0, 2) +
                "*".repeat(Math.max(0, username.length() - 3)) +
                username.substring(username.length() - 1);
        return maskedUsername + domain;
    }
}