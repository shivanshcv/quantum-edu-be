#!/bin/bash
# Loads the demo catalogue, learners, orders and LMS progress (deploy/seed/sample-data.sql).
# {{MEDIA}} placeholders become ${MEDIA_BASE_URL}, i.e. <R2_PUBLIC_URL>/seed.
set -eo pipefail
export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"

if [[ "${SEED_SAMPLE_DATA:-true}" != "true" ]]; then
  echo "[init] SEED_SAMPLE_DATA=${SEED_SAMPLE_DATA}; skipping sample data"
  exit 0
fi

media="$(printf '%s' "${MEDIA_BASE_URL%/}" | sed -E 's#([^:])/+#\1/#g')"
echo "[init] Loading sample data (media base: ${media})"
sed "s#{{MEDIA}}#${media}#g" /seed/sample-data.sql | mysql --protocol=socket -uroot quantum_education
