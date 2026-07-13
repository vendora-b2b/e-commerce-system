output "dns_name" {
  description = "DNS name of the ALB (public endpoint)"
  value       = aws_lb.main.dns_name
}

output "arn" {
  description = "ARN of the ALB"
  value       = aws_lb.main.arn
}

output "backend_target_group_arn" {
  description = "ARN of the backend target group"
  value       = aws_lb_target_group.backend.arn
}

output "zone_id" {
  description = "Route 53 zone ID of the ALB (for DNS aliases)"
  value       = aws_lb.main.zone_id
}
