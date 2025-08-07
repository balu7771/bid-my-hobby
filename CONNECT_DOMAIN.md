# 🌐 Connect bidmyhobby.com to Lambda

## Your Lambda API URL:
`https://4kn7uusr7k.execute-api.ap-south-1.amazonaws.com/Prod/`

## Steps to connect custom domain:

### 1. Get SSL Certificate (AWS Certificate Manager)
```bash
# Request certificate for your domain
aws acm request-certificate \
  --domain-name bidmyhobby.com \
  --domain-name "*.bidmyhobby.com" \
  --validation-method DNS \
  --region ap-south-1
```

### 2. Create Custom Domain in API Gateway
```bash
# Get certificate ARN (after validation)
aws acm list-certificates --region ap-south-1

# Create custom domain
aws apigatewayv2 create-domain-name \
  --domain-name bidmyhobby.com \
  --domain-name-configurations CertificateArn=arn:aws:acm:ap-south-1:YOUR_ACCOUNT:certificate/CERT_ID
```

### 3. Update Route53 (Easiest Method)
1. Go to **AWS Console → Route53 → Hosted Zones → bidmyhobby.com**
2. Create **A Record**:
   - Name: `@` (root domain)
   - Type: `A - IPv4 address`
   - Alias: `Yes`
   - Alias Target: Select your API Gateway domain
3. Create **CNAME Record** for www:
   - Name: `www`
   - Type: `CNAME`
   - Value: `bidmyhobby.com`

## Alternative: Quick CNAME Setup (Temporary)
If you want to test immediately:
1. Go to Route53 → bidmyhobby.com
2. Create CNAME record:
   - Name: `api`
   - Value: `4kn7uusr7k.execute-api.ap-south-1.amazonaws.com`
3. Access via: `https://api.bidmyhobby.com/Prod/`

## Cost: 
- Custom domain: $0.50/month
- SSL certificate: FREE
- Route53: $0.50/month per hosted zone

Total additional cost: ~$1/month