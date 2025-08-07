# Kafka Integration Setup

This project uses Apache Kafka to trigger notifications whenever creators upload items.

## Quick Start

1. **Start Kafka locally:**
   ```bash
   ./start-kafka.sh
   ```

2. **Start your Spring Boot application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Upload an item** through the API and check the logs for Kafka events.

## What's Included

### Dependencies
- `spring-kafka` - Spring Boot integration with Apache Kafka

### Configuration
- **Kafka Broker**: `localhost:9092`
- **Consumer Group**: `bid-my-hobby-notifications`
- **Topic**: `item-upload-notifications`

### Components

#### 1. Event Model (`ItemUploadEvent`)
```java
public class ItemUploadEvent {
    private String itemId;
    private String creatorId;
    private String creatorName;
    private String itemTitle;
    private String itemDescription;
    private String imageUrl;
    private Double startingBid;
    private LocalDateTime uploadTime;
    private String category;
}
```

#### 2. Producer Service (`KafkaProducerService`)
- Publishes events when items are uploaded
- Handles errors gracefully without affecting upload process

#### 3. Consumer Service (`KafkaConsumerService`)
- Listens to item upload events
- Triggers notifications to interested users
- Logs notification messages

#### 4. Configuration (`KafkaConfig`)
- Auto-creates the `item-upload-notifications` topic
- Configures topic with 3 partitions and 1 replica

## Services Included

### Kafka (Port 9092)
- Main Kafka broker for message streaming

### Zookeeper (Port 2181)
- Manages Kafka cluster metadata

### Kafka UI (Port 8081)
- Web interface to monitor topics, messages, and consumers
- Access at: http://localhost:8081

## How It Works

1. **Item Upload**: When a creator uploads an item via `/api/bid/uploadItem`
2. **Event Publishing**: A `ItemUploadEvent` is published to the `item-upload-notifications` topic
3. **Event Consumption**: The consumer service receives the event
4. **Notification**: Notifications are triggered for interested users

## Monitoring

- **Kafka UI**: http://localhost:8081
- **Application Logs**: Check console for Kafka event logs
- **Topics**: Monitor the `item-upload-notifications` topic

## Stopping Kafka

```bash
docker-compose -f docker-compose-kafka.yml down
```

## Troubleshooting

### Kafka Connection Issues
- Ensure Docker is running
- Check if ports 9092, 2181, and 8081 are available
- Restart Kafka services: `./start-kafka.sh`

### No Events Received
- Check if the topic exists in Kafka UI
- Verify consumer group is active
- Check application logs for errors

### Performance Tuning
- Adjust partition count in `KafkaConfig.java`
- Modify consumer group settings in `application.properties`
- Scale consumers by running multiple application instances