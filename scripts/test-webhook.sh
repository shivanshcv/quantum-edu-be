#!/bin/bash
# Test Razorpay webhook locally
# Usage: RAZORPAY_WEBHOOK_SECRET=xxx ./test-webhook.sh order_xxx
# Or: export RAZORPAY_WEBHOOK_SECRET=xxx && ./test-webhook.sh order_xxx

WEBHOOK_SECRET="${RAZORPAY_WEBHOOK_SECRET:?Set RAZORPAY_WEBHOOK_SECRET env var}"
ORDER_ID="${1:?Usage: ./test-webhook.sh <razorpay_order_id>}"
BASE_URL="${2:-http://localhost:8080}"

PAYLOAD="{\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_test123\",\"order_id\":\"$ORDER_ID\",\"status\":\"captured\",\"amount\":94282,\"currency\":\"INR\"}}}}"
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$WEBHOOK_SECRET" | awk '{print $2}')

echo "Calling webhook: $BASE_URL/api/v1/cart/webhook/razorpay"
echo "Order ID: $ORDER_ID"
echo ""
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/api/v1/cart/webhook/razorpay" \
  -H "Content-Type: application/json" \
  -H "X-Razorpay-Signature: $SIGNATURE" \
  -d "$PAYLOAD" | tail -20
