# Frontend AWS Deployment Guide — Next.js on EC2 + GoDaddy Domain

This guide walks you through deploying the **quantum_edu_fe** Next.js frontend on AWS, hosting it on a GoDaddy domain, and ensuring it connects to your backend without issues.

---

## Architecture Overview

| Component | Where | URL |
|-----------|-------|-----|
| **Frontend** (Next.js) | Same EC2 as BE, port 3000 | `https://yourdomain.com`, `https://www.yourdomain.com` |
| **Backend** (Spring Boot) | EC2, port 8080 (Docker) | `https://api.yourdomain.com` |
| **Nginx** | EC2, ports 80/443 | Reverse proxy for FE + BE |
| **Domain** | GoDaddy | A records → EC2 Elastic IP |

**Important:** Attach an **Elastic IP** to your EC2 instance before configuring DNS. EC2 public IPs change on restart; Elastic IPs are stable.

---

## Prerequisites

- [ ] Backend already deployed on EC2 (Docker on port 8080)
- [ ] GoDaddy domain purchased (e.g. `quantumeducation.com`)
- [ ] EC2 Elastic IP attached
- [ ] EC2 Security Group allows: 22 (SSH), 80 (HTTP), 443 (HTTPS)

---

## Part 1: Prepare EC2 for Frontend

### Step 1.1: Install Node.js (via NVM)

SSH into your EC2 and run:

```bash
# Install NVM
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
source ~/.bashrc   # or source ~/.nvm/nvm.sh

# Install Node.js LTS
nvm install --lts
nvm use --lts
node -v   # Should show v20.x or v22.x
```

### Step 1.2: Install PM2 (Process Manager)

```bash
npm install -g pm2
pm2 -v
```

### Step 1.3: Install Nginx

```bash
sudo apt update
sudo apt install -y nginx
sudo systemctl enable nginx
```

---

## Part 2: Deploy Frontend on EC2

### Step 2.1: Clone or Upload Frontend Code

**Option A: Git clone** (if repo is public or you use deploy keys)

```bash
cd /home/ubuntu
git clone https://github.com/YOUR_ORG/quantum_edu_fe.git
cd quantum_edu_fe
```

**Option B: SCP upload** (from your Mac)

```bash
# From your Mac, in quantum_edu_fe directory
cd /Users/shivanshkumar/Documents/repos/quantum_edu_fe
scp -i ~/Downloads/cli-admin.pem -r . ubuntu@<EC2_PUBLIC_IP>:~/quantum_edu_fe/
```

Then on EC2:

```bash
cd ~/quantum_edu_fe
```

### Step 2.2: Set Environment Variables and Build

Replace `yourdomain.com` and `api.yourdomain.com` with your actual domain.

```bash
cd ~/quantum_edu_fe

# Set API URL (must be set BEFORE build - Next.js bakes NEXT_PUBLIC_* at build time)
export NEXT_PUBLIC_API_URL="https://api.yourdomain.com"

# Install dependencies and build
npm ci
npm run build
```

### Step 2.3: Run with PM2

```bash
# Start Next.js on port 3000
pm2 start npm --name "quantum-edu-fe" -- start

# Save process list so it survives reboot
pm2 save
pm2 startup
# Run the command it outputs (e.g. sudo env PATH=... pm2 startup systemd -u ubuntu --hp /home/ubuntu)
```

Verify:

```bash
pm2 status
curl -s http://localhost:3000 | head -20
```

---

## Part 3: Configure Nginx (Reverse Proxy)

### Step 3.1: Create Nginx Config

Create `/etc/nginx/sites-available/quantum-edu`:

```bash
sudo nano /etc/nginx/sites-available/quantum-edu
```

Paste (replace `yourdomain.com` with your domain):

