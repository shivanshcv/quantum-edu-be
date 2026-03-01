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
8. [BFF Page APIs](#8-bff-page-apis)
9. [Complete Error Codes Reference](#9-complete-error-codes-reference)

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


| Field    | Type    | Description                                        |
| -------- | ------- | -------------------------------------------------- |
| success  | boolean | Always `true` for success                          |
| response | object  | API-specific payload; may be `null` for 204/delete |


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


| Field         | Type    | Description                 |
| ------------- | ------- | --------------------------- |
| success       | boolean | Always `false` for errors   |
| error         | object  | Error details               |
| error.code    | string  | Machine-readable error code |
| error.message | string  | Human-readable message      |


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


| Code       | HTTP | Message               | Condition                           |
| ---------- | ---- | --------------------- | ----------------------------------- |
| QE_001     | 500  | Internal server error | Unhandled server exception          |
| QE_VAL_001 | 400  | Validation failed     | Request validation failed (@Valid)  |
| QE_VAL_002 | 400  | Invalid request body  | Malformed JSON or invalid structure |


---

## 4. Auth Module APIs

**Base Path:** `/api/v1/auth`

---

### 4.1 Signup

**Endpoint:** `POST /api/v1/auth/signup`  
**Auth Required:** No

#### Request


| Field     | Type   | Required | Validation           | Description        |
| --------- | ------ | -------- | -------------------- | ------------------ |
| email     | string | Yes      | Valid email, max 255 | Primary identifier |
| password  | string | Yes      | Min 8 chars          | Plain password     |
| firstName | string | Yes      | Max 100 chars        | First name         |
| lastName  | string | No       | Max 100 chars        | Last name          |
| phone     | string | No       | Max 20 chars         | Phone number       |


#### Success Response (200 OK)

Returns a JWT token so the FE can automatically log the user in. Structure matches login response. The token includes `verified: false` until email is verified.

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
      "verified": false
    }
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


| Code        | HTTP | Message                                   | Condition              |
| ----------- | ---- | ----------------------------------------- | ---------------------- |
| QE_VAL_001  | 400  | Validation failed                         | Invalid/missing fields |
| QE_VAL_002  | 400  | Invalid request body                      | Malformed JSON         |
| QE_AUTH_002 | 409  | An account with this email already exists | Duplicate email        |
| QE_001      | 500  | Internal server error                     | Unexpected error       |


---

### 4.2 Login

**Endpoint:** `POST /api/v1/auth/login`  
**Auth Required:** No

#### Request


| Field    | Type   | Required | Description    |
| -------- | ------ | -------- | -------------- |
| email    | string | Yes      | User email     |
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


| Code        | HTTP | Message                                    | Condition              |
| ----------- | ---- | ------------------------------------------ | ---------------------- |
| QE_VAL_001  | 400  | Validation failed                          | Invalid/missing fields |
| QE_VAL_002  | 400  | Invalid request body                       | Malformed JSON         |
| QE_AUTH_003 | 401  | Invalid email or password                  | Wrong credentials      |
| QE_AUTH_004 | 403  | Please verify your email before logging in | Email not verified     |
| QE_001      | 500  | Internal server error                      | Unexpected error       |


---

### 4.3 Verify Email

**Endpoint:** `POST /api/v1/auth/verify-email`  
**Auth Required:** No

#### Request


| Field | Type   | Required | Description                        |
| ----- | ------ | -------- | ---------------------------------- |
| token | string | Yes      | Email verification token from link |


#### Success Response (200 OK)

Returns a new JWT token with `verified: true`. FE should replace the stored token with this one.

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
    },
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


| Code        | HTTP | Message                                             | Condition            |
| ----------- | ---- | --------------------------------------------------- | -------------------- |
| QE_VAL_001  | 400  | Validation failed                                   | Missing token        |
| QE_VAL_002  | 400  | Invalid request body                                | Malformed JSON       |
| QE_AUTH_005 | 400  | Invalid verification token                          | Invalid token format |
| QE_AUTH_006 | 404  | Verification token has expired or already been used | Token expired/used   |
| QE_001      | 500  | Internal server error                               | Unexpected error     |


---

### 4.4 Resend Verification

**Endpoint:** `POST /api/v1/auth/resend-verification`  
**Auth Required:** No

#### Request


| Field | Type   | Required | Description                     |
| ----- | ------ | -------- | ------------------------------- |
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


| Code       | HTTP | Message               | Condition             |
| ---------- | ---- | --------------------- | --------------------- |
| QE_VAL_001 | 400  | Validation failed     | Invalid/missing email |
| QE_VAL_002 | 400  | Invalid request body  | Malformed JSON        |
| QE_001     | 500  | Internal server error | Unexpected error      |


---

## 5. Product Catalogue Module APIs

### 5.1 Admin Category APIs

**Base Path:** `/api/v1/admin/catalogue`

---

#### 5.1.1 Create Category

**Endpoint:** `POST /api/v1/admin/catalogue/createCategory`  
**Auth Required:** No (Phase 1; will be ADMIN-gated later)

#### Request


| Field    | Type   | Required | Validation | Description                     |
| -------- | ------ | -------- | ---------- | ------------------------------- |
| name     | string | Yes      | Max 150    | Category name                   |
| parentId | long   | No       | -          | Parent category ID; null = root |


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
| ---------- | ---- | ------------------------------------ | ---------------------- |
| QE_VAL_001 | 400  | Validation failed                    | Invalid/missing fields |
| QE_PC_001  | 404  | Category not found                   | Invalid parentId       |
| QE_PC_003  | 409  | Category slug already exists         | Slug collision         |
| QE_PC_005  | 400  | Circular category reference detected | Circular parent chain  |
| QE_001     | 500  | Internal server error                | Unexpected error       |


---

#### 5.1.2 Update Category

**Endpoint:** `PUT /api/v1/admin/catalogue/updateCategory/{id}`

#### Request


| Field  | Type    | Required | Validation | Description   |
| ------ | ------- | -------- | ---------- | ------------- |
| name   | string  | No       | Max 150    | Category name |
| active | boolean | No       | -          | Active flag   |


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


| Code       | HTTP | Message                      | Condition                |
| ---------- | ---- | ---------------------------- | ------------------------ |
| QE_VAL_001 | 400  | Validation failed            | Invalid fields           |
| QE_PC_001  | 404  | Category not found           | Invalid category id      |
| QE_PC_003  | 409  | Category slug already exists | Slug collision on update |
| QE_001     | 500  | Internal server error        | Unexpected error         |


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


| Code      | HTTP | Message               | Condition        |
| --------- | ---- | --------------------- | ---------------- |
| QE_PC_001 | 404  | Category not found    | Invalid id       |
| QE_001    | 500  | Internal server error | Unexpected error |


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


| Code   | HTTP | Message               | Condition        |
| ------ | ---- | --------------------- | ---------------- |
| QE_001 | 500  | Internal server error | Unexpected error |


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


| Code   | HTTP | Message               | Condition        |
| ------ | ---- | --------------------- | ---------------- |
| QE_001 | 500  | Internal server error | Unexpected error |


