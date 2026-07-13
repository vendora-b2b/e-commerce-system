# ─────────────────────────────────────────────────────────────
# DB Subnet Group
# ─────────────────────────────────────────────────────────────

resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = var.subnet_ids

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-db-subnet-group"
  })
}

# ─────────────────────────────────────────────────────────────
# RDS MySQL 8.0 Instance
# ─────────────────────────────────────────────────────────────

resource "aws_db_instance" "main" {
  identifier = "${var.project_name}-mysql"

  # Engine
  engine               = "mysql"
  engine_version       = "8.0"
  instance_class       = var.instance_class
  allocated_storage    = var.allocated_storage
  storage_type         = "gp3"
  storage_encrypted    = true

  # Database
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  # Networking
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [var.security_group_id]
  publicly_accessible    = false
  multi_az               = false

  # Backups & Maintenance (minimize cost)
  backup_retention_period = 0
  skip_final_snapshot     = true
  deletion_protection     = false

  # Disable performance insights (not needed for dev/testing)
  performance_insights_enabled = false

  # Apply changes immediately (dev environment)
  apply_immediately = true

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-mysql"
  })
}
