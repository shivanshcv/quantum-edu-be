# Quantum Education — API Contract

**Version:** 1.0  
**Base URL:** `http://localhost:8080`  
**Content-Type:** `application/json`

---

## Table of Contents

1. [Generic Request & Response Format](#1-generic-request--response-format)
2. [Error Response Format](#2-error-response-format)
3. [Global Error Codes](#3-global-error-codes)
4. [Auth Module APIs](#4-auth-module-apis)
5. [Product Catalogue Module APIs](#5-product-catalogue-module-apis)
6. [Cart Module APIs](#6-cart-module-apis)
7. [Ownership Module APIs](#7-ownership-module-apis)
8. [Complete Error Codes Reference](#8-complete-error-codes-reference)

---

## 1. Generic Request & Response Format

### Success Response

All successful API responses follow this structure:

```json
{
  "success": true,
  "response": { ... }
}
```

| Field     | Type    | Description                                      |
|-----------|---------|--------------------------------------------------|
| success   | boolean | Always `true` for success                        |
| response  | object  | API-specific payload; may be `null` for 204/delete |

**HTTP Status:** Typically `200 OK` or `201 Created` for create operations.

### Error Response

All error responses follow this structure:

```json
{
  "success": false,
  "error": {
    "code": "QE_XXX",
    "message": "Human-readable error message"
  }
}
```

| Field   | Type   | Description                    |
|---------|--------|--------------------------------|
| success | boolean| Always `false` for errors      |
| error   | object | Error details                  |
| error.code | string | Machine-readable error code |
| error.message | string | Human-readable message   |

---

## 2. Error Response Format

**Example Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "QE_AUTH_003",
    "message": "Invalid email or password"
  }
}
```

---

## 3. Global Error Codes

These can apply to any API:

| Code       | HTTP | Message              | Condition                          |
|------------|------|----------------------|------------------------------------|
| QE_001     | 500  | Internal server error| Unhandled server exception         |
| QE_VAL_001 | 400  | Validation failed    | Request validation failed (@Valid)  |
| QE_VAL_002 | 400  | Invalid request body | Malformed JSON or invalid structure|

---

## 4. Auth Module APIs

**Base Path:** `/api/v1/auth`

---

### 4.1 Signup

**Endpoint:** `POST /api/v1/auth/signup`  
**Auth Required:** No

#### Request

| Field     | Type   | Required | Validation        | Description        |
|-----------|--------|----------|-------------------|--------------------|
| email     | string | Yes      | Valid email, max 255 | Primary identifier |
| password  | string | Yes      | Min 8 chars       | Plain password     |
| firstName | string | Yes      | Max 100 chars     | First name         |
| lastName  | string | No       | Max 100 chars     | Last name          |
| phone     | string | No       | Max 20 chars      | Phone number       |

#### Success Response (201 Created)

```json
{
  "success": true,
  "response": {
    "userId": 1,
    "email": "jane.doe@example.com",
    "message": "Registration successful. Please verify your email.",
    "requiresEmailVerification": true
  }
}
```

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.doe@example.com",
    "password": "SecureP@ss123",
    "firstName": "Jane",
    "lastName": "Doe",
    "phone": "+919876543210"
  }'
```

#### Error Codes

| Code       | HTTP | Message                                   | Condition              |
|------------|------|-------------------------------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed                          | Invalid/missing fields |
| QE_VAL_002 | 400  | Invalid request body                       | Malformed JSON         |
| QE_AUTH_002| 409  | An account with this email already exists  | Duplicate email        |
| QE_001     | 500  | Internal server error                      | Unexpected error       |

---

### 4.2 Login

**Endpoint:** `POST /api/v1/auth/login`  
**Auth Required:** No

#### Request

| Field    | Type   | Required | Description |
|----------|--------|----------|-------------|
| email    | string | Yes      | User email  |
| password | string | Yes      | Plain password |

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2025-02-20T14:30:00Z",
    "user": {
      "userId": 1,
      "email": "jane.doe@example.com",
      "firstName": "Jane",
      "lastName": "Doe",
      "role": "USER",
      "verified": true
    }
  }
}
```

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.doe@example.com",
    "password": "SecureP@ss123"
  }'
```

#### Error Codes

| Code       | HTTP | Message                                      | Condition              |
|------------|------|----------------------------------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed                             | Invalid/missing fields |
| QE_VAL_002 | 400  | Invalid request body                          | Malformed JSON         |
| QE_AUTH_003| 401  | Invalid email or password                      | Wrong credentials      |
| QE_AUTH_004| 403  | Please verify your email before logging in    | Email not verified     |
| QE_001     | 500  | Internal server error                         | Unexpected error       |

---

### 4.3 Verify Email

**Endpoint:** `POST /api/v1/auth/verify-email`  
**Auth Required:** No

#### Request

| Field | Type   | Required | Description                    |
|-------|--------|----------|--------------------------------|
| token | string | Yes      | Email verification token from link |

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "message": "Email verified successfully",
    "userId": 1
  }
}
```

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{"token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"}'
```

#### Error Codes

| Code       | HTTP | Message                                              | Condition              |
|------------|------|------------------------------------------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed                                     | Missing token          |
| QE_VAL_002 | 400  | Invalid request body                                  | Malformed JSON         |
| QE_AUTH_005| 400  | Invalid verification token                            | Invalid token format   |
| QE_AUTH_006| 404  | Verification token has expired or already been used   | Token expired/used     |
| QE_001     | 500  | Internal server error                                 | Unexpected error       |

---

### 4.4 Resend Verification

**Endpoint:** `POST /api/v1/auth/resend-verification`  
**Auth Required:** No

#### Request

| Field | Type   | Required | Description |
|-------|--------|----------|-------------|
| email | string | Yes      | Email to resend verification to |

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "message": "If an account exists with this email, a verification link has been sent."
  }
}
```

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/resend-verification \
  -H "Content-Type: application/json" \
  -d '{"email": "jane.doe@example.com"}'
```

