#!/bin/bash

echo "🔧 AWS Lambda Deployment Setup"
echo "================================"

# Check AWS credentials
echo "1. Checking AWS credentials..."
if aws sts get-caller-identity &>/dev/null; then
    echo "✅ AWS credentials are configured"
    aws sts get-caller-identity
else
    echo "❌ AWS credentials not configured"
    echo ""
    echo "Please run: aws configure"
    echo "You'll need:"
    echo "- AWS Access Key ID"
    echo "- AWS Secret Access Key"
    echo "- Region: us-east-1"
    echo ""
    echo "Get credentials from: AWS Console → IAM → Users → Security Credentials"
    exit 1
fi

echo ""
echo "2. Ready to deploy! Run these commands:"
echo "   sam deploy --guided"
echo ""
echo "You'll be prompted for:"
echo "- OpenAI API Key"
echo "- Admin Secret Key" 
echo "- S3 Bucket Name (default: bidmyhobby-images-bucket)"
echo ""
echo "💰 Cost: $0-5/month (pay per request only)"