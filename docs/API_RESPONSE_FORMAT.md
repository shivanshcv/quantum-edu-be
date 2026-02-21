# Standard API Response Format

**Applies to:** All APIs in this repository

---

## Success Response

| Property | Type | Description |
|----------|------|-------------|
| success | boolean | Always `true` |
| response | object | Response payload (varies by API) |

**HTTP Status:** 200 (or 201 for creation)

```json
{
  "success": true,
  "response": {}
}
```

---

## Error Response

| Property | Type | Description |
|----------|------|-------------|
| success | boolean | Always `false` |
| error | object | Error details |
| error.code | string | Error code (e.g. `QE_001`) |
| error.message | string | Human-readable message |
| error.details | array | Optional. Field-level validation errors |

**HTTP Status:** 4xx or 5xx (varies by error type)

```json
{
  "success": false,
  "error": {
    "code": "QE_001",
    "message": "INTERNAL SERVER ERROR"
  }
}
```

**With validation details (optional):**
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

---

## Global Error Codes

| Code | HTTP | Description |
|------|------|-------------|
| QE_001 | 500 | Internal server error |

Module-specific error codes (e.g. `QE_AUTH_xxx`) are defined in each module's API spec.
