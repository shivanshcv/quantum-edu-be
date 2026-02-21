# Auth Module — API Specification

**Version:** 1.0  
**Base Path:** `/api/v1/auth`  
**Content-Type:** `application/json`

> **Response Format:** All APIs use the standard response format. See [API_RESPONSE_FORMAT.md](./API_RESPONSE_FORMAT.md).

---

## Design Notes

- **Unverified users:** Backend returns `403` with error code and message. Frontend handles display (e.g. show verification prompt/warning).
- **Email verification:** Required before login. Email service is implemented and sends verification link on signup.
- **Password:** Min 8 characters (Phase-1). No complexity requirements.

---

## Overview

| API | Method | Endpoint | Auth Required |
|-----|--------|----------|---------------|
| Signup | POST | `/auth/signup` | No |
| Login | POST | `/auth/login` | No |
| Verify Email | POST | `/auth/verify-email` | No |
| Resend Verification | POST | `/auth/resend-verification` | No |

---

## 1. Signup

Creates a new user account and sends verification email.

**Flow:**
1. Create entry in `auth_user` (Auth module)
2. Call User Management module to create entry in `user_profile`
3. Send verification email

### Request

```
POST /api/v1/auth/signup
Content-Type: application/json
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| email | string | Yes | Valid email format, max 255 chars | Primary identifier |
| password | string | Yes | Min 8 chars | Plain password (hashed with Argon2) |
| firstName | string | Yes | Max 100 chars | First name |
| lastName | string | No | Max 100 chars | Last name |
| phone | string | No | Max 20 chars | Phone number |

**Example Request:**
```json
{
  "email": "jane.doe@example.com",
  "password": "SecureP@ss123",
  "firstName": "Jane",
  "lastName": "Doe",
  "phone": "+919876543210"
}
```

### Success Response

**201 Created**

```json
{
  "success": true,
  "response": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "jane.doe@example.com",
    "message": "Registration successful. Please verify your email.",
    "requiresEmailVerification": true
  }
}
```

### Error Responses

| HTTP | Error Code | Condition |
|------|------------|-----------|
| 400 | `QE_AUTH_001` | Validation failed (see `details` array) |
| 409 | `QE_AUTH_002` | Email already registered |

**400 Example:**
```json
{
  "success": false,
  "error": {
    "code": "QE_AUTH_001",
    "message": "Validation failed",
    "details": [
      {
        "field": "email",
        "message": "Invalid email format"
      },
      {
        "field": "password",
        "message": "Password must be at least 8 characters"
      }
    ]
  }
}
```

**409 Example:**
```json
{
  "success": false,
  "error": {
    "code": "QE_AUTH_002",
    "message": "An account with this email already exists"
  }
}
```

---

## 2. Login

Authenticates user and returns JWT.

### Request

```
POST /api/v1/auth/login
Content-Type: application/json
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | Yes | User email |
| password | string | Yes | Plain password |

**Example Request:**
```json
{
  "email": "jane.doe@example.com",
  "password": "SecureP@ss123"
}
```

### Success Response

**200 OK**

```json
{
  "success": true,
  "response": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2025-02-20T14:30:00Z",
    "user": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "email": "jane.doe@example.com",
      "firstName": "Jane",
      "lastName": "Doe",
      "role": "USER",
      "isVerified": true
    }
  }
}
```

### Error Responses

| HTTP | Error Code | Condition |
|------|------------|-----------|
| 400 | `QE_AUTH_001` | Missing or invalid email/password |
| 401 | `QE_AUTH_003` | Wrong email or password (generic message) |
| 403 | `QE_AUTH_004` | Email not verified — user must verify before login |

**401 Example:**
```json
{
  "success": false,
  "error": {
    "code": "QE_AUTH_003",
    "message": "Invalid email or password"
  }
}
```

**403 Example (unverified user):**
```json
{
  "success": false,
  "error": {
    "code": "QE_AUTH_004",
    "message": "Please verify your email before logging in"
  }
}
```

---

## 3. Verify Email

Verifies user email using token sent via email.

### Request

```
POST /api/v1/auth/verify-email
Content-Type: application/json
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| token | string | Yes | Email verification token (from link) |

**Example Request:**
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### Success Response

**200 OK**

```json
{
  "success": true,
  "response": {
    "message": "Email verified successfully",
    "userId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

### Error Responses

| HTTP | Error Code | Condition |
|------|------------|-----------|
| 400 | `QE_AUTH_005` | Token missing or invalid format |
| 404 | `QE_AUTH_006` | Token expired or already used |

**400 Example:**
```json
{
  "success": false,
  "error": {
    "code": "QE_AUTH_005",
    "message": "Invalid verification token"
  }
}
```

**404 Example:**
```json
{
  "success": false,
  "error": {
    "code": "QE_AUTH_006",
    "message": "Verification token has expired or already been used"
  }
}
```

---

## 4. Resend Verification

Sends a new verification email.

### Request

```
POST /api/v1/auth/resend-verification
Content-Type: application/json
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | Yes | Email to resend verification to |

**Example Request:**
```json
{
  "email": "jane.doe@example.com"
}
```

### Success Response

**200 OK**

Always returns success for valid, unverified email (prevents email enumeration).

```json
{
  "success": true,
  "response": {
    "message": "If an account exists with this email, a verification link has been sent."
  }
}
```

---

## Auth Error Codes Reference

| Code | HTTP | Description |
|------|------|-------------|
| QE_AUTH_001 | 400 | Validation failed |
| QE_AUTH_002 | 409 | Email already exists |
| QE_AUTH_003 | 401 | Invalid credentials |
| QE_AUTH_004 | 403 | Email not verified |
| QE_AUTH_005 | 400 | Invalid verification token |
| QE_AUTH_006 | 404 | Verification token expired or used |

---

## JWT Token

- **Algorithm:** HS256  
- **TTL:** 24 hours  
- **Header:** `Authorization: Bearer <token>`

**Payload:**
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "role": "USER",
  "iat": 1710000000,
  "exp": 1710086400
}
```

| Claim | Description |
|-------|-------------|
| sub | User UUID |
| role | `USER` or `ADMIN` |
| iat | Issued at (Unix timestamp) |
| exp | Expires at (Unix timestamp) |

---

## Validation Rules Summary

| Field | Rules |
|-------|-------|
| email | Valid format, max 255 chars, unique |
| password | Min 8 chars (Phase-1) |
| firstName | Required, max 100 chars |
| lastName | Optional, max 100 chars |
| phone | Optional, max 20 chars |

---

## Versioning

- Base path includes version: `/api/v1/auth`
- Future breaking changes → `/api/v2/auth`
