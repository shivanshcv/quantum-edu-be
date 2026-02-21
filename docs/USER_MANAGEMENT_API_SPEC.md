# User Management Module — API Specification

**Version:** 1.0  
**Base Path:** `/api/v1/user-management` (external) / internal API (in-process)

> **Response Format:** All APIs use the standard response format. See [API_RESPONSE_FORMAT.md](./API_RESPONSE_FORMAT.md).

---

## Internal API: Create User Profile

**Purpose:** Called by Auth module during signup to create `user_profile` entry.  
**Communication:** In-process (same monolith). When extracted to microservices, this becomes a REST/HTTP call.

### Contract (Service Interface)

```
createUserProfile(CreateUserProfileRequest) -> UserProfile
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| userId | UUID | Yes | From auth_user.id |
| firstName | string | Yes | Max 100 chars |
| lastName | string | No | Max 100 chars |
| phone | string | No | Max 20 chars |

### Request (REST variant — for future microservice)

```
POST /internal/v1/user-management/profiles
Content-Type: application/json
X-Internal-Request: true
```

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
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
    "firstName": "Jane",
    "lastName": "Doe",
    "phone": "+919876543210"
  }
}
```

### Error Responses

| HTTP | Error Code | Condition |
|------|------------|-----------|
| 400 | `QE_UM_001` | Validation failed |
| 409 | `QE_UM_002` | User profile already exists for this userId |

---

## Schema Reference (user_profile)

| Column | Type |
|--------|------|
| user_id | UUID PK (FK → auth_user.id) |
| first_name | VARCHAR(100) NOT NULL |
| last_name | VARCHAR(100) |
| phone | VARCHAR(20) |
| profile_image_url | VARCHAR(500) |
| address_line1 | VARCHAR(255) |
| address_line2 | VARCHAR(255) |
| city | VARCHAR(100) |
| state | VARCHAR(100) |
| country | VARCHAR(100) |
| postal_code | VARCHAR(20) |
| gst_number | VARCHAR(30) |
| created_at | DATETIME NOT NULL |
| updated_at | DATETIME NOT NULL |

For signup, only `user_id`, `first_name`, `last_name`, `phone` are populated. Other fields are updated later via profile update APIs.
