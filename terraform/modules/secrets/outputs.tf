output "db_url_arn" {
  description = "ARN of the database URL parameter"
  value       = aws_ssm_parameter.db_url.arn
}

output "db_username_arn" {
  description = "ARN of the database username parameter"
  value       = aws_ssm_parameter.db_username.arn
}

output "db_password_arn" {
  description = "ARN of the database password parameter"
  value       = aws_ssm_parameter.db_password.arn
}

output "jwt_secret_arn" {
  description = "ARN of the JWT secret parameter"
  value       = aws_ssm_parameter.jwt_secret.arn
}

output "ai_service_url_arn" {
  description = "ARN of the AI service URL parameter"
  value       = aws_ssm_parameter.ai_service_url.arn
}

output "qdrant_host_arn" {
  description = "ARN of the Qdrant host parameter"
  value       = aws_ssm_parameter.qdrant_host.arn
}

output "parameter_arns" {
  description = "All SSM parameter ARNs (for IAM policy)"
  value = [
    aws_ssm_parameter.db_url.arn,
    aws_ssm_parameter.db_username.arn,
    aws_ssm_parameter.db_password.arn,
    aws_ssm_parameter.jwt_secret.arn,
    aws_ssm_parameter.ai_service_url.arn,
    aws_ssm_parameter.qdrant_host.arn,
  ]
}
