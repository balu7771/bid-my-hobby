# Deploying Bid-My-Hobby to AWS Elastic Beanstalk with Docker

This guide provides step-by-step instructions for deploying the Bid-My-Hobby Spring Boot application to AWS Elastic Beanstalk using Docker.

## Prerequisites

- AWS CLI installed and configured with appropriate permissions
- Docker installed locally
- EB CLI installed (can be installed via `pip install awsebcli`)
- Maven installed locally

## Option 1: Using the Automated Script (Recommended)

The simplest way to deploy is to use the provided script:

```bash
# Make the script executable if needed
chmod +x deploy-to-eb.sh

# Run the deployment script
./deploy-to-eb.sh
```

This script will:
- Build your Spring Boot application
- Build a Docker image locally
- Initialize Elastic Beanstalk if needed
- Create or update an Elastic Beanstalk environment
- Deploy your application
- Open the application in your browser

## Option 2: Manual Deployment

If you prefer to deploy manually:

### Step 1: Build the Spring Boot Application

```bash
mvn clean package -DskipTests
```

### Step 2: Build the Docker Image

```bash
docker build -t bid-my-hobby:latest .
```

### Step 3: Initialize Elastic Beanstalk

```bash
eb init bid-my-hobby --region ap-south-1 --platform "Docker running on 64bit Amazon Linux 2"
```

### Step 4: Create an Elastic Beanstalk Environment

```bash
eb create bid-my-hobby-env \
  --instance-type t2.small \
  --single \
  --envvars SPRING_AI_OPENAI_API_KEY=your-openai-api-key,ADMIN_SECRET_KEY=your-admin-secret-key
```

### Step 5: Deploy the Application

```bash
eb deploy bid-my-hobby-env
```

### Step 6: Open the Application

```bash
eb open bid-my-hobby-env
```

## Updating Your Application

When you make changes to your application:

1. Build your application: `mvn clean package -DskipTests`
2. Build a new Docker image: `docker build -t bid-my-hobby:latest .`
3. Deploy the changes: `eb deploy bid-my-hobby-env`

## Updating Environment Variables

To update environment variables:

```bash
eb setenv VARIABLE_NAME=new-value
```

## Monitoring Your Application

You can monitor your application through:

1. The Elastic Beanstalk Console
2. Application logs: `eb logs`
3. SSH into the instance: `eb ssh`

## Cleaning Up

To terminate your environment:

```bash
eb terminate bid-my-hobby-env
```

## Security Considerations

- Never commit your `.env` file or any files containing credentials to version control
- Consider using AWS Parameter Store or Secrets Manager for sensitive information
- Regularly rotate your API keys and credentials

## Cost Optimization

- Use a single-instance environment for development
- Consider using spot instances for non-critical workloads
- Set up auto-scaling based on demand for production environments