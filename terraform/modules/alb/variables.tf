variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "vendora"
}

variable "vpc_id" {
  description = "VPC ID for the target groups"
  type        = string
}

variable "subnet_ids" {
  description = "Public subnet IDs for the ALB"
  type        = list(string)
}

variable "security_group_id" {
  description = "Security group ID for the ALB"
  type        = string
}

variable "common_tags" {
  description = "Common tags applied to all resources"
  type        = map(string)
  default     = {}
}
