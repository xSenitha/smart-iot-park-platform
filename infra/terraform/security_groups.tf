# Security Group for SmartPark Container Host
resource "aws_security_group" "app_sg" {
  name        = "smartpark-app-sg-${var.environment}"
  description = "Security group for Smart IoT Park Platform microservices host"
  vpc_id      = aws_vpc.main.id

  # HTTP Web Traffic (Dashboard / Reverse Proxy)
  ingress {
    description = "HTTP Web Access"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS Web Traffic
  ingress {
    description = "HTTPS Web Access"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # React Dashboard (Port 3000)
  ingress {
    description = "Frontend Admin Dashboard"
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # API Gateway Perimeter (Port 8080)
  ingress {
    description = "API Gateway Perimeter"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # MQTT IoT Broker (Port 1883)
  ingress {
    description = "MQTT Sensor Broker"
    from_port   = 1883
    to_port     = 1883
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Grafana Dashboard (Port 3001)
  ingress {
    description = "Grafana Observability"
    from_port   = 3001
    to_port     = 3001
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Prometheus Metrics (Port 9090)
  ingress {
    description = "Prometheus Metrics"
    from_port   = 9090
    to_port     = 9090
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }

  # SSH for remote maintenance
  ingress {
    description = "SSH Administration"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Outbound Traffic
  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "smartpark-app-sg-${var.environment}"
  }
}

# Security Group for Managed RDS MySQL
resource "aws_security_group" "rds_sg" {
  name        = "smartpark-rds-sg-${var.environment}"
  description = "Enforce private database ingress only from application containers"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "MySQL Access from App Host"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.app_sg.id]
  }

  egress {
    description = "Allow outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "smartpark-rds-sg-${var.environment}"
  }
}
