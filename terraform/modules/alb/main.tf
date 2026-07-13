# ─────────────────────────────────────────────────────────────
# Application Load Balancer
# ─────────────────────────────────────────────────────────────

resource "aws_lb" "main" {
  name               = "${var.project_name}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [var.security_group_id]
  subnets            = var.subnet_ids

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-alb"
  })
}

# ─────────────────────────────────────────────────────────────
# Target Group: Backend (Spring Boot on port 8080)
# ─────────────────────────────────────────────────────────────

resource "aws_lb_target_group" "backend" {
  name        = "${var.project_name}-backend-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip" # Required for awsvpc network mode

  health_check {
    enabled             = true
    path                = "/actuator/health"
    port                = "8081"
    protocol            = "HTTP"
    healthy_threshold   = 2
    unhealthy_threshold = 5
    timeout             = 10
    interval            = 30
    matcher             = "200"
  }

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-backend-tg"
  })
}

# ─────────────────────────────────────────────────────────────
# Listener: HTTP port 80 → Backend
# ─────────────────────────────────────────────────────────────

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.backend.arn
  }

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-http-listener"
  })
}
