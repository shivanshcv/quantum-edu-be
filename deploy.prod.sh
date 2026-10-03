#!/bin/bash
#
# Deploy Quantum Education (backend + Django admin + MySQL + Caddy) to the Oracle Cloud VM.
# Run from your laptop in the quantum-edu-be directory.
#
#   ./deploy.prod.sh --bootstrap        First run on a fresh VM: installs Docker, firewall, swap, backups
#   ./deploy.prod.sh                    Sync code + config and (re)build/start every service
#   ./deploy.prod.sh --only backend     Rebuild/restart a single service (backend | admin | caddy | mysql)
#   ./deploy.prod.sh --reset-db         DESTROYS the database volume, then re-creates schema + sample data
#   ./deploy.prod.sh --status           Show container status and recent logs
#
# Prerequisites: deploy.prod.env (create with ./deploy/oracle/init-env.sh), SSH access to the VM,
# and the admin repo checked out next to this one (override with ADMIN_DIR=/path/to/quantum-edu-admin).
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

BOOTSTRAP=false
RESET_DB=false
STATUS_ONLY=false
ONLY=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --bootstrap) BOOTSTRAP=true ;;
    --reset-db) RESET_DB=true ;;
    --status) STATUS_ONLY=true ;;
    --only) ONLY="$2"; shift ;;
    -h|--help) sed -n '2,15p' "$0"; exit 0 ;;
    *) err "Unknown option: $1 (see --help)" ;;
  esac
  shift
done
if [[ -n "$ONLY" && ! "$ONLY" =~ ^(backend|admin|caddy|mysql)$ ]]; then
  err "--only must be one of: backend, admin, caddy, mysql"
fi

ENV_FILE="${DEPLOY_PROD_ENV_FILE:-deploy.prod.env}"
if [[ ! -f "$ENV_FILE" ]]; then
  err "$ENV_FILE not found. Create it with:
  ./deploy/oracle/init-env.sh"
fi

set -a
# shellcheck source=/dev/null
source "$ENV_FILE"
set +a

for var in VM_HOST VM_USER SSH_KEY API_DOMAIN ADMIN_DOMAIN FRONTEND_URL ACME_EMAIL \
           MYSQL_ROOT_PASSWORD MYSQL_APP_PASSWORD JWT_SECRET DJANGO_SECRET_KEY; do
  if [[ -z "${!var}" ]]; then
    err "Missing required variable in $ENV_FILE: $var"
  fi
done
if grep -q '__GENERATE__' "$ENV_FILE"; then
  err "$ENV_FILE still contains __GENERATE__ placeholders. Use ./deploy/oracle/init-env.sh or replace them."
fi
[[ -z "$R2_ACCOUNT_ID" || "$R2_ACCOUNT_ID" == your_* ]] && warn "R2 is not configured: media will not load and admin uploads are disabled."
[[ "$RAZORPAY_KEY_ID" == *xxxx* || -z "$RAZORPAY_KEY_ID" ]] && warn "Razorpay keys not set: paid checkout will fail (free courses still work)."

SSH_KEY="${SSH_KEY/#\~/$HOME}"
[[ -f "$SSH_KEY" ]] || err "SSH key not found: $SSH_KEY"

ADMIN_DIR="${ADMIN_DIR:-$SCRIPT_DIR/../quantum-edu-admin}"
[[ -f "$ADMIN_DIR/manage.py" ]] || err "Admin repo not found at $ADMIN_DIR (set ADMIN_DIR)"

REMOTE_ROOT="/home/${VM_USER}/quantum-edu"
SSH_OPTS=(-i "$SSH_KEY" -o StrictHostKeyChecking=accept-new -o ServerAliveInterval=30)
REMOTE="${VM_USER}@${VM_HOST}"
remote() { ssh "${SSH_OPTS[@]}" "$REMOTE" "$@"; }

if $STATUS_ONLY; then
  remote "cd $REMOTE_ROOT/quantum-edu-be/deploy/oracle && docker compose ps && docker compose logs --tail 30"
  exit 0
fi

if $BOOTSTRAP; then
  log "Bootstrapping VM ${VM_HOST} (Docker, firewall, swap, backups)..."
  scp "${SSH_OPTS[@]}" deploy/oracle/bootstrap-vm.sh "$REMOTE:/tmp/bootstrap-vm.sh"
  remote "sudo bash /tmp/bootstrap-vm.sh"
fi

log "Syncing quantum-edu-be -> ${VM_HOST}:${REMOTE_ROOT}/quantum-edu-be"
remote "mkdir -p $REMOTE_ROOT/quantum-edu-be $REMOTE_ROOT/quantum-edu-admin"
rsync -az --delete -e "ssh ${SSH_OPTS[*]}" \
  --exclude '.git' --exclude 'target' --exclude '.idea' --exclude '.vscode' --exclude '.cursor' \
  --exclude '.DS_Store' --exclude 'deploy.env' --exclude 'deploy.prod.env*' \
  --exclude 'deploy/oracle/.env' --exclude 'deploy/seed/media/out' \
  ./ "$REMOTE:$REMOTE_ROOT/quantum-edu-be/"

log "Syncing quantum-edu-admin -> ${VM_HOST}:${REMOTE_ROOT}/quantum-edu-admin"
rsync -az --delete -e "ssh ${SSH_OPTS[*]}" \
  --exclude '.git' --exclude '.venv' --exclude '__pycache__' --exclude '*.pyc' --exclude 'staticfiles' \
  --exclude '.DS_Store' --exclude '.env' --exclude 'deploy.env' --exclude 'deploy.prod.env' \
  --exclude 'gunicorn.env' --exclude 'db.sqlite3' \
  "$ADMIN_DIR/" "$REMOTE:$REMOTE_ROOT/quantum-edu-admin/"

log "Uploading configuration"
scp "${SSH_OPTS[@]}" "$ENV_FILE" "$REMOTE:$REMOTE_ROOT/quantum-edu-be/deploy/oracle/.env"
remote "chmod 600 $REMOTE_ROOT/quantum-edu-be/deploy/oracle/.env && chmod +x $REMOTE_ROOT/quantum-edu-be/deploy/oracle/*.sh $REMOTE_ROOT/quantum-edu-be/deploy/oracle/mysql/init/*.sh"

log "Building and starting containers${ONLY:+ ($ONLY)} — first build takes several minutes..."
remote bash -s << REMOTE_SCRIPT
set -e
cd $REMOTE_ROOT/quantum-edu-be/deploy/oracle
DOCKER=docker
docker info >/dev/null 2>&1 || DOCKER="sudo docker"

if [[ "$RESET_DB" == "true" ]]; then
  echo "[VM] Resetting database volume..."
  \$DOCKER compose stop backend admin mysql 2>/dev/null || true
  \$DOCKER compose rm -f mysql 2>/dev/null || true
  \$DOCKER volume rm quantum-edu_mysql-data 2>/dev/null || true
fi

\$DOCKER compose up -d --build --remove-orphans $ONLY
\$DOCKER image prune -f >/dev/null
sleep 5
\$DOCKER compose ps
REMOTE_SCRIPT

log "Deploy complete."
log "API:      https://${API_DOMAIN}/health"
log "Admin:    https://${ADMIN_DOMAIN}/admin/"
log "Frontend: ${FRONTEND_URL}"
log "Status/logs: ./deploy.prod.sh --status"
