package com.bidmyhobby.service;

import com.bidmyhobby.kafka.BidEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BidService {

    @Autowired
    private BidEventProducer bidEventProducer;

    public void placeBid(String itemId, String userId, BigDecimal amount) {
        String bidId = UUID.randomUUID().toString();
        bidEventProducer.publishBidEvent(bidId, userId, itemId, amount.doubleValue());
    }
}
