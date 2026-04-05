#!/bin/bash
#
# Deploy quantum-edu-be to AWS EC2 — PRODUCTION.
# Run from quantum-edu-be directory: ./deploy.prod.sh
#
# Prerequisites:
#   1. cp deploy.prod.env.example deploy.prod.env
#   2. Edit deploy.prod.env
#   3. AWS CLI configured, Docker running
#   4. ECR repo created (e.g. quantum-edu-be-production)
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() { echo -e "${GREEN}[deploy.prod]${NC} $1"; }
warn() { echo -e "${YELLOW}[deploy.prod]${NC} $1"; }
err() { echo -e "${RED}[deploy.prod]${NC} $1"; exit 1; }

ENV_FILE="${DEPLOY_PROD_ENV_FILE:-deploy.prod.env}"

if [[ ! -f "$ENV_FILE" ]]; then
  err "$ENV_FILE not found. Copy deploy.prod.env.example to deploy.prod.env and fill in your values:
  cp deploy.prod.env.example deploy.prod.env"
fi

set -a
# shellcheck source=/dev/null
source "$ENV_FILE"
set +a

for var in EC2_HOST EC2_USER SSH_KEY AWS_REGION ECR_URI RDS_ENDPOINT RDS_DB RDS_USER RDS_PASS JWT_SECRET APP_CORS_ORIGINS; do
  if [[ -z "${!var}" ]]; then
    err "Missing required variable in $ENV_FILE: $var"
  fi
done

SSH_KEY="${SSH_KEY/#\~/$HOME}"
if [[ ! -f "$SSH_KEY" ]]; then
  err "SSH key not found: $SSH_KEY"
fi

ECR_REGISTRY="${ECR_URI%%/*}"
VERIFICATION_BASE_URL="${VERIFICATION_BASE_URL:-}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"
MAIL_HOST="${MAIL_HOST:-smtp.example.com}"
MAIL_PORT="${MAIL_PORT:-587}"
MAIL_USERNAME="${MAIL_USERNAME:-}"
MAIL_PASSWORD="${MAIL_PASSWORD:-}"
EMAIL_FROM="${EMAIL_FROM:-noreply@example.com}"
APP_EMAIL_ENABLED="${APP_EMAIL_ENABLED:-true}"

if [[ -z "$VERIFICATION_BASE_URL" ]]; then
  warn "VERIFICATION_BASE_URL empty; ensure APP_CORS_ORIGINS includes FE origin and set VERIFICATION_BASE_URL in $ENV_FILE."
fi
VERIFICATION_BASE_URL="${VERIFICATION_BASE_URL:-${APP_CORS_ORIGINS%%,*}/verify-email}"

if [[ -z "${RAZORPAY_KEY_ID:-}" || -z "${RAZORPAY_KEY_SECRET:-}" ]]; then
  warn "RAZORPAY_KEY_ID or RAZORPAY_KEY_SECRET empty — payment features will fail until set in $ENV_FILE."
fi
RAZORPAY_KEY_ID="${RAZORPAY_KEY_ID:-}"
RAZORPAY_KEY_SECRET="${RAZORPAY_KEY_SECRET:-}"
RAZORPAY_WEBHOOK_SECRET="${RAZORPAY_WEBHOOK_SECRET:-}"

log "Building Docker image (linux/amd64)..."
docker build --platform linux/amd64 -t quantum-edu-be-production:latest . || err "Docker build failed"

log "Tagging for ECR..."
docker tag quantum-edu-be-production:latest "${ECR_URI}:latest"

log "Logging in to ECR..."
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$ECR_REGISTRY" || err "ECR login failed"

log "Pushing to ECR..."
docker push "${ECR_URI}:latest" || err "Docker push failed"

log "Deploying on EC2 ($SPRING_PROFILES_ACTIVE)..."
ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "${EC2_USER}@${EC2_HOST}" bash -s << REMOTE_SCRIPT
set -e
echo "[EC2] Logging in to ECR..."
aws ecr get-login-password --region ${AWS_REGION} | sudo docker login --username AWS --password-stdin ${ECR_REGISTRY}

echo "[EC2] Creating docker network..."
sudo docker network create quantum-net 2>/dev/null || true

if [[ "${MAIL_HOST}" == "mailpit" ]]; then
  echo "[EC2] Starting Mailpit..."
  if ! sudo docker ps -a --format '{{.Names}}' | grep -qx mailpit; then
    sudo docker run -d --name mailpit --restart unless-stopped --network quantum-net -p 1025:1025 -p 8025:8025 axllent/mailpit
  elif ! sudo docker ps --format '{{.Names}}' | grep -qx mailpit; then
    sudo docker start mailpit
  fi
else
  echo "[EC2] Using external SMTP (${MAIL_HOST})."
fi

echo "[EC2] Stopping old container..."
sudo docker stop quantum-edu-be 2>/dev/null || true
sudo docker rm quantum-edu-be 2>/dev/null || true

echo "[EC2] Pulling image..."
sudo docker pull ${ECR_URI}:latest

echo "[EC2] Starting container..."
sudo docker run -d \
  --name quantum-edu-be \
  --restart unless-stopped \
  --network quantum-net \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
  -e MYSQL_HOST="${RDS_ENDPOINT}" \
  -e MYSQL_PORT=3306 \
  -e MYSQL_DATABASE="${RDS_DB}" \
  -e MYSQL_USER="${RDS_USER}" \
  -e MYSQL_PASSWORD="${RDS_PASS}" \
  -e JWT_SECRET="${JWT_SECRET}" \
  -e APP_CORS_ORIGINS="${APP_CORS_ORIGINS}" \
  -e VERIFICATION_BASE_URL="${VERIFICATION_BASE_URL}" \
  -e RAZORPAY_KEY_ID="${RAZORPAY_KEY_ID}" \
  -e RAZORPAY_KEY_SECRET="${RAZORPAY_KEY_SECRET}" \
  -e RAZORPAY_WEBHOOK_SECRET="${RAZORPAY_WEBHOOK_SECRET}" \
  -e APP_EMAIL_ENABLED="${APP_EMAIL_ENABLED}" \
  -e MAIL_HOST="${MAIL_HOST}" \
  -e MAIL_PORT="${MAIL_PORT}" \
  -e MAIL_USERNAME="${MAIL_USERNAME}" \
  -e MAIL_PASSWORD="${MAIL_PASSWORD}" \
  -e EMAIL_FROM="${EMAIL_FROM}" \
  ${ECR_URI}:latest

sleep 3
sudo docker ps | grep quantum-edu-be
REMOTE_SCRIPT

log "Deploy complete."
log "Logs: ssh -i $SSH_KEY ${EC2_USER}@${EC2_HOST} 'sudo docker logs quantum-edu-be --tail 50'"
