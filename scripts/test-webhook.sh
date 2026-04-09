#!/bin/bash
# Test Razorpay webhook - simulates payment.captured callback
#
# Usage:
#   RAZORPAY_WEBHOOK_SECRET=xxx ./test-webhook.sh <order_id>                          # localhost (default)
#   RAZORPAY_WEBHOOK_SECRET=xxx ./test-webhook.sh <order_id> staging                  # staging (Cloudflare tunnel)
#   QUANTUM_EDU_PROD_WEBHOOK_BASE_URL=https://api.example.com RAZORPAY_WEBHOOK_SECRET=xxx \
#     ./test-webhook.sh <order_id> prod                                              # production API base URL
#   RAZORPAY_WEBHOOK_SECRET=xxx ./test-webhook.sh <order_id> <base_url>               # any base URL
#
# prod / production: set QUANTUM_EDU_PROD_WEBHOOK_BASE_URL to your live app origin (no path, no trailing slash).
# Use the same webhook secret your prod app expects (e.g. test Razorpay app credentials on prod for safe testing).
#
# Examples:
#   ./test-webhook.sh order_xxx
#   ./test-webhook.sh order_xxx staging
#   QUANTUM_EDU_PROD_WEBHOOK_BASE_URL=https://api.yourdomain.com RAZORPAY_WEBHOOK_SECRET=shivansh1234 \
#     ./test-webhook.sh order_xxx prod
#   ./test-webhook.sh order_xxx https://lucia-dream-bids-extensions.trycloudflare.com

WEBHOOK_SECRET="${RAZORPAY_WEBHOOK_SECRET:?Set RAZORPAY_WEBHOOK_SECRET env var}"
ORDER_ID="${1:?Usage: ./test-webhook.sh <razorpay_order_id> [base_url|staging|prod]}"

# Base URL: arg 2 = staging | prod | production | <url> | (default) localhost
STAGING_BASE_URL="https://lucia-dream-bids-extensions.trycloudflare.com"
if [[ -z "${2:-}" ]]; then
  BASE_URL="http://localhost:8080"
elif [[ "${2}" == "staging" ]]; then
  BASE_URL="$STAGING_BASE_URL"
elif [[ "${2}" == "prod" || "${2}" == "production" ]]; then
  BASE_URL="${QUANTUM_EDU_PROD_WEBHOOK_BASE_URL:?Set QUANTUM_EDU_PROD_WEBHOOK_BASE_URL (production API origin, e.g. https://api.example.com)}"
else
  BASE_URL="$2"
fi
# Strip trailing slash
BASE_URL="${BASE_URL%/}"

PAYLOAD="{\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_test123\",\"order_id\":\"$ORDER_ID\",\"status\":\"captured\",\"amount\":94282,\"currency\":\"INR\"}}}}"
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$WEBHOOK_SECRET" | awk '{print $2}')

echo "Calling webhook: $BASE_URL/api/v1/cart/webhook/razorpay"
echo "Order ID: $ORDER_ID"
echo ""
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/api/v1/cart/webhook/razorpay" \
  -H "Content-Type: application/json" \
  -H "X-Razorpay-Signature: $SIGNATURE" \
  -d "$PAYLOAD" | tail -20
