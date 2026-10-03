#!/bin/bash
#
# Create the databases on Aiven MySQL and load the schema + demo data. Run from your laptop
# (needs Docker; uses the mysql:8.4 client image).
#
#   ./deploy/render/init-db.sh            first-time setup (refuses if tables already exist)
#   ./deploy/render/init-db.sh --reset    DROPS both databases and recreates everything
#   ./deploy/render/init-db.sh --no-seed  schema only, no demo data
#
# Databases:
#   quantum_education  schema from docs/schema.sql + deploy/seed/sample-data.sql
#   admin_django       empty; the admin service creates Django's tables on startup (migrate)
#
set -eo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$HERE/../.." && pwd)"
ENV_FILE="${DEPLOY_RENDER_ENV_FILE:-$REPO_DIR/deploy.render.env}"

RESET=false
SEED=true
for arg in "$@"; do
  case "$arg" in
    --reset) RESET=true ;;
    --no-seed) SEED=false ;;
    -h|--help) sed -n '2,13p' "$0"; exit 0 ;;
    *) echo "Unknown option: $arg" >&2; exit 1 ;;
  esac
done

[[ -f "$ENV_FILE" ]] || { echo "$ENV_FILE not found. cp deploy.render.env.example deploy.render.env and fill it in." >&2; exit 1; }
set -a
# shellcheck source=/dev/null
source "$ENV_FILE"
set +a
for var in MYSQL_HOST MYSQL_PORT MYSQL_USER MYSQL_PASSWORD; do
  [[ -n "${!var}" ]] || { echo "Missing $var in $ENV_FILE" >&2; exit 1; }
done
if $SEED && [[ -z "$R2_PUBLIC_URL" || "$R2_PUBLIC_URL" == *xxxx* ]]; then
  echo "Set R2_PUBLIC_URL in $ENV_FILE first (demo images/videos/PDFs are linked to it)." >&2
  exit 1
fi

MYSQL_IMAGE="${MYSQL_CLIENT_IMAGE:-mysql:8.4}"
sql() {
  docker run --rm -i --network "${DOCKER_NETWORK:-bridge}" -e MYSQL_PWD="$MYSQL_PASSWORD" "$MYSQL_IMAGE" \
    mysql --ssl-mode=REQUIRED --default-character-set=utf8mb4 \
    -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" "$@"
}

echo "Connecting to $MYSQL_HOST:$MYSQL_PORT as $MYSQL_USER ..."
sql -N -e "SELECT CONCAT('Connected: MySQL ', VERSION())"

existing="$(sql -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='quantum_education'")"
if [[ "$existing" != "0" ]]; then
  if ! $RESET; then
    echo "quantum_education already has $existing tables. Nothing to do."
    echo "Use --reset to DROP both databases and reload schema + demo data."
    exit 0
  fi
  read -r -p "This DELETES all data in quantum_education and admin_django. Type 'reset' to continue: " answer
  [[ "$answer" == "reset" ]] || { echo "Aborted."; exit 1; }
fi

if $RESET; then
  echo "Dropping databases..."
  sql -e "DROP DATABASE IF EXISTS quantum_education; DROP DATABASE IF EXISTS admin_django;"
fi

echo "Creating databases..."
sql -e "CREATE DATABASE IF NOT EXISTS quantum_education CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
        CREATE DATABASE IF NOT EXISTS admin_django CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo "Loading schema (docs/schema.sql)..."
sql quantum_education < "$REPO_DIR/docs/schema.sql"

if $SEED; then
  media="$(printf '%s' "${R2_PUBLIC_URL%/}/seed" | sed -E 's#([^:])/+#\1/#g')"
  echo "Loading demo data (media base: $media)..."
  sed "s#{{MEDIA}}#${media}#g" "$REPO_DIR/deploy/seed/sample-data.sql" | sql quantum_education
fi

sql -N quantum_education -e "SELECT CONCAT('Products: ', (SELECT COUNT(*) FROM product), ', lessons/quizzes: ', (SELECT COUNT(*) FROM product_content), ', users: ', (SELECT COUNT(*) FROM auth_user))"
echo "Done. The admin service creates its own tables in admin_django on its next start."
