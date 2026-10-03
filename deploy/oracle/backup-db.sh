#!/bin/bash
#
# Dump both databases to ~/quantum-edu/backups (7 days kept) and optionally copy the dump to R2.
# Run on the VM from deploy/oracle/ (cron does this nightly; see bootstrap-vm.sh).
#
set -eo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"
set -a
# shellcheck source=/dev/null
source .env
set +a

BACKUP_DIR="${BACKUP_DIR:-$HOME/quantum-edu/backups}"
STAMP="$(date +%Y%m%d-%H%M%S)"
FILE="$BACKUP_DIR/quantum-edu-$STAMP.sql.gz"
mkdir -p "$BACKUP_DIR"

echo "[backup] $(date) -> $FILE"
docker compose exec -T -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql \
  mysqldump -uroot --single-transaction --routines --triggers --set-gtid-purged=OFF \
  --databases quantum_education admin_django | gzip > "$FILE"

find "$BACKUP_DIR" -name 'quantum-edu-*.sql.gz' -mtime +7 -delete

# Never the public media bucket: dumps contain user emails and password hashes.
if [[ -n "${BACKUP_R2_BUCKET:-}" && -n "$R2_ACCOUNT_ID" && -n "$R2_ACCESS_KEY_ID" ]]; then
  echo "[backup] Uploading to r2://$BACKUP_R2_BUCKET/"
  docker run --rm -v "$BACKUP_DIR:/backups:ro" \
    -e AWS_ACCESS_KEY_ID="$R2_ACCESS_KEY_ID" -e AWS_SECRET_ACCESS_KEY="$R2_SECRET_ACCESS_KEY" \
    -e AWS_DEFAULT_REGION=auto -e AWS_REQUEST_CHECKSUM_CALCULATION=when_required \
    amazon/aws-cli s3 cp "/backups/$(basename "$FILE")" "s3://$BACKUP_R2_BUCKET/$(basename "$FILE")" \
    --endpoint-url "https://$R2_ACCOUNT_ID.r2.cloudflarestorage.com" --only-show-errors
fi
echo "[backup] done"