---

### 5.2 Admin Product APIs

**Base Path:** `/api/v1/admin/catalogue`

---

#### 5.2.1 Create Product

**Endpoint:** `POST /api/v1/admin/catalogue/createProduct`

#### Request


| Field            | Type    | Required | Validation                       | Description         |
| ---------------- | ------- | -------- | -------------------------------- | ------------------- |
| title            | string  | Yes      | Max 255                          | Product title       |
| shortDescription | string  | Yes      | -                                | Short description   |
| longDescription  | string  | Yes      | -                                | Long description    |
| price            | decimal | Yes      | >= 0                             | Price               |
| discountPrice    | decimal | No       | >= 0                             | Discounted price    |
| thumbnailUrl     | string  | No       | Max 500                          | Thumbnail URL       |
| previewVideoUrl  | string  | No       | Max 500                          | Preview video URL   |
| difficultyLevel  | enum    | No       | BEGINNER, INTERMEDIATE, ADVANCED | Difficulty          |
| durationMinutes  | integer | No       | >= 0                             | Duration in minutes |
| categoryIds      | set     | No       | -                                | Set of category IDs |


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
| ---------- | ---- | --------------------------- | ---------------------- |
| QE_VAL_001 | 400  | Validation failed           | Invalid/missing fields |
| QE_PC_002  | 404  | Product not found           | Invalid categoryIds    |
| QE_PC_004  | 409  | Product slug already exists | Slug collision         |
| QE_001     | 500  | Internal server error       | Unexpected error       |


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


| Code       | HTTP | Message                     | Condition          |
| ---------- | ---- | --------------------------- | ------------------ |
| QE_VAL_001 | 400  | Validation failed           | Invalid fields     |
| QE_PC_002  | 404  | Product not found           | Invalid product id |
| QE_PC_004  | 409  | Product slug already exists | Slug collision     |
| QE_001     | 500  | Internal server error       | Unexpected error   |


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


| Code      | HTTP | Message               | Condition        |
| --------- | ---- | --------------------- | ---------------- |
| QE_PC_002 | 404  | Product not found     | Invalid id       |
| QE_001    | 500  | Internal server error | Unexpected error |


---

#### 5.2.4 Publish / Unpublish Product

**Endpoint:** `PATCH /api/v1/admin/catalogue/updateProductPublish/{id}`

#### Request


| Field     | Type    | Required | Description                       |
| --------- | ------- | -------- | --------------------------------- |
| published | boolean | Yes      | true = publish, false = unpublish |


#### Success Response (200 OK)

Same structure as Product List response.

#### Sample cURL

```bash
curl -s -X PATCH http://localhost:8080/api/v1/admin/catalogue/updateProductPublish/1 \
  -H "Content-Type: application/json" \
  -d '{"published": true}'
```

#### Error Codes


| Code       | HTTP | Message               | Condition         |
| ---------- | ---- | --------------------- | ----------------- |
| QE_VAL_001 | 400  | Validation failed     | Missing published |
| QE_PC_002  | 404  | Product not found     | Invalid id        |
| QE_001     | 500  | Internal server error | Unexpected error  |


---

#### 5.2.5 Set Featured / Unfeatured

**Endpoint:** `PATCH /api/v1/admin/catalogue/setFeatured/{id}`

#### Request


| Field    | Type    | Required | Description                         |
| -------- | ------- | -------- | ----------------------------------- |
| featured | boolean | Yes      | true = featured, false = unfeatured |


#### Request Body

```json
{
  "featured": true
}
```

#### Success Response (200 OK)

Same structure as Product List response.

#### Sample cURL

```bash
curl -s -X PATCH http://localhost:8080/api/v1/admin/catalogue/setFeatured/1 \
  -H "Content-Type: application/json" \
  -d '{"featured": true}'
```

#### Error Codes


| Code       | HTTP | Message               | Condition        |
| ---------- | ---- | --------------------- | ---------------- |
| QE_VAL_001 | 400  | Validation failed     | Missing featured |
| QE_PC_002  | 404  | Product not found     | Invalid id       |
| QE_001     | 500  | Internal server error | Unexpected error |


---

#### 5.2.6 List All Products (Admin)

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


| Code   | HTTP | Message               | Condition        |
| ------ | ---- | --------------------- | ---------------- |
| QE_001 | 500  | Internal server error | Unexpected error |


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


| Code      | HTTP | Message               | Condition        |
| --------- | ---- | --------------------- | ---------------- |
| QE_PC_002 | 404  | Product not found     | Invalid id       |
| QE_001    | 500  | Internal server error | Unexpected error |


---

### 5.3 Admin Product Content APIs

**Base Path:** `/api/v1/admin/catalogue`

---

#### 5.3.1 Add Content Item

**Endpoint:** `POST /api/v1/admin/catalogue/addProductContent/{productId}`

#### Request


| Field           | Type    | Required | Validation         | Description                         |
| --------------- | ------- | -------- | ------------------ | ----------------------------------- |
| contentType     | enum    | Yes      | LESSON, ASSESSMENT | Content type                        |
| title           | string  | Yes      | Max 255            | Content title                       |
| orderIndex      | integer | Yes      | >= 0               | Order in spine                      |
| mandatory       | boolean | No       | -                  | Default true                        |
| lessonType      | enum    | No*      | VIDEO, PDF         | *Required if contentType=LESSON     |
| videoUrl        | string  | No       | Max 500            | Video URL (for LESSON)              |
| pdfUrl          | string  | No       | Max 500            | PDF URL (for LESSON)                |
| durationSeconds | integer | No       | >= 0               | Duration (for LESSON)               |
| passPercentage  | integer | No       | >= 0               | Pass % (for ASSESSMENT, default 70) |


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


| Code       | HTTP | Message                            | Condition              |
| ---------- | ---- | ---------------------------------- | ---------------------- |
| QE_VAL_001 | 400  | Validation failed                  | Invalid/missing fields |
| QE_PC_002  | 404  | Product not found                  | Invalid productId      |
| QE_PC_007  | 409  | Content order index already exists | Duplicate orderIndex   |
| QE_001     | 500  | Internal server error              | Unexpected error       |


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


| Code       | HTTP | Message                   | Condition         |
| ---------- | ---- | ------------------------- | ----------------- |
| QE_VAL_001 | 400  | Validation failed         | Invalid fields    |
| QE_PC_002  | 404  | Product not found         | Invalid productId |
| QE_PC_006  | 404  | Product content not found | Invalid contentId |
| QE_001     | 500  | Internal server error     | Unexpected error  |


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


| Code      | HTTP | Message                   | Condition         |
| --------- | ---- | ------------------------- | ----------------- |
| QE_PC_002 | 404  | Product not found         | Invalid productId |
| QE_PC_006 | 404  | Product content not found | Invalid contentId |
| QE_001    | 500  | Internal server error     | Unexpected error  |


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


| Field              | Type  | Required | Description          |
| ------------------ | ----- | -------- | -------------------- |
| items              | array | Yes      | List of ContentOrder |
| items[].contentId  | long  | Yes      | Content ID           |
| items[].orderIndex | int   | Yes      | New order index      |


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


