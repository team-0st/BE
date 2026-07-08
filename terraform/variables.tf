variable "aws_region" {
  description = "AWS region for the target environment."
  type        = string
}

variable "environment" {
  description = "Deployment environment name, for example dev or prod."
  type        = string
}
