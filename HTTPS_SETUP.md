# 🔒 HTTPS Setup for bidmyhobby.com

## Your EB app is working!
**HTTP URL**: http://bid-my-hobby-env.eba-qgmjgbp5.ap-south-1.elasticbeanstalk.com

## Step 1: Configure Load Balancer for HTTPS

### In AWS Console → EC2 → Load Balancers:
1. **Find your EB Load Balancer** (starts with `awseb-`)
2. **Add HTTPS Listener**:
   - Port: `443`
   - Protocol: `HTTPS`
   - SSL Certificate: Select your existing `bidmyhobby.com` certificate
   - Target: Forward to existing target group (port 80)

## Step 2: Update Route53
### In AWS Console → Route53 → bidmyhobby.com:
1. **Update A Record**:
   - Name: `@` (root domain)
   - Type: `A - IPv4 address`
   - Alias: `Yes`
   - Alias Target: Select your EB Load Balancer
2. **Add CNAME for www**:
   - Name: `www`
   - Type: `CNAME`
   - Value: `bidmyhobby.com`

## Step 3: Force HTTPS Redirect
### In EB Console → Configuration → Load Balancer:
1. **Add HTTP Listener Rule**:
   - Port: `80`
   - Action: `Redirect to HTTPS`
   - Port: `443`

## Alternative: Quick Fix
If above is complex, just update Route53 CNAME:
- Name: `bidmyhobby.com`
- Value: `bid-my-hobby-env.eba-qgmjgbp5.ap-south-1.elasticbeanstalk.com`

## Result:
- ✅ `https://bidmyhobby.com` will work
- ✅ `http://bidmyhobby.com` will redirect to HTTPS
- ✅ SSL certificate will be used

Your app will be live at `https://bidmyhobby.com`! 🚀