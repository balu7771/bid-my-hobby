package com.bidmyhobby.controller;

import com.bidmyhobby.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    
    @Autowired
    private PaymentService paymentService;
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
    
    @GetMapping("/api/status")
    public String apiStatus() {
        return "API is running on Lambda";
    }
    
    @Operation(summary = "Check RazorPay configuration")
    @ApiResponse(responseCode = "200", description = "RazorPay configuration status")
    @GetMapping("/api/razorpay/status")
    public ResponseEntity<?> checkRazorpayConfig() {
        Map<String, Object> response = new HashMap<>();
        boolean isValid = paymentService.isRazorpayConfigValid();
        
        response.put("status", isValid ? "UP" : "DOWN");
        response.put("message", isValid ? "RazorPay configuration is valid" : "RazorPay configuration is invalid");
        response.put("usingLiveKeys", true);
        
        return ResponseEntity.ok(response);
    }
}