# 🚀 Elastic Beanstalk Deployment

## Your JAR is ready!
**File**: `target/bid-my-hobby-0.0.1-SNAPSHOT.jar`

## Deploy to Elastic Beanstalk:

### Option 1: AWS Console (Easiest)
1. **Go to AWS Console → Elastic Beanstalk**
2. **Create Application** or **Update existing**
3. **Upload JAR**: `target/bid-my-hobby-0.0.1-SNAPSHOT.jar`
4. **Platform**: Java 17 running on Amazon Linux 2
5. **Environment Variables**:
   ```
   SPRING_AI_OPENAI_API_KEY=<your_openai_api_key>
   ADMIN_SECRET_KEY=<your_admin_secret_key>
   AWS_BUCKET_NAME=bid-my-hobby
   AWS_REGION=ap-south-1
   SERVER_PORT=5000
   ```

### Option 2: EB CLI
```bash
# Initialize (if not done)
eb init

# Deploy
eb deploy

# Set environment variables
eb setenv SPRING_AI_OPENAI_API_KEY=<your_openai_key> ADMIN_SECRET_KEY=<your_admin_key> AWS_BUCKET_NAME=bid-my-hobby
```

## After deployment:
- You'll get a URL like: `http://bidmyhobby.ap-south-1.elasticbeanstalk.com`
- Connect to `bidmyhobby.com` via Route53 CNAME
- Cost: ~$20-50/month but reliable and proven to work

Your Spring Boot app will work perfectly with EB! 🎉