| Code       | HTTP | Message                   | Condition         |
| ---------- | ---- | ------------------------- | ----------------- |
| QE_VAL_001 | 400  | Validation failed         | Invalid items     |
| QE_PC_002  | 404  | Product not found         | Invalid productId |
| QE_PC_006  | 404  | Product content not found | Invalid contentId |
| QE_001     | 500  | Internal server error     | Unexpected error  |


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


| Code      | HTTP | Message               | Condition         |
| --------- | ---- | --------------------- | ----------------- |
| QE_PC_002 | 404  | Product not found     | Invalid productId |
| QE_001    | 500  | Internal server error | Unexpected error  |


---

#### 5.3.6 Add Question to Assessment

**Endpoint:** `POST /api/v1/admin/catalogue/addAssessmentQuestion/{contentId}`

#### Request


| Field                | Type    | Required | Description       |
| -------------------- | ------- | -------- | ----------------- |
| questionText         | string  | Yes      | Question text     |
| options              | array   | Yes      | List of options   |
| options[].optionText | string  | Yes      | Option text       |
| options[].correct    | boolean | Yes      | Is correct answer |


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


| Code       | HTTP | Message               | Condition                          |
| ---------- | ---- | --------------------- | ---------------------------------- |
| QE_VAL_001 | 400  | Validation failed     | Invalid/missing fields             |
| QE_PC_008  | 404  | Assessment not found  | Invalid contentId (not ASSESSMENT) |
| QE_001     | 500  | Internal server error | Unexpected error                   |


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


| Code       | HTTP | Message               | Condition          |
| ---------- | ---- | --------------------- | ------------------ |
| QE_VAL_001 | 400  | Validation failed     | Invalid fields     |
| QE_PC_008  | 404  | Assessment not found  | Invalid contentId  |
| QE_PC_009  | 404  | Question not found    | Invalid questionId |
| QE_001     | 500  | Internal server error | Unexpected error   |


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


| Code      | HTTP | Message               | Condition          |
| --------- | ---- | --------------------- | ------------------ |
| QE_PC_008 | 404  | Assessment not found  | Invalid contentId  |
| QE_PC_009 | 404  | Question not found    | Invalid questionId |
| QE_001    | 500  | Internal server error | Unexpected error   |


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


| Code   | HTTP | Message               | Condition        |
| ------ | ---- | --------------------- | ---------------- |
| QE_001 | 500  | Internal server error | Unexpected error |


---

#### 5.4.2 List Published Products

**Endpoint:** `GET /api/v1/catalogue/getProducts`

**Query Parameters:**


| Parameter  | Type   | Required | Description                       |
| ---------- | ------ | -------- | --------------------------------- |
| search     | string | No       | Search in title, shortDescription |
| categoryId | long   | No       | Filter by category                |
| difficulty | enum   | No       | BEGINNER, INTERMEDIATE, ADVANCED  |
| page       | int    | No       | Page number (default 0)           |
| size       | int    | No       | Page size (default 20)            |
| sort       | string | No       | Sort field, e.g. `title,asc`      |


#### Success Response (200 OK)

Paginated response. Each item in `content` includes:


| Field            | Type    | Description                      |
| ---------------- | ------- | -------------------------------- |
| id               | long    | Product ID                       |
| title            | string  | Product title                    |
| slug             | string  | URL-friendly slug                |
| shortDescription | string  | Short description                |
| longDescription  | string  | Full description                 |
| price            | number  | Price                            |
| discountPrice    | number  | Discounted price (nullable)      |
| thumbnailUrl     | string  | Thumbnail URL (nullable)         |
| previewVideoUrl  | string  | Preview video URL (nullable)     |
| difficultyLevel  | string  | BEGINNER, INTERMEDIATE, ADVANCED |
| durationMinutes  | number  | Duration in minutes (nullable)   |
| published        | boolean | Whether published                |
| categories       | array   | Category objects                 |
| createdAt        | string  | ISO-8601 timestamp               |
| updatedAt        | string  | ISO-8601 timestamp               |


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


| Code   | HTTP | Message               | Condition        |
| ------ | ---- | --------------------- | ---------------- |
| QE_001 | 500  | Internal server error | Unexpected error |


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


| Code      | HTTP | Message               | Condition                     |
| --------- | ---- | --------------------- | ----------------------------- |
| QE_PC_002 | 404  | Product not found     | Invalid slug or not published |
| QE_001    | 500  | Internal server error | Unexpected error              |


---

#### 5.4.4 Get Product Details by ID (Public)

**Endpoint:** `GET /api/v1/catalogue/getProductDetails`

**Query Parameters:**


| Parameter | Type | Required | Description |
| --------- | ---- | -------- | ----------- |
| productId | long | Yes      | Product ID  |


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


| Code      | HTTP | Message               | Condition                          |
| --------- | ---- | --------------------- | ---------------------------------- |
| QE_PC_002 | 404  | Product not found     | Invalid productId or not published |
| QE_001    | 500  | Internal server error | Unexpected error                   |


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


| Field     | Type | Required | Description                                                   |
| --------- | ---- | -------- | ------------------------------------------------------------- |
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


| Code        | HTTP | Message                                                                      | Condition                            |
| ----------- | ---- | ---------------------------------------------------------------------------- | ------------------------------------ |
| QE_CART_008 | 400  | Cart supports only one item. Remove the existing item before adding another. | Cart has different product (Phase 1) |


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


| Parameter | Type | Description          |
| --------- | ---- | -------------------- |
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


| Field                 | Type    | Required | Description                          |
| --------------------- | ------- | -------- | ------------------------------------ |
| items                 | array   | Yes      | Product details from BFF             |
| items[].productId     | long    | Yes      | Product ID                           |
| items[].title         | string  | Yes      | Product title                        |
| items[].price         | decimal | Yes      | Base price                           |
| items[].discountPrice | decimal | No       | Discounted price; if null, use price |
| billingGstNumber      | string  | No       | Optional; affects GST calculation    |


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
**Auth Required:** Yes (verified email required)

BFF fetches product details and billing from User Profile, then calls this. Returns Razorpay data for FE to open checkout. **Checkout is blocked for unverified users** — the JWT filter returns `403 QE_AUTH_004` if `isVerified` is false in the token.

#### Request


| Field                       | Type    | Required | Description                                       |
| --------------------------- | ------- | -------- | ------------------------------------------------- |
| items                       | array   | Yes      | Order line items with prices                      |
| items[].productId           | long    | Yes      | Product ID                                        |
| items[].price               | decimal | Yes      | Base price                                        |
| items[].discountPrice       | decimal | No       | Discount price                                    |
| items[].finalPrice          | decimal | Yes      | Final price per item                              |
| items[].gstAmount           | decimal | Yes      | GST amount per item                               |
| billing                     | object  | Yes      | Billing snapshot (BFF prefills from User Profile) |
| billing.billingName         | string  | Yes      | Billing name                                      |
| billing.billingAddressLine1 | string  | Yes      | Address line 1                                    |
| billing.billingAddressLine2 | string  | No       | Address line 2                                    |
| billing.billingCity         | string  | Yes      | City                                              |
| billing.billingState        | string  | Yes      | State                                             |
| billing.billingCountry      | string  | Yes      | Country                                           |
| billing.billingPostalCode   | string  | Yes      | Postal code                                       |
| billing.billingGstNumber    | string  | No       | GST number (optional)                             |


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


