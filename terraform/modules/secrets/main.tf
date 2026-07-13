# ─────────────────────────────────────────────────────────────
# SSM Parameter Store — Standard tier (free)
# ─────────────────────────────────────────────────────────────

resource "aws_ssm_parameter" "db_url" {
  name  = "/${var.project_name}/db/url"
  type  = "String"
  value = var.db_connection_url

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-db-url"
  })
}

resource "aws_ssm_parameter" "db_username" {
  name  = "/${var.project_name}/db/username"
  type  = "String"
  value = var.db_username

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-db-username"
  })
}

resource "aws_ssm_parameter" "db_password" {
  name  = "/${var.project_name}/db/password"
  type  = "SecureString"
  value = var.db_password

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-db-password"
  })
}

resource "aws_ssm_parameter" "jwt_secret" {
  name  = "/${var.project_name}/jwt/secret"
  type  = "SecureString"
  value = var.jwt_secret

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-jwt-secret"
  })
}

resource "aws_ssm_parameter" "ai_service_url" {
  name  = "/${var.project_name}/ai/service-url"
  type  = "String"
  value = "http://ai-service.${var.project_name}.local:8000"

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-ai-service-url"
  })
}

resource "aws_ssm_parameter" "qdrant_host" {
  name  = "/${var.project_name}/ai/qdrant-host"
  type  = "String"
  value = "qdrant.${var.project_name}.local"

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-qdrant-host"
  })
}
