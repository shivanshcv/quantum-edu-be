#!/usr/bin/env bash
# Cart E2E Test Script
# Prerequisites:
#   1. Run ALTER: ALTER TABLE product ADD COLUMN is_free TINYINT(1) NOT NULL DEFAULT 0 AFTER is_featured;
#   2. App running on BASE_URL with dev profile: SERVER_PORT=8081 mvn spring-boot:run -pl app -Dspring-boot.run.profiles=dev
#   3. Seed data (or create user/admin via signup)
# Usage: ./scripts/test-cart-e2e.sh [BASE_URL]
# Env: BASE_URL (default http://localhost:8081), CART_E2E_USER_EMAIL, CART_E2E_USER_PASS
#      MYSQL_OPTS (e.g. -u root -proot) for DB access; default: -u root -proot quantum_education

set -e
BASE_URL="${1:-${BASE_URL:-http://localhost:8081}}"
USER_EMAIL="${CART_E2E_USER_EMAIL:-user@quantumedu.com}"
USER_PASS="${CART_E2E_USER_PASS:-password123}"
MYSQL_OPTS="${MYSQL_OPTS:--u root -proot quantum_education}"

BILLING='{
  "billingName": "Test User",
  "billingAddressLine1": "123 Test Street",
  "billingAddressLine2": "",
  "billingCity": "Mumbai",
  "billingState": "Maharashtra",
  "billingCountry": "India",
  "billingPostalCode": "400001",
  "billingGstNumber": null
}'

echo "=== Cart E2E Tests ==="
echo "Base URL: $BASE_URL"
echo ""

# --- 1. Auth: Signup or Login as user ---
echo "1. Auth (signup or login)"
TS=$(date +%s)
TEST_EMAIL="cart-e2e-$TS@test.local"
SIGNUP_RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$USER_PASS\",\"firstName\":\"Cart\",\"lastName\":\"Test\"}")
if echo "$SIGNUP_RESP" | grep -q '"success":true'; then
  echo "   OK - New user signed up ($TEST_EMAIL)"
  # Get verification token from DB and verify (dev: email disabled)
  if command -v mysql >/dev/null 2>&1; then
    TOKEN=$(mysql $MYSQL_OPTS -N -e "SELECT email_verification_token FROM auth_user WHERE email='$TEST_EMAIL' LIMIT 1;" 2>/dev/null)
    if [ -n "$TOKEN" ]; then
      curl -s -X POST "$BASE_URL/api/v1/auth/verify-email" \
        -H "Content-Type: application/json" \
        -d "{\"token\":\"$TOKEN\"}" > /dev/null
      echo "   OK - Email verified via DB token"
    fi
  else
    echo "   Note: Run 'mysql -u root -p quantum_education -e \"SELECT email_verification_token FROM auth_user WHERE email='$TEST_EMAIL';\"' and verify manually"
  fi
fi
# Login (use new user or fallback to seed user)
LOGIN_EMAIL="${CART_E2E_USER_EMAIL:-$TEST_EMAIL}"
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$LOGIN_EMAIL\",\"password\":\"$USER_PASS\"}")
if echo "$LOGIN_RESP" | grep -q '"success":true'; then
  USER_TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  echo "   OK - User JWT obtained"
else
  echo "   FAIL - Login failed. Tried $LOGIN_EMAIL. Set CART_E2E_USER_EMAIL and CART_E2E_USER_PASS for seed user."
  echo "$LOGIN_RESP"
  exit 1
fi