| Header               | Required | Description                     |
| -------------------- | -------- | ------------------------------- |
| Content-Type         | Yes      | application/json                |
| X-Razorpay-Signature | Yes      | HMAC signature for verification |


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

## 8. BFF Page APIs

**Base Path:** `/pages`  
**Auth Required:** Varies by endpoint. Home, courses, and course detail are public. Cart page and verify cart require JWT.

The BFF (Backend for Frontend) layer provides page-composition APIs. Each endpoint assembles data from multiple domain modules into a single FE-ready response.

---

### 8.1 Home Page

**Endpoint:** `GET /pages/home`  
**Auth Required:** No

Returns the home page structure with components (HERO_SECTION, CARD_GRID). The hero section is configured via application properties. The card grid is populated dynamically from featured published products.

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "main": {
      "type": "HOME",
      "components": [
        {
          "type": "HERO_SECTION",
          "config": {
            "theme": "dark",
            "padding": "large"
          },
          "details": {
            "title": "Accelerate Your Income.",
            "headline": {
              "line1": "Transform your career.",
              "line2": "Accelerate Your Income.",
              "highlightWord": "Income"
            },
            "subtitle": "Discover the specialized Laser Therapist career path that pays more than traditional nursing.",
            "badge": "NEW 2026 CURRICULUM",
            "image": {
              "src": "/images/hero.png",
              "alt": "Hero Image"
            },
            "stats": [
              { "id": "s1", "value": "97%", "label": "job-ready grads", "icon": "users" },
              { "id": "s2", "value": "+40%", "label": "avg. salary uplift", "icon": "trending" },
              { "id": "s3", "value": "8", "label": "week accelerator", "icon": "clock" }
            ],
            "floatingCards": [
              { "id": "f1", "title": "Accelerate", "subtitle": "+40%", "position": "bottom-right" },
              { "id": "f2", "title": "Avg. salary uplift", "subtitle": "+40%", "position": "top-left" },
              { "id": "f3", "title": "Next cohort", "subtitle": "April 2026", "position": "bottom-left" }
            ],
            "ctas": [
              { "label": "Enquire via WhatsApp", "url": "https://wa.me/1234567890", "variant": "primary", "type": "button" },
              { "label": "View All Courses", "url": "/courses", "variant": "secondary", "type": "button" }
            ]
          }
        },
        {
          "type": "CARD_GRID",
          "config": {
            "theme": "light",
            "layout": "grid-3-col"
          },
          "details": {
            "title": "Industry Leading Accelerators",
            "subtitle": "Engineered by specialists with decades of experience.",
            "badge": "OUR PATHWAYS",
            "sections": [
              {
                "id": 1,
                "title": "Advanced Medical Aesthetics",
                "description": "Advanced medical aesthetics course description.",
                "badge": "Beauty",
                "image": { "src": "/images/course1.png", "alt": "Advanced Medical Aesthetics" },
                "priceDetails": { "price": "$199.00" },
                "ctas": [
                  { "label": "Enroll Now", "url": "/course/1", "variant": "primary", "type": "button", "action": "native" }
                ]
              }
            ],
            "cta": { "label": "Browse Catalog", "url": "/courses", "variant": "link", "type": "link" }
          }
        }
      ],
      "data": {}
    }
  }
}
```

#### Sample cURL

```bash
curl -s http://localhost:8080/pages/home
```

#### Notes

- **HERO_SECTION**: Static content from application properties (`bff.home.hero.`*).
- **CARD_GRID**: Dynamic content from Product Catalogue. Sections are populated from products that are both `published = true` AND `featured = true`.
- **Enroll Now CTA**: URL format is `/course/{productId}` (PDP page route).
- To mark a product as featured: `PATCH /api/v1/admin/catalogue/setFeatured/{id}` with `{ "featured": true }`.

---

### 8.2 Courses Page

**Endpoint:** `GET /pages/courses`  
**Auth Required:** No

Returns the course catalogue page with a single `COURSE_CATALOG` component. Supports server-side pagination and optional category filtering.

#### Query Parameters


| Param        | Type | Required | Default | Description                                |
| ------------ | ---- | -------- | ------- | ------------------------------------------ |
| `categoryId` | Long | No       | —       | Filter products by category. Omit for all. |
| `page`       | int  | No       | `0`     | Zero-based page index.                     |
| `size`       | int  | No       | `12`    | Number of items per page.                  |


#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "main": {
      "type": "CATALOG",
      "components": [
        {
          "type": "COURSE_CATALOG",
          "config": {
            "theme": "brand-light-green",
            "layout": "grid-3-col"
          },
          "details": {
            "badge": "OUR CURRICULUM",
            "title": "Explore Professional Certifications",
            "subtitle": "Filter through our catalog of high-impact career programs.",
            "filters": [
              { "id": null, "value": "all", "label": "All" },
              { "id": 1, "value": "medical-aesthetics", "label": "Medical Aesthetics" },
              { "id": 2, "value": "laser-therapy", "label": "Laser Therapy" },
              { "id": 3, "value": "cosmetic-dermatology", "label": "Cosmetic Dermatology" }
            ],
            "sections": [
              {
                "id": 1,
                "title": "Advanced Medical Aesthetics",
                "description": "Advanced medical aesthetics course description.",
                "badge": "medical-aesthetics",
                "image": { "src": "/images/course1.png", "alt": "Advanced Medical Aesthetics" },
                "priceDetails": { "price": "$199.00" },
                "ctas": [
                  { "label": "Enroll Now", "url": "/course/1", "variant": "primary", "type": "button", "action": "native" }
                ]
              }
            ],
            "pagination": {
              "page": 0,
              "size": 12,
              "totalElements": 25,
              "totalPages": 3,
              "hasNext": true
            }
          }
        }
      ],
      "data": {}
    }
  }
}
```

#### Sample cURL

