#!/bin/bash

# Exit on error
set -e

# Configuration
AWS_REGION="ap-south-1"
ECR_REPO_NAME="bid-my-hobby"
IMAGE_TAG="latest"

# Check if AWS_ACCOUNT_ID is set
if [ -z "$AWS_ACCOUNT_ID" ]; then
  echo "Please set your AWS_ACCOUNT_ID environment variable"
  echo "Example: export AWS_ACCOUNT_ID=123456789012"
  exit 1
fi

# Build the Spring Boot application
echo "Building Spring Boot application..."
mvn clean package -DskipTests

# Create ECR repository if it doesn't exist
echo "Checking if ECR repository exists..."
aws ecr describe-repositories --repository-names $ECR_REPO_NAME --region $AWS_REGION || \
  aws ecr create-repository --repository-name $ECR_REPO_NAME --region $AWS_REGION

# Login to ECR
echo "Logging in to ECR..."
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Build Docker image
echo "Building Docker image..."
docker build -t $ECR_REPO_NAME:$IMAGE_TAG .

# Tag image for ECR
echo "Tagging image for ECR..."
docker tag $ECR_REPO_NAME:$IMAGE_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME:$IMAGE_TAG

# Push image to ECR
echo "Pushing image to ECR..."
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME:$IMAGE_TAG

echo "Docker image successfully built and pushed to ECR!"
echo "ECR Repository: $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME:$IMAGE_TAG"

# Check if we should deploy to EKS
read -p "Do you want to deploy to EKS? (y/n): " deploy_to_eks

if [ "$deploy_to_eks" = "y" ]; then
  # Check if EKS cluster exists
  CLUSTER_NAME="bid-my-hobby-cluster"
  
  echo "Checking if EKS cluster exists..."
  if ! aws eks describe-cluster --name $CLUSTER_NAME --region $AWS_REGION &> /dev/null; then
    echo "Creating EKS cluster (this may take 15-20 minutes)..."
    eksctl create cluster \
      --name $CLUSTER_NAME \
      --region $AWS_REGION \
      --nodegroup-name standard-workers \
      --node-type t3.medium \
      --nodes 2 \
      --nodes-min 1 \
      --nodes-max 3 \
      --managed
  fi
  
  # Update kubeconfig
  echo "Updating kubeconfig..."
  aws eks update-kubeconfig --name $CLUSTER_NAME --region $AWS_REGION
  
  # Replace AWS_ACCOUNT_ID in deployment.yaml
  echo "Updating Kubernetes deployment configuration..."
  sed -i.bak "s/\${AWS_ACCOUNT_ID}/$AWS_ACCOUNT_ID/g" kubernetes/deployment.yaml
  sed -i.bak "s/\${AWS_REGION}/$AWS_REGION/g" kubernetes/deployment.yaml
  
  # Prompt for secrets
  echo "Setting up Kubernetes secrets..."
  read -sp "Enter your OpenAI API key: " openai_api_key
  echo
  read -sp "Enter your admin secret key: " admin_secret_key
  echo
  
  # Create base64 encoded secrets
  openai_api_key_base64=$(echo -n "$openai_api_key" | base64)
  admin_secret_key_base64=$(echo -n "$admin_secret_key" | base64)
  
  # Update secrets.yaml
  sed -i.bak "s/BASE64_ENCODED_OPENAI_API_KEY/$openai_api_key_base64/g" kubernetes/secrets.yaml
  sed -i.bak "s/BASE64_ENCODED_ADMIN_SECRET_KEY/$admin_secret_key_base64/g" kubernetes/secrets.yaml
  
  # Apply Kubernetes configurations
  echo "Applying Kubernetes configurations..."
  kubectl apply -f kubernetes/secrets.yaml
  kubectl apply -f kubernetes/deployment.yaml
  kubectl apply -f kubernetes/service.yaml
  
  # Wait for service to get external IP
  echo "Waiting for service to get external IP..."
  external_ip=""
  while [ -z $external_ip ]; do
    echo "Waiting for external IP..."
    external_ip=$(kubectl get service bid-my-hobby -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
    [ -z "$external_ip" ] && sleep 10
  done
  
  echo "Application deployed successfully!"
  echo "You can access your application at: http://$external_ip"
else
  echo "Skipping EKS deployment."
  echo "To deploy manually later, follow the instructions in AWS_DOCKER_K8S_DEPLOY.md"
fi