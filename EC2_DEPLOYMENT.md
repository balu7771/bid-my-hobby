# EC2 Deployment Guide

## Quick Setup for Interviews

### 1. Launch EC2 Instance
- **Instance Type**: t3.small or t3.medium (for cost efficiency)
- **AMI**: Amazon Linux 2023 or Ubuntu 22.04
- **Security Group**: Allow HTTP (80), HTTPS (443), SSH (22), and Custom TCP (8080)
- **Storage**: 20GB GP3 (sufficient for the app)

### 2. Connect and Install Docker
```bash
# For Amazon Linux 2023
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Logout and login again for group changes to take effect
```

### 3. Deploy Your Application
```bash
# Upload your files (app.zip, Dockerfile.production, docker-compose.yml, deploy-ec2.sh)
scp -i your-key.pem app.zip docker-compose.yml Dockerfile.production deploy-ec2.sh ec2-user@your-ec2-ip:~/

# SSH into EC2
ssh -i your-key.pem ec2-user@your-ec2-ip

# Build and start the application
./deploy-ec2.sh build
./deploy-ec2.sh start
```

### 4. Quick Commands for Interviews
```bash
# Start the service
./deploy-ec2.sh start

# Stop the service  
./deploy-ec2.sh stop

# Check status
./deploy-ec2.sh status

# View logs
./deploy-ec2.sh logs

# Restart (for updates)
./deploy-ec2.sh restart
```

### 5. Access Your Application
- **URL**: `http://your-ec2-public-ip:8080`
- **Health Check**: `http://your-ec2-public-ip:8080/actuator/health`
- **API Docs**: `http://your-ec2-public-ip:8080/swagger-ui.html`

### 6. Environment Variables
Create a `.env` file in the same directory:
```bash
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=us-east-1
OPENAI_API_KEY=your_openai_key
```

### 7. Cost Optimization
- **Start**: `./deploy-ec2.sh start` (before interviews)
- **Stop**: `./deploy-ec2.sh stop` (after interviews)
- **Instance**: Stop EC2 instance when not needed (you only pay for storage)

### 8. Monitoring
```bash
# Check container status
docker ps

# Check resource usage
docker stats

# Check application logs
docker-compose logs -f bid-my-hobby
```

## Estimated AWS Costs
- **t3.small**: ~$15/month (if running 24/7), ~$0.50/day
- **Storage**: ~$2/month for 20GB
- **Data Transfer**: Minimal for demo purposes

**Interview Usage**: ~$1-2 per interview session (few hours)