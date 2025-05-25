package com.bidmyhobby.kafka;

import com.bidmyhobby.kafka.model.BidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BidEventProducer {

    @Autowired
    private KafkaTemplate<String, BidEvent> kafkaTemplate;



    private static final String TOPIC = "bid-events";

    public void publishBidEvent(BidEvent bidEventMsg) {
        kafkaTemplate.send(TOPIC, bidEventMsg);
    }
}