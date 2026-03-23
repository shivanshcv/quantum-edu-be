# Netlify Frontend ↔ AWS Backend Connection Guide

This guide explains how to connect your **Netlify-hosted frontend** (quantum_edu_fe) with your **AWS EC2 backend** (quantum-edu-be). Follow every step in order.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Prerequisites](#2-prerequisites)
3. [Step 1: Ensure Backend Is Reachable](#3-step-1-ensure-backend-is-reachable)
4. [Step 2: Open EC2 Port 8080](#4-step-2-open-ec2-port-8080)
5. [Step 3: Configure Backend CORS](#5-step-3-configure-backend-cors)
6. [Step 4: Set Netlify Environment Variable](#6-step-4-set-netlify-environment-variable)
7. [Step 5: Redeploy Netlify](#7-step-5-redeploy-netlify)
8. [Step 6: Verify Connection](#8-step-6-verify-connection)
9. [Step 7: Razorpay Webhook (If Using Payments)](#9-step-7-razorpay-webhook-if-using-payments)
10. [Step 8: R2 CORS (If Frontend Loads Media)](#10-step-8-r2-cors-if-frontend-loads-media)
11. [Troubleshooting](#11-troubleshooting)
12. [Quick Reference](#12-quick-reference)

---

## 1. Architecture Overview

```
┌─────────────────┐                    ┌─────────────────┐
│   User Browser  │                    │   AWS EC2      │
│                 │                    │                 │
│  1. Loads FE    │◄───────────────────│  Backend        │
│     from        │   (Netlify CDN)    │  (Docker)       │
│     Netlify     │                    │  Port 8080      │
│                 │                    │                 │
│  2. API calls   │───────────────────►│  Spring Boot    │
│     (fetch)     │   (Direct)         │  + RDS MySQL    │
│                 │                    │                 │
└─────────────────┘                    └─────────────────┘
```

- **Frontend:** Hosted on Netlify (e.g. `https://quantum-education.netlify.app`)
- **Backend:** Running in Docker on EC2 (port 8080)
- **Connection:** The user's browser loads the FE from Netlify, then makes API calls directly to the BE on EC2. CORS must allow the Netlify origin.

---

## 2. Prerequisites

Before starting, ensure:

| Requirement | How to Verify |
|-------------|---------------|
| Backend deployed on EC2 | SSH to EC2, run `sudo docker ps` — `quantum-edu-be` should be running |
| RDS database set up | Backend can connect; check `docker logs quantum-edu-be` |
| Netlify site deployed | Open your Netlify URL in a browser — FE loads |
| EC2 public IP or domain | Note it for configuration |

---

## 3. Step 1: Ensure Backend Is Reachable

Your backend must be reachable from the internet. You have two options:

### Option A: Using EC2 Public IP (Simpler, for testing)

- **Backend URL:** `http://<EC2_PUBLIC_IP>:8080`
- Example: `http://52.66.209.243:8080`
- **Requirement:** EC2 Security Group must allow inbound traffic on port 8080 (see Step 2).

### Option B: Using a Custom Domain (Recommended for production)

- **Backend URL:** `https://api.yourdomain.com`
- Requires:
  1. GoDaddy (or other) DNS: A record `api` → EC2 Elastic IP
  2. Nginx on EC2: proxy `api.yourdomain.com` → `localhost:8080`
  3. HTTPS: Certbot/Let's Encrypt for `api.yourdomain.com`

See [FE_AWS_DEPLOYMENT_GUIDE.md](./FE_AWS_DEPLOYMENT_GUIDE.md) for domain setup.

**For this guide, we'll use Option A (EC2 IP) unless you already have a domain configured.**

---

## 4. Step 2: Open EC2 Port 8080

The backend listens on port 8080. The EC2 Security Group must allow inbound traffic on that port.

### 4.1 Find Your Security Group

1. **AWS Console** → **EC2** → **Instances**
2. Select your EC2 instance
3. In the **Details** tab, note the **Security group** (e.g. `sg-0abc123def456`)

### 4.2 Add Inbound Rule for Port 8080

1. **EC2** → **Security Groups** → click your instance's security group
2. **Inbound rules** tab → **Edit inbound rules**
3. **Add rule:**
   - **Type:** Custom TCP
   - **Port range:** 8080
   - **Source:** `0.0.0.0/0` (anywhere) — or restrict to your IP for testing
   - **Description:** Backend API (optional)
4. **Save rules**

### 4.3 Verify Backend Is Reachable

From your Mac (replace with your EC2 IP):

```bash
curl http://52.66.209.243:8080/health
```

Expected: JSON response (e.g. `{"status":"UP"}`). If you get "Connection refused" or timeout, the Security Group or Docker container is misconfigured.

---

## 5. Step 3: Configure Backend CORS

The backend uses `APP_CORS_ORIGINS` to allow requests from your frontend. Without it, the browser will block API calls with CORS errors.

### 5.1 Get Your Netlify URL

- Default: `https://<site-name>.netlify.app`
- Example: `https://quantum-education.netlify.app`
- **Important:** No trailing slash. Use `https://` (not `http://`).

### 5.2 Restart Backend Container with CORS

SSH to EC2 and run (replace placeholders with your values):

```bash
# SSH to EC2
ssh -i ~/Downloads/cli-admin.pem ubuntu@52.66.209.243

# Set variables
export ECR_URI="632127306445.dkr.ecr.ap-south-1.amazonaws.com/quantum-edu-be-staging"
export RDS_ENDPOINT="<your-rds>.ap-south-1.rds.amazonaws.com"
export RDS_DB="quantum_education"
export RDS_USER="admin"
export RDS_PASS="<your-rds-password>"
export JWT_SECRET="<your-jwt-secret>"
export NETLIFY_URL="https://quantum-education.netlify.app"   # Your actual Netlify URL

# Stop and remove old container
sudo docker stop quantum-edu-be
sudo docker rm quantum-edu-be

# Login to ECR (if needed)
aws ecr get-login-password --region ap-south-1 | sudo docker login --username AWS --password-stdin 632127306445.dkr.ecr.ap-south-1.amazonaws.com

# Pull latest (optional, if you've pushed new image)
sudo docker pull ${ECR_URI}:latest

# Run with CORS and verification URL
sudo docker run -d \
  --name quantum-edu-be \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=staging \
  -e MYSQL_HOST=$RDS_ENDPOINT \
  -e MYSQL_PORT=3306 \
  -e MYSQL_DATABASE=$RDS_DB \
  -e MYSQL_USER=$RDS_USER \
  -e MYSQL_PASSWORD=$RDS_PASS \
  -e JWT_SECRET=$JWT_SECRET \
  -e APP_CORS_ORIGINS="$NETLIFY_URL" \
  -e VERIFICATION_BASE_URL="$NETLIFY_URL/verify-email" \
  -e RAZORPAY_KEY_ID=rzp_test_SLepofnigNDLcv \
  -e RAZORPAY_KEY_SECRET=QfBuE7002SgcCcF2Jv0jYn1Y \
  -e RAZORPAY_WEBHOOK_SECRET=shivansh1234 \
  ${ECR_URI}:latest

# Verify
sudo docker ps
sudo docker logs quantum-edu-be --tail 30

# Follow logs in real time (stream as requests come in)
sudo docker logs quantum-edu-be -f
```

### 5.3 Multiple Origins (Netlify + Custom Domain)

If you use both Netlify and a custom domain for the frontend:

```bash
-e APP_CORS_ORIGINS="https://quantum-education.netlify.app,https://yourdomain.com,https://www.yourdomain.com"
```

Comma-separated, no spaces after commas (or spaces are trimmed by the backend).

### 5.4 What VERIFICATION_BASE_URL Does

Email verification links (e.g. "Verify your email") point users to your frontend. Set this to the page where the FE handles the token:

- `VERIFICATION_BASE_URL` = `https://quantum-education.netlify.app/verify-email`
- The backend appends `?token=...` when sending the email.

---

## 6. Step 4: Set Netlify Environment Variable

The frontend needs to know the backend URL. It uses `NEXT_PUBLIC_API_URL` (Next.js bakes this at build time).

### 6.1 Add Variable in Netlify

1. Go to [app.netlify.com](https://app.netlify.com)
2. Select your site (e.g. quantum-education)
3. **Site configuration** → **Environment variables** → **Add a variable** → **Add a single variable**
4. **Key:** `NEXT_PUBLIC_API_URL`
5. **Value:** Your backend URL:
   - **Using EC2 IP:** `http://52.66.209.243:8080` (replace with your EC2 IP)
   - **Using domain:** `https://api.yourdomain.com`
6. **Scopes:** Select **All scopes** (or at least "Production" and "Deploy Previews")
7. **Save**

### 6.2 Important Notes

- **No trailing slash:** Use `http://52.66.209.243:8080` not `http://52.66.209.243:8080/`
- **HTTP vs HTTPS:** With EC2 IP, use `http://`. With a domain + Certbot, use `https://`.
- **Build-time variable:** `NEXT_PUBLIC_*` is embedded during `npm run build`. You must redeploy (Step 5) for changes to take effect.

---

## 7. Step 5: Redeploy Netlify

Environment variable changes require a new build.

1. **Netlify** → your site → **Deploys** tab
2. **Trigger deploy** → **Deploy site**
3. Wait for the build to complete (usually 2–5 minutes)

Or: Push a commit to your connected Git branch — Netlify will auto-deploy.

---

## 8. Step 6: Verify Connection

### 8.1 Backend Health Check

```bash
curl http://65.0.74.204:8080/health
# Or: curl http://52.66.209.243:8080/api/v1/bff/home
```

### 8.2 Frontend Loads

Open `https://quantum-education.netlify.app` (or your Netlify URL) in a browser.

### 8.3 API Calls Work (Browser)

1. Open DevTools (F12) → **Network** tab
2. Perform an action that calls the API (e.g. load home page, login, view courses)
3. Check requests to your backend URL — they should return 200, not CORS errors

### 8.4 CORS Check

If you see in the Console:

```
Access to fetch at 'http://52.66.209.243:8080/...' from origin 'https://quantum-education.netlify.app' 
has been blocked by CORS policy
```

Then `APP_CORS_ORIGINS` does not include your Netlify URL. Re-check Step 5.3.

### 8.5 Login Flow

1. Go to `/login`
2. Enter credentials
3. Submit — should succeed and redirect
4. In Network tab, the login request should go to `NEXT_PUBLIC_API_URL/api/v1/auth/login`

---

## 9. Step 7: Razorpay Webhook (If Using Payments)

If you use Razorpay for payments, the webhook must point to your backend.

1. **Razorpay Dashboard** → [dashboard.razorpay.com](https://dashboard.razorpay.com) → **Webhooks**
2. **Add endpoint**
3. **URL:**
   - With EC2 IP: `http://52.66.209.243:8080/api/v1/cart/webhook/razorpay`
   - With domain: `https://api.yourdomain.com/api/v1/cart/webhook/razorpay`
4. **Events:** Select `payment.captured` (and any others you need)
5. **Secret:** Copy the webhook secret
6. Restart the backend container with `RAZORPAY_WEBHOOK_SECRET=<copied-secret>`

---

## 10. Step 8: R2 CORS (If Frontend Loads Media)

If your frontend loads images or videos directly from Cloudflare R2 (e.g. `pub-xxxxx.r2.dev`):

1. **Cloudflare Dashboard** → **R2** → your bucket → **Settings**
2. **CORS policy** → **Add**
3. Add your Netlify origin: `https://quantum-education.netlify.app`

---

## 11. Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| **CORS error in browser** | Backend doesn't allow Netlify origin | Set `APP_CORS_ORIGINS` to your Netlify URL (no trailing slash). Restart container. |
| **Network error / Failed to fetch** | Backend unreachable | Check EC2 Security Group allows 8080. Verify backend is running: `docker ps`. |
| **404 on API calls** | Wrong base URL or path | Ensure `NEXT_PUBLIC_API_URL` has no trailing slash. Paths like `/api/v1/auth/login` are appended by the FE. |
| **Mixed content (HTTP/HTTPS)** | FE on HTTPS, BE on HTTP | Browsers block HTTP from HTTPS pages. Use HTTPS for BE (domain + Certbot) or accept mixed content for testing. |
| **Env var not applied** | Next.js bakes at build time | Redeploy Netlify after changing `NEXT_PUBLIC_API_URL`. |
| **Login works, other calls fail** | JWT or auth issue | Check Network tab for 401/403. Verify JWT_SECRET is same across restarts. |
| **Email verification link wrong** | VERIFICATION_BASE_URL incorrect | Set to `https://quantum-education.netlify.app/verify-email` (or your FE verify page). |

---

## 12. Quick Reference

### Configuration Summary

| Component | Variable | Value |
|-----------|----------|-------|
| **Netlify** | `NEXT_PUBLIC_API_URL` | `http://<EC2_IP>:8080` or `https://api.yourdomain.com` |
| **Backend (EC2)** | `APP_CORS_ORIGINS` | `https://quantum-education.netlify.app` |
| **Backend (EC2)** | `VERIFICATION_BASE_URL` | `https://quantum-education.netlify.app/verify-email` |

### Checklist

- [ ] EC2 Security Group allows inbound port 8080
- [ ] Backend container running (`docker ps`)
- [ ] `APP_CORS_ORIGINS` includes Netlify URL
- [ ] `VERIFICATION_BASE_URL` points to Netlify verify-email page
- [ ] Netlify env var `NEXT_PUBLIC_API_URL` set to backend URL
- [ ] Netlify redeployed after env var change
- [ ] No CORS errors in browser console
- [ ] Login and API calls succeed

### Related Docs

- [AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md) — Backend deployment
- [FE_AWS_DEPLOYMENT_GUIDE.md](./FE_AWS_DEPLOYMENT_GUIDE.md) — Frontend on EC2 + domain
- [STAGING_AWS_ENV.md](./STAGING_AWS_ENV.md) — Backend environment variables
