#!/bin/bash

echo "Starting PostgreSQL and Kafka services..."
docker-compose up -d

echo "Waiting for services to be ready..."
sleep 10

echo "Services started successfully!"
echo "PostgreSQL: localhost:5432 (bidmyhobby/biduser/bidpass123)"
echo "Kafka: localhost:9092"
echo "Kafka UI: http://localhost:8081"
echo "Zookeeper: localhost:2181"