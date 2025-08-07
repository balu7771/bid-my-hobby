package com.bidmyhobby.service;

import com.bidmyhobby.model.ItemUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topic.item-upload}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleItemUploadEvent(ItemUploadEvent event) {
        try {
            log.info("Received item upload event for item: {} by creator: {}", 
                    event.getItemId(), event.getCreatorName());
            
            // Send notification to interested users
            String notificationMessage = String.format(
                "New item '%s' uploaded by %s! Starting bid: ₹%.2f", 
                event.getItemTitle(), 
                event.getCreatorName(), 
                event.getStartingBid()
            );
            
            // You can extend this to send notifications to specific users
            // For now, we'll just log the notification
            log.info("Notification: {}", notificationMessage);
            
            // Optional: Send email notifications to subscribers
            // notificationService.sendItemUploadNotification(event);
            
        } catch (Exception e) {
            log.error("Failed to process item upload event for item: {}", event.getItemId(), e);
        }
    }
}