```bash
# All published products, first page
{
    "success": true,
    "response": {
        "main": {
            "type": "CATALOG",
            "components": [
                {
                    "type": "COURSE_CATALOG",
                    "config": {
                        "theme": "brand-light-green",
                        "layout": "grid-3-col"
                    },
                    "details": {
                        "badge": "OUR CURRICULUM",
                        "title": "Explore Professional Certifications",
                        "subtitle": "Filter through our catalog of high-impact career programs.",
                        "filters": [
                            {
                                "id": null,
                                "value": "all",
                                "label": "All"
                            },
                            {
                                "id": 1,
                                "value": "beauty",
                                "label": "Beauty"
                            },
                            {
                                "id": 2,
                                "value": "science",
                                "label": "Science"
                            },
                            {
                                "id": 3,
                                "value": "skincare",
                                "label": "Skincare"
                            },
                            {
                                "id": 4,
                                "value": "makeup",
                                "label": "Makeup"
                            },
                            {
                                "id": 5,
                                "value": "physics",
                                "label": "Physics"
                            },
                            {
                                "id": 6,
                                "value": "anti-aging",
                                "label": "Anti-Aging"
                            }
                        ],
                        "sections": [
                            {
                                "id": 1,
                                "title": "Complete Skincare Masterclass",
                                "description": "Learn professional skincare techniques from experts.",
                                "badge": "skincare",
                                "image": {
                                    "src": "https://example.com/thumb1.jpg",
                                    "alt": "Complete Skincare Masterclass"
                                },
                                "priceDetails": {
                                    "price": "$1,999.00"
                                },
                                "ctas": [
                                    {
                                        "label": "Enroll Now",
                                        "url": "/course/1",
                                        "variant": "primary",
                                        "type": "button",
                                        "action": "native"
                                    }
                                ]
                            },
                            {
                                "id": 2,
                                "title": "Quantum Physics Fundamentals",
                                "description": "Introduction to quantum mechanics and its applications.",
                                "badge": "physics",
                                "image": {
                                    "src": "https://example.com/thumb2.jpg",
                                    "alt": "Quantum Physics Fundamentals"
                                },
                                "priceDetails": {
                                    "price": "$4,999.00"
                                },
                                "ctas": [
                                    {
                                        "label": "Enroll Now",
                                        "url": "/course/2",
                                        "variant": "primary",
                                        "type": "button",
                                        "action": "native"
                                    }
                                ]
                            }
                        ],
                        "pagination": {
                            "page": 0,
                            "size": 12,
                            "totalElements": 2,
                            "totalPages": 1
                        }
                    }
                }
            ],
            "data": {}
        }
    }
}

# Filtered by category, page 2
curl -s "http://localhost:8080/pages/courses?categoryId=1&page=1&size=12"
```

#### Notes

- **Static content** (badge, title, subtitle): Configured via application properties (`bff.courses.catalog.`*).
- **filters**: Built from all active categories. The first entry is always `"All"` (`id: null`). Each filter includes the category `id` so the FE can send it back as the `categoryId` query param.
- **sections**: Paginated published products. Card `badge` uses the category **slug** (matching filter `value`) for client-side filter highlighting.
- **Enroll Now CTA**: URL format is `/course/{productId}` (PDP page route).
- **pagination**: Standard page metadata. When `categoryId` is omitted, returns all published products paginated.

---

### 8.3 Course Detail Page (PDP)

**Endpoint:** `GET /pages/course`  
**Auth Required:** No

Returns the Product Detail Page structure with five components: `COURSE_HERO_DETAILS`, `COURSE_LEARNING_OUTCOMES`, `COURSE_SYLLABUS`, `INSTRUCTOR_PROFILE`, and `COURSE_CERTIFICATION`. All data is fetched from the Product Catalogue module using the product ID.

#### Query Parameters


| Param      | Type | Required | Description        |
| ---------- | ---- | -------- | ------------------ |
| `productId` | Long | Yes      | Product (course) ID. |


#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "main": {
      "type": "COURSE_DETAILS",
      "components": [
        {
          "type": "COURSE_HERO_DETAILS",
          "details": {
            "badge": "BESTSELLER",
            "title": "Advanced Medical Aesthetics",
            "shortDescription": "Master advanced medical aesthetics techniques.",
            "image": { "src": "/images/course1.png", "alt": "Advanced Medical Aesthetics" },
            "highlights": [
              { "icon": "Clock", "label": "Duration", "value": "8 Weeks" },
              { "icon": "BookOpen", "label": "Modules", "value": "12 Modules" },
              { "icon": "Award", "label": "Certificate", "value": "Included" }
            ],
            "priceDetails": { "price": "₹1999.00" },
            "ctas": [
              { "label": "Enroll Now", "action": "ENROLL", "variant": "primary" }
            ]
          }
        },
        {
          "type": "COURSE_LEARNING_OUTCOMES",
          "details": {
            "title": "What You'll Learn",
            "outcomes": [
              { "id": 1, "text": "Master advanced injection techniques", "checked": true },
              { "id": 2, "text": "Understand facial anatomy in depth", "checked": true }
            ]
          }
        },
        {
          "type": "COURSE_SYLLABUS",
          "details": {
            "title": "Course Curriculum",
            "modules": [
              {
                "id": 1,
                "title": "Module 1: Foundation",
                "duration": "2h 30m",
                "lessons": [
                  { "id": 1, "title": "Introduction to Aesthetics" },
                  { "id": 2, "title": "Safety Protocols" }
                ]
              }
            ]
          }
        },
        {
          "type": "INSTRUCTOR_PROFILE",
          "details": {
            "title": "Meet Your Instructor",
            "instructor": {
              "id": 1,
              "name": "Dr. Sarah Johnson",
              "role": "Lead Instructor",
              "image": { "src": "/images/instructor.png", "alt": "Dr. Sarah Johnson" },
              "bio": "Board-certified dermatologist with 15+ years of experience.",
              "credentials": [
                "MD, Dermatology — Harvard Medical School",
                "Board Certified — American Board of Dermatology"
              ]
            }
          }
        },
        {
          "type": "COURSE_CERTIFICATION",
          "details": {
            "title": "Certification & Career Outcomes",
            "certificationDetails": {
              "icon": "Award",
              "title": "Professional Certification",
              "description": "Earn an industry-recognized certification upon completion.",
              "highlights": [
                "Industry-recognized credential",
                "Verified digital certificate"
              ]
            },
            "outcomesHighlights": [
              { "id": 1, "title": "Career Advancement", "description": "Average 40% salary increase" },
              { "id": 2, "title": "Job Placement", "description": "97% placement rate within 3 months" }
            ]
          }
        }
      ],
      "data": {
        "courseId": 1,
        "slug": "advanced-medical-aesthetics"
      }
    }
  }
}
```

#### Sample cURL

```bash
curl -s "http://localhost:8080/pages/course?productId=1"
```

#### Error Responses

- **400 Bad Request**: Missing `productId` query parameter.
- **404 Not Found**: Product with the given ID is not found or not published (`QE_PC_002`).

#### Notes

- **COURSE_HERO_DETAILS**: Basic product info, highlights, pricing, and CTAs.
- **COURSE_LEARNING_OUTCOMES**: Ordered list of learning outcomes. `checked` is always `true`.
- **COURSE_SYLLABUS**: Hierarchical structure — modules containing lessons. Duration is computed from lesson `durationSeconds`.
- **INSTRUCTOR_PROFILE**: Instructor data with credentials. `null` if no instructor is assigned.
- **COURSE_CERTIFICATION**: Certification details with highlights and career outcome highlights.

---

### 8.4 My Learning Page

**Endpoint:** `GET /pages/my-learning`  
**Auth Required:** Yes (JWT Bearer token)

Returns the user's enrolled courses for the My Learning dashboard. BFF fetches enrolled course IDs from Ownership module, then product details from Product Catalogue for each course.

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "badge": "MY LEARNING",
    "title": "My Learning Dashboard",
    "subtitle": "Welcome back. Continue where you left off.",
    "enrollmentCount": 1,
    "emptyState": {
      "icon": "book",
      "title": "No active enrollments",
      "message": "Your learning path is empty. Explore our catalog to find the program that fits your career goals.",
      "cta": {
        "label": "FIND A PROGRAM",
        "url": "/courses",
        "variant": "primary",
        "type": "button"
      }
    },
    "sections": [
      {
        "id": 1,
        "title": "Complete Skincare Masterclass",
        "description": "Learn professional skincare techniques from experts.",
        "badge": "Beauty",
        "image": {
          "src": "https://example.com/thumb1.jpg",
          "alt": "Complete Skincare Masterclass"
        },
        "ctas": [
          {
            "label": "CONTINUE",
            "url": "/lms/1",
            "variant": "primary",
            "type": "button"
          }
        ]
      }
    ]
  }
}
```