#### Error Codes

| Code       | HTTP | Message              | Condition              |
|------------|------|----------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed    | Invalid/missing email  |
| QE_VAL_002 | 400  | Invalid request body | Malformed JSON         |
| QE_001     | 500  | Internal server error| Unexpected error       |

---

## 5. Product Catalogue Module APIs

### 5.1 Admin Category APIs

**Base Path:** `/api/v1/admin/catalogue`

---

#### 5.1.1 Create Category

**Endpoint:** `POST /api/v1/admin/catalogue/createCategory`  
**Auth Required:** No (Phase 1; will be ADMIN-gated later)

#### Request

| Field    | Type  | Required | Validation | Description      |
|----------|-------|----------|------------|------------------|
| name     | string| Yes      | Max 150    | Category name    |
| parentId | long  | No       | -          | Parent category ID; null = root |

#### Success Response (201 Created)

```json
{
  "success": true,
  "response": {
    "id": 1,
    "name": "Beauty",
    "slug": "beauty",
    "parentId": null,
    "level": 0,
    "active": true,
    "children": null
  }
}
```

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/admin/catalogue/createCategory \
  -H "Content-Type: application/json" \
  -d '{"name": "Beauty"}'
```

#### Error Codes

| Code       | HTTP | Message                              | Condition              |
|------------|------|--------------------------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed                     | Invalid/missing fields |
| QE_PC_001  | 404  | Category not found                    | Invalid parentId       |
| QE_PC_003  | 409  | Category slug already exists          | Slug collision         |
| QE_PC_005  | 400  | Circular category reference detected  | Circular parent chain  |
| QE_001     | 500  | Internal server error                 | Unexpected error       |

---

#### 5.1.2 Update Category

**Endpoint:** `PUT /api/v1/admin/catalogue/updateCategory/{id}`

#### Request

| Field | Type    | Required | Validation | Description     |
|-------|---------|----------|------------|-----------------|
| name  | string  | No       | Max 150    | Category name   |
| active| boolean | No       | -          | Active flag     |

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "id": 1,
    "name": "Skin Care",
    "slug": "skin-care",
    "parentId": null,
    "level": 0,
    "active": true,
    "children": null
  }
}
```

#### Sample cURL

```bash
curl -s -X PUT http://localhost:8080/api/v1/admin/catalogue/updateCategory/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Skin Care", "active": true}'
```

#### Error Codes

| Code       | HTTP | Message                     | Condition              |
|------------|------|-----------------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed            | Invalid fields         |
| QE_PC_001  | 404  | Category not found           | Invalid category id    |
| QE_PC_003  | 409  | Category slug already exists | Slug collision on update |
| QE_001     | 500  | Internal server error        | Unexpected error       |

---

#### 5.1.3 Deactivate Category (Soft Delete)

**Endpoint:** `DELETE /api/v1/admin/catalogue/deactivateCategory/{id}`

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": null
}
```

#### Sample cURL

```bash
curl -s -X DELETE http://localhost:8080/api/v1/admin/catalogue/deactivateCategory/2
```

#### Error Codes

| Code   | HTTP | Message              | Condition       |
|--------|------|----------------------|-----------------|
| QE_PC_001 | 404 | Category not found   | Invalid id      |
| QE_001 | 500  | Internal server error| Unexpected error |

---

#### 5.1.4 List All Categories (Flat)

**Endpoint:** `GET /api/v1/admin/catalogue/getCategories`

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": [
    {
      "id": 1,
      "name": "Beauty",
      "slug": "beauty",
      "parentId": null,
      "level": 0,
      "active": true,
      "children": null
    },
    {
      "id": 2,
      "name": "Skincare",
      "slug": "skincare",
      "parentId": 1,
      "level": 1,
      "active": true,
      "children": null
    }
  ]
}
```

#### Sample cURL

```bash
curl -s http://localhost:8080/api/v1/admin/catalogue/getCategories
```

#### Error Codes

| Code   | HTTP | Message              | Condition       |
|--------|------|----------------------|-----------------|
| QE_001 | 500  | Internal server error| Unexpected error |

---

#### 5.1.5 Get Category Tree

**Endpoint:** `GET /api/v1/admin/catalogue/getCategoriesTree`

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": [
    {
      "id": 1,
      "name": "Beauty",
      "slug": "beauty",
      "parentId": null,
      "level": 0,
      "active": true,
      "children": [
        {
          "id": 2,
          "name": "Skincare",
          "slug": "skincare",
          "parentId": 1,
          "level": 1,
          "active": true,
          "children": null
        }
      ]
    }
  ]
}
```

#### Sample cURL

```bash
curl -s http://localhost:8080/api/v1/admin/catalogue/getCategoriesTree
```

#### Error Codes

| Code   | HTTP | Message              | Condition       |
|--------|------|----------------------|-----------------|
| QE_001 | 500  | Internal server error| Unexpected error |

---

### 5.2 Admin Product APIs

**Base Path:** `/api/v1/admin/catalogue`

---

#### 5.2.1 Create Product

**Endpoint:** `POST /api/v1/admin/catalogue/createProduct`

#### Request

| Field           | Type    | Required | Validation   | Description          |
|-----------------|---------|----------|-------------|----------------------|
| title           | string  | Yes      | Max 255     | Product title        |
| shortDescription| string | Yes      | -           | Short description    |
| longDescription | string  | Yes      | -           | Long description     |
| price           | decimal | Yes      | >= 0        | Price                |
| discountPrice   | decimal | No       | >= 0        | Discounted price     |
| thumbnailUrl    | string  | No       | Max 500     | Thumbnail URL        |
| previewVideoUrl | string  | No       | Max 500     | Preview video URL    |
| difficultyLevel | enum    | No       | BEGINNER, INTERMEDIATE, ADVANCED | Difficulty |
| durationMinutes | integer | No       | >= 0        | Duration in minutes  |
| categoryIds     | set     | No       | -           | Set of category IDs  |

#### Success Response (201 Created)

```json
{
  "success": true,
  "response": {
    "id": 1,
    "title": "Complete Skincare Masterclass",
    "slug": "complete-skincare-masterclass",
    "shortDescription": "Learn professional skincare techniques",
    "price": 2999.00,
    "discountPrice": 1999.00,
    "thumbnailUrl": null,
    "difficultyLevel": "BEGINNER",
    "durationMinutes": 480,
    "published": false,
    "categories": [
      {"id": 1, "name": "Beauty", "slug": "beauty", "parentId": null, "level": 0, "active": true, "children": null}
    ]
  }
}
```

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/admin/catalogue/createProduct \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete Skincare Masterclass",
    "shortDescription": "Learn professional skincare techniques",
    "longDescription": "A comprehensive course covering all aspects of professional skincare.",
    "price": 2999.00,
    "discountPrice": 1999.00,
    "difficultyLevel": "BEGINNER",
    "durationMinutes": 480,
    "categoryIds": [1, 2]
  }'
```

