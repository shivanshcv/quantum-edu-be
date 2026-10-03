#!/bin/bash
#
# Upload the demo media (seed/media/out) to R2 under seed/ and set the bucket CORS policy so the
# frontend's PDF viewer can fetch handouts. Run from your laptop; needs Docker (uses amazon/aws-cli).
#
#   ./deploy/upload-media.sh            # sync media + CORS
#   ./deploy/upload-media.sh --cors     # only (re)apply CORS
#
set -e

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$HERE/.." && pwd)"
if [[ -n "$DEPLOY_PROD_ENV_FILE" ]]; then
  ENV_FILE="$DEPLOY_PROD_ENV_FILE"
elif [[ -f "$REPO_DIR/deploy.render.env" ]]; then
  ENV_FILE="$REPO_DIR/deploy.render.env"
else
  ENV_FILE="$REPO_DIR/deploy.prod.env"
fi
MEDIA_DIR="$HERE/seed/media/out"

[[ -f "$ENV_FILE" ]] || { echo "$ENV_FILE not found (create deploy.render.env from deploy.render.env.example)" >&2; exit 1; }
set -a
# shellcheck source=/dev/null
source "$ENV_FILE"
set +a
for var in R2_ACCOUNT_ID R2_ACCESS_KEY_ID R2_SECRET_ACCESS_KEY R2_BUCKET_NAME FRONTEND_URL; do
  [[ -n "${!var}" ]] || { echo "Missing $var in $ENV_FILE" >&2; exit 1; }
done

aws() {
  docker run --rm -v "$HERE:/work" -w /work \
    -e AWS_ACCESS_KEY_ID="$R2_ACCESS_KEY_ID" -e AWS_SECRET_ACCESS_KEY="$R2_SECRET_ACCESS_KEY" \
    -e AWS_DEFAULT_REGION=auto \
    -e AWS_REQUEST_CHECKSUM_CALCULATION=when_required -e AWS_RESPONSE_CHECKSUM_VALIDATION=when_required \
    amazon/aws-cli "$@" --endpoint-url "https://$R2_ACCOUNT_ID.r2.cloudflarestorage.com"
}

if [[ "$1" != "--cors" ]]; then
  if [[ ! -d "$MEDIA_DIR" ]] || [[ -z "$(ls -A "$MEDIA_DIR")" ]]; then
    echo "No media in $MEDIA_DIR. Generate it first: python3 deploy/seed/build.py media" >&2
    exit 1
  fi
  echo "Uploading $(find "$MEDIA_DIR" -type f | wc -l | tr -d ' ') files to r2://$R2_BUCKET_NAME/seed/ ..."
  aws s3 sync /work/seed/media/out "s3://$R2_BUCKET_NAME/seed/" --only-show-errors
  echo "Media uploaded."
fi

ORIGINS="\"$FRONTEND_URL\", \"http://localhost:5173\""
if [[ -n "$EXTRA_CORS_ORIGINS" ]]; then
  IFS=',' read -ra extra <<< "$EXTRA_CORS_ORIGINS"
  for o in "${extra[@]}"; do ORIGINS="$ORIGINS, \"$(echo "$o" | xargs)\""; done
fi
cat > "$HERE/.r2-cors.json" <<JSON
{
  "CORSRules": [
    {
      "AllowedOrigins": [$ORIGINS],
      "AllowedMethods": ["GET", "HEAD"],
      "AllowedHeaders": ["*"],
      "ExposeHeaders": ["Accept-Ranges", "Content-Range", "Content-Length", "Content-Type", "ETag"],
      "MaxAgeSeconds": 3600
    }
  ]
}
JSON
echo "Applying CORS policy for: $ORIGINS"
if aws s3api put-bucket-cors --bucket "$R2_BUCKET_NAME" --cors-configuration file:///work/.r2-cors.json; then
  echo "CORS applied."
else
  echo "Could not set CORS with this API token (needs 'Admin Read & Write'). Add it in the dashboard instead:"
  echo "  R2 -> $R2_BUCKET_NAME -> Settings -> CORS Policy -> paste:"
  sed -n '3,9p' "$HERE/.r2-cors.json" | sed 's/^/    /'
fi
rm -f "$HERE/.r2-cors.json"