#### Sample cURL

```bash
curl -s http://localhost:8080/pages/my-learning \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Notes

- **Static content** (badge, title, subtitle, emptyState): Configured via `bff.my-learning.*` in application properties.
- **sections[].id**: Product ID (no prefix). Use for LMS URL.
- **sections[].badge**: Root category name (level 0). Falls back to `"Course"` if none.
- **sections[].image**: `thumbnailUrl` from product. `null` if not set.
- **sections[].ctas[].url**: Format `/lms/{productId}` for CONTINUE CTA.
- **enrollmentCount**: Number of enrolled courses. `sections` may be empty when 0.

#### Error Codes

| Code | HTTP | Description |
| ---- | ---- | ----------- |
| QE_AUTH_001 | 401 | Invalid or missing authentication token |

---

### 8.5 Cart Page

**Endpoint:** `GET /pages/cart`  
**Auth Required:** Yes (JWT Bearer token)

BFF calls Cart's getCart API, then fetches product details from Product Catalogue for each cart item. Returns cart items enriched with full product details so FE can display product info (title, price, thumbnail, description, etc.) in the cart view.

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "main": {
      "type": "CART",
      "components": [
        {
          "type": "CART",
          "config": { "theme": "light" },
          "details": {
            "items": [
              {
                "productId": 1,
                "addedAt": "2025-02-19T10:30:00Z",
                "product": {
                  "id": 1,
                  "title": "Advanced Makeup Masterclass",
                  "slug": "advanced-makeup-masterclass",
                  "shortDescription": "Master advanced techniques...",
                  "price": 999.00,
                  "discountPrice": 799.00,
                  "thumbnailUrl": "/images/course1.png",
                  "difficultyLevel": "INTERMEDIATE",
                  "durationMinutes": 480
                }
              }
            ]
          }
        }
      ],
      "data": {}
    }
  }
}
```

#### Sample cURL

```bash
curl -s http://localhost:8080/pages/cart \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Notes

- Empty cart returns `items: []`.
- Product details include fields FE may use for cart display: title, slug, shortDescription, price, discountPrice, thumbnailUrl, difficultyLevel, durationMinutes.

---

### 8.6 Verify Cart Page

**Endpoint:** `POST /pages/verify-cart`  
**Auth Required:** Yes (JWT Bearer token)

FE sends cart items in the request body. BFF fetches billing info from User Profile, enriches items with product details from Product Catalogue, and calls Cart verify. Cart module validates that the items sent by FE actually exist in the user's cart at backend.

- **Billing available:** `billingDetails.isAvailable: true`, `billing` object with pre-filled data. FE sends this back as-is in checkout.
- **Billing not available:** `billingDetails.isAvailable: false`, `section.fields` with form definitions. FE collects billing from user and sends in checkout with `billingInfoPresent: false`.

#### Request

| Field | Type | Required | Description |
| ----- | ---- | -------- | ----------- |
| items | array | Yes | Cart items from FE |
| items[].productId | long | Yes | Product ID |
| items[].price | decimal | Yes | Final product price FE has shown to the user. BFF validates this matches the actual product price in BE. |

#### Request Body

```json
{
  "items": [
    { "productId": 1, "price": 799.00 },
    { "productId": 2, "price": 999.00 }
  ]
}
```

#### Price Validation

BFF fetches product details from Product Catalogue and computes the expected final price (`discountPrice` if present and > 0, else `price`). If FE's `price` does not match, BFF returns `400` with `QE_BFF_001` (Product price does not match. Please refresh and try again.).

#### Success Response (200 OK)

```json
{
  "success": true,
  "response": {
    "main": {
      "type": "VERIFY_CART",
      "components": [
        {
          "type": "VERIFY_CART",
          "config": { "theme": "light" },
          "details": {
            "badge": "CHECKOUT",
            "billingDetails": {
              "isAvailable": false,
              "title": "Billing details",
              "section": {
                "fields": [
                  { "id": "billingName", "label": "Full name", "type": "text", "placeholder": "Jane Doe", "required": true },
                  { "id": "billingAddressLine1", "label": "Address line 1", "type": "text", "placeholder": "123 Main Street", "required": true },
                  { "id": "billingAddressLine2", "label": "Address line 2 (optional)", "type": "text", "placeholder": "Apt 4B", "required": false },
                  { "id": "billingCity", "label": "City", "type": "text", "placeholder": "Mumbai", "required": true },
                  { "id": "billingState", "label": "State", "type": "text", "placeholder": "Maharashtra", "required": true },
                  { "id": "billingCountry", "label": "Country", "type": "text", "placeholder": "India", "required": true },
                  { "id": "billingPostalCode", "label": "Postal code", "type": "text", "placeholder": "400001", "required": true },
                  { "id": "billingGstNumber", "label": "GST number (optional)", "type": "text", "placeholder": "22AAAAA0000A1Z5", "required": false }
                ],
                "submitLabel": "Save changes"
              }
            },
            "emailVerification": {
              "isVerified": false,
              "verificationMessage": "Please verify your email to continue.",
              "verificationButtonText": "Verify Email",
              "verificationButtonLink": "/verify-email"
            },
            "title": "Checkout",
            "subtitle": "Review your courses and continue to payment.",
            "courseListTitle": "Course List",
            "orderSummaryTitle": "Order Summary",
            "subtotalLabel": "Subtotal",
            "taxesLabel": "Taxes (if applicable)",
            "totalLabel": "Final Payable Total",
            "payNowLabel": "Pay now",
            "emptyStateMessage": "No courses in your cart yet.",
            "loadingMessage": "Loading checkout details...",
            "errorMessage": "Unable to load checkout details. Please try again.",
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
            "subtotal": "INR 799",
            "taxes": "INR 144",
            "total": "INR 943"
          }
        }
      ],
      "data": {}
    }
  }
}
```

When `billingDetails.isAvailable` is `true`, `billingDetails.billing` is present with `billingName`, `billingAddressLine1`, etc. FE sends this object back in BFF checkout request.

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/pages/verify-cart \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "items": [
      { "productId": 1, "price": 799.00 },
      { "productId": 2, "price": 999.00 }
    ]
  }'
```

