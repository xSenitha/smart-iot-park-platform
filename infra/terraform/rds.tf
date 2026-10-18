# DB Subnet Group spanning multiple private AZs
resource "aws_db_subnet_group" "main" {
  name       = "smartpark-db-subnet-group-${var.environment}"
  subnet_ids = aws_subnet.private[*].id

  tags = {
    Name = "smartpark-db-subnet-group-${var.environment}"
  }
}

# Managed AWS RDS MySQL 8.0 Instance
resource "aws_db_instance" "mysql" {
  identifier             = "smartpark-mysql-${var.environment}"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = var.db_instance_class
  allocated_storage      = 20
  max_allocated_storage  = 100
  storage_type           = "gp3"

  db_name                = var.db_name
  username               = var.db_username
  password               = var.db_password

  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name
  publicly_accessible    = false

  backup_retention_period   = 7
  backup_window             = "03:00-04:00"
  maintenance_window        = "sun:04:30-sun:05:30"
  auto_minor_version_upgrade = true
  skip_final_snapshot       = true
  deletion_protection       = false

  tags = {
    Name = "smartpark-mysql-${var.environment}"
  }
}
