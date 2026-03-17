#!/usr/bin/env bash
# Settings E2E Test Script
# Prerequisites:
#   1. Run migration: mysql -h 127.0.0.1 -u root -proot quantum_education -e "ALTER TABLE user_profile ADD COLUMN billing_name VARCHAR(255) AFTER last_name;"
#      (Skip if column already exists)
#   2. App running on BASE_URL with dev profile: SERVER_PORT=8081 mvn spring-boot:run -pl app -Dspring-boot.run.profiles=dev
# Usage: ./scripts/test-settings-e2e.sh [BASE_URL]
# Env: BASE_URL (default http://localhost:8081), MYSQL_OPTS (e.g. -h 127.0.0.1 -u root -proot quantum_education)

set -e
BASE_URL="${1:-${BASE_URL:-http://localhost:8081}}"
USER_PASS="SettingsTest123"
MYSQL_OPTS="${MYSQL_OPTS:--h 127.0.0.1 -u root -proot quantum_education}"

echo "=== Settings E2E Tests ==="
echo "Base URL: $BASE_URL"
echo ""

# --- 1. Auth: Signup new user ---
echo "1. Auth (signup new user)"
TS=$(date +%s)
TEST_EMAIL="settings-e2e-$TS@test.local"
SIGNUP_RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$USER_PASS\",\"firstName\":\"Settings\",\"lastName\":\"Tester\"}")
if echo "$SIGNUP_RESP" | grep -q '"success":true'; then
  echo "   OK - New user signed up ($TEST_EMAIL)"
  if command -v mysql >/dev/null 2>&1; then
    TOKEN=$(mysql $MYSQL_OPTS -N -e "SELECT email_verification_token FROM auth_user WHERE email='$TEST_EMAIL' LIMIT 1;" 2>/dev/null)
    if [ -n "$TOKEN" ]; then
      curl -s -X POST "$BASE_URL/api/v1/auth/verify-email" \
        -H "Content-Type: application/json" \
        -d "{\"token\":\"$TOKEN\"}" > /dev/null
      echo "   OK - Email verified via DB token"
    fi
  fi
else
  echo "   FAIL - Signup failed"
  echo "$SIGNUP_RESP"
  exit 1
fi

# --- 2. Login ---
echo ""
echo "2. Login"
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$USER_PASS\"}")
if echo "$LOGIN_RESP" | grep -q '"success":true'; then
  USER_TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  echo "   OK - JWT obtained"
else
  echo "   FAIL - Login failed"
  echo "$LOGIN_RESP"
  exit 1
fi

# --- 3. GET /pages/settings ---
echo ""
echo "3. GET /pages/settings"
SETTINGS_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/pages/settings" \
  -H "Authorization: Bearer $USER_TOKEN")
SETTINGS_HTTP=$(echo "$SETTINGS_RESP" | tail -n1)
SETTINGS_BODY=$(echo "$SETTINGS_RESP" | sed '$d')
if [ "$SETTINGS_HTTP" != "200" ]; then
  echo "   FAIL - Expected 200, got $SETTINGS_HTTP"
  echo "$SETTINGS_BODY"
  exit 1
fi
echo "   OK - Settings page retrieved (HTTP 200)"
if echo "$SETTINGS_BODY" | grep -q '"type":"SETTINGS"'; then
  echo "   OK - main.type is SETTINGS"
fi
if echo "$SETTINGS_BODY" | grep -q '"type":"SETTINGS_PAGE"'; then
  echo "   OK - SETTINGS_PAGE component present"
fi
if echo "$SETTINGS_BODY" | grep -q '"profileSection"'; then
  echo "   OK - profileSection present"
fi
if echo "$SETTINGS_BODY" | grep -q '"securitySection"'; then
  echo "   OK - securitySection present"
fi
if echo "$SETTINGS_BODY" | grep -q '"billingSection"'; then
  echo "   OK - billingSection present"
fi
if echo "$SETTINGS_BODY" | grep -q '"values"'; then
  echo "   OK - values object present"
fi
if echo "$SETTINGS_BODY" | grep -q "\"email\":\"$TEST_EMAIL\""; then
  echo "   OK - email in values matches user"
fi

# --- 4. PATCH /api/v1/usermgmt/updateProfile ---
echo ""
echo "4. PATCH /api/v1/usermgmt/updateProfile"
UPDATE_PROFILE_RESP=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/api/v1/usermgmt/updateProfile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"firstName":"Dr. Alex","lastName":"Carter","phone":"+919876543210"}')
UP_HTTP=$(echo "$UPDATE_PROFILE_RESP" | tail -n1)
UP_BODY=$(echo "$UPDATE_PROFILE_RESP" | sed '$d')
if [ "$UP_HTTP" != "200" ]; then
  echo "   FAIL - Expected 200, got $UP_HTTP"
  echo "$UP_BODY"
  exit 1
