# Razorpay Integration - Backend-Only Testing Guide

This guide explains how to test the Razorpay payment integration from the backend only, without a frontend.

---

## 1. Where Razorpay Keys Are Configured

| Property | Location | Purpose |
| -------- | -------- | ------- |
| `razorpay.key_id` | `app/src/main/resources/application-dev.properties` | Razorpay API key (used for creating orders) |
| `razorpay.key_secret` | `app/src/main/resources/application-dev.properties` | Razorpay API secret (used for creating orders) |
| `razorpay.webhook_secret` | Same file; defaults to `shivansh1234` in dev; override via `RAZORPAY_WEBHOOK_SECRET` | Used to verify webhook signatures |

**Note:** Test keys and webhook secret (`shivansh1234`) are set as defaults for local dev. For production, set `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, and `RAZORPAY_WEBHOOK_SECRET` as environment variables.

---

## 2. Prerequisites

- MySQL running with `quantum_education` database and schema applied
- At least one published product in the database
- Application running: `mvn spring-boot:run -pl app -Dspring-boot.run.profiles=dev`

**Dev JWT bypass:** In dev profile, JWT validation is bypassed. Use `X-User-Id: <userId>` header to impersonate a user (default: 1). No Bearer token needed.

---

## 3. Step-by-Step Testing

### Step 3.1: Create a User and Verify Email

```bash
# Signup
curl -s -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "razorpay-test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
# Response includes token; user is auto-logged in
```

Get the verification token from the database (since email is disabled in dev):

```sql
SELECT email_verification_token FROM auth_user WHERE email = 'razorpay-test@example.com';
```

```bash
# Verify email (replace <TOKEN> with value from DB)
curl -s -X POST http://localhost:8080/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{"token": "<TOKEN>"}'
# Save the JWT from response.response.token
```

Or use login if the user was already verified:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "razorpay-test@example.com", "password": "password123"}'
```

**Save the JWT** from the response (e.g. `response.token`). Use it as `JWT_TOKEN` in the following steps.

---

### Step 3.2: Add Product to Cart

```bash
# With dev JWT bypass: use X-User-Id header (no JWT needed)
curl -s -X POST http://localhost:8080/api/v1/cart/addItems \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{"productId": 1}'

# Or with JWT:
# curl ... -H "Authorization: Bearer JWT_TOKEN" ...
```

---

### Step 3.3: Verify Cart

```bash
# Use the product's final price (e.g. 799 if discount, else 999)
curl -s -X POST http://localhost:8080/pages/verify-cart \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer JWT_TOKEN" \
  -d '{"items": [{"productId": 1, "price": 799.00}]}'
```

---

### Step 3.4: Checkout (Creates Razorpay Order)

```bash
curl -s -X POST http://localhost:8080/api/v1/bff/checkout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer JWT_TOKEN" \
  -d '{
    "billingInfoPresent": false,
    "items": [{
      "productId": 1,
      "price": 999.00,
      "discountPrice": 799.00,
      "finalPrice": 799.00,
      "gstAmount": 143.82
    }],
    "billing": {
      "billingName": "Test User",
      "billingAddressLine1": "123 Test Street",
      "billingAddressLine2": "",
      "billingCity": "Mumbai",
      "billingState": "Maharashtra",
      "billingCountry": "India",
      "billingPostalCode": "400001",
      "billingGstNumber": null
    }
  }'
```

**Expected response:**
```json
{
  "success": true,
  "response": {
    "razorpayOrderId": "order_xxxxxxxxxxxx",
    "amount": 94282,
    "currency": "INR",
    "keyId": "rzp_test_SLepofnigNDLcv",
    "orderIds": [101]
  }
}
```

**Save `razorpayOrderId`** for the webhook test.

**Verify in DB:**
```sql
SELECT id, user_id, product_id, status, payment_gateway_order_id 
FROM orders 
WHERE payment_gateway_order_id = 'order_xxx';
-- status should be PENDING
```

---

### Step 3.5: Test Webhook (Simulate Payment Success)

Razorpay sends webhooks to a **public URL**. For local testing, you have two options:

#### Option A: Manual Webhook Call with Computed Signature

1. Get your webhook secret from Razorpay Dashboard:
   - Go to https://dashboard.razorpay.com (Test mode)
   - Settings → Webhooks → Add New URL → Enter any URL (e.g. `https://example.com/webhook`) → Copy the secret

2. Set the secret:
   ```bash
   export RAZORPAY_WEBHOOK_SECRET=your_webhook_secret
   ```

3. Restart the app and run the script below:

