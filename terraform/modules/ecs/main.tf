# ═════════════════════════════════════════════════════════════
# 5a. ECS Cluster + Cloud Map Namespace (Service Connect)
# ═════════════════════════════════════════════════════════════

# The ECS Service Linked Role must exist before creating an ECS cluster
# with Service Connect. New AWS accounts don't have it pre-created.
resource "aws_iam_service_linked_role" "ecs" {
  aws_service_name = "ecs.amazonaws.com"
  description      = "ECS Service Linked Role"

  # If the SLR already exists (e.g. on a non-fresh account), ignore the error
  lifecycle {
    ignore_changes = [description]
  }
}

# Allow time for the SLR to propagate across AWS
resource "time_sleep" "ecs_slr_propagation" {
  depends_on      = [aws_iam_service_linked_role.ecs]
  create_duration = "15s"
}

resource "aws_service_discovery_private_dns_namespace" "main" {
  name        = "${var.project_name}.local"
  description = "Private DNS namespace for ECS Service Connect"
  vpc         = var.vpc_id

  depends_on = [time_sleep.ecs_slr_propagation]

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-service-discovery"
  })
}

resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster"

  service_connect_defaults {
    namespace = aws_service_discovery_private_dns_namespace.main.arn
  }

  setting {
    name  = "containerInsights"
    value = "disabled" # Keep costs down
  }

  depends_on = [time_sleep.ecs_slr_propagation]

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-cluster"
  })
}

# ═════════════════════════════════════════════════════════════
# 5b. EC2 Launch Template + Auto Scaling Group
# ═════════════════════════════════════════════════════════════

# Fetch latest ECS-optimized AMI from SSM Parameter Store
data "aws_ssm_parameter" "ecs_ami" {
  name = "/aws/service/ecs/optimized-ami/amazon-linux-2023/recommended/image_id"
}

resource "aws_launch_template" "ecs" {
  name_prefix   = "${var.project_name}-ecs-"
  image_id      = data.aws_ssm_parameter.ecs_ami.value
  instance_type = var.instance_type

  iam_instance_profile {
    name = aws_iam_instance_profile.ecs_node.name
  }

  network_interfaces {
    associate_public_ip_address = true
    security_groups             = [var.ecs_sg_id]
  }

  user_data = base64encode(templatefile("${path.module}/user_data.sh", {
    cluster_name = aws_ecs_cluster.main.name
  }))

  tag_specifications {
    resource_type = "instance"
    tags = merge(var.common_tags, {
      Name = "${var.project_name}-ecs-node"
    })
  }

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-ecs-lt"
  })
}

