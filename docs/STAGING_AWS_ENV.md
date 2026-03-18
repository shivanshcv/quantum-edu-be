# Staging Environment Variables for AWS (EC2 + RDS + R2)

Add these as environment variables when running the Docker container on EC2. Do not commit actual secrets.

| Variable | Required | Example / Notes |
|----------|----------|-----------------|
| `SPRING_PROFILES_ACTIVE` | Yes | `staging` |
| `MYSQL_HOST` | Yes | RDS endpoint (e.g. `xxx.ap-south-1.rds.amazonaws.com`) |
| `MYSQL_PORT` | Yes | `3306` |
| `MYSQL_DATABASE` | Yes | `quantum_education` (or your DB name) |
| `MYSQL_USER` | Yes | RDS master username |
| `MYSQL_PASSWORD` | Yes | RDS master password (Secret) |
| `JWT_SECRET` | Yes | `openssl rand -base64 32` (or strong random string) |
| `APP_CORS_ORIGINS` | Yes | `https://quantum-education.netlify.app` (your FE URL) |
| `VERIFICATION_BASE_URL` | No | `https://quantum-education.netlify.app/verify-email` |
| `RAZORPAY_KEY_ID` | No | Razorpay test key (default in staging) |
| `RAZORPAY_KEY_SECRET` | No | Razorpay test secret (Secret) |
| `RAZORPAY_WEBHOOK_SECRET` | No | From Razorpay Dashboard → Webhooks (must match) |

## R2 (Media)

No env vars. Media URLs are stored in the DB. Ensure R2 bucket has public access and CORS for your FE origin. See [R2_SETUP_GUIDE.md](./R2_SETUP_GUIDE.md).

## RDS Connection Issues

If you get "Public Key Retrieval is not allowed", override the datasource URL:

```bash
-e SPRING_DATASOURCE_URL="jdbc:mysql://<RDS_HOST>:3306/<DB>?serverTimezone=%2B05:30&useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true"
```

Then you can omit `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE` (or they may be ignored when full URL is set).
