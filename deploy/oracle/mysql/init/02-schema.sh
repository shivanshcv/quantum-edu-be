#!/bin/bash
# Creates all quantum_education tables from docs/schema.sql (mounted at /seed/schema.sql).
# admin_django tables are created by Django's `migrate` when the admin container starts.
set -eo pipefail
export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"

echo "[init] Loading schema into quantum_education"
mysql --protocol=socket -uroot quantum_education < /seed/schema.sql
