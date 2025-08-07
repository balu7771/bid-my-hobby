#!/bin/bash

set -e

echo "🚀 Deploying to AWS App Runner..."

# Build the application
echo "📦 Building application..."
./build-for-eb.sh

# Build and push Docker image
echo "🐳 Building Docker image..."
docker build -t bidmyhobby .

# Tag for ECR (replace with your account ID)
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION=$(aws configure get region)
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/bidmyhobby"

# Create ECR repository if it doesn't exist
aws ecr describe-repositories --repository-names bidmyhobby || aws ecr create-repository --repository-name bidmyhobby

# Login to ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_URI

# Tag and push
docker tag bidmyhobby:latest $ECR_URI:latest
docker push $ECR_URI:latest

echo "✅ Image pushed to ECR!"
echo "🌐 Now create App Runner service in AWS Console pointing to: $ECR_URI:latest"
echo "💰 Cost: ~$7-15/month, scales to zero when not used"