#!/bin/bash

# Deployment script for AWS EC2
# Usage: ./deploy-ec2.sh [start|stop|restart|status]

ACTION=${1:-start}
APP_NAME="bid-my-hobby"
DOCKER_IMAGE="$APP_NAME:latest"

case $ACTION in
  "start")
    echo "Starting $APP_NAME..."
    docker-compose up -d
    echo "$APP_NAME started successfully!"
    ;;
  "stop")
    echo "Stopping $APP_NAME..."
    docker-compose down
    echo "$APP_NAME stopped successfully!"
    ;;
  "restart")
    echo "Restarting $APP_NAME..."
    docker-compose down
    docker-compose up -d
    echo "$APP_NAME restarted successfully!"
    ;;
  "status")
    echo "Status of $APP_NAME:"
    docker-compose ps
    ;;
  "logs")
    echo "Logs for $APP_NAME:"
    docker-compose logs -f
    ;;
  "build")
    echo "Building $APP_NAME..."
    docker-compose build --no-cache
    echo "$APP_NAME built successfully!"
    ;;
  *)
    echo "Usage: $0 [start|stop|restart|status|logs|build]"
    exit 1
    ;;
esac