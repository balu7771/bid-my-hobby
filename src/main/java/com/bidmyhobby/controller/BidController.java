package com.bidmyhobby.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.bidmyhobby.service.BidService;
import com.bidmyhobby.service.ScalityStorageService;

@RestController
@RequestMapping("/api/bid")
public class BidController {

    @Autowired
    private BidService bidService;
    
    @Autowired
    private ScalityStorageService scalityStorageService;

    @Operation(summary = "Get all the catalog items from Scality bucket")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/allItems")
    public ResponseEntity<?> getAllItems() {
        try {
            List<Map<String, String>> items = scalityStorageService.listAllItems();
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
            String userId = (String) bidRequest.get("userId");
            BigDecimal bidAmount = new BigDecimal(bidRequest.get("bidAmount").toString());
            
            bidService.placeBid(itemId, userId, bidAmount);
            return ResponseEntity.ok().body("Bid placed successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error placing bid: " + e.getMessage());
        }
    }
}
