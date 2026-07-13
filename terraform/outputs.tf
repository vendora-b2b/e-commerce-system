# ─────────────────────────────────────────────────────────────
# Key Outputs — displayed after terraform apply
# ─────────────────────────────────────────────────────────────

output "alb_dns_name" {
  description = "Public URL of the application (ALB DNS name)"
  value       = "http://${module.alb.dns_name}"
}

output "rds_endpoint" {
  description = "RDS MySQL endpoint"
  value       = module.database.endpoint
}

output "ecr_backend_url" {
  description = "ECR repository URL for the backend image"
  value       = module.ecr.backend_repository_url
}

output "ecr_ai_service_url" {
  description = "ECR repository URL for the AI service image"
  value       = module.ecr.ai_service_repository_url
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = module.ecs.cluster_name
}

output "aws_region" {
  description = "AWS region the infrastructure is deployed in"
  value       = var.aws_region
}
