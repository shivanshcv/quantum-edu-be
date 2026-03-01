# Cloudflare R2 Setup Guide (Manual Upload)

This guide walks you through setting up an R2 bucket and getting public URLs for media files. You'll upload manually, copy URLs, and we'll update the DB for testing.

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
