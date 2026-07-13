# ═════════════════════════════════════════════════════════════
# Vendora E-Commerce — AWS Infrastructure
# ═════════════════════════════════════════════════════════════

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "vendora"
      ManagedBy   = "terraform"
      Environment = "dev"
    }
  }
}

# ─────────────────────────────────────────────────────────────
# Data Sources
# ─────────────────────────────────────────────────────────────

data "aws_availability_zones" "available" {
  state = "available"
}

# ─────────────────────────────────────────────────────────────
# Module: Networking (VPC, Subnets, Security Groups)
# ─────────────────────────────────────────────────────────────

module "networking" {
  source = "./modules/networking"

  project_name         = var.project_name
  vpc_cidr             = "10.0.0.0/16"
  public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
  private_subnet_cidrs = ["10.0.10.0/24", "10.0.11.0/24"]
  availability_zones   = slice(data.aws_availability_zones.available.names, 0, 2)
}

# ─────────────────────────────────────────────────────────────
# Module: ECR (Container Registries)
# ─────────────────────────────────────────────────────────────

module "ecr" {
  source = "./modules/ecr"

  project_name = var.project_name
}

# ─────────────────────────────────────────────────────────────
# Module: ALB (Application Load Balancer)
# ─────────────────────────────────────────────────────────────

module "alb" {
  source = "./modules/alb"

  project_name      = var.project_name
  vpc_id            = module.networking.vpc_id
  subnet_ids        = module.networking.public_subnet_ids
  security_group_id = module.networking.alb_sg_id
}

# ─────────────────────────────────────────────────────────────
# Module: Database (RDS MySQL)
# ─────────────────────────────────────────────────────────────

module "database" {
  source = "./modules/database"

  project_name      = var.project_name
  vpc_id            = module.networking.vpc_id
  subnet_ids        = module.networking.private_subnet_ids
  security_group_id = module.networking.rds_sg_id
  db_password       = var.db_password
}

# ─────────────────────────────────────────────────────────────
# Module: Secrets (SSM Parameter Store)
# ─────────────────────────────────────────────────────────────

module "secrets" {
  source = "./modules/secrets"

  project_name      = var.project_name
  db_connection_url = module.database.connection_url
  db_password       = var.db_password
  jwt_secret        = var.jwt_secret
}

# ─────────────────────────────────────────────────────────────
# Module: ECS (Cluster, Tasks, Services, Service Connect)
# ─────────────────────────────────────────────────────────────

module "ecs" {
  source = "./modules/ecs"

  project_name             = var.project_name
  aws_region               = var.aws_region
  vpc_id                   = module.networking.vpc_id
  subnet_ids               = module.networking.public_subnet_ids
  ecs_sg_id                = module.networking.ecs_sg_id
  instance_type            = var.instance_type
  backend_image            = module.ecr.backend_repository_url
  ai_service_image         = module.ecr.ai_service_repository_url
  rds_endpoint             = module.database.endpoint
  ssm_parameter_arns       = module.secrets.parameter_arns
  backend_target_group_arn = module.alb.backend_target_group_arn
}