#### Error Codes

| Code       | HTTP | Message                     | Condition              |
|------------|------|-----------------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed            | Invalid/missing fields  |
| QE_PC_002  | 404  | Product not found            | Invalid categoryIds    |
| QE_PC_004  | 409  | Product slug already exists  | Slug collision         |
| QE_001     | 500  | Internal server error        | Unexpected error       |

---

#### 5.2.2 Update Product

**Endpoint:** `PUT /api/v1/admin/catalogue/updateProduct/{id}`

#### Request

Same fields as Create Product; all optional.

#### Success Response (200 OK)

Same structure as Create Product response.

#### Sample cURL

```bash
curl -s -X PUT http://localhost:8080/api/v1/admin/catalogue/updateProduct/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Skincare Pro Masterclass", "durationMinutes": 600}'
```

#### Error Codes

| Code       | HTTP | Message                     | Condition              |
|------------|------|-----------------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed            | Invalid fields         |
| QE_PC_002  | 404  | Product not found            | Invalid product id     |
| QE_PC_004  | 409  | Product slug already exists  | Slug collision         |
| QE_001     | 500  | Internal server error        | Unexpected error       |

---

#### 5.2.3 Delete Product (Soft Delete / Unpublish)

**Endpoint:** `DELETE /api/v1/admin/catalogue/deleteProduct/{id}`

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": null
}
```

#### Sample cURL

```bash
curl -s -X DELETE http://localhost:8080/api/v1/admin/catalogue/deleteProduct/1
```

#### Error Codes

| Code   | HTTP | Message              | Condition       |
|--------|------|----------------------|-----------------|
| QE_PC_002 | 404 | Product not found    | Invalid id      |
| QE_001 | 500  | Internal server error | Unexpected error |

---

#### 5.2.4 Publish / Unpublish Product

**Endpoint:** `PATCH /api/v1/admin/catalogue/updateProductPublish/{id}`

#### Request

| Field    | Type    | Required | Description      |
|----------|---------|----------|------------------|
| published| boolean | Yes     | true = publish, false = unpublish |

#### Success Response (200 OK)

Same structure as Product List response.

#### Sample cURL

```bash
curl -s -X PATCH http://localhost:8080/api/v1/admin/catalogue/updateProductPublish/1 \
  -H "Content-Type: application/json" \
  -d '{"published": true}'
```

#### Error Codes

| Code   | HTTP | Message              | Condition       |
|--------|------|----------------------|-----------------|
| QE_VAL_001 | 400 | Validation failed    | Missing published |
| QE_PC_002 | 404 | Product not found    | Invalid id      |
| QE_001 | 500  | Internal server error | Unexpected error |

---

#### 5.2.5 List All Products (Admin)

**Endpoint:** `GET /api/v1/admin/catalogue/getProducts`

**Query Parameters:** `page`, `size`, `sort` (Spring Pageable)

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "content": [
      {
        "id": 1,
        "title": "Complete Skincare Masterclass",
        "slug": "complete-skincare-masterclass",
        "shortDescription": "Learn professional skincare techniques",
        "longDescription": "A comprehensive course...",
        "price": 2999.00,
        "discountPrice": 1999.00,
        "thumbnailUrl": null,
        "previewVideoUrl": null,
        "difficultyLevel": "BEGINNER",
        "durationMinutes": 480,
        "published": true,
        "categories": [...],
        "createdAt": "2025-02-19T10:30:00.000Z",
        "updatedAt": "2025-02-19T10:30:00.000Z"
      }
    ],
    "pageable": {...},
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    "first": true,
    "size": 20,
    "number": 0,
    "numberOfElements": 1,
    "empty": false
  }
}
```

#### Sample cURL

```bash
curl -s "http://localhost:8080/api/v1/admin/catalogue/getProducts?page=0&size=20"
```

#### Error Codes

| Code   | HTTP | Message              | Condition       |
|--------|------|----------------------|-----------------|
| QE_001 | 500  | Internal server error| Unexpected error |

---

#### 5.2.6 Get Product by ID (Admin)

