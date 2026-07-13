#!/usr/bin/env bash
set -euo pipefail

# ═══════════════════════════════════════════════════════════════
# push-images.sh — Build & push Docker images to AWS ECR
#
# Usage:
#   cd terraform/
#   chmod +x push-images.sh
#   ./push-images.sh
#
# Prerequisites:
#   - AWS CLI configured with valid credentials
#   - Docker (with buildx) installed and running
#   - terraform apply has been run (ECR repos exist)
# ═══════════════════════════════════════════════════════════════

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Read outputs from Terraform state
REGION=$(cd "${SCRIPT_DIR}" && terraform output -raw aws_region 2>/dev/null || echo "us-east-1")
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_URL="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
PLATFORM="linux/amd64" # m7i-flex.large is x86_64 (Intel)

echo "══════════════════════════════════════════════════"
echo "  Vendora — ECR Image Push"
echo "  Region:   ${REGION}"
echo "  Account:  ${ACCOUNT_ID}"
echo "  Platform: ${PLATFORM}"
echo "  Project:  ${PROJECT_ROOT}"
echo "══════════════════════════════════════════════════"

# ── Step 1: Authenticate Docker to ECR ──────────────────────
echo ""
echo "🔑 Authenticating Docker to ECR..."
aws ecr get-login-password --region "${REGION}" \
  | docker login --username AWS --password-stdin "${ECR_URL}"

# ── Step 2: Build & push backend ────────────────────────────
BACKEND_IMAGE="${ECR_URL}/vendora-backend:latest"
echo ""
echo "🏗️  Building backend image (${PLATFORM})..."
docker buildx build --platform "${PLATFORM}" \
  -t "${BACKEND_IMAGE}" \
  --push \
  "${PROJECT_ROOT}"
echo "✅ Backend pushed: ${BACKEND_IMAGE}"

# ── Step 3: Build & push AI service ─────────────────────────
AI_IMAGE="${ECR_URL}/vendora-ai-service:latest"
echo ""
echo "🏗️  Building AI service image (${PLATFORM})..."
docker buildx build --platform "${PLATFORM}" \
  -t "${AI_IMAGE}" \
  --push \
  "${PROJECT_ROOT}/ai-service"
echo "✅ AI service pushed: ${AI_IMAGE}"

echo ""
echo "🎉 All images pushed successfully!"
echo ""
echo "Next steps:"
echo "  # Force ECS to pick up new images:"
echo "  aws ecs update-service --cluster vendora-cluster --service vendora-backend-svc --force-new-deployment --region ${REGION}"
echo "  aws ecs update-service --cluster vendora-cluster --service vendora-ai-svc --force-new-deployment --region ${REGION}"
