# ─────────────────────────────────────────────────────────────
# ECR Repository: Backend (Spring Boot)
# ─────────────────────────────────────────────────────────────

resource "aws_ecr_repository" "backend" {
  name                 = "${var.project_name}-backend"
  image_tag_mutability = "MUTABLE"
  force_delete         = true # Allow terraform destroy to delete even with images

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-backend"
  })
}

# ─────────────────────────────────────────────────────────────
# ECR Repository: AI Service (Python FastAPI)
# ─────────────────────────────────────────────────────────────

resource "aws_ecr_repository" "ai_service" {
  name                 = "${var.project_name}-ai-service"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-ai-service"
  })
}

# ─────────────────────────────────────────────────────────────
# Lifecycle Policy: Keep only the last 5 images per repo
# (stay within 500 MB free tier)
# ─────────────────────────────────────────────────────────────

resource "aws_ecr_lifecycle_policy" "backend" {
  repository = aws_ecr_repository.backend.name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep only last 5 images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 5
      }
      action = {
        type = "expire"
      }
    }]
  })
}

resource "aws_ecr_lifecycle_policy" "ai_service" {
  repository = aws_ecr_repository.ai_service.name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep only last 5 images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 5
      }
      action = {
        type = "expire"
      }
    }]
  })
}
