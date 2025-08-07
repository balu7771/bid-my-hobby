#!/bin/bash

# Configuration
OCI_INSTANCE_IP="YOUR_OCI_INSTANCE_IP"
OCI_USER="opc"  # Default OCI user
SSH_KEY_PATH="~/.ssh/your-oci-key"

echo "Building Spring Boot application..."
mvn clean package -DskipTests

echo "Building Docker image..."
docker build -t bid-my-hobby:latest .

echo "Saving Docker image to tar file..."
docker save bid-my-hobby:latest > bid-my-hobby.tar

echo "Copying files to OCI instance..."
scp -i $SSH_KEY_PATH bid-my-hobby.tar $OCI_USER@$OCI_INSTANCE_IP:~/
scp -i $SSH_KEY_PATH docker-compose.yml $OCI_USER@$OCI_INSTANCE_IP:~/

echo "Deploying on OCI instance..."
ssh -i $SSH_KEY_PATH $OCI_USER@$OCI_INSTANCE_IP << 'EOF'
# Install Docker if not already installed
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Load and run the Docker image
docker load < bid-my-hobby.tar
docker-compose up -d

echo "Application deployed successfully on OCI!"
EOF

echo "Cleaning up local tar file..."
rm bid-my-hobby.tar

echo "Deployment complete! Your app should be accessible at http://$OCI_INSTANCE_IP:8080"