resource "aws_autoscaling_group" "ecs" {
  name_prefix      = "${var.project_name}-ecs-"
  min_size         = 2
  max_size         = 2
  desired_capacity = 2
  force_delete     = true # Don't wait for instances to drain on destroy

  vpc_zone_identifier = var.subnet_ids

  launch_template {
    id      = aws_launch_template.ecs.id
    version = "$Latest"
  }

  tag {
    key                 = "AmazonECSManaged"
    value               = "true"
    propagate_at_launch = true
  }

  tag {
    key                 = "Name"
    value               = "${var.project_name}-ecs-node"
    propagate_at_launch = true
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ECS Capacity Provider: links ASG to ECS cluster
resource "aws_ecs_capacity_provider" "main" {
  name = "${var.project_name}-ec2-cp"

  auto_scaling_group_provider {
    auto_scaling_group_arn         = aws_autoscaling_group.ecs.arn
    managed_termination_protection = "DISABLED"

    managed_scaling {
      status          = "ENABLED"
      target_capacity = 100
    }
  }

  tags = var.common_tags
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name       = aws_ecs_cluster.main.name
  capacity_providers = [aws_ecs_capacity_provider.main.name]

  default_capacity_provider_strategy {
    base              = 1
    weight            = 100
    capacity_provider = aws_ecs_capacity_provider.main.name
  }
}

# ═════════════════════════════════════════════════════════════
# 5c. IAM Roles
# ═════════════════════════════════════════════════════════════

# --- EC2 Instance Role (for ECS agent) ---

data "aws_iam_policy_document" "ec2_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_node" {
  name_prefix        = "${var.project_name}-ecs-node-"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume.json
  tags               = var.common_tags
}

resource "aws_iam_role_policy_attachment" "ecs_node_ecs" {
  role       = aws_iam_role.ecs_node.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}

resource "aws_iam_role_policy_attachment" "ecs_node_ssm" {
  role       = aws_iam_role.ecs_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "ecs_node_cw" {
  role       = aws_iam_role.ecs_node.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

resource "aws_iam_role_policy_attachment" "ecs_node_ecr" {
  role       = aws_iam_role.ecs_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_instance_profile" "ecs_node" {
  name_prefix = "${var.project_name}-ecs-node-"
  role        = aws_iam_role.ecs_node.name
  tags        = var.common_tags
}

# --- ECS Task Execution Role (pulling images + reading secrets) ---

data "aws_iam_policy_document" "ecs_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "task_execution" {
  name_prefix        = "${var.project_name}-task-exec-"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume.json
  tags               = var.common_tags
}

resource "aws_iam_role_policy_attachment" "task_exec_policy" {
  role       = aws_iam_role.task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Allow reading SSM parameters for secrets injection
data "aws_iam_policy_document" "ssm_read" {
  statement {
    actions = [
      "ssm:GetParameters",
      "ssm:GetParameter",
    ]
    resources = var.ssm_parameter_arns
  }
}

resource "aws_iam_policy" "ssm_read" {
  name_prefix = "${var.project_name}-ssm-read-"
  policy      = data.aws_iam_policy_document.ssm_read.json
  tags        = var.common_tags
}

resource "aws_iam_role_policy_attachment" "task_exec_ssm" {
  role       = aws_iam_role.task_execution.name
  policy_arn = aws_iam_policy.ssm_read.arn
}

# --- ECS Task Role (for application-level permissions) ---

resource "aws_iam_role" "task" {
  name_prefix        = "${var.project_name}-task-"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume.json
  tags               = var.common_tags
}

resource "aws_iam_role_policy_attachment" "task_cw" {
  role       = aws_iam_role.task.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

# ═════════════════════════════════════════════════════════════
# 5d. CloudWatch Log Groups
# ═════════════════════════════════════════════════════════════

resource "aws_cloudwatch_log_group" "backend" {
  name              = "/ecs/${var.project_name}-backend"
  retention_in_days = 7
  tags              = var.common_tags
}

resource "aws_cloudwatch_log_group" "ai_service" {
  name              = "/ecs/${var.project_name}-ai-service"
  retention_in_days = 7
  tags              = var.common_tags
}

# ═════════════════════════════════════════════════════════════
# 5e. Task Definitions (awsvpc network mode)
# ═════════════════════════════════════════════════════════════

# --- Backend (Spring Boot) ---

resource "aws_ecs_task_definition" "backend" {
  family                   = "${var.project_name}-backend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["EC2"]
  execution_role_arn       = aws_iam_role.task_execution.arn
  task_role_arn            = aws_iam_role.task.arn
  cpu                      = "512"
  memory                   = "1024"

  container_definitions = jsonencode([{
    name  = "backend"
    image = "${var.backend_image}:latest"
    cpu   = 512

    portMappings = [{
      containerPort = 8080
      name          = "backend"
      appProtocol   = "http"
    }]

    environment = [
      { name = "DATABASE_URL", value = "jdbc:mysql://${var.rds_endpoint}:${var.rds_port}/${var.db_name}" },
      { name = "AI_SERVICE_URL", value = "http://ai-service.${var.project_name}.local:8000" },
      { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
    ]

    secrets = [
      { name = "DATABASE_USERNAME", valueFrom = "/${var.project_name}/db/username" },
      { name = "DATABASE_PASSWORD", valueFrom = "/${var.project_name}/db/password" },
      { name = "JWT_SECRET", valueFrom = "/${var.project_name}/jwt/secret" },
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.backend.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "backend"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "wget -q --spider http://localhost:8081/actuator/health || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 120
    }

    essential = true
  }])

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-backend-task"
  })
}

# --- AI Service (Python FastAPI) ---

resource "aws_ecs_task_definition" "ai_service" {
  family                   = "${var.project_name}-ai-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["EC2"]
  execution_role_arn       = aws_iam_role.task_execution.arn
  task_role_arn            = aws_iam_role.task.arn
  cpu                      = "512"
  memory                   = "2048"

  container_definitions = jsonencode([{
    name  = "ai-service"
    image = "${var.ai_service_image}:latest"
    cpu   = 512

    portMappings = [{
      containerPort = 8000
      name          = "ai-service"
      appProtocol   = "http"
    }]

    environment = [
      { name = "QDRANT_HOST", value = "qdrant.${var.project_name}.local" },
      { name = "QDRANT_PORT", value = "6333" },
      { name = "SPRING_BOOT_URL", value = "http://backend.${var.project_name}.local:8080" },
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.ai_service.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ai-service"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "curl -f http://localhost:8000/health || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 60
    }

    essential = true
  }])

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-ai-service-task"
  })
}

# --- Qdrant (Vector Database) ---

resource "aws_ecs_task_definition" "qdrant" {
  family                   = "${var.project_name}-qdrant"
  network_mode             = "awsvpc"
  requires_compatibilities = ["EC2"]
  execution_role_arn       = aws_iam_role.task_execution.arn
  task_role_arn            = aws_iam_role.task.arn
  cpu                      = "256"
  memory                   = "1024"

  container_definitions = jsonencode([{
    name  = "qdrant"
    image = "qdrant/qdrant:latest"
    cpu   = 256

    portMappings = [
      {
        containerPort = 6333
        name          = "qdrant"
        appProtocol   = "http"
      },
      {
        containerPort = 6334
        name          = "qdrant-grpc"
        appProtocol   = "grpc"
      },
    ]

    mountPoints = [{
      sourceVolume  = "qdrant-storage"
      containerPath = "/qdrant/storage"
    }]

    essential = true
  }])

  volume {
    name = "qdrant-storage"

    docker_volume_configuration {
      scope         = "shared"
      autoprovision = true
      driver        = "local"
    }
  }

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-qdrant-task"
  })
}

