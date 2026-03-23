#!/bin/bash
#
# Deploy quantum-edu-be to AWS EC2 staging.
# Run from quantum-edu-be directory: ./deploy.sh
#
# Prerequisites:
#   1. cp deploy.env.example deploy.env
#   2. Edit deploy.env with your actual values
#   3. AWS CLI configured (aws configure)
#   4. Docker running
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() { echo -e "${GREEN}[deploy]${NC} $1"; }
warn() { echo -e "${YELLOW}[deploy]${NC} $1"; }
err() { echo -e "${RED}[deploy]${NC} $1"; exit 1; }

# Load deploy.env
if [[ ! -f deploy.env ]]; then
  err "deploy.env not found. Copy deploy.env.example to deploy.env and fill in your values:
  cp deploy.env.example deploy.env
  # Edit deploy.env"
fi

set -a
source deploy.env
set +a

# Validate required vars
for var in EC2_HOST EC2_USER SSH_KEY AWS_REGION ECR_URI RDS_ENDPOINT RDS_DB RDS_USER RDS_PASS JWT_SECRET APP_CORS_ORIGINS; do
  if [[ -z "${!var}" ]]; then
    err "Missing required variable in deploy.env: $var"
  fi
done

# Expand ~ in paths
SSH_KEY="${SSH_KEY/#\~/$HOME}"
if [[ ! -f "$SSH_KEY" ]]; then
  err "SSH key not found: $SSH_KEY"
fi

ECR_REGISTRY="${ECR_URI%%/*}"
VERIFICATION_BASE_URL="${VERIFICATION_BASE_URL:-${APP_CORS_ORIGINS}/verify-email}"
RAZORPAY_KEY_ID="${RAZORPAY_KEY_ID:-rzp_test_SLepofnigNDLcv}"
RAZORPAY_KEY_SECRET="${RAZORPAY_KEY_SECRET:-}"
RAZORPAY_WEBHOOK_SECRET="${RAZORPAY_WEBHOOK_SECRET:-shivansh1234}"
MAIL_HOST="${MAIL_HOST:-mailpit}"
MAIL_PORT="${MAIL_PORT:-587}"
MAIL_USERNAME="${MAIL_USERNAME:-}"
MAIL_PASSWORD="${MAIL_PASSWORD:-}"

log "Building Docker image..."
docker build --platform linux/amd64 -t quantum-edu-be-staging:latest . || err "Docker build failed"

log "Tagging image..."
docker tag quantum-edu-be-staging:latest "${ECR_URI}:latest"

log "Logging in to ECR..."
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$ECR_REGISTRY" || err "ECR login failed"

log "Pushing to ECR..."
docker push "${ECR_URI}:latest" || err "Docker push failed"

log "Deploying on EC2..."
ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "${EC2_USER}@${EC2_HOST}" bash -s << REMOTE_SCRIPT
set -e
echo "[EC2] Logging in to ECR..."
aws ecr get-login-password --region ${AWS_REGION} | sudo docker login --username AWS --password-stdin ${ECR_REGISTRY}

echo "[EC2] Creating docker network..."
sudo docker network create quantum-net 2>/dev/null || true

if [[ "${MAIL_HOST}" == "mailpit" ]]; then
  echo "[EC2] Starting Mailpit (SMTP 1025, Web UI 8025) - only when using Mailpit for email capture..."
  if ! sudo docker ps -a --format '{{.Names}}' | grep -qx mailpit; then
    sudo docker run -d --name mailpit --restart unless-stopped --network quantum-net -p 1025:1025 -p 8025:8025 axllent/mailpit
  elif ! sudo docker ps --format '{{.Names}}' | grep -qx mailpit; then
    sudo docker start mailpit
  fi
else
  echo "[EC2] Using external SMTP (${MAIL_HOST}) - Mailpit not started."
fi

echo "[EC2] Stopping and removing old container..."
sudo docker stop quantum-edu-be 2>/dev/null || true
sudo docker rm quantum-edu-be 2>/dev/null || true

echo "[EC2] Pulling latest image..."
sudo docker pull ${ECR_URI}:latest

echo "[EC2] Starting new container..."
sudo docker run -d \
  --name quantum-edu-be \
  --restart unless-stopped \
  --network quantum-net \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=staging \
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
  -e APP_EMAIL_ENABLED="${APP_EMAIL_ENABLED:-false}" \
  -e MAIL_HOST="${MAIL_HOST}" \
  -e MAIL_PORT="${MAIL_PORT}" \
  -e MAIL_USERNAME="${MAIL_USERNAME:-}" \
  -e MAIL_PASSWORD="${MAIL_PASSWORD:-}" \
  -e EMAIL_FROM="${EMAIL_FROM:-noreply@quantumedu.com}" \
  "${ECR_URI}:latest"

echo "[EC2] Verifying..."
sleep 3
sudo docker ps | grep quantum-edu-be
REMOTE_SCRIPT

log "Deploy complete."
log "Check logs: ssh -i $SSH_KEY ${EC2_USER}@${EC2_HOST} 'sudo docker logs quantum-edu-be --tail 50'"