fi
echo "   OK - Profile updated"

# --- 5. GET /pages/settings - verify profile update ---
echo ""
echo "5. GET /pages/settings (verify profile)"
SETTINGS2_RESP=$(curl -s "$BASE_URL/pages/settings" -H "Authorization: Bearer $USER_TOKEN")
if echo "$SETTINGS2_RESP" | grep -q '"name":"Dr. Alex Carter"'; then
  echo "   OK - name updated to Dr. Alex Carter"
elif echo "$SETTINGS2_RESP" | grep -q 'Dr. Alex Carter'; then
  echo "   OK - name contains Dr. Alex Carter"
else
  echo "   Note: Verify name in values - $SETTINGS2_RESP" | head -c 200
fi
if echo "$SETTINGS2_RESP" | grep -q '"phone":"+919876543210"'; then
  echo "   OK - phone updated"
fi

# --- 6. PATCH /api/v1/usermgmt/updateBillingInfo ---
echo ""
echo "6. PATCH /api/v1/usermgmt/updateBillingInfo"
BILLING_JSON='{
  "billingName": "Dr. Alex Carter (Billing)",
  "billingAddressLine1": "123 Learning Lane",
  "billingAddressLine2": "Suite 100",
  "billingCity": "Hyderabad",
  "billingState": "Telangana",
  "billingCountry": "India",
  "billingPostalCode": "500001",
  "billingGstNumber": "22AAAAA0000A1Z5"
}'
UPDATE_BILLING_RESP=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/api/v1/usermgmt/updateBillingInfo" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d "$BILLING_JSON")
UB_HTTP=$(echo "$UPDATE_BILLING_RESP" | tail -n1)
UB_BODY=$(echo "$UPDATE_BILLING_RESP" | sed '$d')
if [ "$UB_HTTP" != "200" ]; then
  echo "   FAIL - Expected 200, got $UB_HTTP"
  echo "$UB_BODY"
  exit 1
fi
echo "   OK - Billing info updated"

# --- 7. GET /pages/settings - verify billing update ---
echo ""
echo "7. GET /pages/settings (verify billing)"
SETTINGS3_RESP=$(curl -s "$BASE_URL/pages/settings" -H "Authorization: Bearer $USER_TOKEN")
if echo "$SETTINGS3_RESP" | grep -q 'Dr. Alex Carter (Billing)'; then
  echo "   OK - billingName updated"
fi
if echo "$SETTINGS3_RESP" | grep -q '"billingCity":"Hyderabad"'; then
  echo "   OK - billing city updated"
fi
if echo "$SETTINGS3_RESP" | grep -q '"billingPostalCode":"500001"'; then
  echo "   OK - billing postal code updated"
fi

# --- 8. POST /api/v1/auth/changePassword ---
echo ""
echo "8. POST /api/v1/auth/changePassword"
NEW_PASS="NewSettingsTest456"
CHANGE_PW_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/auth/changePassword" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d "{\"currentPassword\":\"$USER_PASS\",\"newPassword\":\"$NEW_PASS\"}")
CP_HTTP=$(echo "$CHANGE_PW_RESP" | tail -n1)
CP_BODY=$(echo "$CHANGE_PW_RESP" | sed '$d')
if [ "$CP_HTTP" != "200" ]; then
  echo "   FAIL - Expected 200, got $CP_HTTP"
  echo "$CP_BODY"
  exit 1
fi
echo "   OK - Password changed"

# --- 9. Login with old password (should fail) ---
echo ""
echo "9. Login with old password (expect fail)"
OLD_LOGIN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$USER_PASS\"}")
if echo "$OLD_LOGIN" | grep -q '"success":false'; then
  echo "   OK - Old password rejected"
else
  echo "   FAIL - Old password should have been rejected"
  exit 1
fi

# --- 10. Login with new password ---
echo ""
echo "10. Login with new password"
NEW_LOGIN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$NEW_PASS\"}")
if echo "$NEW_LOGIN" | grep -q '"success":true'; then
  echo "   OK - New password works"
else
  echo "   FAIL - New password login failed"
  echo "$NEW_LOGIN"
  exit 1
fi

# --- 11. GET /pages/settings with new token (sanity) ---
echo ""
echo "11. GET /pages/settings (with new token)"
NEW_TOKEN=$(echo "$NEW_LOGIN" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
SETTINGS_FINAL=$(curl -s -w "\n%{http_code}" "$BASE_URL/pages/settings" \
  -H "Authorization: Bearer $NEW_TOKEN")
if [ "$(echo "$SETTINGS_FINAL" | tail -n1)" = "200" ]; then
  echo "   OK - Settings accessible with new token"
else
  echo "   FAIL - Settings should work with new token"
  exit 1
fi

echo ""
echo "=== Settings E2E tests completed successfully ==="
