# Staging: Email Verification Flow

This guide describes how to test the full email verification flow on staging. You can use **Gmail** (real delivery) or **Mailpit** (capture only).

---

## Which Mail Service Runs on EC2?

| Mode | What runs on EC2 | Where emails go |
|------|------------------|-----------------|
| **Gmail** | Spring Boot only (no mail server) | Spring Boot → smtp.gmail.com → user's inbox |
| **Mailpit** | Spring Boot + Mailpit container | Spring Boot → Mailpit (Docker) → view at EC2:8025 |

**Gmail:** The Spring Boot app connects directly to Google's SMTP servers over the internet. No mail server runs on EC2. Gmail relays and delivers the email.

**Mailpit:** A local Docker container captures emails for testing. No real delivery.

---

## Gmail (Real Email Delivery) — Recommended for Staging

### 1. Configure deploy.env

```
APP_EMAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
EMAIL_FROM=your-email@gmail.com
```

Create an [App Password](https://myaccount.google.com/apppasswords) (requires 2FA on the Google account).

### 2. Deploy

```bash
cd quantum-edu-be
./deploy.sh
```

Deploy skips Mailpit when `MAIL_HOST` is not `mailpit`. Spring Boot connects to Gmail over the internet.

### 3. Test the Flow

1. **Sign up** at `https://quantum-education.netlify.app/register`
2. Use a **real email address** you can access
3. **Check your inbox** for the verification email
4. Click the verification link → frontend calls backend `/api/v1/auth/verify-email`
5. Log in to confirm end-to-end

---

## Mailpit (Capture Only — No Real Delivery)

Use when you want to test the flow without sending real emails.

### 1. Configure deploy.env

```
APP_EMAIL_ENABLED=true
MAIL_HOST=mailpit
MAIL_PORT=1025
# MAIL_USERNAME, MAIL_PASSWORD can be empty
```

### 2. Open Port 8025 (optional, for Web UI)

In EC2 Security Group, add inbound rule: Custom TCP, port 8025.

### 3. Deploy

```bash
./deploy.sh
```

Deploy starts Mailpit when `MAIL_HOST=mailpit`. Spring Boot sends to Mailpit on the Docker network.

### 4. Test the Flow

1. Sign up with any email
2. Open **Mailpit Web UI**: `http://YOUR_EC2_IP:8025`
3. View the captured email and click the verification link
4. Log in

---

## Gmail vs Other Options

| Service | Pros | Cons |
|---------|------|------|
| **Gmail SMTP** | Free, simple, works for staging | ~500 emails/day limit, not ideal for production |
| **AWS SES** | Same AWS account, high deliverability, pay per email, cheap | Needs setup (verify domain, etc.) |
| **SendGrid / Brevo / Mailgun** | Good free tiers, production-ready | External service |
| **Postfix on EC2** | Self-hosted | Complex, deliverability issues (spam folders) |

**Recommendation:** Use **Gmail for staging** (what you have now). For production, use **AWS SES** — integrate with your existing AWS infra, better deliverability, and low cost.

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| No emails received (Gmail) | Check `APP_EMAIL_ENABLED=true`, `MAIL_USERNAME`, `MAIL_PASSWORD`; check spam; `docker logs quantum-edu-be` for errors |
| Gmail "Less secure app" / Auth failed | Use App Password, not account password; enable 2FA |
| No emails in Mailpit | `MAIL_HOST=mailpit`, `MAIL_PORT=1025`; redeploy; `docker logs quantum-edu-be` |
| Can't open Mailpit :8025 | Open port 8025 in EC2 Security Group |
| Connection refused | Ensure `quantum-net` exists; both containers on same network when using Mailpit |

---

## Flow Summary

1. User signs up → AuthService creates user with verification token
2. EmailNotificationService sends verification email (Gmail or Mailpit)
3. Email contains link: `VERIFICATION_BASE_URL?token=xxx` (points to frontend)
4. User clicks link → frontend calls `POST /api/v1/auth/verify-email` with token
5. Backend verifies token, marks user verified
6. User can log in