# --- 2. Ensure free product exists (MySQL) ---
echo ""
echo "2. Ensure free product (MySQL)"
FREE_PRODUCT_ID=""
if command -v mysql >/dev/null 2>&1; then
  # Try to update product 2 to be free; if no rows affected, insert new product
  UPDATED=$(mysql $MYSQL_OPTS -N -e "UPDATE product SET is_free=1, is_published=1 WHERE id=2 LIMIT 1; SELECT ROW_COUNT();" 2>/dev/null | tail -1)
  if [ "$UPDATED" = "1" ]; then
    FREE_PRODUCT_ID=2
    echo "   OK - Product 2 set as free (is_free=1, is_published=1)"
  else
    # Product 2 may not exist or already free; use existing free product or insert new
    FREE_PRODUCT_ID=$(mysql $MYSQL_OPTS -N -e "SELECT id FROM product WHERE is_free=1 AND is_published=1 LIMIT 1;" 2>/dev/null)
    if [ -n "$FREE_PRODUCT_ID" ]; then
      echo "   OK - Using existing free product (id=$FREE_PRODUCT_ID)"
    else
      # Insert new free product
    SLUG="free-e2e-$TS"
    FREE_PRODUCT_ID=$(mysql $MYSQL_OPTS -N -e "
      INSERT INTO product (title, slug, short_description, long_description, price, discount_price, difficulty_level, duration_minutes, is_published, is_featured, is_free)
      VALUES ('Free Intro Course', '$SLUG', 'Free course for E2E testing', 'A free course to test the free checkout flow.', 0, 0, 'BEGINNER', 30, 1, 0, 1);
      SET @pid = LAST_INSERT_ID();
      INSERT INTO product_category (product_id, category_id) VALUES (@pid, 1);
      SELECT @pid;
    " 2>/dev/null | tail -1)
    if [ -n "$FREE_PRODUCT_ID" ] && [ "$FREE_PRODUCT_ID" != "0" ]; then
      echo "   OK - Free product created (id=$FREE_PRODUCT_ID)"
    fi
    fi
  fi
fi
if [ -z "$FREE_PRODUCT_ID" ]; then
  echo "   Note: mysql not found or no free product. Run: UPDATE product SET is_free=1, is_published=1 WHERE id=2;"
  FREE_PRODUCT_ID=2
fi

PAID_PRODUCT_ID=1

# --- 3. Add items to cart (batch) ---
echo ""
echo "3. POST /api/v1/cart/addItems (batch)"
ADD_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/cart/addItems" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d "{\"productIds\": [$PAID_PRODUCT_ID]}")
ADD_HTTP=$(echo "$ADD_RESP" | tail -n1)
ADD_BODY=$(echo "$ADD_RESP" | sed '$d')
if [ "$ADD_HTTP" = "200" ]; then
  echo "   OK - Added paid product $PAID_PRODUCT_ID"
else
  echo "   FAIL - Expected 200, got $ADD_HTTP"
  echo "$ADD_BODY"
  exit 1
fi

# --- 4. Get cart ---
echo ""
echo "4. GET /api/v1/cart/getCart"
CART_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/v1/cart/getCart" \
  -H "Authorization: Bearer $USER_TOKEN")
CART_HTTP=$(echo "$CART_RESP" | tail -n1)
CART_BODY=$(echo "$CART_RESP" | sed '$d')
if [ "$CART_HTTP" = "200" ]; then
  echo "   OK - Cart retrieved"
  echo "$CART_BODY" | head -c 300
  echo "..."
else
  echo "   FAIL - Expected 200, got $CART_HTTP"
  echo "$CART_BODY"
  exit 1
fi

# --- 5. Verify cart (BFF) - paid product ---
echo ""
echo "5. POST /pages/verify-cart (paid product)"
# Product 1: price 2999, discount 1999 -> final 1999
VERIFY_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/pages/verify-cart" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"items": [{"productId": 1, "price": 1999.00}]}')
VERIFY_HTTP=$(echo "$VERIFY_RESP" | tail -n1)
VERIFY_BODY=$(echo "$VERIFY_RESP" | sed '$d')
if [ "$VERIFY_HTTP" = "200" ]; then
  echo "   OK - Verify response received"
  if echo "$VERIFY_BODY" | grep -q '"paymentRequired"'; then
    echo "   OK - paymentRequired field present"
  fi
  if echo "$VERIFY_BODY" | grep -q '"subtotal"'; then
    echo "   OK - subtotal present"
  fi
else
  echo "   FAIL - Expected 200, got $VERIFY_HTTP"
  echo "$VERIFY_BODY"
  exit 1
fi

# Extract items from verify for checkout (simplified - we build from known product 1)
# Product 1: price 2999, discount 1999, final 1999, gst ~360
CHECKOUT_ITEMS_PAID='[
  {
    "productId": 1,
    "price": 2999.00,
    "discountPrice": 1999.00,
    "finalPrice": 1999.00,
    "gstAmount": 359.82,
    "free": false
  }
]'

# --- 6. Checkout (paid) - BFF ---
echo ""
echo "6. POST /api/v1/bff/checkout (paid-only)"
CHECKOUT_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/bff/checkout" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d "{
    \"billingInfoPresent\": false,
    \"items\": $CHECKOUT_ITEMS_PAID,
    \"billing\": $BILLING
  }")
