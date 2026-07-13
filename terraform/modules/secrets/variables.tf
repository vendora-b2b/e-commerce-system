variable "project_name" {
  description = "Project name used for parameter naming"
  type        = string
  default     = "vendora"
}

variable "db_connection_url" {
  description = "JDBC connection URL for the database"
  type        = string
}

variable "db_username" {
  description = "Database username"
  type        = string
  default     = "vendora_app"
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT signing secret"
  type        = string
  sensitive   = true
}

variable "common_tags" {
  description = "Common tags applied to all resources"
  type        = map(string)
  default     = {}
}
