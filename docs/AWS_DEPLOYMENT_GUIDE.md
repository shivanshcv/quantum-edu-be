# AWS Deployment Guide — Staging on Free Credits

This guide helps you deploy `quantum-edu-be` to AWS for **staging** using your new account's free credits. It covers tasks to earn credits, time estimates, and a step-by-step deployment path.

**Architecture:** Compute + DB on AWS (EC2, RDS MySQL, ECR). Media storage on **Cloudflare R2** (zero egress, cost-effective for video-heavy LMS).

---

## Part 1: AWS Free Credits — Tasks & Time Estimates

### Credit Amounts

| Source | Amount |
|--------|--------|
| $100 | Automatically upon sign-up |
| $100 | By completing 5 onboarding tasks ($20 each) |
| **Total** | **$200** |

### Five Tasks to Earn Credits

Find these in the **Explore AWS** widget on your [AWS Console Home](https://console.aws.amazon.com/).

| # | Task | Time | Difficulty |
|---|------|------|------------|
| 1 | **Set up a cost budget using AWS Budgets** | 5–10 min | Easy |
| 2 | **Use a foundational model in the Amazon Bedrock playground** | 5–10 min | Easy |
| 3 | **Create a web app using AWS Lambda** | 15–30 min | Easy |
| 4 | **Create an Amazon RDS database** | 15–20 min | Easy |
| 5 | **Launch an instance using Amazon EC2** | 15–20 min | Easy |

**Total time to complete all 5 tasks:** ~1–2 hours.

**Recommendation:** Do these in order. Tasks 1–2 are quick (≈15 min). Tasks 3–5 are useful for your deployment anyway (e.g. RDS for MySQL, EC2 for compute).

**Note:** Tasks 3, 4, 5 incur small charges. Use your free credits to cover them.

---

## Part 2: Your Application Overview

### Stack

- **Runtime:** Java 21, Spring Boot 3.3.5
- **Database:** MySQL 8 (RDS)
- **Build:** Maven
- **Container:** Dockerfile (multi-stage build)
- **Media storage:** Cloudflare R2 (thumbnails, videos, PDFs, PPTs)

### Dependencies

- MySQL (auth, products, cart, orders, LMS)
- JWT (auth)
- Razorpay (payments)
- Email (verification)
- **R2** (media URLs stored in DB; no backend upload API — upload via Cloudflare dashboard, store URLs in DB)

### Target Staging Architecture

- **Compute:** EC2 t2.micro (AWS)
- **Database:** RDS MySQL db.t3.micro (AWS)
- **Registry:** ECR (AWS)
- **Media:** Cloudflare R2 (outside AWS; zero egress for video streaming)

---

## Part 3: AWS Staging Architecture

### Chosen Architecture: EC2 + RDS MySQL + R2 (Media)

| Component | Service | Cost |
|-----------|---------|------|
| Compute | EC2 t2.micro | 750 hrs/month free for 12 months |
| Database | RDS MySQL db.t3.micro | 750 hrs/month free for 12 months |
| Registry | ECR | 500 MB free |
| **Media** | **Cloudflare R2** | 10 GB free, zero egress |

**Why R2 for media:** Zero egress fees — ideal for video-heavy LMS. Thumbnails, lesson videos, PDFs, PPTs are stored in R2; URLs are stored in the DB. See [R2_SETUP_GUIDE.md](./R2_SETUP_GUIDE.md).

### Alternative: ECS Fargate (Paid)

| Component | Service | Cost |
|-----------|---------|------|
| Compute | ECS Fargate | ~$15–30/month |
| Database | RDS MySQL | 750 hrs/month free |
| Load balancer | ALB | ~$16/month |

Use when you need managed containers and can afford the cost.

---

## Part 4: Step-by-Step — Staging on AWS (EC2 + RDS + R2)

**Deployment order:** R2 → AWS tasks → RDS → ECR → EC2 → Run container → Migrations → Razorpay webhook → Frontend

### Prerequisites

- [ ] AWS account created
- [ ] AWS CLI installed (`brew install awscli`)
- [ ] Docker installed and running
- [ ] Cloudflare account (for R2)

### Step 0: Set Up Cloudflare R2 (Media Storage)

1. Follow [R2_SETUP_GUIDE.md](./R2_SETUP_GUIDE.md) to create a bucket and enable public access.
2. Upload your media (thumbnail.jpg, video.mp4, etc.) and note the public base URL (e.g. `https://pub-xxxxx.r2.dev`).
3. Seed data and migrations already use R2 URLs. If you created a new bucket, update `docs/seed-test-data.sql` and `docs/migrations/add-lms-course-saisseru.sql` with your R2 base URL, or run migrations and then update product/lesson URLs via admin API.

### Step 1: Complete the 5 Credit Tasks (~1–2 hours)

1. Go to [AWS Console Home](https://console.aws.amazon.com/).
2. Look for **Explore AWS** widget.
3. Complete each task in order:
   - AWS Budgets
   - Amazon Bedrock
   - AWS Lambda
   - Amazon RDS
   - Amazon EC2

### Step 2: Create ECR Repository

```bash
aws ecr create-repository --repository-name quantum-edu-be-staging --region ap-south-1
```

Note the repository URI (e.g. `123456789012.dkr.ecr.ap-south-1.amazonaws.com/quantum-edu-be-staging`).

### Step 3: Build and Push Docker Image

```bash
# 1. Authenticate Docker to ECR
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin <YOUR_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com

# 2. Build (from repo root)
docker build -t quantum-edu-be-staging .

# 3. Tag for ECR
docker tag quantum-edu-be-staging:latest <YOUR_ECR_URI>:latest

# 4. Push
docker push <YOUR_ECR_URI>:latest
```

### Step 4: Launch EC2 Instance (Free Tier)

1. **EC2** → **Launch Instance**
2. **Name:** `quantum-edu-be-staging`
3. **AMI:** Amazon Linux 2023
4. **Instance type:** t2.micro (free tier)
5. **Key pair:** Create or select
6. **Security group:** Allow HTTP (80), HTTPS (443), SSH (22)
7. **Storage:** 8 GB gp3 (free tier)
8. **Advanced:** User data (optional) — see below

**User data script** (optional):

```bash
#!/bin/bash
yum update -y
yum install -y docker
systemctl start docker
systemctl enable docker
usermod -aG docker ec2-user
```

### Step 5: Install Docker on EC2 and Run Container

```bash
# SSH into EC2
ssh -i your-key.pem ec2-user@<EC2_PUBLIC_IP>

# Install Docker (if not in user data)
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user
# Log out and back in for group to take effect

# Login to ECR
aws ecr get-login-password --region ap-south-1 | sudo docker login --username AWS --password-stdin <YOUR_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com

# Pull and run (use RDS endpoint and credentials)
sudo docker pull <YOUR_ECR_URI>:latest
sudo docker run -d \
  --name quantum-edu-be \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=staging \
  -e MYSQL_HOST=<RDS_ENDPOINT> \
  -e MYSQL_PORT=3306 \
  -e MYSQL_DATABASE=<RDS_DATABASE> \
  -e MYSQL_USER=<RDS_USER> \
  -e MYSQL_PASSWORD=<RDS_PASSWORD> \
  -e JWT_SECRET=<your-jwt-secret> \
  -e APP_CORS_ORIGINS=https://quantum-education.netlify.app \
  -e VERIFICATION_BASE_URL=https://quantum-education.netlify.app/verify-email \
  <YOUR_ECR_URI>:latest
```

### Step 6: Expose Port 8080

In the EC2 security group, add inbound rule:

- Type: Custom TCP
- Port: 8080
- Source: 0.0.0.0/0 (or restrict to your IP / Netlify)

### Step 7: (Optional) Reverse Proxy

Add Nginx or Apache to route traffic from 80/443 to 8080.

```bash
# Nginx example
sudo yum install -y nginx
# Configure /etc/nginx/conf.d/quantum.conf to proxy to localhost:8080
sudo systemctl start nginx
sudo systemctl enable nginx
```

### Step 8: Update Razorpay Webhook

In Razorpay Dashboard → Webhooks:

- URL: `http://<EC2_PUBLIC_IP>:8080/api/v1/cart/webhook/razorpay`
- For HTTPS: use a domain or ALB.

### Step 9: Update Frontend

Set staging API base URL to `http://<EC2_PUBLIC_IP>:8080` (or your domain).

---

## Part 5: Alternative — ECS Fargate (More Managed)

If you prefer managed containers:

1. Create ECS cluster.
2. Create task definition (ECR image, CPU/memory, env vars).
3. Create ECS service (Fargate, 1 task).
4. Configure ALB (optional).

This uses more credits but is simpler operationally.

---

## Part 6: Create RDS MySQL

1. **RDS** → **Create database**
2. **Engine:** MySQL 8.0
3. **Template:** Free tier
4. **Instance:** db.t3.micro
5. **Storage:** 20 GB (free tier)
6. **Public access:** Yes (for staging from your machine)
7. **VPC:** Default or create new

After creation:

- Get endpoint, port, username, password.
- **Security group:** Allow inbound MySQL (3306) from your EC2 security group (or your IP for initial setup).
- Run migrations from your machine (ensure RDS has public access for staging, or run from EC2):

```bash
mysql -h <RDS_ENDPOINT> -P 3306 -u admin -p'<PASSWORD>' <DATABASE> < docs/schema.sql
mysql -h <RDS_ENDPOINT> -P 3306 -u admin -p'<PASSWORD>' <DATABASE> < docs/seed-test-data.sql
mysql -h <RDS_ENDPOINT> -P 3306 -u admin -p'<PASSWORD>' <DATABASE> < docs/migrations/add-lms-course-saisseru.sql
# ... apply other migrations as needed
```

---

## Part 7: Environment Variables Summary

| Variable | Required | Notes |
|----------|----------|-------|
| `SPRING_PROFILES_ACTIVE` | Yes | `staging` |
| `MYSQL_HOST` | Yes | RDS endpoint |
| `MYSQL_PORT` | Yes | 3306 |
| `MYSQL_DATABASE` | Yes | RDS database name |
| `MYSQL_USER` | Yes | RDS master username |
| `MYSQL_PASSWORD` | Yes | RDS master password |
| `JWT_SECRET` | Yes | e.g. `openssl rand -base64 32` |
| `APP_CORS_ORIGINS` | Yes | Netlify URL (or your FE URL) |
| `VERIFICATION_BASE_URL` | No | FE verify-email URL |
| `RAZORPAY_KEY_ID` | No | Test key |
| `RAZORPAY_KEY_SECRET` | No | Test secret |
| `RAZORPAY_WEBHOOK_SECRET` | No | From Razorpay webhook |

**Media (R2):** No env vars needed. Media URLs are stored in the DB. Ensure R2 bucket has public access and CORS configured if your FE loads media directly. See [R2_SETUP_GUIDE.md](./R2_SETUP_GUIDE.md).

---

## Part 8: Cost Estimates (Staging)

| Component | Monthly Cost |
|-----------|--------------|
| EC2 t2.micro | $0 (750 hrs free, 12 months) |
| RDS db.t3.micro | $0 (750 hrs free, 12 months) |
| ECR | $0 (500 MB free) |
| **Cloudflare R2** | **$0** (10 GB storage, 10M reads, 1M writes; zero egress) |
| **Total** | **$0** |

**Free tier:** AWS 12 months from account creation. R2 free tier has no expiry.

---

## Part 9: Production Checklist (Later)

When moving to paid:

- [ ] Use RDS in private subnet (if not already)
- [ ] Use ALB + HTTPS
- [ ] Use Secrets Manager or Parameter Store for secrets
- [ ] Enable Auto Scaling
- [ ] Set up CloudWatch alarms
- [ ] Use Route 53 for custom domain
- [ ] Enable RDS backups and encryption
- [ ] R2: Consider custom domain for media URLs; ensure CORS allows prod FE origin

---

## Quick Reference

| Task | Time | Link |
|------|------|------|
| R2 setup | 10–15 min | [R2_SETUP_GUIDE.md](./R2_SETUP_GUIDE.md) |
| AWS Budgets | 5–10 min | [AWS Console](https://console.aws.amazon.com/) → Explore AWS |
| Bedrock | 5–10 min | Same |
| Lambda | 15–30 min | Same |
| RDS | 15–20 min | Same |
| EC2 | 15–20 min | Same |
| EC2 + ECR deploy | 30–60 min | This guide |

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| ECR push denied | Run `aws ecr get-login-password` and `docker login` |
| DB connection failed | Check RDS security group; allow inbound 3306 from EC2 SG |
| CORS errors | Set `APP_CORS_ORIGINS` to Netlify/FE URL |
| Container exits | Check logs: `docker logs <container_id>` |
| Port 8080 not reachable | Add inbound rule for 8080 in EC2 security group |
| Media 403/404 | R2: Enable public access; check object keys and CORS |
| RDS SSL/connection failed | Ensure RDS is publicly accessible; security group allows 3306 from EC2 (or your IP). Staging config uses `useSSL=true`; RDS supports it. |