**Endpoint:** `GET /api/v1/admin/catalogue/getProduct/{id}`

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "id": 1,
    "title": "Complete Skincare Masterclass",
    "slug": "complete-skincare-masterclass",
    "shortDescription": "Learn professional skincare techniques",
    "longDescription": "A comprehensive course...",
    "price": 2999.00,
    "discountPrice": 1999.00,
    "thumbnailUrl": null,
    "previewVideoUrl": null,
    "difficultyLevel": "BEGINNER",
    "durationMinutes": 480,
    "published": true,
    "categories": [...],
    "contents": [
      {
        "id": 1,
        "contentType": "LESSON",
        "title": "Introduction to Skincare",
        "orderIndex": 0,
        "mandatory": true
      }
    ],
    "createdAt": "2025-02-19T10:30:00.000Z",
    "updatedAt": "2025-02-19T10:30:00.000Z"
  }
}
```

#### Sample cURL

```bash
curl -s http://localhost:8080/api/v1/admin/catalogue/getProduct/1
```

#### Error Codes

| Code   | HTTP | Message              | Condition       |
|--------|------|----------------------|-----------------|
| QE_PC_002 | 404 | Product not found    | Invalid id      |
| QE_001 | 500  | Internal server error | Unexpected error |

---

### 5.3 Admin Product Content APIs

**Base Path:** `/api/v1/admin/catalogue`

---

#### 5.3.1 Add Content Item

**Endpoint:** `POST /api/v1/admin/catalogue/addProductContent/{productId}`

#### Request

| Field          | Type    | Required | Validation | Description                    |
|----------------|---------|----------|------------|--------------------------------|
| contentType    | enum    | Yes      | LESSON, ASSESSMENT | Content type           |
| title          | string  | Yes      | Max 255    | Content title                  |
| orderIndex     | integer | Yes      | >= 0       | Order in spine                 |
| mandatory      | boolean | No       | -          | Default true                   |
| lessonType     | enum    | No*      | VIDEO, PDF | *Required if contentType=LESSON |
| videoUrl       | string  | No       | Max 500    | Video URL (for LESSON)         |
| pdfUrl         | string  | No       | Max 500    | PDF URL (for LESSON)           |
| durationSeconds| integer | No       | >= 0       | Duration (for LESSON)          |
| passPercentage | integer | No       | >= 0       | Pass % (for ASSESSMENT, default 70) |

#### Success Response (201 Created)

```json
{
  "success": true,
  "response": {
    "id": 1,
    "contentType": "LESSON",
    "title": "Introduction to Skincare",
    "orderIndex": 0,
    "mandatory": true,
    "lesson": {
      "id": 1,
      "lessonType": "VIDEO",
      "videoUrl": "https://example.com/videos/intro.mp4",
      "pdfUrl": null,
      "durationSeconds": 1200
    },
    "assessment": null
  }
}
```

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/admin/catalogue/addProductContent/1 \
  -H "Content-Type: application/json" \
  -d '{
    "contentType": "LESSON",
    "title": "Introduction to Skincare",
    "orderIndex": 0,
    "mandatory": true,
    "lessonType": "VIDEO",
    "videoUrl": "https://example.com/videos/intro.mp4",
    "durationSeconds": 1200
  }'
```

#### Error Codes

| Code       | HTTP | Message                     | Condition              |
|------------|------|-----------------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed            | Invalid/missing fields  |
| QE_PC_002  | 404  | Product not found            | Invalid productId       |
| QE_PC_007  | 409  | Content order index already exists | Duplicate orderIndex |
| QE_001     | 500  | Internal server error        | Unexpected error       |

---

#### 5.3.2 Update Content Item

**Endpoint:** `PUT /api/v1/admin/catalogue/updateProductContent/{productId}/{contentId}`

#### Request

Same fields as Add Content; all optional (title, mandatory, lessonType, videoUrl, pdfUrl, durationSeconds, passPercentage).

#### Success Response (200 OK)

Same structure as Add Content response.

#### Sample cURL

```bash
curl -s -X PUT http://localhost:8080/api/v1/admin/catalogue/updateProductContent/1/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Introduction (Updated)", "videoUrl": "https://example.com/v2.mp4"}'
```

#### Error Codes

| Code       | HTTP | Message              | Condition              |
|------------|------|----------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed     | Invalid fields         |
| QE_PC_002  | 404  | Product not found     | Invalid productId      |
| QE_PC_006  | 404  | Product content not found | Invalid contentId  |
| QE_001     | 500  | Internal server error | Unexpected error       |

---

#### 5.3.3 Delete Content Item

**Endpoint:** `DELETE /api/v1/admin/catalogue/deleteProductContent/{productId}/{contentId}`

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": null
}
```

#### Sample cURL

```bash
curl -s -X DELETE http://localhost:8080/api/v1/admin/catalogue/deleteProductContent/1/3
```

#### Error Codes

| Code   | HTTP | Message                    | Condition       |
|--------|------|----------------------------|-----------------|
| QE_PC_002 | 404 | Product not found          | Invalid productId |
| QE_PC_006 | 404 | Product content not found  | Invalid contentId |
| QE_001 | 500  | Internal server error      | Unexpected error |

---

#### 5.3.4 Reorder Content Items

**Endpoint:** `PUT /api/v1/admin/catalogue/reorderProductContent/{productId}`

#### Request

```json
{
  "items": [
    {"contentId": 1, "orderIndex": 2},
    {"contentId": 2, "orderIndex": 0},
    {"contentId": 3, "orderIndex": 1}
  ]
}
```

| Field | Type  | Required | Description        |
|-------|-------|----------|--------------------|
| items | array | Yes      | List of ContentOrder |
| items[].contentId | long | Yes | Content ID   |
| items[].orderIndex| int  | Yes | New order index |

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": [
    {"id": 2, "contentType": "ASSESSMENT", "title": "Quiz 1", "orderIndex": 0, ...},
    {"id": 3, "contentType": "LESSON", "title": "Advanced", "orderIndex": 1, ...},
    {"id": 1, "contentType": "LESSON", "title": "Intro", "orderIndex": 2, ...}
  ]
}
```

#### Sample cURL

```bash
curl -s -X PUT http://localhost:8080/api/v1/admin/catalogue/reorderProductContent/1 \
  -H "Content-Type: application/json" \
  -d '{"items":[{"contentId":1,"orderIndex":2},{"contentId":2,"orderIndex":0},{"contentId":3,"orderIndex":1}]}'
```

#### Error Codes

