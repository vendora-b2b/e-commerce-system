variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "vendora"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID for the ECS cluster"
  type        = string
}

variable "subnet_ids" {
  description = "Public subnet IDs for the ECS EC2 instances and tasks"
  type        = list(string)
}

variable "ecs_sg_id" {
  description = "Security group ID for ECS tasks"
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type for ECS cluster"
  type        = string
  default     = "m7i-flex.large"
}

variable "backend_image" {
  description = "ECR image URL for the backend service"
  type        = string
}

variable "ai_service_image" {
  description = "ECR image URL for the AI service"
  type        = string
}

variable "rds_endpoint" {
  description = "RDS instance endpoint (hostname)"
  type        = string
}

variable "rds_port" {
  description = "RDS instance port"
  type        = number
  default     = 3306
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "vendora_db"
}

variable "ssm_parameter_arns" {
  description = "ARNs of SSM parameters for ECS task execution role"
  type        = list(string)
}

variable "backend_target_group_arn" {
  description = "ARN of the ALB target group for the backend service"
  type        = string
}

variable "common_tags" {
  description = "Common tags applied to all resources"
  type        = map(string)
  default     = {}
}
