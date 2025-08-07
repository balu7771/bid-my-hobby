# Deploying Bid-My-Hobby to AWS with Docker and Kubernetes

This guide provides step-by-step instructions for deploying the Bid-My-Hobby Spring Boot application to AWS using Docker and Kubernetes.

## Prerequisites

- AWS CLI installed and configured with appropriate permissions
- Docker installed locally
- kubectl installed locally
- eksctl installed locally (for EKS cluster management)
- Maven installed locally

## Step 1: Build the Spring Boot Application

```bash
# Navigate to your project directory
cd /path/to/bid-my-hobby

# Build the application with Maven
mvn clean package -DskipTests
```

## Step 2: Create an Amazon ECR Repository

```bash
# Create an ECR repository to store your Docker images
aws ecr create-repository --repository-name bid-my-hobby --region ap-south-1
```

## Step 3: Build and Push the Docker Image

```bash
# Get the ECR login command
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com

# Build the Docker image
docker build -t bid-my-hobby:latest .

# Tag the image for ECR
docker tag bid-my-hobby:latest ${AWS_ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com/bid-my-hobby:latest

# Push the image to ECR
docker push ${AWS_ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com/bid-my-hobby:latest
```

## Step 4: Create an EKS Cluster

```bash
# Create an EKS cluster
eksctl create cluster \\
  --name bid-my-hobby-cluster \\
  --region ap-south-1 \\
  --nodegroup-name standard-workers \\
  --node-type t3.medium \\
  --nodes 2 \\
  --nodes-min 1 \\
  --nodes-max 3 \\
  --managed
```

## Step 5: Configure kubectl for Your EKS Cluster

```bash
# Update kubeconfig to connect to your EKS cluster
aws eks update-kubeconfig --name bid-my-hobby-cluster --region ap-south-1
```

## Step 6: Create Kubernetes Secrets

Before applying the secrets.yaml file, you need to encode your secrets in base64:

```bash
# Encode your OpenAI API key
echo -n "your-openai-api-key" | base64

# Encode your admin secret key
echo -n "BravoPulse19#" | base64
```

Edit the kubernetes/secrets.yaml file and replace the placeholder values with your base64-encoded secrets.

Then apply the secrets:

```bash
kubectl apply -f kubernetes/secrets.yaml
```

## Step 7: Deploy the Application to Kubernetes

```bash
# Apply the Kubernetes deployment and service configurations
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
```

## Step 8: Verify the Deployment

```bash
# Check the status of your pods
kubectl get pods

# Check the status of your service
kubectl get services

# Get the external URL of your service
kubectl get service bid-my-hobby -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

## Step 9: Scaling Your Application

To scale your application horizontally:

```bash
# Scale to 5 replicas
kubectl scale deployment bid-my-hobby --replicas=5
```

## Step 10: Updating Your Application

When you make changes to your application:

1. Build a new version of your application
2. Build and push a new Docker image with a new tag
3. Update the deployment to use the new image:

```bash
kubectl set image deployment/bid-my-hobby bid-my-hobby=${AWS_ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com/bid-my-hobby:v2
```

## Monitoring and Logging

For monitoring and logging, consider setting up:

1. Amazon CloudWatch for logs and metrics
2. Prometheus and Grafana for more detailed monitoring
3. AWS X-Ray for distributed tracing

## Cost Optimization

- Use spot instances for non-critical workloads
- Set up auto-scaling based on demand
- Use AWS Cost Explorer to monitor and optimize costs

## Security Considerations

- Regularly update your Docker images and dependencies
- Use IAM roles for service accounts in EKS
- Implement network policies to restrict pod-to-pod communication
- Enable encryption for data at rest and in transit

## Next Steps for Microservices Architecture

As you expand to a microservices architecture:

1. Identify bounded contexts in your application
2. Extract services based on business capabilities
3. Implement service discovery using AWS App Mesh or Istio
4. Set up API Gateway for external access
5. Implement distributed tracing and centralized logging
6. Consider using AWS Step Functions for orchestrating workflows across services

## Useful Commands

```bash
# View logs for a pod
kubectl logs -f <pod-name>

# Execute a command in a pod
kubectl exec -it <pod-name> -- /bin/bash

# Describe a pod to see detailed information
kubectl describe pod <pod-name>

# Get all resources in the namespace
kubectl get all
```

Remember to replace `${AWS_ACCOUNT_ID}` with your actual AWS account ID in all commands and configuration files.