#### Notes

- Cart module validates that each `productId` exists in the user's cart. Items not in cart are ignored; if none match, Cart returns `CART_EMPTY` (400).

---

### 8.6 BFF Checkout

**Endpoint:** `POST /api/v1/bff/checkout`  
**Auth Required:** Yes (verified email required)

BFF orchestrates checkout. If `billingInfoPresent` is `false`, BFF saves billing to User Profile first, then calls Cart checkout. If `billingInfoPresent` is `true`, BFF uses billing from request (FE sends back what verify returned) and proceeds directly to Cart checkout.

#### Request

| Field | Type | Required | Description |
| ----- | ---- | -------- | ----------- |
| billingInfoPresent | boolean | Yes | `true` if billing was in verify response; `false` if FE collected it |
| items | array | Yes | Order line items (productId, price, discountPrice, finalPrice, gstAmount) |
| billing | object | Yes | Billing snapshot (from verify when present, or from FE form when not) |

#### Request Body

```json
{
  "billingInfoPresent": false,
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

Same as Cart checkout: `razorpayOrderId`, `amount`, `currency`, `keyId`, `orderIds`.

#### Sample cURL

```bash
curl -s -X POST http://localhost:8080/api/v1/bff/checkout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "billingInfoPresent": false,
    "items": [...],
    "billing": {...}
  }'
```

---

### 8.7 LMS Player

**Base Path:** `/lms`  
**Auth Required:** Yes

#### GET /lms/player

Returns course structure and progress for the LMS player. User must own the course.

**Query Parameters**

| Parameter | Type | Required | Description |
| --------- | ---- | -------- | ----------- |
| productId | long | Yes | Product (course) ID |

**Success Response (200 OK)**

```json
{
  "success": true,
  "response": {
    "courseSlug": "advanced-medical-aesthetics",
    "courseTitle": "Advanced Medical Aesthetics",
    "subtitle": "Continue from where you left off with guided modules and practical lessons.",
    "instructor": "Dr. Alex Carter",
    "completedLessons": 3,
    "totalLessons": 8,
    "progressPercentage": 38,
    "modules": [
      {
        "id": 1,
        "title": "Foundations",
        "isLocked": false,
        "lessons": [
          {
            "id": 1,
            "title": "Skin Anatomy Basics",
            "description": "Understand skin layers, structures, and clinical relevance.",
            "durationMinutes": 14,
            "status": "completed",
            "videoUrl": "https://example.com/video.mp4",
            "pdfUrl": null,
            "moduleType": "LESSON",
            "type": "VIDEO",
            "assessment": null
          },
          {
            "id": 5,
            "title": "Module 1 Quiz",
            "description": "Module 1 Quiz",
            "durationMinutes": 5,
            "status": "not_started",
            "videoUrl": null,
            "pdfUrl": null,
            "moduleType": "QUIZ",
            "type": null,
            "assessment": {
              "passPercentage": 70,
              "questions": [
                {
                  "id": 1,
                  "questionText": "What is the first step in a skincare routine?",
                  "options": [
                    { "id": 1, "optionText": "Cleansing" },
                    { "id": 2, "optionText": "Moisturizing" },
                    { "id": 3, "optionText": "Sunscreen" }
                  ]
                }
              ]
            }
          }
        ]
      }
    ]
  }
}
```

**Lesson id:** Product content ID (numeric). Use this value as `contentId` in `POST /lms/lessons/{contentId}/complete` and `POST /lms/lessons/{contentId}/validate-quiz`.  
**Lesson status:** `completed`, `in_progress`, `not_started`, `locked`  
**Module isLocked:** `true` when previous module has incomplete lessons  
**moduleType:** `LESSON` or `QUIZ` (assessment)  
**type:** `VIDEO` or `PDF` for lessons; `null` for quiz  
**videoUrl / pdfUrl:** For VIDEO lessons use `videoUrl`; for PDF lessons use `pdfUrl`. Quiz has both null.  
**assessment:** For QUIZ lessons only. Contains `passPercentage` and `questions` (each with `id`, `questionText`, `options`). Options have `id` and `optionText` only (no correct answer exposed). FE displays quiz, collects answers, then calls `POST /lms/lessons/{contentId}/validate-quiz` before allowing complete.

**Error Codes**

| Code | HTTP | Description |
| ---- | ---- | ----------- |
| QE_PC_002 | 404 | Product not found |
| QE_LMS_001 | 403 | Course access denied (user does not own course) |

#### POST /lms/lessons/{contentId}/validate-quiz

Validates quiz answers for an assessment (QUIZ) lesson. Returns pass/fail and per-question correctness. If passed, the result is stored; FE must call this before `POST /lms/lessons/{contentId}/complete` for quiz lessons. User must own the course.

**Path Parameters**

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| contentId | long | Product content ID (quiz lesson id from player response) |

**Request Body**

```json
{
  "answers": [
    { "questionId": 1, "optionId": 2 },
    { "questionId": 2, "optionId": 5 }
  ]
}
```

| Field | Type | Required | Description |
| ----- | ---- | -------- | ----------- |
| answers | array | Yes | User's selected option per question |
| answers[].questionId | long | Yes | Assessment question ID |
| answers[].optionId | long | Yes | Selected assessment option ID |

**Success Response (200 OK)**

```json
{
  "success": true,
  "response": {
    "passed": true,
    "scorePercentage": 75,
    "passPercentage": 70,
    "results": [
      { "questionId": 1, "correct": true },
      { "questionId": 2, "correct": false }
    ]
  }
}
```

**Flow:** FE shows quiz from `assessment` in player response → user selects options → FE calls this API → if `passed` is true, FE shows success and enables "Continue" → user clicks Continue → FE calls `POST /lms/lessons/{contentId}/complete`.

**Error Codes**

| Code | HTTP | Description |
| ---- | ---- | ----------- |
| QE_LMS_001 | 403 | Course access denied |
| QE_LMS_002 | 404 | Content not found or not an assessment |

#### POST /lms/lessons/{contentId}/complete

Marks a lesson or quiz as complete for the authenticated user. User must own the course containing the content.

**For QUIZ lessons:** User must have passed the quiz first (via `POST /lms/lessons/{contentId}/validate-quiz`). If not passed, returns `QE_LMS_003`.

**Path Parameters**

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| contentId | long | Product content ID — same as the `id` field in each lesson/quiz object in the player response |

**Success Response (200 OK)**

```json
{
  "success": true,
  "response": null
}
```

**Error Codes**

| Code | HTTP | Description |
| ---- | ---- | ----------- |
| QE_LMS_001 | 403 | Course access denied |
| QE_LMS_002 | 404 | Lesson not found |
| QE_LMS_003 | 400 | Quiz not passed (for QUIZ lessons; must call validate-quiz first and pass) |

**Sample cURL**

```bash
# Get player data
curl -s "http://localhost:8080/lms/player?productId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Validate quiz answers (for QUIZ lessons)
curl -s -X POST http://localhost:8080/lms/lessons/5/validate-quiz \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"answers":[{"questionId":1,"optionId":2},{"questionId":2,"optionId":5}]}'

