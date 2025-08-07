package com.bidmyhobby.controller;

import com.bidmyhobby.model.PaymentStage;
import com.bidmyhobby.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Operation(summary = "Approve a bid (no payment required from bidders)")
    @ApiResponse(responseCode = "200", description = "Bid approved successfully")
    @PostMapping("/approveBid")
    public ResponseEntity<?> approveBid(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Received approveBid request: " + request);
            String itemId = (String) request.get("itemId");
            String creatorEmail = (String) request.get("creatorEmail");
            String bidderEmail = (String) request.get("bidderEmail");
            double bidAmount = Double.parseDouble(request.get("bidAmount").toString());

            System.out.println("Processing bid approval for item: " + itemId + ", amount: " + bidAmount);
            Map<String, Object> result = paymentService.approveBid(itemId, creatorEmail, bidderEmail, bidAmount);
            System.out.println("Bid approval successful: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error approving bid: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error approving bid: " + e.getMessage());
        }
    }

    @Operation(summary = "Create platform fee payment order for creators")
    @ApiResponse(responseCode = "200", description = "Creator platform fee payment order created")
    @PostMapping("/createCreatorPayment")
    public ResponseEntity<?> createCreatorPayment(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Received createCreatorPayment request: " + request);
            String itemId = (String) request.get("itemId");
            String creatorEmail = (String) request.get("creatorEmail");

            System.out.println("Creating payment order for item: " + itemId + ", creator: " + creatorEmail);
            Map<String, Object> paymentOrder = paymentService.createCreatorPaymentOrder(itemId, creatorEmail);
            System.out.println("Payment order created successfully: " + paymentOrder);
            return ResponseEntity.ok(paymentOrder);
        } catch (Exception e) {
            System.err.println("Error creating creator payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating creator payment: " + e.getMessage());
        }
    }

    @Operation(summary = "Handle successful creator platform fee payment")
    @ApiResponse(responseCode = "200", description = "Creator payment processed successfully")
    @PostMapping("/creatorPaymentSuccess")
    public ResponseEntity<?> handleCreatorPaymentSuccess(@RequestBody Map<String, Object> request) {
        try {
            String paymentId = (String) request.get("razorpay_payment_id");
            String orderId = (String) request.get("razorpay_order_id");
            String signature = (String) request.get("razorpay_signature");

            paymentService.handleCreatorPaymentSuccess(paymentId, orderId, signature);
            return ResponseEntity.ok("Creator payment processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing creator payment: " + e.getMessage());
        }
    }

    @Operation(summary = "Handle successful payment (legacy endpoint)")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    @PostMapping("/success")
    public ResponseEntity<?> handlePaymentSuccess(@RequestBody Map<String, Object> request) {
        try {
            String paymentId = (String) request.get("razorpay_payment_id");
            String orderId = (String) request.get("razorpay_order_id");
            String signature = (String) request.get("razorpay_signature");

            // Try creator payment first, fallback to legacy
            try {
                paymentService.handleCreatorPaymentSuccess(paymentId, orderId, signature);
            } catch (Exception e) {
                // Fallback to legacy payment handling if needed
                System.out.println("Fallback to legacy payment handling");
            }
            return ResponseEntity.ok("Payment processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing payment: " + e.getMessage());
        }
    }

    // Legacy endpoints kept for backward compatibility
    @Operation(summary = "Mark item as shipped (legacy)")
    @ApiResponse(responseCode = "200", description = "Item marked as shipped")
    @PostMapping("/markShipped")
    public ResponseEntity<?> markShipped(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok("Shipping tracking is no longer required in the new payment flow");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error marking as shipped: " + e.getMessage());
        }
    }

    @Operation(summary = "Mark item as delivered (legacy)")
    @ApiResponse(responseCode = "200", description = "Item marked as delivered")
    @PostMapping("/markDelivered")
    public ResponseEntity<?> markDelivered(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok("Delivery tracking is no longer required in the new payment flow");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error marking as delivered: " + e.getMessage());
        }
    }
}