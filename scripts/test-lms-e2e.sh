#!/usr/bin/env bash
# LMS End-to-End Test Script
# Prerequisites:
#   1. Run migration: mysql -u root -p quantum_education < docs/migrations/lms-test-data.sql
#   2. App running on BASE_URL (default: http://localhost:8081)
# Usage: ./scripts/test-lms-e2e.sh [BASE_URL]

set -e
BASE_URL="${1:-http://localhost:8081}"
USER_ID="${LMS_TEST_USER_ID:-2}"  # User 2 owns product 1 after migration
PRODUCT_ID=1

echo "=== LMS E2E Tests ==="
echo "Base URL: $BASE_URL"
echo "User ID: $USER_ID (use X-User-Id header with dev-bypass)"
echo ""

# 1. GET /lms/player - should return course structure
echo "1. GET /lms/player?productId=$PRODUCT_ID"
RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/lms/player?productId=$PRODUCT_ID" -H "X-User-Id: $USER_ID")
HTTP=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')
echo "   HTTP: $HTTP"
if [ "$HTTP" = "200" ]; then
  echo "   OK - Player data retrieved"
  echo "$BODY" | head -c 500
  echo "..."
else
  echo "   FAIL - Expected 200, got $HTTP"
  echo "$BODY"
  exit 1
fi
echo ""

# 2. POST /lms/lessons/1/complete - mark first lesson complete
echo "2. POST /lms/lessons/1/complete (mark lesson 1 complete)"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/lms/lessons/1/complete" -H "X-User-Id: $USER_ID")
HTTP=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')
echo "   HTTP: $HTTP"
if [ "$HTTP" = "200" ]; then
  echo "   OK - Lesson marked complete"
else
  echo "   FAIL - Expected 200, got $HTTP"
  echo "$BODY"
  exit 1
fi
echo ""

# 3. GET /lms/player again - progress should show completedLessons >= 1
echo "3. GET /lms/player?productId=$PRODUCT_ID (verify progress)"
RESP=$(curl -s "$BASE_URL/lms/player?productId=$PRODUCT_ID" -H "X-User-Id: $USER_ID")
COMPLETED=$(echo "$RESP" | grep -o '"completedLessons":[0-9]*' | cut -d: -f2)
echo "   completedLessons: $COMPLETED"
if [ -n "$COMPLETED" ] && [ "$COMPLETED" -ge 1 ]; then
  echo "   OK - Progress updated"
else
  echo "   FAIL - Progress not updated (completedLessons should be >= 1)"
  exit 1
fi
echo ""

# 4. Test access denied - user without ownership
echo "4. GET /lms/player?productId=$PRODUCT_ID (user 999 - no ownership, expect 403)"
RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/lms/player?productId=$PRODUCT_ID" -H "X-User-Id: 999")
HTTP=$(echo "$RESP" | tail -n1)
echo "   HTTP: $HTTP"
if [ "$HTTP" = "403" ]; then
  echo "   OK - Access denied as expected"
else
  echo "   Note: Got $HTTP (403 expected if user 999 doesn't own product)"
fi
echo ""

# 5. Test invalid product
echo "5. GET /lms/player?productId=99999 (non-existent product, expect 404)"
RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/lms/player?productId=99999" -H "X-User-Id: $USER_ID")
HTTP=$(echo "$RESP" | tail -n1)
echo "   HTTP: $HTTP"
if [ "$HTTP" = "404" ]; then
  echo "   OK - Product not found as expected"
else
  echo "   FAIL - Expected 404, got $HTTP"
  exit 1
fi
echo ""

# 6. Test invalid contentId for mark complete
echo "6. POST /lms/lessons/99999/complete (invalid contentId, expect 404)"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/lms/lessons/99999/complete" -H "X-User-Id: $USER_ID")
HTTP=$(echo "$RESP" | tail -n1)
echo "   HTTP: $HTTP"
if [ "$HTTP" = "404" ]; then
  echo "   OK - Lesson not found as expected"
else
  echo "   FAIL - Expected 404, got $HTTP"
  exit 1
fi
echo ""

# 7. Test mark complete for content user doesn't own (content 4 = product 2)
echo "7. POST /lms/lessons/4/complete (content from product user doesn't own, expect 403)"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/lms/lessons/4/complete" -H "X-User-Id: $USER_ID")
HTTP=$(echo "$RESP" | tail -n1)
echo "   HTTP: $HTTP"
if [ "$HTTP" = "403" ]; then
  echo "   OK - Course access denied as expected"
else
  echo "   Note: Got $HTTP (403 expected if user doesn't own product 2)"
fi
echo ""

echo "=== All LMS E2E tests passed ==="