# Mark lesson complete (for lessons; for quiz, must pass validate-quiz first)
curl -s -X POST http://localhost:8080/lms/lessons/1/complete \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 8.8 Admin PDP Data Endpoints

These admin endpoints manage the PDP-specific data for products. Highlights, learning outcomes, instructor info, certification, and outcome highlights are stored in a single `attributes` JSON column on the product table.

#### 8.8.1 Product Module CRUD

**Base Path:** `/api/v1/admin/catalogue/products/{productId}`

##### POST /createModule

```bash
curl -s -X POST http://localhost:8080/api/v1/admin/catalogue/products/1/createModule \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{ "title": "Module 1: Foundation", "orderIndex": 0 }'
```

##### PUT /updateModule/{moduleId}

##### DELETE /deleteModule/{moduleId}

##### PUT /reorderModules

Request body: `[3, 1, 2]` (array of module IDs in desired order)

##### GET /getModules

Returns ordered list of modules for the product.

#### 8.8.2 Product PDP Data Endpoints

**Base Path:** `/api/v1/admin/catalogue`

##### PUT /setAttributes/{id}

Sets the full PDP attributes JSON for a product. Replaces the entire `attributes` column. All PDP display data (badge, highlights, learning outcomes, instructor, certification, outcome highlights) is stored here.

```bash
curl -s -X PUT http://localhost:8080/api/v1/admin/catalogue/setAttributes/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "badge": "BESTSELLER",
    "highlights": [
      { "icon": "Clock", "label": "Duration", "value": "8 Weeks" },
      { "icon": "BookOpen", "label": "Modules", "value": "12 Modules" }
    ],
    "learningOutcomes": [
      "Master advanced injection techniques",
      "Understand facial anatomy in depth"
    ],
    "instructor": {
      "name": "Dr. Sarah Johnson",
      "role": "Lead Instructor",
      "imageUrl": "/images/instructor.png",
      "bio": "Board-certified dermatologist with 15+ years of experience.",
      "credentials": ["MD, Dermatology — Harvard", "Board Certified — ABD"]
    },
    "certification": {
      "icon": "Award",
      "title": "Professional Certification",
      "description": "Earn an industry-recognized certification upon completion.",
      "highlights": ["Industry-recognized credential", "Verified digital certificate"]
    },
    "outcomeHighlights": [
      { "title": "Career Advancement", "description": "Average 40% salary increase" },
      { "title": "Job Placement", "description": "97% placement rate within 3 months" }
    ]
  }'
```

---

## 9. Complete Error Codes Reference

### Global


| Code       | HTTP | Message               | Module |
| ---------- | ---- | --------------------- | ------ |
| QE_001     | 500  | Internal server error | Global |
| QE_VAL_001 | 400  | Validation failed     | Global |
| QE_VAL_002 | 400  | Invalid request body  | Global |


### Auth


| Code        | HTTP | Message                                             | API(s)       |
| ----------- | ---- | --------------------------------------------------- | ------------ |
| QE_AUTH_002 | 409  | An account with this email already exists           | Signup       |
| QE_AUTH_003 | 401  | Invalid email or password                           | Login        |
| QE_AUTH_004 | 403  | Please verify your email before logging in          | Login        |
| QE_AUTH_005 | 400  | Invalid verification token                          | Verify Email |
| QE_AUTH_006 | 404  | Verification token has expired or already been used | Verify Email |


### User Management


| Code      | HTTP | Message                                   | Module    |
| --------- | ---- | ----------------------------------------- | --------- |
| QE_UM_001 | 400  | Validation failed                         | User Mgmt |
| QE_UM_002 | 409  | User profile already exists for this user | User Mgmt |
| QE_UM_003 | 404  | User profile not found                   | BFF Checkout (update billing) |


### BFF


| Code       | HTTP | Message                                                       | API(s)       |
| ---------- | ---- | ------------------------------------------------------------- | ------------ |
| QE_BFF_001 | 400  | Product price does not match. Please refresh and try again.   | Verify Cart  |


### LMS


| Code       | HTTP | Message              | API(s)           |
| ---------- | ---- | -------------------- | ---------------- |
| QE_LMS_001 | 403  | Course access denied | Player, Complete |
| QE_LMS_002 | 404  | Lesson not found     | Mark Complete    |


### Product Catalogue


| Code      | HTTP | Message                              | API(s)                 |
| --------- | ---- | ------------------------------------ | ---------------------- |
| QE_PC_001 | 404  | Category not found                   | Category CRUD          |
| QE_PC_002 | 404  | Product not found                    | Product, Content       |
| QE_PC_003 | 409  | Category slug already exists         | Create/Update Category |
| QE_PC_004 | 409  | Product slug already exists          | Create/Update Product  |
| QE_PC_005 | 400  | Circular category reference detected | Create/Update Category |
| QE_PC_006 | 404  | Product content not found            | Content CRUD, Reorder  |
| QE_PC_007 | 409  | Content order index already exists   | Add Content            |
| QE_PC_008 | 404  | Assessment not found                 | Question CRUD          |
| QE_PC_009 | 404  | Question not found                   | Update/Delete Question |
| QE_PC_010 | 400  | Validation failed                    | Product Catalogue      |
| QE_PC_012 | 404  | Product module not found             | Module CRUD            |


### Cart


| Code        | HTTP | Message                                                                      | API(s)                |
| ----------- | ---- | ---------------------------------------------------------------------------- | --------------------- |
| QE_CART_001 | 400  | Invalid request                                                              | Cart                  |
| QE_CART_003 | 409  | Product already in cart                                                      | Add to cart           |
| QE_CART_004 | 400  | Cart is empty                                                                | Verify, Checkout      |
| QE_CART_009 | 400  | Checkout items do not match cart. Please refresh and try again.              | Checkout              |
| QE_CART_008 | 400  | Cart supports only one item. Remove the existing item before adding another. | Add to cart (Phase 1) |
| QE_CART_005 | 400  | Invalid webhook signature                                                    | Webhook               |
| QE_CART_006 | 400  | All items in cart are already owned by you                                   | Verify                |
| QE_CART_007 | 400  | One or more products are already owned. Remove from cart before checkout     | Checkout              |


### Ownership


| Code       | HTTP | Message                       | API(s)           |
| ---------- | ---- | ----------------------------- | ---------------- |
| QE_OWN_001 | 409  | User already owns this course | Create ownership |


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
  "isVerified": true,
  "iat": 1710000000,
  "exp": 1710086400
}
```


| Claim      | Description                          |
| ---------- | ------------------------------------ |
| sub        | User ID (Long)                       |
| role       | `USER` or `ADMIN`                    |
| isVerified | `true` if email verified, else `false` |
| iat        | Issued at (Unix)                    |
| exp        | Expires at (Unix)                   |

**Note:** Checkout requires `isVerified: true`. Unverified users receive `403` with `QE_AUTH_004` when calling checkout.


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

