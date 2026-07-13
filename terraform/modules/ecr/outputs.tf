output "backend_repository_url" {
  description = "URL of the backend ECR repository"
  value       = aws_ecr_repository.backend.repository_url
}

output "ai_service_repository_url" {
  description = "URL of the AI service ECR repository"
  value       = aws_ecr_repository.ai_service.repository_url
}

output "backend_repository_arn" {
  description = "ARN of the backend ECR repository"
  value       = aws_ecr_repository.backend.arn
}

output "ai_service_repository_arn" {
  description = "ARN of the AI service ECR repository"
  value       = aws_ecr_repository.ai_service.arn
}