# ═════════════════════════════════════════════════════════════
# 5f. ECS Services (with Service Connect)
# ═════════════════════════════════════════════════════════════

# --- Qdrant Service (must start first — AI service depends on it) ---

resource "aws_ecs_service" "qdrant" {
  name            = "${var.project_name}-qdrant-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.qdrant.arn
  desired_count   = 1
  force_delete    = true # Force-stop tasks on destroy instead of waiting

  capacity_provider_strategy {
    capacity_provider = aws_ecs_capacity_provider.main.name
    weight            = 100
    base              = 1
  }

  network_configuration {
    subnets         = var.subnet_ids
    security_groups = [var.ecs_sg_id]
  }

  service_connect_configuration {
    enabled   = true
    namespace = aws_service_discovery_private_dns_namespace.main.arn

    service {
      port_name      = "qdrant"
      discovery_name = "qdrant"
      client_alias {
        port     = 6333
        dns_name = "qdrant.${var.project_name}.local"
      }
    }

    service {
      port_name      = "qdrant-grpc"
      discovery_name = "qdrant-grpc"
      client_alias {
        port     = 6334
        dns_name = "qdrant-grpc.${var.project_name}.local"
      }
    }
  }

  depends_on = [aws_ecs_cluster_capacity_providers.main]

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-qdrant-svc"
  })
}

# --- AI Service (depends on Qdrant) ---

resource "aws_ecs_service" "ai_service" {
  name            = "${var.project_name}-ai-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.ai_service.arn
  desired_count   = 1
  force_delete    = true # Force-stop tasks on destroy instead of waiting

  capacity_provider_strategy {
    capacity_provider = aws_ecs_capacity_provider.main.name
    weight            = 100
  }

  network_configuration {
    subnets         = var.subnet_ids
    security_groups = [var.ecs_sg_id]
  }

  service_connect_configuration {
    enabled   = true
    namespace = aws_service_discovery_private_dns_namespace.main.arn

    service {
      port_name      = "ai-service"
      discovery_name = "ai-service"
      client_alias {
        port     = 8000
        dns_name = "ai-service.${var.project_name}.local"
      }
    }
  }

  depends_on = [
    aws_ecs_cluster_capacity_providers.main,
    aws_ecs_service.qdrant,
  ]

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-ai-svc"
  })
}

# --- Backend Service (depends on AI service, registered with ALB) ---

resource "aws_ecs_service" "backend" {
  name            = "${var.project_name}-backend-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = 1
  force_delete    = true # Force-stop tasks on destroy instead of waiting

  capacity_provider_strategy {
    capacity_provider = aws_ecs_capacity_provider.main.name
    weight            = 100
  }

  network_configuration {
    subnets         = var.subnet_ids
    security_groups = [var.ecs_sg_id]
  }

  load_balancer {
    target_group_arn = var.backend_target_group_arn
    container_name   = "backend"
    container_port   = 8080
  }

  service_connect_configuration {
    enabled   = true
    namespace = aws_service_discovery_private_dns_namespace.main.arn

    service {
      port_name      = "backend"
      discovery_name = "backend"
      client_alias {
        port     = 8080
        dns_name = "backend.${var.project_name}.local"
      }
    }
  }

  depends_on = [
    aws_ecs_cluster_capacity_providers.main,
    aws_ecs_service.ai_service,
  ]

  tags = merge(var.common_tags, {
    Name = "${var.project_name}-backend-svc"
  })
}
