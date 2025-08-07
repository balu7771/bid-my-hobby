package com.bidmyhobby.service;

import com.bidmyhobby.model.ItemUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.item-upload}")
    private String itemUploadTopic;

    public void publishItemUploadEvent(ItemUploadEvent event) {
        try {
            kafkaTemplate.send(itemUploadTopic, event.getItemId(), event);
            log.info("Published item upload event for item: {} by creator: {}", 
                    event.getItemId(), event.getCreatorName());
        } catch (Exception e) {
            log.error("Failed to publish item upload event for item: {}", event.getItemId(), e);
        }
    }
}