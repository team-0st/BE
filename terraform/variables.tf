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
