package com.bidmyhobby.kafka;

import com.bidmyhobby.kafka.model.BidEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BidEventListener {

    @KafkaListener(topics = "bid-events", groupId = "bidmyhobby-group")
    public void consume(BidEvent bidEvent) {
        System.out.println("📥 New Bid Event Received: " + bidEvent.toString());
        // TODO: Call SMS sender here
    }
}