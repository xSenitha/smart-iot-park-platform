# AWS Production Deployment Runbook

This runbook describes the end-to-end operational procedure for provisioning and running the **Smart IoT Park Platform** on Amazon Web Services (AWS) using Terraform and Docker.

---

## 1. Cloud Architecture Overview

```
                      Internet / Field IoT Devices
                                   │
              ┌────────────────────┴────────────────────┐
              │                                         │
        Port 80/443/3000                           Port 1883
     (HTTP Web & REST APIs)                     (MQTT Telemetry)
              │                                         │
              ▼                                         ▼
   ┌────────────────────────────────────────────────────────┐
   │ Public Subnet (us-east-1a)                             │
   │                                                        │
   │  EC2 Container Host (Ubuntu 22.04 LTS, t3.xlarge)      │
   │  ┌──────────────────────────────────────────────────┐  │
   │  │ Docker Engine & Docker Compose Microservices     │  │
   │  │                                                  │  │
   │  │  - smartpark-frontend        (Port 3000 / 80)    │  │
   │  │  - smartpark-api-gateway     (Port 8080)         │  │
   │  │  - smartpark-core-api        (Port 8081)         │  │
   │  │  - smartpark-ingestion       (Port 8082)         │  │
   │  │  - smartpark-ai-engine       (Port 8000)         │  │
   │  │  - smartpark-sensor-simulator                    │  │
   │  │  - smartpark-mosquitto       (Port 1883)         │  │
   │  │  - smartpark-mongodb         (Port 27017)        │  │
   │  │  - smartpark-prometheus      (Port 9090)         │  │
   │  │  - smartpark-grafana         (Port 3001)         │  │
   │  └───────────────────────┬──────────────────────────┘  │
   └──────────────────────────┼─────────────────────────────┘
                              │ Private Subnet (3306)
                              ▼
   ┌────────────────────────────────────────────────────────┐
   │ Private Multi-AZ Subnets (us-east-1a, us-east-1b)       │
   │                                                        │
   │  Managed AWS RDS MySQL 8.0 (db.t4g.medium)             │
   │  Database: smartpark_core                              │
   │  Multi-AZ automated daily backups & storage autoscaling│
   └────────────────────────────────────────────────────────┘
```

---

## 2. Prerequisites

1. **AWS CLI v2** installed and configured:
   ```bash
   aws configure
   # Verify credentials
   aws sts get-caller-identity
   ```
2. **Terraform CLI >= 1.5.0** installed:
   ```bash
   terraform version
   ```
3. **SSH Key Pair** (optional, as AWS Systems Manager SSM is enabled by default):
   ```bash
   aws ec2 create-key-pair --key-name smartpark-key --query 'KeyMaterial' --output text > smartpark-key.pem
   chmod 400 smartpark-key.pem
   ```

---

## 3. Infrastructure Provisioning

1. Navigate to the terraform configuration root:
   ```bash
   cd infra/terraform
   ```

2. Initialize Terraform and download AWS provider plugins:
   ```bash
   terraform init
   ```

3. Review the execution plan:
   ```bash
   terraform plan -out=tfplan \
     -var="aws_region=us-east-1" \
     -var="environment=production" \
     -var="instance_type=t3.xlarge" \
     -var="db_password=YourSecurePassword2026!"
   ```

4. Apply the infrastructure:
   ```bash
   terraform apply tfplan
   ```

5. Capture the Terraform outputs:
   - `ec2_public_ip`: Public IP of the deployed EC2 host
   - `rds_endpoint`: Internal MySQL DNS endpoint
   - `dashboard_url`: `http://<ec2_public_ip>:3000`
   - `api_gateway_url`: `http://<ec2_public_ip>:8080`
   - `grafana_url`: `http://<ec2_public_ip>:3001`
   - `mqtt_broker_endpoint`: `<ec2_public_ip>:1883`

---

## 4. Deploying the Microservices Stack

1. Connect to the host using AWS SSM Session Manager:
   ```bash
   aws ssm start-session --target <instance_id>
   ```
   Or via SSH:
   ```bash
   ssh -i smartpark-key.pem ubuntu@<ec2_public_ip>
   ```

2. Clone the repository and configure `.env`:
   ```bash
   cd /opt/smartpark
   git clone https://github.com/xSenitha/smart-iot-park-platform.git .
   cp .env.example .env
   # Update MYSQL_HOST with the Terraform RDS endpoint output
   ```

3. Launch all services via Docker Compose:
   ```bash
   docker compose up -d --build
   ```

4. Verify service health and container status:
   ```bash
   docker compose ps
   ```

---

## 5. Verification & Smoke Testing

1. **API Gateway Health**:
   ```bash
   curl -s http://<ec2_public_ip>:8080/actuator/health | jq .
   ```
2. **Core API Health & Schema**:
   ```bash
   curl -s http://<ec2_public_ip>:8080/api/v1/health | jq .
   curl -s http://<ec2_public_ip>:8080/api/v1/zones | jq .
   ```
3. **AI Engine Health & Inference**:
   ```bash
   curl -s http://<ec2_public_ip>:8080/api/v1/predict/health | jq .
   curl -s -X POST http://<ec2_public_ip>:8080/api/v1/jobs/predict-batch | jq .
   ```
4. **MQTT Ingestion Verification**:
   ```bash
   docker logs smartpark-ingestion-service --tail 50
   ```
5. **Observability**:
   - Access Grafana at `http://<ec2_public_ip>:3001` (Credentials: `admin` / `admin`).
   - View pre-provisioned dashboard: **Smart IoT Park - Operations Overview**.

---

## 6. Disaster Recovery & Tear-Down

To destroy all cloud resources and stop billing:
```bash
cd infra/terraform
terraform destroy \
  -var="aws_region=us-east-1" \
  -var="environment=production" \
  -var="db_password=YourSecurePassword2026!"
```
