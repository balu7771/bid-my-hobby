Y# 🚀 Deploy to AWS Lambda - Step by Step

## Run these commands in your terminal:

```bash
# 1. Navigate to your project
cd /Users/balajimudipalli/Documents/MyCode/bid-my-hobby

# 2. Update AWS region to ap-south-1
aws configure set region ap-south-1

# 3. Deploy with SAM
sam deploy --guided
```

## When prompted, enter:

1. **Stack Name**: `bid-my-hobby` (press Enter for default)
2. **AWS Region**: `ap-south-1` (should be default)
3. **Parameter OpenAIApiKey**: `your-openai-api-key`
4. **Parameter AdminSecretKey**: `your-admin-secret-key`  
5. **Parameter S3BucketName**: `bidmyhobby-images-bucket` (or your preferred name)
6. **Confirm changes before deploy**: `Y`
7. **Allow SAM CLI IAM role creation**: `Y`
8. **Disable rollback**: `N`
9. **Save parameters to config file**: `Y`
10. **SAM configuration file**: `samconfig.toml` (press Enter)
11. **SAM configuration environment**: `default` (press Enter)

## After deployment:
- You'll get an API Gateway URL
- Your S3 bucket will be created automatically
- Cost: $0-5/month (pay only when someone visits)

## If deployment fails:
```bash
# Check logs
sam logs -n BidMyHobbyFunction --stack-name bid-my-hobby --tail

# Delete and retry
sam delete --stack-name bid-my-hobby
sam deploy --guided
```