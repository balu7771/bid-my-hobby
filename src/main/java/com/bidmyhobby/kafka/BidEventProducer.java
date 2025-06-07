package com.bidmyhobby.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BidEventProducer {

    private static final String TOPIC = "bid-events";

    public void publishBidEvent(String bidId, String userId, String itemId, double amount) {
        // Kafka functionality removed to fix application
        System.out.println("Bid event would be published: bidId=" + bidId + 
                           ", userId=" + userId + 
                           ", itemId=" + itemId + 
                           ", amount=" + amount);
    }
}