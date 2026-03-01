# LMS End-to-End Testing Guide

## Prerequisites

- MySQL running with `quantum_education` database
- Existing tables (auth_user, product, product_content, lesson, orders, etc.)

## 1. Run Migration (Add New Tables & Test Data)

The migration creates `user_lesson_progress` if missing and adds test data so user 2 owns product 1. **No existing data is deleted.**

```bash
mysql -u root -p quantum_education < docs/migrations/lms-test-data.sql
```

Or with password inline (avoid in production):

```bash
mysql -u root -proot quantum_education < docs/migrations/lms-test-data.sql
```

### What the migration does

- Creates `user_lesson_progress` table if it doesn't exist
- Inserts an order + `course_ownership` so **user 2** owns **product 1** (only if ownership doesn't already exist)
- Optionally adds instructor to product 1 attributes

## 2. Start the Application

If port 8080 is in use, run on 8081:

```bash
SERVER_PORT=8081 mvn spring-boot:run -pl app
```

Or with Maven property:

```bash
mvn spring-boot:run -pl app -Dserver.port=8081
```

## 3. Run E2E Tests

With dev profile, JWT is bypassed and `X-User-Id` header is used for auth.

```bash
# App on 8081
./scripts/test-lms-e2e.sh http://localhost:8081

# App on default 8080
./scripts/test-lms-e2e.sh http://localhost:8080
```

### Manual cURL Examples

```bash
# Get LMS player data (user 2 owns product 1)
curl -s "http://localhost:8081/lms/player?productId=1" -H "X-User-Id: 2"

# Mark lesson 1 complete
curl -s -X POST "http://localhost:8081/lms/lessons/1/complete" -H "X-User-Id: 2"

# Get player again to see updated progress
curl -s "http://localhost:8081/lms/player?productId=1" -H "X-User-Id: 2"
```

### With JWT (when dev-bypass is false)

```bash
# Login first
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@quantumedu.com","password":"password123"}' \
  | jq -r '.response.token')

# Use token
curl -s "http://localhost:8081/lms/player?productId=1" -H "Authorization: Bearer $TOKEN"
```

## Troubleshooting

### 500 Internal Server Error

If you get `QE_001` (Internal server error), the most common cause is a **stale app process**:

1. **Stop any running instance** (e.g. the process on port 8080).
2. **Rebuild and restart**:
   ```bash
   mvn clean install -DskipTests
   SERVER_PORT=8081 mvn spring-boot:run -pl app
   ```
3. **Verify DB state** – Ensure `user_lesson_progress` exists and `course_ownership` has a row for `user_id=2, course_id=1`:
   ```bash
   mysql -u root -p quantum_education -e "SHOW TABLES LIKE 'user_lesson_progress'; SELECT * FROM course_ownership WHERE user_id=2 AND course_id=1;"
   ```

## 4. Expected Data (from seed-test-data.sql)

- **User 2** (user@quantumedu.com) – used for LMS tests after migration
- **Product 1** (Complete Skincare Masterclass) – has content IDs 1, 2, 3 (2 lessons + 1 assessment)
- **Product content 1** – "Introduction to Skincare" (LESSON)
- **Product content 2** – "Understanding Your Skin Type" (LESSON)
- **Product content 3** – "Module 1 Quiz" (ASSESSMENT)

If your DB has different IDs, adjust the test script or cURL commands accordingly.
