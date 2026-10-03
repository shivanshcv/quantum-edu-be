#!/bin/bash
#
# One-time setup of a fresh Oracle Cloud Ubuntu VM (ARM or x86). Idempotent; safe to re-run.
# Run on the VM as the default user:  sudo bash bootstrap-vm.sh
# (deploy.prod.sh --bootstrap uploads and runs this for you.)
#
set -e

APP_USER="${SUDO_USER:-ubuntu}"
APP_HOME="$(getent passwd "$APP_USER" | cut -d: -f6)"

log() { echo -e "\033[0;32m[bootstrap]\033[0m $1"; }

if [[ $EUID -ne 0 ]]; then
  echo "Run with sudo: sudo bash $0" >&2
  exit 1
fi

log "Timezone -> Asia/Kolkata"
timedatectl set-timezone Asia/Kolkata || true

log "Installing base packages"
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq ca-certificates curl rsync gzip iptables-persistent netfilter-persistent >/dev/null

if ! command -v docker >/dev/null 2>&1; then
  log "Installing Docker Engine + compose plugin"
  curl -fsSL https://get.docker.com | sh
fi
systemctl enable --now docker
usermod -aG docker "$APP_USER"

# Oracle's Ubuntu images ship iptables rules that REJECT everything except SSH.
# Open HTTP/HTTPS (the VCN security list must allow them too).
log "Opening ports 80/443 in the host firewall"
for proto_port in "tcp 80" "tcp 443" "udp 443"; do
  set -- $proto_port
  if ! iptables -C INPUT -p "$1" --dport "$2" -m state --state NEW -j ACCEPT 2>/dev/null; then
    iptables -I INPUT 1 -p "$1" --dport "$2" -m state --state NEW -j ACCEPT
  fi
done
netfilter-persistent save >/dev/null

# Maven builds need headroom; swap keeps small shapes from OOM-ing during image builds.
if ! swapon --show | grep -q '/swapfile'; then
  log "Creating 2G swap file"
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile >/dev/null
  swapon /swapfile
  grep -q '^/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

log "Docker log rotation"
mkdir -p /etc/docker
if [[ ! -f /etc/docker/daemon.json ]]; then
  cat > /etc/docker/daemon.json <<'JSON'
{ "log-driver": "json-file", "log-opts": { "max-size": "10m", "max-file": "3" } }
JSON
  systemctl restart docker
fi

log "Preparing $APP_HOME/quantum-edu"
mkdir -p "$APP_HOME/quantum-edu/backups"
chown -R "$APP_USER:$APP_USER" "$APP_HOME/quantum-edu"

log "Nightly database backup at 02:30"
CRON_LINE="30 2 * * * cd $APP_HOME/quantum-edu/quantum-edu-be/deploy/oracle && ./backup-db.sh >> $APP_HOME/quantum-edu/backups/backup.log 2>&1"
( crontab -u "$APP_USER" -l 2>/dev/null | grep -v 'backup-db.sh' ; echo "$CRON_LINE" ) | crontab -u "$APP_USER" -

log "Done. Docker $(docker --version | cut -d' ' -f3 | tr -d ,) installed; re-login for docker group to apply."
