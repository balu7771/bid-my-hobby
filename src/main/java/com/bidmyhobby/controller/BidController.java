package com.bidmyhobby.controller;

import com.bidmyhobby.kafka.model.BidEvent;
import com.bidmyhobby.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bid")
public class BidController {

    @Autowired
    BidService bidService;

    @Operation(summary = "Get all the catalog items")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/allItems")
    public ResponseEntity<?> getAllItems(){
        return ResponseEntity.ok().body("Hello Balaji , learning every day!!");
    }

    // a logged-in user, places a bid on one of the item.

    @PostMapping("/placeBid")
    public ResponseEntity<?> placeBid(@RequestBody BidEvent bidEvent){
        bidService.placeBid(bidEvent.getItemId(),bidEvent.getUserId(),bidEvent.getBidAmount());
        return ResponseEntity.ok().body("Bid placed and event published to Kafka!!!");
    }



}
