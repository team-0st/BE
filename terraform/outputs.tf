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

output "db_security_group_id" {
  description = "Security group ID for the database."
  value       = aws_security_group.db.id
}

output "db_subnet_group_name" {
  description = "DB subnet group name."
  value       = aws_db_subnet_group.main.name
}

output "db_instance_endpoint" {
  description = "RDS endpoint address."
  value       = aws_db_instance.main.address
}

output "db_instance_id" {
  description = "RDS instance identifier."
  value       = aws_db_instance.main.id
}

output "ecr_repository_name" {
  description = "ECR repository name."
  value       = aws_ecr_repository.app.name
}

output "ecr_repository_url" {
  description = "ECR repository URL."
  value       = aws_ecr_repository.app.repository_url
}
