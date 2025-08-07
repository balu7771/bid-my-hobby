#!/bin/bash

echo "Stopping any running Docker containers..."
docker stop $(docker ps -q) 2>/dev/null || echo "No containers to stop"

echo "Starting fresh services..."
docker-compose up -d

echo "Waiting for services to be ready..."
sleep 15

echo "Services restarted successfully!"
echo "PostgreSQL: localhost:5432 (bidmyhobby/biduser/bidpass123)"
echo "Kafka: localhost:9092"
echo "Kafka UI: http://localhost:8081"
echo "Zookeeper: localhost:2181"