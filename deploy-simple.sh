#!/bin/bash

set -e

echo "🚀 Building and preparing JAR for deployment..."

# Build the application
./build-for-eb.sh

# Create deployment package
echo "📦 Creating deployment package..."
mkdir -p deploy
cp target/bid-my-hobby-0.0.1-SNAPSHOT.jar deploy/app.jar

# Create startup script
cat > deploy/start.sh << 'EOF'
#!/bin/bash
export SPRING_AI_OPENAI_API_KEY="your-openai-key"
export AWS_ACCESS_KEY="your-aws-access-key"
export AWS_SECRET_KEY="your-aws-secret-key"
export AWS_BUCKET_NAME="bidmyhobby-images"
export ADMIN_SECRET_KEY="your-admin-secret"

java -jar app.jar
EOF

chmod +x deploy/start.sh

# Create systemd service file
cat > deploy/bidmyhobby.service << 'EOF'
[Unit]
Description=Bid My Hobby Application
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/bidmyhobby
ExecStart=/home/ubuntu/bidmyhobby/start.sh
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

echo "✅ Deployment package ready in ./deploy/"
echo "📋 To deploy on any server:"
echo "   1. Copy ./deploy/ folder to your server"
echo "   2. Update environment variables in start.sh"
echo "   3. Run: sudo cp bidmyhobby.service /etc/systemd/system/"
echo "   4. Run: sudo systemctl enable bidmyhobby && sudo systemctl start bidmyhobby"
echo ""
echo "💡 Cheapest options:"
echo "   - DigitalOcean Droplet: $4/month"
echo "   - AWS Lightsail: $3.50/month"
echo "   - Linode: $5/month"