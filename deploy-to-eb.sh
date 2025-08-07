#!/bin/bash

# Exit on error
set -e

# Configuration
APP_NAME="bid-my-hobby"
ENV_NAME="bid-my-hobby-env"
AWS_REGION="ap-south-1"
EB_PLATFORM="Docker running on 64bit Amazon Linux 2"

# Build the Spring Boot application
echo "Building Spring Boot application..."
mvn clean package -DskipTests

# Build Docker image locally
echo "Building Docker image..."
docker build -t bid-my-hobby:latest .

# Check if EB CLI is installed
if ! command -v eb &> /dev/null; then
    echo "EB CLI is not installed. Installing..."
    pip install awsebcli
fi

# Initialize EB if not already done
if [ ! -d .elasticbeanstalk ]; then
    echo "Initializing Elastic Beanstalk..."
    eb init $APP_NAME --region $AWS_REGION --platform "$EB_PLATFORM"
fi

# Check if environment exists
if ! eb status $ENV_NAME &> /dev/null; then
    echo "Creating Elastic Beanstalk environment..."
    
    # Get environment variables from .env file
    source .env
    
    # Create environment with environment variables
    eb create $ENV_NAME \
        --instance-type t2.small \
        --single \
        --envvars SPRING_AI_OPENAI_API_KEY=$SPRING_AI_OPENAI_API_KEY,ADMIN_SECRET_KEY=$ADMIN_SECRET_KEY
else
    echo "Updating Elastic Beanstalk environment..."
    
    # Get environment variables from .env file
    source .env
    
    # Update environment variables
    eb setenv SPRING_AI_OPENAI_API_KEY=$SPRING_AI_OPENAI_API_KEY ADMIN_SECRET_KEY=$ADMIN_SECRET_KEY
fi

# Deploy the application
echo "Deploying application to Elastic Beanstalk..."
eb deploy $ENV_NAME

# Open the application in a browser
echo "Deployment complete! Opening application in browser..."
eb open $ENV_NAME

echo "Your application is now running on Elastic Beanstalk!"
echo "You can manage it through the AWS Elastic Beanstalk Console."