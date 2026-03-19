# Cloudflare R2 Setup Guide

This guide covers R2 bucket setup, public access, and API tokens for programmatic uploads (e.g. Django admin).

---

## Create R2 API Token (for Django Admin Upload)

Use this when you need to upload files from Django admin to R2.

### Step 1: Open R2 API Tokens

1. Go to [Cloudflare Dashboard](https://dash.cloudflare.com) → log in.
2. In the left sidebar, click **R2 Object Storage**.
3. Click **Manage R2 API Tokens** (top right, or under Overview).

### Step 2: Create API Token

1. Click **Create API token**.
2. **Token name**: e.g. `quantum-edu-django-admin`.
3. **Permissions**: Select **Object Read & Write**.
4. **Specify bucket(s)**: Optional. Leave "Apply to all buckets" or restrict to `quantum-edu-media`.
5. **TTL**: Optional. Leave blank for no expiry.
6. Click **Create API Token**.

### Step 3: Save Credentials

You will see:

- **Access Key ID** (e.g. `a1b2c3d4e5f6...` — long alphanumeric string)
- **Secret Access Key** (e.g. `xyz123...` — another long string)

**Important:** Copy both values immediately. The Secret Access Key is shown only once and cannot be retrieved later.

**Note:** This is different from a Cloudflare API token (which starts with `cfut_`). R2 S3 API requires the Access Key ID and Secret Access Key from **Manage R2 API Tokens**.

### Step 4: Get Your Account ID

1. In Cloudflare Dashboard, go to **R2** → **Overview**.
2. Your **Account ID** is shown in the right sidebar (or in the URL: `dash.cloudflare.com/<ACCOUNT_ID>/r2`).

### Step 5: Configure Django (quantum-edu-admin)

Add to `.env`:

```
R2_ACCOUNT_ID=your_account_id
R2_ACCESS_KEY_ID=your_access_key_id
R2_SECRET_ACCESS_KEY=your_secret_access_key
R2_BUCKET_NAME=quantum-edu-media
R2_PUBLIC_URL=https://pub-96a817cca5de440db5e3364bbb57f3ed.r2.dev
```

R2 S3 endpoint: `https://<ACCOUNT_ID>.r2.cloudflarestorage.com`

---

## Step 1: Create Cloudflare Account (if needed)

1. Go to [cloudflare.com](https://cloudflare.com) and sign up / log in.
2. R2 is available on the free plan.

---

## Step 2: Create R2 Bucket

1. In Cloudflare Dashboard: **R2 Object Storage** → **Overview**.
2. Click **Create bucket**.
3. **Bucket name**: e.g. `quantum-edu-media`.
4. **Location**: Choose a region (e.g. `WNAM` or `APAC`).
5. Click **Create bucket**.

---

## Step 3: Enable Public Access

R2 buckets are private by default. To get public URLs:

1. Open your bucket → **Settings** tab.
2. Under **Public access**, click **Allow Access**.
3. Choose **R2.dev subdomain** (simplest for testing):
   - You get a URL like: `https://pub-xxxxx.r2.dev`
   - All objects become publicly accessible at `https://pub-xxxxx.r2.dev/<object-key>`
4. Or use **Custom domain** if you have one (e.g. `media.yourdomain.com`).

---

## Step 4: Upload Files

1. Open your bucket → **Objects** tab.
2. Click **Upload**.
3. Upload your files.

**Suggested folder structure** (for consistency):

```
products/{productId}/
  thumbnail.jpg
  preview-video.mp4
lessons/{lessonId}/
  video.mp4
  handout.pdf
```

Or simpler for testing:

```
thumbnails/product-1.jpg
previews/product-1.mp4
lessons/lesson-1.mp4
lessons/lesson-1.pdf
```

---

## Step 5: Get Public URLs

After upload:

1. Click the object name in the bucket.
2. Copy the **Public URL** (or construct it).

**R2.dev subdomain format:**
```
https://pub-<bucket-id>.r2.dev/<object-key>
```

Example:
- Bucket public URL: `https://pub-abc123xyz.r2.dev`
- Object key: `products/1/thumbnail.jpg`
- Full URL: `https://pub-abc123xyz.r2.dev/products/1/thumbnail.jpg`

---

## Step 6: Share URLs for DB Update

Once you have URLs, share them in this format so we can update the DB:

| Field | Product/Course ID | URL |
|-------|-------------------|-----|
| `thumbnail_url` | 1 | `https://...` |
| `preview_video_url` | 1 | `https://...` |
| `lesson.video_url` | lesson content id | `https://...` |
| `lesson.pdf_url` | lesson content id | `https://...` |

**DB columns we can update for a course:**

- **product** table: `thumbnail_url`, `preview_video_url`
- **lesson** table: `video_url`, `pdf_url` (linked via `product_content_id`)

---

## Quick Reference

| Action | Where |
|--------|-------|
| Create bucket | R2 → Create bucket |
| Enable public access | Bucket → Settings → Public access → Allow |
| Upload files | Bucket → Objects → Upload |
| Get URL | Click object → Copy Public URL |

---

## Troubleshooting

- **403 Forbidden**: Public access may not be enabled. Re-check bucket Settings.
- **404 Not Found**: Verify object key and that the file was uploaded.
- **CORS (if FE loads media directly)**: Bucket → Settings → CORS policy → Add your frontend origin.