```bash
#!/bin/bash
# Save as test-webhook.sh

WEBHOOK_SECRET="${RAZORPAY_WEBHOOK_SECRET:?Set RAZORPAY_WEBHOOK_SECRET}"
ORDER_ID="${1:?Usage: ./test-webhook.sh order_xxx}"

PAYLOAD="{\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_test123\",\"order_id\":\"$ORDER_ID\",\"status\":\"captured\",\"amount\":94282,\"currency\":\"INR\"}}}}"
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$WEBHOOK_SECRET" | awk '{print $2}')

echo "Calling webhook with order_id=$ORDER_ID"
curl -v -X POST http://localhost:8080/api/v1/cart/webhook/razorpay \
  -H "Content-Type: application/json" \
  -H "X-Razorpay-Signature: $SIGNATURE" \
  -d "$PAYLOAD"
```

Run:
```bash
chmod +x test-webhook.sh
./test-webhook.sh order_xxxxxxxxxxxx
```

#### Option B: Use ngrok/zrok for Real Webhook Delivery

1. Start tunnel: `ngrok http 8080`
2. In Razorpay Dashboard: Webhooks → Add URL → `https://xxx.ngrok.io/api/v1/cart/webhook/razorpay`
3. Subscribe to `payment.captured`
4. Copy webhook secret and set `RAZORPAY_WEBHOOK_SECRET`
5. Complete a real test payment (use Razorpay test card) on a checkout page

---

### Step 3.6: Verify Post-Webhook State

After a successful webhook call:

```sql
-- Order should be SUCCESS
SELECT id, status, payment_gateway_payment_id FROM orders WHERE payment_gateway_order_id = 'order_xxx';

-- Ownership should be created
SELECT * FROM course_ownership WHERE user_id = (SELECT id FROM auth_user WHERE email = 'razorpay-test@example.com');

-- Cart should be empty
SELECT * FROM cart WHERE user_id = (SELECT id FROM auth_user WHERE email = 'razorpay-test@example.com');
```

---

## 4. Quick Test Script (All Steps)

Save as `test-razorpay-flow.sh`:

```bash
#!/bin/bash
set -e
BASE_URL="http://localhost:8080"
EMAIL="razorpay-test-$(date +%s)@example.com"

echo "1. Signup..."
SIGNUP=$(curl -s -X POST $BASE_URL/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"password123\",\"firstName\":\"Test\",\"lastName\":\"User\"}")
echo "$SIGNUP" | jq .

# Extract token from signup (user is auto-logged in)
JWT=$(echo "$SIGNUP" | jq -r '.response.token')
if [ "$JWT" = "null" ] || [ -z "$JWT" ]; then
  echo "Signup failed or no token. Check if user already exists."
  exit 1
fi

echo "2. Add to cart..."
curl -s -X POST $BASE_URL/api/v1/cart/addItems \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT" \
  -d '{"productId": 1}' | jq .

echo "3. Verify cart..."
curl -s -X POST $BASE_URL/pages/verify-cart \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT" \
  -d '{"items": [{"productId": 1, "price": 799.00}]}' | jq .

echo "4. Checkout..."
CHECKOUT=$(curl -s -X POST $BASE_URL/api/v1/bff/checkout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT" \
  -d '{
    "billingInfoPresent": false,
    "items": [{"productId": 1, "price": 999.00, "discountPrice": 799.00, "finalPrice": 799.00, "gstAmount": 143.82}],
    "billing": {"billingName": "Test", "billingAddressLine1": "123 St", "billingAddressLine2": "", "billingCity": "Mumbai", "billingState": "MH", "billingCountry": "India", "billingPostalCode": "400001", "billingGstNumber": null}
  }')
echo "$CHECKOUT" | jq .

ORDER_ID=$(echo "$CHECKOUT" | jq -r '.response.razorpayOrderId')
echo ""
echo "Razorpay Order ID: $ORDER_ID"
echo ""
echo "5. To test webhook, run:"
echo "   export RAZORPAY_WEBHOOK_SECRET=<your_secret>"
echo "   ./test-webhook.sh $ORDER_ID"
```

---

## 5. Troubleshooting

| Issue | Cause | Fix |
| ----- | ----- | --- |
| Checkout fails with connection error | Razorpay API unreachable | Check network; Razorpay uses `api.razorpay.com` |
| Checkout returns 500 | Invalid key_id/key_secret | Verify keys in application-dev.properties |
| Webhook 400 CART_INVALID_WEBHOOK_SIGNATURE | Wrong secret or payload | Use exact raw body; secret must match Razorpay dashboard |
| Webhook 200 but no ownership | order_id not found | Use razorpayOrderId from checkout response |
| CART_EMPTY on checkout | No items in cart | Run addItems first |
| QE_AUTH_004 on checkout | Email not verified | Verify email before checkout |
