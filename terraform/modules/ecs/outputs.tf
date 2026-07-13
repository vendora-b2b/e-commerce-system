output "cluster_id" {
  description = "ECS cluster ID"
  value       = aws_ecs_cluster.main.id
}

output "cluster_name" {
  description = "ECS cluster name"
  value       = aws_ecs_cluster.main.name
}

output "backend_service_name" {
  description = "Name of the backend ECS service"
  value       = aws_ecs_service.backend.name
}

output "ai_service_name" {
  description = "Name of the AI ECS service"
  value       = aws_ecs_service.ai_service.name
}

output "qdrant_service_name" {
  description = "Name of the Qdrant ECS service"
  value       = aws_ecs_service.qdrant.name
}

output "service_discovery_namespace_id" {
  description = "Cloud Map namespace ID"
  value       = aws_service_discovery_private_dns_namespace.main.id
}