| Code   | HTTP | Message                    | Condition       |
|--------|------|----------------------------|-----------------|
| QE_VAL_001 | 400 | Validation failed        | Invalid items   |
| QE_PC_002 | 404 | Product not found          | Invalid productId |
| QE_PC_006 | 404 | Product content not found  | Invalid contentId |
| QE_001 | 500  | Internal server error      | Unexpected error |

---

#### 5.3.5 List Product Content

**Endpoint:** `GET /api/v1/admin/catalogue/getProductContent/{productId}`

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": [
    {
      "id": 1,
      "contentType": "LESSON",
      "title": "Introduction to Skincare",
      "orderIndex": 0,
      "mandatory": true,
      "lesson": {...},
      "assessment": null
    },
    {
      "id": 2,
      "contentType": "ASSESSMENT",
      "title": "Module 1 Quiz",
      "orderIndex": 1,
      "mandatory": true,
      "lesson": null,
      "assessment": {"id": 1, "passPercentage": 80, "questionCount": 2}
    }
  ]
}
```

#### Sample cURL

```bash
curl -s http://localhost:8080/api/v1/admin/catalogue/getProductContent/1
```

#### Error Codes

| Code   | HTTP | Message              | Condition       |
|--------|------|----------------------|-----------------|
| QE_PC_002 | 404 | Product not found    | Invalid productId |
| QE_001 | 500  | Internal server error | Unexpected error |

---

#### 5.3.6 Add Question to Assessment

**Endpoint:** `POST /api/v1/admin/catalogue/addAssessmentQuestion/{contentId}`

#### Request

| Field       | Type   | Required | Description        |
|-------------|--------|----------|--------------------|
| questionText| string | Yes      | Question text      |
| options     | array  | Yes      | List of options    |
| options[].optionText | string | Yes | Option text |
| options[].correct    | boolean| Yes | Is correct answer |

#### Success Response (201 Created)

```json
{
  "success": true,
  "response": {
    "id": 1,
    "questionText": "What is the first step in a skincare routine?",
    "options": [
      {"id": 1, "optionText": "Moisturizing", "correct": false},
      {"id": 2, "optionText": "Cleansing", "correct": true}
    ]
  }
}
```

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/admin/catalogue/addAssessmentQuestion/2 \
  -H "Content-Type: application/json" \
  -d '{
    "questionText": "What is the first step in a skincare routine?",
    "options": [
      {"optionText": "Moisturizing", "correct": false},
      {"optionText": "Cleansing", "correct": true},
      {"optionText": "Toning", "correct": false}
    ]
  }'
```

#### Error Codes

| Code       | HTTP | Message              | Condition              |
|------------|------|----------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed     | Invalid/missing fields |
| QE_PC_008  | 404  | Assessment not found  | Invalid contentId (not ASSESSMENT) |
| QE_001     | 500  | Internal server error | Unexpected error       |

---

#### 5.3.7 Update Question

**Endpoint:** `PUT /api/v1/admin/catalogue/updateAssessmentQuestion/{contentId}/{questionId}`

#### Request

Same as Add Question (questionText, options).

#### Success Response (200 OK)

Same structure as Add Question response.

#### Sample cURL

```bash
curl -s -X PUT http://localhost:8080/api/v1/admin/catalogue/updateAssessmentQuestion/2/1 \
  -H "Content-Type: application/json" \
  -d '{
    "questionText": "What is the FIRST step in any skincare routine?",
    "options": [
      {"optionText": "Moisturize", "correct": false},
      {"optionText": "Cleanse", "correct": true},
      {"optionText": "Tone", "correct": false}
    ]
  }'
```

#### Error Codes

| Code       | HTTP | Message              | Condition              |
|------------|------|----------------------|------------------------|
| QE_VAL_001 | 400  | Validation failed     | Invalid fields         |
| QE_PC_008  | 404  | Assessment not found  | Invalid contentId      |
| QE_PC_009  | 404  | Question not found    | Invalid questionId     |
| QE_001     | 500  | Internal server error | Unexpected error       |

---

#### 5.3.8 Delete Question

