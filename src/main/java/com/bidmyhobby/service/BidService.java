package com.bidmyhobby.service;

import com.bidmyhobby.kafka.BidEventProducer;
import com.bidmyhobby.kafka.model.BidEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BidService {

    @Autowired
    private BidEventProducer bidEventProducer;

    public void placeBid(String itemId, String userId, BigDecimal amount) {
        BidEvent event = new BidEvent(itemId, userId, amount);
        bidEventProducer.publishBidEvent(event);
    }
}