```nginx
# Frontend - yourdomain.com and www
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}

# Backend API - api.yourdomain.com
server {
    listen 80;
    server_name api.yourdomain.com;
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Step 3.2: Enable Site and Test

```bash
sudo ln -sf /etc/nginx/sites-available/quantum-edu /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default   # Remove default site if it conflicts
sudo nginx -t
sudo systemctl reload nginx
```

---

## Part 4: Configure GoDaddy DNS

### Step 4.1: Get Your EC2 Elastic IP

In AWS Console: **EC2** → **Elastic IPs** → note the IP attached to your instance (e.g. `52.66.209.243`).

If you don't have an Elastic IP:

1. **EC2** → **Elastic IPs** → **Allocate Elastic IP address**
2. **Actions** → **Associate Elastic IP address** → select your instance

### Step 4.2: Add DNS Records in GoDaddy

1. Log in to [GoDaddy](https://www.godaddy.com) → **My Products**
2. Click **DNS** (or **Manage DNS**) for your domain
3. Add/Edit records:

| Type | Name | Value | TTL |
|------|------|-------|-----|
| **A** | `@` | `<YOUR_ELASTIC_IP>` | 600 |
| **A** | `www` | `<YOUR_ELASTIC_IP>` | 600 |
| **A** | `api` | `<YOUR_ELASTIC_IP>` | 600 |

Example: If Elastic IP is `52.66.209.243`:
- `@` → `52.66.209.243` (root: yourdomain.com)
- `www` → `52.66.209.243` (www.yourdomain.com)
- `api` → `52.66.209.243` (api.yourdomain.com)

4. Save. DNS propagation: 15 min to 48 hours (usually ~15–30 min).

### Step 4.3: Verify DNS

```bash
# From your Mac
dig yourdomain.com +short
dig www.yourdomain.com +short
dig api.yourdomain.com +short
# All should return your Elastic IP
```

---

## Part 5: Enable HTTPS (Let's Encrypt)

### Step 5.1: Install Certbot

```bash
sudo apt install -y certbot python3-certbot-nginx
```

### Step 5.2: Obtain Certificates

**Important:** DNS must be propagated first. `yourdomain.com` and `api.yourdomain.com` must resolve to your EC2 IP.

```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com -d api.yourdomain.com
```

Follow prompts:
- Enter email for renewal notices
- Agree to terms
- Choose whether to redirect HTTP → HTTPS (recommended: Yes)

Certbot will auto-update your Nginx config and add SSL.

### Step 5.3: Test Auto-Renewal

```bash
sudo certbot renew --dry-run
```

---

## Part 6: Update Backend for New Domain

Your backend Docker container was started with `APP_CORS_ORIGINS` and `VERIFICATION_BASE_URL` pointing to Netlify. Update them for your new domain.

### Step 6.1: Stop and Recreate Backend Container

```bash
# Set variables (same as before, but with new domain)
export ECR_URI="632127306445.dkr.ecr.ap-south-1.amazonaws.com/quantum-edu-be-staging"
export RDS_ENDPOINT="<your-rds>.ap-south-1.rds.amazonaws.com"
export RDS_DB="quantum_education"
export RDS_USER="admin"
export RDS_PASS="<your-password>"
export JWT_SECRET="<your-jwt-secret>"

# Stop and remove old container
sudo docker stop quantum-edu-be
sudo docker rm quantum-edu-be

# Run with NEW CORS and verification URL for your domain
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
  -e APP_CORS_ORIGINS="https://yourdomain.com,https://www.yourdomain.com" \
  -e VERIFICATION_BASE_URL="https://yourdomain.com/verify-email" \
  -e RAZORPAY_KEY_ID=rzp_test_SLepofnigNDLcv \
  -e RAZORPAY_KEY_SECRET=QfBuE7002SgcCcF2Jv0jYn1Y \
  -e RAZORPAY_WEBHOOK_SECRET=shivansh1234 \
  ${ECR_URI}:latest
```

**Replace** `yourdomain.com` with your actual domain in `APP_CORS_ORIGINS` and `VERIFICATION_BASE_URL`.

---

## Part 7: Razorpay Webhook (If Using Payments)

If you use Razorpay webhooks (e.g. payment confirmation):

1. **Razorpay Dashboard** → **Webhooks** → Add endpoint
2. **URL:** `https://api.yourdomain.com/api/v1/cart/webhook` (or your webhook path)
3. **Events:** Select payment/order events you need
4. Copy the **Webhook Secret** and use it in `RAZORPAY_WEBHOOK_SECRET` when starting the backend container

---

## Part 8: R2 CORS (If FE Loads Media Directly)

If your frontend loads images/videos directly from R2 (e.g. `pub-xxxxx.r2.dev`):

1. **Cloudflare Dashboard** → **R2** → your bucket → **Settings**
2. **CORS policy** → Add your frontend origin: `https://yourdomain.com` and `https://www.yourdomain.com`

---

## Part 9: Verification Checklist

| Check | Command / Action |
|-------|------------------|
| FE loads | Open `https://yourdomain.com` in browser |
| www works | Open `https://www.yourdomain.com` |
| API responds | `curl https://api.yourdomain.com/api/v1/bff/home` |
| Login works | Try login on FE, check Network tab for `api.yourdomain.com` |
| CORS | No CORS errors in browser console |
| HTTPS | Padlock icon, no mixed content warnings |

---

## Part 10: Redeploying Frontend (Updates)

When you push changes to the frontend:

```bash
# On EC2
cd ~/quantum_edu_fe
git pull   # or upload new files via SCP

# Rebuild with API URL (must match your domain)
export NEXT_PUBLIC_API_URL="https://api.yourdomain.com"
npm ci
npm run build

# Restart PM2
pm2 restart quantum-edu-fe
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **502 Bad Gateway** | Check `pm2 status`; ensure Next.js is running on 3000. Check `sudo nginx -t` and `/var/log/nginx/error.log` |
| **CORS errors** | Ensure `APP_CORS_ORIGINS` includes `https://yourdomain.com` and `https://www.yourdomain.com` (no trailing slash) |
| **API 404** | Verify `api.yourdomain.com` resolves to EC2; Nginx proxies to 8080; backend container is running |
| **DNS not resolving** | Wait for propagation; use `dig yourdomain.com` to verify |
| **Certbot fails** | Ensure ports 80/443 are open in Security Group; DNS must point to EC2 before running certbot |
| **Out of memory** | t2.micro has 1GB RAM. If both FE+BE struggle, consider t3.small (2GB) or move FE to AWS Amplify |

---

## Summary: URLs After Deployment

| Purpose | URL |
|---------|-----|
| Website | `https://yourdomain.com` |
| Website (www) | `https://www.yourdomain.com` |
| Backend API | `https://api.yourdomain.com` |
| Email verification link | `https://yourdomain.com/verify-email?token=...` |

Replace `yourdomain.com` with your actual GoDaddy domain throughout this guide.
