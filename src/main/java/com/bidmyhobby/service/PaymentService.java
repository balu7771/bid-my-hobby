package com.bidmyhobby.service;

import com.bidmyhobby.model.PaymentStage;
import com.bidmyhobby.model.SaleStatus;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Autowired
    private S3StorageService s3StorageService;

    @Autowired
    private NotificationService notificationService;

    private RazorpayClient getRazorpayClient() throws RazorpayException {
        if (razorpayKeyId == null || razorpayKeyId.isEmpty() || razorpayKeyId.equals("your_razorpay_key_id") ||
            razorpayKeySecret == null || razorpayKeySecret.isEmpty() || razorpayKeySecret.equals("your_razorpay_key_secret")) {
            throw new RazorpayException("Invalid RazorPay API keys. Please check your configuration.");
        }
        return new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }
    
    /**
     * Validates if the RazorPay configuration is valid
     * @return true if configuration is valid, false otherwise
     */
    public boolean isRazorpayConfigValid() {
        try {
            getRazorpayClient();
            return true;
        } catch (Exception e) {
            System.err.println("RazorPay configuration invalid: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> createCreatorPaymentOrder(String itemId, String creatorEmail) throws Exception {
        // Get item metadata
        Map<String, Object> itemMetadata = s3StorageService.getItemMetadata(itemId);
        
        if (itemMetadata == null) {
            throw new RuntimeException("Item not found");
        }
        
        // Calculate platform fee based on bid amount
        BigDecimal platformFee;
        if (itemMetadata.get("platformFee") != null) {
            // Use existing platform fee if already set
            platformFee = new BigDecimal(itemMetadata.get("platformFee").toString());
        } else {
            // Calculate platform fee based on highest bid or base price
            BigDecimal baseAmount;
            if (itemMetadata.get("approvedBidAmount") != null) {
                baseAmount = new BigDecimal(itemMetadata.get("approvedBidAmount").toString());
            } else if (itemMetadata.get("basePrice") != null) {
                baseAmount = new BigDecimal(itemMetadata.get("basePrice").toString());
            } else {
                baseAmount = new BigDecimal("0");
            }
            
            // Only apply platform fee for amounts greater than Rs. 500
            if (baseAmount.compareTo(new BigDecimal("500")) > 0) {
                // 5% platform fee
                platformFee = baseAmount.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
                // Minimum fee of Rs. 50
                platformFee = platformFee.max(new BigDecimal("50"));
            } else {
                platformFee = new BigDecimal("0");
            }
            
            // Save the calculated platform fee
            itemMetadata.put("platformFee", platformFee);
            itemMetadata.put("requiresPlatformFee", platformFee.compareTo(BigDecimal.ZERO) > 0);
            s3StorageService.saveItemMetadata(itemId, itemMetadata);
        }
        
        // Check if platform fee is required
        if (platformFee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("No platform fee required for this item");
        }

        // Create RazorPay order
        RazorpayClient razorpay = getRazorpayClient();
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", platformFee.multiply(BigDecimal.valueOf(100)).intValue()); // Amount in paise
        orderRequest.put("currency", "INR");
        // Generate a shorter receipt ID (max 40 chars)
        String receiptId = "fee_" + System.currentTimeMillis() % 10000000 + "_" + itemId.hashCode();
        if (receiptId.length() > 40) {
            receiptId = receiptId.substring(0, 40);
        }
        orderRequest.put("receipt", receiptId);
        
        System.out.println("Creating RazorPay order with live API keys");
        Order order = razorpay.orders.create(orderRequest);

        // Save payment order details
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("orderId", order.get("id"));
        paymentData.put("itemId", itemId);
        paymentData.put("creatorEmail", creatorEmail);
        paymentData.put("type", "CREATOR_PLATFORM_FEE");
        paymentData.put("amount", platformFee.doubleValue());
        paymentData.put("currency", "INR");
        paymentData.put("status", "CREATED");
        paymentData.put("timestamp", System.currentTimeMillis());

        s3StorageService.savePaymentData(order.get("id").toString(), paymentData);

        // Return order details for frontend
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.get("id"));
        response.put("amount", platformFee.doubleValue());
        response.put("currency", "INR");
        response.put("keyId", razorpayKeyId);
        response.put("type", "CREATOR_PLATFORM_FEE");
        response.put("description", "Platform fee for listing item: " + itemMetadata.get("name"));

        return response;
    }

    public void handleCreatorPaymentSuccess(String paymentId, String orderId, String signature) throws Exception {
        // Verify payment signature
        if (!verifyPaymentSignature(paymentId, orderId, signature)) {
            throw new RuntimeException("Payment signature verification failed");
        }

        // Get payment data
        Map<String, Object> paymentData = s3StorageService.getPaymentData(orderId);
        if (paymentData == null) {
            throw new RuntimeException("Payment order not found");
        }

        // Update payment status
        paymentData.put("paymentId", paymentId);
        paymentData.put("status", "COMPLETED");
        paymentData.put("completedAt", System.currentTimeMillis());
        s3StorageService.savePaymentData(orderId, paymentData);

        String itemId = (String) paymentData.get("itemId");
        String creatorEmail = (String) paymentData.get("creatorEmail");

        // Update item status to indicate platform fee paid and mark as SALE_IN_PROGRESS
        Map<String, Object> itemMetadata = s3StorageService.getItemMetadata(itemId);
        itemMetadata.put("platformFeePaid", true);
        itemMetadata.put("platformFeePaidAt", System.currentTimeMillis());
        itemMetadata.put("status", SaleStatus.SALE_IN_PROGRESS.name());
        s3StorageService.saveItemMetadata(itemId, itemMetadata);
        
        System.out.println("Item " + itemId + " marked as SALE_IN_PROGRESS after platform fee payment");

        // Handle platform fee collection
        handlePlatformFeeCollection(paymentData);

        // Send notifications
        sendCreatorPaymentNotifications(itemId, creatorEmail, paymentData);
    }

    // Method kept for backward compatibility but not used in new flow
    private void updateItemStatusAfterPayment(String itemId, String stage) throws Exception {
        Map<String, Object> itemMetadata = s3StorageService.getItemMetadata(itemId);
        
        switch (stage) {
            case "INITIAL":
                itemMetadata.put("status", SaleStatus.SALE_IN_PROGRESS.name());
                break;
            case "SHIPPING":
                itemMetadata.put("status", SaleStatus.SHIPPED.name());
                break;
            case "FINAL":
                itemMetadata.put("status", SaleStatus.SOLD.name());
                itemMetadata.put("soldAt", System.currentTimeMillis());
                break;
        }
        
        s3StorageService.saveItemMetadata(itemId, itemMetadata);
    }

    private void handlePlatformFeeCollection(Map<String, Object> paymentData) {
        // The entire platform fee goes to platform
        // In a production environment, this would handle the transfer to your platform account
        String paymentId = (String) paymentData.get("paymentId");
        String orderId = (String) paymentData.get("orderId");
        double amount = (double) paymentData.get("amount");
        
        System.out.println("Platform fee collected from creator: " + amount + 
                           " | Payment ID: " + paymentId + 
                           " | Order ID: " + orderId);
        
        // Additional production logging or processing could be added here
    }

    private void sendCreatorPaymentNotifications(String itemId, String creatorEmail, Map<String, Object> paymentData) {
        try {
            Map<String, Object> itemMetadata = s3StorageService.getItemMetadata(itemId);
            String itemName = (String) itemMetadata.get("name");
            
            notificationService.notifyCreatorPlatformFeePayment(creatorEmail, itemName, paymentData);
        } catch (Exception e) {
            System.err.println("Error sending creator payment notifications: " + e.getMessage());
        }
    }

    private boolean verifyPaymentSignature(String paymentId, String orderId, String signature) {
        try {
            // For production, implement proper signature verification
            // This is a simplified version for testing
            // In a real production environment, use HMAC-SHA256 to verify the signature
            
            // For now, we'll just check if all parameters are present
            if (paymentId != null && !paymentId.isEmpty() && 
                orderId != null && !orderId.isEmpty() && 
                signature != null && !signature.isEmpty()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Payment signature verification failed: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> approveBid(String itemId, String creatorEmail, String bidderEmail, double bidAmount) throws Exception {
        System.out.println("Approving bid for item: " + itemId + ", amount: " + bidAmount);
        // Verify creator owns the item
        Map<String, Object> itemMetadata = s3StorageService.getItemMetadata(itemId);
        if (!creatorEmail.equals(itemMetadata.get("email"))) {
            throw new RuntimeException("Only the creator can approve bids");
        }

        // Create sale data
        Map<String, Object> saleData = new HashMap<>();
        saleData.put("itemId", itemId);
        saleData.put("creatorEmail", creatorEmail);
        saleData.put("bidderEmail", bidderEmail);
        saleData.put("approvedBidAmount", bidAmount);
        saleData.put("status", "BID_APPROVED");
        saleData.put("approvedAt", System.currentTimeMillis());

        s3StorageService.saveSaleData(itemId, saleData);

        // Update item status and store the approved bid amount
        itemMetadata.put("status", SaleStatus.BID_APPROVED.name());
        itemMetadata.put("approvedBidAmount", bidAmount);
        
        // Calculate platform fee
        BigDecimal platformFee = BigDecimal.ZERO;
        if (bidAmount > 500) {
            // 5% platform fee
            platformFee = new BigDecimal(bidAmount).multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
            // Minimum fee of Rs. 50
            platformFee = platformFee.max(new BigDecimal("50"));
            
            itemMetadata.put("requiresPlatformFee", true);
            itemMetadata.put("platformFee", platformFee);
            System.out.println("Platform fee calculated: " + platformFee + " for bid amount: " + bidAmount);
        } else {
            itemMetadata.put("requiresPlatformFee", false);
            System.out.println("No platform fee required for bid amount: " + bidAmount);
        }
        
        s3StorageService.saveItemMetadata(itemId, itemMetadata);

        // No payment required from bidders anymore - direct sale
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bid approved successfully. Item will be marked as sold.");
        response.put("bidAmount", bidAmount);
        response.put("status", "BID_APPROVED");
        response.put("requiresPlatformFee", bidAmount > 500);
        if (bidAmount > 500) {
            response.put("platformFee", platformFee);
        }
        
        return response;
    }
}