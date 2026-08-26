#!/bin/bash

# EC2 Setup Script for Reading Material Library
# Run this once on a fresh EC2 instance to set up Docker and deployment environment

set -e

echo "=========================================="
echo "Setting up EC2 for Reading Material Library"
echo "=========================================="

# Update system
echo "Updating system packages..."
sudo apt-get update
sudo apt-get upgrade -y

# Install Docker
echo "Installing Docker..."
sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# Add Docker's official GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Set up Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker packages
sudo apt-get update
sudo apt-get install -y \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-compose-plugin

# Add current user to docker group
sudo usermod -aG docker ${USER}

# Install Docker Compose standalone (optional, for convenience)
echo "Installing Docker Compose..."
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install AWS CLI
echo "Installing AWS CLI..."
sudo apt-get install -y awscli

# Install monitoring tools
echo "Installing monitoring tools..."
sudo apt-get install -y htop curl wget

# Create application directory
echo "Creating application directory..."
sudo mkdir -p /opt/app
sudo mkdir -p /opt/logs
sudo chown -R ${USER}:${USER} /opt/app
sudo chown -R ${USER}:${USER} /opt/logs

# Create environment file template
echo "Creating environment file template..."
cat > /opt/app/.env.template << 'EOF'
# Database Configuration
DB_HOST=your-rds-endpoint.amazonaws.com
DB_USERNAME=reading_user
DB_PASSWORD=your_secure_password

# Docker Image
REGISTRY=ghcr.io
IMAGE_NAME=your-username/reading-material-library

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
EOF

# Create deployment directory structure
echo "Creating deployment directory structure..."
mkdir -p /opt/app/{backups,logs,config}
touch /opt/app/logs/app.log

# Set up CloudWatch agent for monitoring (optional)
echo "Setting up log rotation..."
cat | sudo tee /etc/logrotate.d/reading-material-library << 'EOF'
/opt/logs/*.log {
    daily
    rotate 7
    compress
    delaycompress
    notifempty
    create 0640 1000 1000
    sharedscripts
    postrotate
        docker kill -s HUP reading-material-library 2>/dev/null || true
    endscript
}
EOF

# Set up automatic updates
echo "Setting up automatic security updates..."
sudo apt-get install -y unattended-upgrades
sudo dpkg-reconfigure -plow unattended-upgrades

# Create startup script
echo "Creating startup script..."
cat > /opt/app/start.sh << 'EOF'
#!/bin/bash
set -e

DOCKER_IMAGE="${REGISTRY}/${IMAGE_NAME}:latest"
CONTAINER_NAME="reading-material-library"
PORT="8080"

echo "Starting Reading Material Library..."
docker run -d \
  --name $CONTAINER_NAME \
  --restart unless-stopped \
  -p $PORT:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:5432/reading_library \
  -e SPRING_DATASOURCE_USERNAME=${DB_USERNAME} \
  -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \
  -e SPRING_PROFILES_ACTIVE=prod \
  -v /opt/logs:/logs \
  --health-cmd='curl -f http://localhost:8080/actuator/health || exit 1' \
  --health-interval=10s \
  --health-timeout=5s \
  --health-retries=3 \
  $DOCKER_IMAGE

echo "Container started!"
docker ps | grep $CONTAINER_NAME
EOF

chmod +x /opt/app/start.sh

# Create backup script
echo "Creating backup script..."
cat > /opt/app/backup.sh << 'EOF'
#!/bin/bash

BACKUP_DIR="/opt/app/backups"
BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/backup_$BACKUP_DATE.tar.gz"

echo "Creating backup..."
tar -czf $BACKUP_FILE /opt/logs /opt/app/config 2>/dev/null || true

# Keep only last 7 backups
find $BACKUP_DIR -name "backup_*.tar.gz" -mtime +7 -delete

echo "Backup completed: $BACKUP_FILE"
EOF

chmod +x /opt/app/backup.sh

# Set up cron job for backups
echo "Setting up backup cron job..."
(crontab -l 2>/dev/null; echo "0 2 * * * /opt/app/backup.sh") | crontab -

# Enable Docker service
echo "Enabling Docker service..."
sudo systemctl enable docker
sudo systemctl start docker

# Verify installation
echo "Verifying installations..."
docker --version
docker-compose --version
aws --version

echo ""
echo "=========================================="
echo "✓ EC2 setup completed successfully!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Update /opt/app/.env.template with your configuration"
echo "2. Rename to /opt/app/.env when ready"
echo "3. Configure AWS credentials:"
echo "   aws configure"
echo "4. Test Docker:"
echo "   docker ps"
echo "5. Monitor logs:"
echo "   docker logs -f reading-material-library"
echo ""