**Endpoint:** `DELETE /api/v1/admin/catalogue/deleteAssessmentQuestion/{contentId}/{questionId}`

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": null
}
```

#### Sample cURL

```bash
curl -s -X DELETE http://localhost:8080/api/v1/admin/catalogue/deleteAssessmentQuestion/2/1
```

#### Error Codes

| Code       | HTTP | Message              | Condition              |
|------------|------|----------------------|------------------------|
| QE_PC_008  | 404  | Assessment not found  | Invalid contentId      |
| QE_PC_009  | 404  | Question not found    | Invalid questionId     |
| QE_001     | 500  | Internal server error | Unexpected error       |

---

### 5.4 Public Catalogue APIs

**Base Path:** `/api/v1/catalogue`

---

#### 5.4.1 Get Active Categories (Tree)

**Endpoint:** `GET /api/v1/catalogue/getCategories`

Returns only active categories in tree structure.

#### Success Response (200 OK)

Same structure as Admin Category Tree.

#### Sample cURL

```bash
curl -s http://localhost:8080/api/v1/catalogue/getCategories
```

#### Error Codes

| Code   | HTTP | Message              | Condition       |
|--------|------|----------------------|-----------------|
| QE_001 | 500  | Internal server error| Unexpected error |

---

#### 5.4.2 List Published Products

**Endpoint:** `GET /api/v1/catalogue/getProducts`

**Query Parameters:**

| Parameter   | Type   | Required | Description                    |
|-------------|--------|----------|--------------------------------|
| search      | string | No       | Search in title, shortDescription |
| categoryId  | long   | No       | Filter by category             |
| difficulty  | enum   | No       | BEGINNER, INTERMEDIATE, ADVANCED |
| page        | int    | No       | Page number (default 0)        |
| size        | int    | No       | Page size (default 20)         |
| sort        | string | No       | Sort field, e.g. `title,asc`   |

#### Success Response (200 OK)

Paginated response. Each item in `content` includes:

| Field | Type | Description |
|-------|------|-------------|
| id | long | Product ID |
| title | string | Product title |
| slug | string | URL-friendly slug |
| shortDescription | string | Short description |
| longDescription | string | Full description |
| price | number | Price |
| discountPrice | number | Discounted price (nullable) |
| thumbnailUrl | string | Thumbnail URL (nullable) |
| previewVideoUrl | string | Preview video URL (nullable) |
| difficultyLevel | string | BEGINNER, INTERMEDIATE, ADVANCED |
| durationMinutes | number | Duration in minutes (nullable) |
| published | boolean | Whether published |
| categories | array | Category objects |
| createdAt | string | ISO-8601 timestamp |
| updatedAt | string | ISO-8601 timestamp |

**Sample response structure:**

```json
{
  "success": true,
  "response": {
    "content": [
      {
        "id": 1,
        "title": "Complete Skincare Masterclass",
        "slug": "complete-skincare-masterclass",
        "shortDescription": "Learn professional skincare techniques",
        "longDescription": "A comprehensive course...",
        "price": 2999.00,
        "discountPrice": 1999.00,
        "thumbnailUrl": null,
        "previewVideoUrl": null,
        "difficultyLevel": "BEGINNER",
        "durationMinutes": 480,
        "published": true,
        "categories": [...],
        "createdAt": "2025-02-19T10:30:00.000Z",
        "updatedAt": "2025-02-19T10:30:00.000Z"
      }
    ],
    "pageable": {...},
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    "first": true,
    "size": 20,
    "number": 0,
    "numberOfElements": 1,
    "empty": false
  }
}
```

#### Sample cURL

```bash
curl -s "http://localhost:8080/api/v1/catalogue/getProducts?search=skincare&categoryId=1&page=0&size=20"
```

#### Error Codes

| Code   | HTTP | Message              | Condition       |
|--------|------|----------------------|-----------------|
| QE_001 | 500  | Internal server error| Unexpected error |

---

#### 5.4.3 Get Product by Slug (Public)

**Endpoint:** `GET /api/v1/catalogue/getProduct/{slug}`

Returns published product detail with content spine (titles only, no lesson/assessment data).

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "id": 1,
    "title": "Complete Skincare Masterclass",
    "slug": "complete-skincare-masterclass",
    "shortDescription": "Learn professional skincare techniques",
    "longDescription": "A comprehensive course...",
    "price": 2999.00,
    "discountPrice": 1999.00,
    "thumbnailUrl": null,
    "previewVideoUrl": null,
    "difficultyLevel": "BEGINNER",
    "durationMinutes": 480,
    "published": true,
    "categories": [...],
    "contents": [
      {"id": 1, "contentType": "LESSON", "title": "Introduction", "orderIndex": 0, "mandatory": true},
      {"id": 2, "contentType": "ASSESSMENT", "title": "Quiz 1", "orderIndex": 1, "mandatory": true}
    ],
    "createdAt": "2025-02-19T10:30:00.000Z",
    "updatedAt": "2025-02-19T10:30:00.000Z"
  }
}
```

#### Sample cURL

```bash
curl -s http://localhost:8080/api/v1/catalogue/getProduct/complete-skincare-masterclass
```

#### Error Codes

| Code   | HTTP | Message              | Condition                    |
|--------|------|----------------------|------------------------------|
| QE_PC_002 | 404 | Product not found    | Invalid slug or not published |
| QE_001 | 500  | Internal server error | Unexpected error            |

---

#### 5.4.4 Get Product Details by ID (Public)

**Endpoint:** `GET /api/v1/catalogue/getProductDetails`

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| productId | long | Yes | Product ID |

Returns published product detail with content spine (same structure as Get Product by Slug). Returns 404 if product not found or not published.

#### Success Response (200 OK)

Same structure as [Get Product by Slug](#543-get-product-by-slug-public).

```json
{
  "success": true,
  "response": {
    "id": 1,
    "title": "Complete Skincare Masterclass",
    "slug": "complete-skincare-masterclass",
    "shortDescription": "Learn professional skincare techniques",
    "longDescription": "A comprehensive course...",
    "price": 2999.00,
    "discountPrice": 1999.00,
    "thumbnailUrl": null,
    "previewVideoUrl": null,
    "difficultyLevel": "BEGINNER",
    "durationMinutes": 480,
    "published": true,
    "categories": [...],
    "contents": [
      {"id": 1, "contentType": "LESSON", "title": "Introduction", "orderIndex": 0, "mandatory": true},
      {"id": 2, "contentType": "ASSESSMENT", "title": "Quiz 1", "orderIndex": 1, "mandatory": true}
    ],
    "createdAt": "2025-02-19T10:30:00.000Z",
    "updatedAt": "2025-02-19T10:30:00.000Z"
  }
}
```

#### Sample cURL

```bash
curl -s "http://localhost:8080/api/v1/catalogue/getProductDetails?productId=1"
```

#### Error Codes

| Code   | HTTP | Message              | Condition                    |
|--------|------|----------------------|------------------------------|
| QE_PC_002 | 404 | Product not found    | Invalid productId or not published |
| QE_001 | 500  | Internal server error | Unexpected error            |

---

### 5.5 Health Check

**Endpoint:** `GET /health`  
**Auth Required:** No

#### Success Response (200 OK)

```json
{
  "status": "UP"
}
```

#### Sample cURL

```bash
curl -s http://localhost:8080/health
```

---

## 6. Cart Module APIs

**Base Path:** `/api/v1/cart`  
**Auth Required:** Yes (JWT Bearer token). All endpoints except webhook require `Authorization: Bearer <token>`.

### 6.1 Add to Cart

**Endpoint:** `POST /api/v1/cart/addItems`  
**Auth Required:** Yes

#### Request

| Field     | Type | Required | Description                          |
|-----------|------|----------|--------------------------------------|
| productId | long | Yes      | Product ID to add (BFF validates via Product Catalogue first) |

#### Request Body

```json
{
  "productId": 1
}
```

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "productId": 1,
    "addedAt": "2025-02-19T10:30:00Z"
  }
}
```
Idempotent: returns 200 if product already in cart.

**Phase 1:** Cart supports only one item. If the cart already has a different product, returns `QE_CART_008`. Remove the existing item first.

#### Error Codes

| Code       | HTTP | Message                                                                 | Condition              |
|------------|------|-------------------------------------------------------------------------|------------------------|
| QE_CART_008| 400  | Cart supports only one item. Remove the existing item before adding another. | Cart has different product (Phase 1) |

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/cart/addItems \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"productId": 1}'
```

