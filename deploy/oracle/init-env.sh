#!/bin/bash
#
# Create quantum-edu-be/deploy.prod.env from deploy.prod.env.example, replacing every
# __GENERATE__ placeholder with a fresh random secret. Refuses to overwrite an existing file.
#
set -e

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
EXAMPLE="$REPO_DIR/deploy.prod.env.example"
TARGET="$REPO_DIR/deploy.prod.env"

if [[ -f "$TARGET" ]]; then
  if grep -q '^EC2_HOST=' "$TARGET"; then
    mv "$TARGET" "$TARGET.aws-backup"
    echo "Moved old AWS deploy.prod.env to deploy.prod.env.aws-backup"
  else
    echo "$TARGET already exists; not overwriting." >&2
    exit 1
  fi
fi

: > "$TARGET"
chmod 600 "$TARGET"
while IFS= read -r line || [[ -n "$line" ]]; do
  while [[ "$line" == *__GENERATE__* ]]; do
    line="${line/__GENERATE__/$(openssl rand -hex 24)}"
  done
  printf '%s\n' "$line" >> "$TARGET"
done < "$EXAMPLE"

echo "Created $TARGET with generated secrets."
echo "Now edit the REQUIRED values (VM_HOST, SSH_KEY, domains, FRONTEND_URL, Gmail, Razorpay, R2)."
