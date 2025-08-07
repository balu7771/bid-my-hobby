#!/bin/bash

echo "Starting Kafka locally..."
echo "This will start Zookeeper, Kafka, and Kafka UI"
echo "Kafka will be available at localhost:9092"
echo "Kafka UI will be available at http://localhost:8081"
echo ""

# Start Kafka services
docker-compose -f docker-compose-kafka.yml up -d

echo ""
echo "Waiting for Kafka to be ready..."
sleep 10

echo ""
echo "Kafka services started successfully!"
echo "- Kafka: localhost:9092"
echo "- Kafka UI: http://localhost:8081"
echo ""
echo "To stop Kafka, run: docker-compose -f docker-compose-kafka.yml down"