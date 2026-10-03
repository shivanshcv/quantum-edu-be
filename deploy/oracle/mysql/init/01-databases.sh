#!/bin/bash
# Runs once, on first start with an empty data volume.
# Creates both databases and the application user shared by Spring Boot and Django.
set -eo pipefail
export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"

echo "[init] Creating databases quantum_education, admin_django and user ${MYSQL_APP_USER}"
mysql --protocol=socket -uroot <<SQL
CREATE DATABASE IF NOT EXISTS quantum_education CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS admin_django CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${MYSQL_APP_USER}'@'%' IDENTIFIED BY '${MYSQL_APP_PASSWORD}';
GRANT ALL PRIVILEGES ON quantum_education.* TO '${MYSQL_APP_USER}'@'%';
GRANT ALL PRIVILEGES ON admin_django.* TO '${MYSQL_APP_USER}'@'%';
FLUSH PRIVILEGES;
SQL