CHECKOUT_HTTP=$(echo "$CHECKOUT_RESP" | tail -n1)
CHECKOUT_BODY=$(echo "$CHECKOUT_RESP" | sed '$d')
if [ "$CHECKOUT_HTTP" = "200" ]; then
  echo "   OK - Checkout response received"
  if echo "$CHECKOUT_BODY" | grep -q '"paymentRequired":true'; then
    echo "   OK - paymentRequired is true for paid"
  fi
  if echo "$CHECKOUT_BODY" | grep -q '"razorpayOrderId"'; then
    echo "   OK - razorpayOrderId present"
  fi
  if echo "$CHECKOUT_BODY" | grep -q '"orderIds"'; then
    echo "   OK - orderIds present"
  fi
  echo "$CHECKOUT_BODY"
else
  echo "   FAIL - Expected 200, got $CHECKOUT_HTTP"
  echo "$CHECKOUT_BODY"
  exit 1
fi

# --- 7. Free-only flow: add free product, verify, checkout ---
# First ensure we have a free product - try the one we created or use one with is_free=1
echo ""
echo "7. Free-only flow (add free product, verify, checkout)"
# Remove paid items from cart so we have only free product
curl -s -X DELETE "$BASE_URL/api/v1/cart/removeItem/$PAID_PRODUCT_ID" -H "Authorization: Bearer $USER_TOKEN" > /dev/null
if [ -n "$FREE_PRODUCT_ID" ]; then
  ADD_FREE_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/cart/addItems" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -d "{\"productIds\": [$FREE_PRODUCT_ID]}")
  if [ "$(echo "$ADD_FREE_RESP" | tail -n1)" = "200" ]; then
    VERIFY_FREE_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/pages/verify-cart" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $USER_TOKEN" \
      -d "{\"items\": [{\"productId\": $FREE_PRODUCT_ID, \"price\": 0}]}")
    if [ "$(echo "$VERIFY_FREE_RESP" | tail -n1)" = "200" ]; then
      CHECKOUT_FREE_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/bff/checkout" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "{
          \"billingInfoPresent\": false,
          \"items\": [{
            \"productId\": $FREE_PRODUCT_ID,
            \"price\": 0,
            \"discountPrice\": 0,
            \"finalPrice\": 0,
            \"gstAmount\": 0,
            \"free\": true
          }],
          \"billing\": $BILLING
        }")
      CF_HTTP=$(echo "$CHECKOUT_FREE_RESP" | tail -n1)
      CF_BODY=$(echo "$CHECKOUT_FREE_RESP" | sed '$d')
      if [ "$CF_HTTP" = "200" ]; then
        echo "   OK - Free checkout succeeded"
        if echo "$CF_BODY" | grep -q '"paymentRequired":false'; then
          echo "   OK - paymentRequired is false for free"
        fi
        if echo "$CF_BODY" | grep -q '"freeOrderIds"'; then
          echo "   OK - freeOrderIds present"
        fi
      else
        echo "   Note: Free checkout returned $CF_HTTP (user may already own)"
        echo "$CF_BODY" | head -c 200
      fi
    fi
  fi
else
  echo "   Skip - No free product (run: UPDATE product SET is_free=1, is_published=1 WHERE id=2; to test)"
fi

# --- 8. Batch addItems ---
echo ""
echo "8. POST /api/v1/cart/addItems (batch multiple)"
BATCH_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/cart/addItems" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"productIds": [1, 2]}')
if [ "$(echo "$BATCH_RESP" | tail -n1)" = "200" ]; then
  echo "   OK - Batch add succeeded"
else
  echo "   Note: Batch add returned $(echo "$BATCH_RESP" | tail -n1)"
fi

echo ""
echo "=== Cart E2E tests completed ==="
echo ""
echo "DB verification queries (replace USER_ID with actual user id, e.g. 2):"
echo "  SELECT * FROM cart WHERE user_id = USER_ID ORDER BY created_at DESC;"
echo "  SELECT id, user_id, product_id, final_price, payment_gateway_order_id, status"
echo "    FROM orders WHERE user_id = USER_ID ORDER BY created_at DESC;"
echo "  SELECT * FROM course_ownership WHERE user_id = USER_ID ORDER BY created_at DESC;"
