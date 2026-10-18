#!/usr/bin/env bash
set -e

echo "=== Bootstrapping Smart IoT Park Platform Host ==="

# 1. Update system packages
apt-get update -y
apt-get install -y ca-certificates curl gnupg lsb-release git jq

# 2. Install official Docker Engine & Docker Compose Plugin
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

systemctl enable docker
systemctl start docker

# 3. Create platform directory
mkdir -p /opt/smartpark
cd /opt/smartpark

# 4. Write deployment environment variables
cat << 'EOF' > /opt/smartpark/.env
SPRING_PROFILES_ACTIVE=docker
MYSQL_HOST=${MYSQL_HOST}
MYSQL_PORT=3306
MYSQL_DATABASE=${MYSQL_DATABASE}
MYSQL_USER=${MYSQL_USER}
MYSQL_PASSWORD=${MYSQL_PASSWORD}
MONGO_URI=mongodb://smartpark-mongodb:27017/smartpark_telemetry
MQTT_BROKER_HOST=smartpark-mosquitto
MQTT_BROKER_PORT=1883
CORE_API_URL=http://core-api:8081
INGESTION_SERVICE_URL=http://ingestion-service:8082
AI_ENGINE_URL=http://ai-engine:8000
EOF

echo "Smart IoT Park platform bootstrap completed."
