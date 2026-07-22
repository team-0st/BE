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

output "app_instance_profile_name" {
  description = "Instance profile name attached to the application server."
  value       = aws_iam_instance_profile.app.name
}

output "app_role_arn" {
  description = "IAM role ARN attached to the application server."
  value       = aws_iam_role.app.arn
}

output "app_instance_public_ip" {
  description = "Public IP address of the application server."
  value       = aws_eip.app.public_ip
}

output "app_elastic_ip_allocation_id" {
  description = "Allocation ID of the Elastic IP attached to the application server."
  value       = aws_eip.app.id
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

output "upload_bucket_name" {
  description = "S3 bucket name for application file uploads."
  value       = aws_s3_bucket.upload.bucket
}

output "upload_bucket_arn" {
  description = "S3 bucket ARN for application file uploads."
  value       = aws_s3_bucket.upload.arn
}

output "github_actions_role_arn" {
  description = "IAM role ARN assumed by GitHub Actions via OIDC."
  value       = aws_iam_role.github_actions_deploy.arn
}

output "public_assets_cloudfront_distribution_id" {
  description = "CloudFront distribution ID used for public assets."
  value       = aws_cloudfront_distribution.public_assets.id
}

output "public_assets_cloudfront_domain_name" {
  description = "CloudFront domain name for public assets."
  value       = aws_cloudfront_distribution.public_assets.domain_name
}

output "public_assets_base_url" {
  description = "Base URL to use for public asset delivery."
  value       = var.public_assets_attach_custom_domain ? "https://${var.public_assets_domain}" : "https://${aws_cloudfront_distribution.public_assets.domain_name}"
}

output "public_assets_acm_certificate_arn" {
  description = "ACM certificate ARN for the public assets domain in us-east-1."
  value       = local.public_assets_effective_certificate_arn != "" ? local.public_assets_effective_certificate_arn : null
}

output "public_assets_acm_validation_records" {
  description = "DNS validation records that must be added in Cloudflare before enabling the custom assets domain."
  value = [
    for option in try(aws_acm_certificate.public_assets[0].domain_validation_options, []) : {
      domain_name  = option.domain_name
      record_name  = option.resource_record_name
      record_type  = option.resource_record_type
      record_value = option.resource_record_value
    }
  ]
}
