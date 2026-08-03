variable "aws_region" {
  description = "AWS region for the target environment."
  type        = string
}

variable "project_name" {
  description = "Project name used in resource tags and naming."
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets."
  type        = list(string)
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets."
  type        = list(string)
}

variable "availability_zones" {
  description = "Availability zones used by the environment."
  type        = list(string)
}

variable "instance_type" {
  description = "EC2 instance type for the application server."
  type        = string
}

variable "key_name" {
  description = "Existing AWS key pair name used for EC2 access."
  type        = string
}

variable "ami_id" {
  description = "AMI ID for the application server."
  type        = string
}

variable "app_port" {
  description = "Application port exposed by the backend server."
  type        = number

  validation {
    condition     = floor(var.app_port) == var.app_port && var.app_port >= 1 && var.app_port <= 65535
    error_message = "app_port must be an integer between 1 and 65535."
  }
}

variable "allowed_ssh_cidr" {
  description = "CIDR block allowed to access the server over SSH."
  type        = string
}

variable "db_name" {
  description = "Initial database name for RDS."
  type        = string
}

variable "db_username" {
  description = "Master username for RDS."
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Master password for RDS. Provide via CLI or a local secret tfvars file."
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "RDS instance class."
  type        = string
}

variable "db_allocated_storage" {
  description = "Allocated storage size in GB for RDS."
  type        = number
}

variable "db_engine_version" {
  description = "MySQL engine version for RDS."
  type        = string
}

variable "ecr_repository_name" {
  description = "ECR repository name used for application images."
  type        = string
}

variable "upload_bucket_name" {
  description = "S3 bucket name used for application file uploads."
  type        = string
}

variable "github_repository" {
  description = "GitHub repository in owner/name format allowed to assume the deploy role."
  type        = string
}

variable "github_oidc_branches" {
  description = "Git branches allowed to assume the GitHub Actions deploy role."
  type        = list(string)
}

variable "public_assets_domain" {
  description = "Custom domain used for public asset delivery through CloudFront."
  type        = string
  default     = "assets.zero-st.com"
}

variable "public_assets_origin_prefix" {
  description = "S3 prefix used as the CloudFront origin path for public assets."
  type        = string
  default     = "public"
}

variable "public_assets_enable_custom_domain" {
  description = "Whether to request or manage the custom public assets domain certificate."
  type        = bool
  default     = false
}

variable "public_assets_attach_custom_domain" {
  description = "Whether to attach the custom public assets domain to CloudFront."
  type        = bool
  default     = false
}

variable "public_assets_acm_certificate_arn" {
  description = "Issued ACM certificate ARN in us-east-1 for the public assets domain. Leave empty to use the Terraform-managed certificate."
  type        = string
  default     = ""
}
