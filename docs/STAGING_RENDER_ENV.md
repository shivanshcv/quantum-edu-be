# Staging Environment Variables for Render

Add these in **Render Dashboard** → Your Service → **Environment** tab.

**Note:** Do not commit actual secrets. Use this as a reference when configuring Render.

| Variable | Value |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `staging` |
| `MYSQL_HOST` | `mysql-6893282-shivansh-6f29.j.aivencloud.com` |
| `MYSQL_PORT` | `25679` |
| `MYSQL_DATABASE` | `defaultdb` |
| `MYSQL_USER` | `avnadmin` |
| `MYSQL_PASSWORD` | *(from Aiven – set as Secret in Render)* |
| `JWT_SECRET` | `quantum-edu-jwt-secret-key-min-256-bits-for-hs256-algorithm` |
| `APP_CORS_ORIGINS` | `https://quantum-education.netlify.app` |
| `VERIFICATION_BASE_URL` | `https://quantum-education.netlify.app/verify-email` |
| `RAZORPAY_KEY_ID` | `rzp_test_SLepofnigNDLcv` |
| `RAZORPAY_KEY_SECRET` | *(same as dev – set as Secret)* |
| `RAZORPAY_WEBHOOK_SECRET` | *(same as dev)* |
| `EMAIL_FROM` | `noreply@quantumedu.com` |
| `JWT_DEV_BYPASS` | `true` |

## CORS

Use `https://quantum-education.netlify.app` (no trailing slash). CORS origins typically omit the trailing slash.

## Aiven SSL

Aiven requires SSL. The staging config already includes `useSSL=true&requireSSL=true` in the datasource URL.