---

### 6.2 Remove from Cart

**Endpoint:** `DELETE /api/v1/cart/removeItem/{productId}`  
**Auth Required:** Yes

#### Path Parameters

| Parameter | Type | Description   |
|-----------|------|---------------|
| productId | long | Product ID to remove |

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": null
}
```

#### Sample cURL

```bash
curl -s -X DELETE http://localhost:8080/api/v1/cart/removeItem/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 6.3 Get Cart

**Endpoint:** `GET /api/v1/cart/getCart`  
**Auth Required:** Yes

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "items": [
      { "productId": 1, "addedAt": "2025-02-19T10:30:00Z" }
    ]
  }
}
```
BFF enriches each item with product details from Product Catalogue.

#### Sample cURL

```bash
curl -s http://localhost:8080/api/v1/cart/getCart \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 6.4 Verify Cart (Before Checkout)

**Endpoint:** `POST /api/v1/cart/verifyCart`  
**Auth Required:** Yes

BFF fetches cart, gets product details from Product Catalogue, then calls this with items. Cart validates ownership and computes GST.

#### Request

| Field           | Type   | Required | Description                                    |
|-----------------|--------|----------|------------------------------------------------|
| items           | array  | Yes      | Product details from BFF                      |
| items[].productId | long | Yes    | Product ID                                     |
| items[].title  | string | Yes      | Product title                                  |
| items[].price  | decimal| Yes      | Base price                                     |
| items[].discountPrice | decimal | No  | Discounted price; if null, use price           |
| billingGstNumber | string | No     | Optional; affects GST calculation              |

#### Request Body

```json
{
  "items": [
    {
      "productId": 1,
      "title": "Advanced Makeup Masterclass",
      "price": 999.00,
      "discountPrice": 799.00
    }
  ],
  "billingGstNumber": null
}
```

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "items": [
      {
        "productId": 1,
        "title": "Advanced Makeup Masterclass",
        "price": 999.00,
        "discountPrice": 799.00,
        "finalPrice": 799.00,
        "gstAmount": 143.82,
        "quantity": 1
      }
    ],
    "alreadyOwned": [
      { "productId": 2, "title": "Basic Skincare", "reason": "ALREADY_OWNED" }
    ],
    "subtotal": 799.00,
    "gstAmount": 143.82,
    "finalAmount": 942.82,
    "currency": "INR"
  }
}
```

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/cart/verifyCart \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "items": [
      {
        "productId": 1,
        "title": "Advanced Makeup Masterclass",
        "price": 999.00,
        "discountPrice": 799.00
      }
    ]
  }'
```

---

### 6.5 Checkout

**Endpoint:** `POST /api/v1/cart/checkout`  
**Auth Required:** Yes

BFF fetches product details and billing from User Profile, then calls this. Returns Razorpay data for FE to open checkout.

#### Request

| Field | Type   | Required | Description                          |
|-------|--------|----------|--------------------------------------|
| items | array  | Yes      | Order line items with prices         |
| items[].productId | long | Yes | Product ID                           |
| items[].price | decimal | Yes   | Base price                           |
| items[].discountPrice | decimal | No | Discount price                       |
| items[].finalPrice | decimal | Yes | Final price per item                 |
| items[].gstAmount | decimal | Yes | GST amount per item                  |
| billing | object | Yes     | Billing snapshot (BFF prefills from User Profile) |
| billing.billingName | string | Yes | Billing name                         |
| billing.billingAddressLine1 | string | Yes | Address line 1                   |
| billing.billingAddressLine2 | string | No | Address line 2                       |
| billing.billingCity | string | Yes | City                                 |
| billing.billingState | string | Yes | State                                |
| billing.billingCountry | string | Yes | Country                            |
| billing.billingPostalCode | string | Yes | Postal code                        |
| billing.billingGstNumber | string | No | GST number (optional)               |

#### Request Body

```json
{
  "items": [
    {
      "productId": 1,
      "price": 999.00,
      "discountPrice": 799.00,
      "finalPrice": 799.00,
      "gstAmount": 143.82
    }
  ],
  "billing": {
    "billingName": "Jane Doe",
    "billingAddressLine1": "123 Main Street",
    "billingAddressLine2": "Apt 4B",
    "billingCity": "Mumbai",
    "billingState": "Maharashtra",
    "billingCountry": "India",
    "billingPostalCode": "400001",
    "billingGstNumber": null
  }
}
```

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "razorpayOrderId": "order_xxx",
    "amount": 94282,
    "currency": "INR",
    "keyId": "rzp_test_xxx",
    "orderIds": [101, 102]
  }
}
```
FE uses `razorpayOrderId`, `amount`, `currency`, `keyId` to open Razorpay checkout.

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/cart/checkout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "items": [
      {
        "productId": 1,
        "price": 999.00,
        "discountPrice": 799.00,
        "finalPrice": 799.00,
        "gstAmount": 143.82
      }
    ],
    "billing": {
      "billingName": "Jane Doe",
      "billingAddressLine1": "123 Main Street",
      "billingAddressLine2": "Apt 4B",
      "billingCity": "Mumbai",
      "billingState": "Maharashtra",
      "billingCountry": "India",
      "billingPostalCode": "400001"
    }
  }'
```

