# 🚀 AWS App Runner Setup

## Your Docker image is ready!
**ECR URI**: `010526286593.dkr.ecr.ap-south-1.amazonaws.com/bidmyhobby:latest`

## Create App Runner Service (AWS Console):

1. **Go to AWS Console → App Runner → Create service**

2. **Source**: 
   - Repository type: `Container registry`
   - Provider: `Amazon ECR`
   - Container image URI: `010526286593.dkr.ecr.ap-south-1.amazonaws.com/bidmyhobby:latest`
   - Deployment trigger: `Manual`

3. **Service settings**:
   - Service name: `bidmyhobby`
   - CPU: `0.25 vCPU`
   - Memory: `0.5 GB`
   - Port: `8080`

4. **Environment variables**:
   ```
   SPRING_AI_OPENAI_API_KEY = your-openai-api-key
   ADMIN_SECRET_KEY = your-admin-secret-key
   AWS_BUCKET_NAME = bidmyhobby-images-010526286593-010526286593
   ```

5. **Click Create & Deploy**

## After deployment:
- You'll get a URL like: `https://xyz.ap-south-1.awsapprunner.com`
- Update your Route53 CNAME to point to this URL
- Cost: ~$7-15/month, scales to zero when not used

## Test your app:
Your Spring Boot app will work perfectly with App Runner! 🎉