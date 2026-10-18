variable "aws_region" {
  description = "AWS deployment region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Deployment environment name"
  type        = string
  default     = "production"
}

variable "vpc_cidr" {
  description = "CIDR block for the SmartPark VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "AZs for multi-AZ subnet deployment"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private database subnets"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.20.0/24"]
}

variable "instance_type" {
  description = "EC2 instance size for running the containerized microservices stack"
  type        = string
  default     = "t3.xlarge"
}

variable "db_instance_class" {
  description = "RDS instance class for MySQL"
  type        = string
  default     = "db.t4g.medium"
}

variable "db_name" {
  description = "Initial RDS MySQL database name"
  type        = string
  default     = "smartpark_core"
}

variable "db_username" {
  description = "RDS master username"
  type        = string
  default     = "smartpark_admin"
}

variable "db_password" {
  description = "RDS master password"
  type        = string
  sensitive   = true
  default     = "SmartPark_ProdPass_2026!"
}
