package com.bidmyhobby.kafka.serialization;

import com.bidmyhobby.kafka.model.BidEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class BidEventDeserializer implements Deserializer<BidEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BidEvent deserialize(String topic, byte[] data) {
        try {
            if (data == null || data.length == 0) {
                return null;
            }
            return objectMapper.readValue(data, BidEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing BidEvent", e);
        }
    }

    @Override
    public void close() {}
}
