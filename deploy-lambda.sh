#!/bin/bash

set -e

echo "🚀 Deploying Bid My Hobby to AWS Lambda..."

# Build the application
echo "📦 Building application..."
./build-for-eb.sh

# Deploy with SAM
echo "☁️ Deploying to AWS..."
sam build
sam deploy --guided

echo "✅ Deployment complete!"
echo "Your app is now running serverless on AWS Lambda!"
echo "💰 Cost: Pay only when someone visits your site"