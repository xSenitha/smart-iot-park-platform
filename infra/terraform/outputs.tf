output "ec2_public_ip" {
  description = "Public IP address of the SmartPark microservices host"
  value       = aws_instance.app_host.public_ip
}

output "rds_endpoint" {
  description = "Connection endpoint for the managed MySQL database"
  value       = aws_db_instance.mysql.endpoint
}

output "dashboard_url" {
  description = "Frontend React Dashboard Web URL"
  value       = "http://${aws_instance.app_host.public_ip}:3000"
}

output "api_gateway_url" {
  description = "API Gateway Perimeter URL"
  value       = "http://${aws_instance.app_host.public_ip}:8080"
}

output "grafana_url" {
  description = "Grafana Observability Dashboard URL"
  value       = "http://${aws_instance.app_host.public_ip}:3001"
}

output "mqtt_broker_endpoint" {
  description = "MQTT Sensor Telemetry Broker Endpoint"
  value       = "${aws_instance.app_host.public_ip}:1883"
}
