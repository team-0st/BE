output "vpc_id" {
  description = "VPC ID for the environment."
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "Public subnet IDs."
  value       = [for subnet in aws_subnet.public : subnet.id]
}

output "private_subnet_ids" {
  description = "Private subnet IDs."
  value       = [for subnet in aws_subnet.private : subnet.id]
}

output "app_security_group_id" {
  description = "Security group ID for the application server."
  value       = aws_security_group.app.id
}

output "app_instance_id" {
  description = "EC2 instance ID for the application server."
  value       = aws_instance.app.id
}

output "app_instance_public_ip" {
  description = "Public IP address of the application server."
  value       = aws_instance.app.public_ip
}