---

### 6.6 Razorpay Webhook

**Endpoint:** `POST /api/v1/cart/webhook/razorpay`  
**Auth Required:** No (signature-verified)

Razorpay sends payment events. Backend verifies `X-Razorpay-Signature` and processes `payment.captured`.

#### Request Headers

| Header | Required | Description                          |
|--------|----------|--------------------------------------|
| Content-Type | Yes | application/json                 |
| X-Razorpay-Signature | Yes | HMAC signature for verification |

#### Request Body (Razorpay payload)

Razorpay sends the raw webhook payload. Example `payment.captured` event structure:

```json
{
  "event": "payment.captured",
  "payload": {
    "payment": {
      "entity": {
        "id": "pay_xxx",
        "order_id": "order_xxx",
        "status": "captured",
        "amount": 94282,
        "currency": "INR"
      }
    }
  }
}
```

#### Sample cURL (for testing; signature will fail without valid secret)

```bash
curl -s -X POST http://localhost:8080/api/v1/cart/webhook/razorpay \
  -H "Content-Type: application/json" \
  -H "X-Razorpay-Signature: <hmac_signature>" \
  -d '{"event":"payment.captured","payload":{"payment":{"entity":{"order_id":"order_xxx","status":"captured"}}}}'
```

---

## 7. Ownership Module APIs

**Base Path:** `/api/v1/ownership`  
**Auth Required:** Yes (JWT Bearer token)

### 7.1 List Owned Courses

**Endpoint:** `GET /api/v1/ownership/courses`  
**Auth Required:** Yes

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "courses": [
      { "productId": 1, "purchasedAt": "2025-02-19T10:30:00Z" }
    ]
  }
}
```

---

### 7.2 Check Ownership

**Endpoint:** `GET /api/v1/ownership/owns/{productId}`  
**Auth Required:** Yes

#### Success Response (200 OK)

```json
{ "success": true, "response": { "owns": true } }
```
or
```json
{ "success": true, "response": { "owns": false } }
```

---

## 8. Complete Error Codes Reference

### Global

| Code       | HTTP | Message              | Module  |
|------------|------|----------------------|---------|
| QE_001     | 500  | Internal server error| Global  |
| QE_VAL_001 | 400  | Validation failed    | Global  |
| QE_VAL_002 | 400  | Invalid request body | Global  |

### Auth

| Code       | HTTP | Message                                          | API(s)                    |
|------------|------|--------------------------------------------------|---------------------------|
| QE_AUTH_002| 409  | An account with this email already exists        | Signup                    |
| QE_AUTH_003| 401  | Invalid email or password                        | Login                     |
| QE_AUTH_004| 403  | Please verify your email before logging in       | Login                     |
| QE_AUTH_005| 400  | Invalid verification token                       | Verify Email              |
| QE_AUTH_006| 404  | Verification token has expired or already been used | Verify Email          |

### User Management

| Code       | HTTP | Message                                   | Module        |
|------------|------|-------------------------------------------|---------------|
| QE_UM_001  | 400  | Validation failed                         | User Mgmt     |
| QE_UM_002  | 409  | User profile already exists for this user  | User Mgmt     |

### Product Catalogue

| Code       | HTTP | Message                              | API(s)                    |
|------------|------|--------------------------------------|---------------------------|
| QE_PC_001  | 404  | Category not found                   | Category CRUD             |
| QE_PC_002  | 404  | Product not found                    | Product, Content          |
| QE_PC_003  | 409  | Category slug already exists         | Create/Update Category    |
| QE_PC_004  | 409  | Product slug already exists          | Create/Update Product     |
| QE_PC_005  | 400  | Circular category reference detected | Create/Update Category    |
| QE_PC_006  | 404  | Product content not found            | Content CRUD, Reorder      |
| QE_PC_007  | 409  | Content order index already exists   | Add Content               |
| QE_PC_008  | 404  | Assessment not found                 | Question CRUD             |
| QE_PC_009  | 404  | Question not found                   | Update/Delete Question    |
| QE_PC_010  | 400  | Validation failed                    | Product Catalogue         |

### Cart

| Code       | HTTP | Message                                                         | API(s)        |
|------------|------|-----------------------------------------------------------------|---------------|
| QE_CART_001| 400  | Invalid request                                                  | Cart          |
| QE_CART_003| 409  | Product already in cart                                          | Add to cart   |
| QE_CART_004| 400  | Cart is empty                                                    | Verify, Checkout |
| QE_CART_008| 400  | Cart supports only one item. Remove the existing item before adding another. | Add to cart (Phase 1) |
| QE_CART_005| 400  | Invalid webhook signature                                        | Webhook       |
| QE_CART_006| 400  | All items in cart are already owned by you                       | Verify        |
| QE_CART_007| 400  | One or more products are already owned. Remove from cart before checkout | Checkout |

### Ownership

| Code       | HTTP | Message                          | API(s)        |
|------------|------|----------------------------------|---------------|
| QE_OWN_001 | 409  | User already owns this course    | Create ownership |

---

## JWT Token (Auth)

- **Algorithm:** HS256  
- **TTL:** 24 hours  
- **Header:** `Authorization: Bearer <token>`

**Payload:**
```json
{
  "sub": "1",
  "role": "USER",
  "iat": 1710000000,
  "exp": 1710086400
}
```

| Claim | Description        |
|-------|--------------------|
| sub   | User ID (Long)     |
| role  | `USER` or `ADMIN`  |
| iat   | Issued at (Unix)   |
| exp   | Expires at (Unix)  |

---

## Enums Reference

### Product.DifficultyLevel
- `BEGINNER`
- `INTERMEDIATE`
- `ADVANCED`

### ProductContent.ContentType
- `LESSON`
- `ASSESSMENT`

### LessonType
- `VIDEO`
- `PDF`
