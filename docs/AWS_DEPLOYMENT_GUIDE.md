# AWS Deployment Guide — Staging on Free Credits

This guide helps you deploy `quantum-edu-be` to AWS for **staging** using your new account's free credits. It covers tasks to earn credits, time estimates, and a step-by-step deployment path.

**Architecture:** EC2 (Ubuntu 22.04) + RDS MySQL (private) + ECR. Media on **Cloudflare R2** (zero egress). Manual push workflow for staging.

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

- **Compute:** EC2 t2.micro (Ubuntu 22.04 LTS)
- **Database:** RDS MySQL db.t3.micro (private, EC2-only access)
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

## Part 4: End-to-End Deployment — Step by Step

This section walks you through deploying the backend on AWS staging from scratch. Follow steps in order.

---

### Prerequisites

| Tool | Purpose |
|------|---------|
| AWS account | With free credits; IAM user with access keys |
| AWS CLI | `brew install awscli`; run `aws configure` with Access Key ID and Secret |
| Docker Desktop | Build and push images; must be running (`open -a Docker`) |
| Cloudflare account | For R2 media |
| MySQL client | Run migrations (`brew install mysql-client`) |

**Note:** Run all commands from the `quantum-edu-be` repo root (where the Dockerfile is).

---

### Step 1: Set Up Cloudflare R2 (Media Storage)

1. Go to [cloudflare.com](https://cloudflare.com) → **R2 Object Storage** → **Create bucket**.
2. Name: `quantum-edu-staging-media`. Region: e.g. APAC.
3. **Settings** → **Public access** → **Allow Access** → choose **R2.dev subdomain**.
4. **Objects** → **Upload** → upload `thumbnail.jpg`, `video.mp4` (or your media).
5. Note the public base URL: `https://pub-xxxxx.r2.dev`.
6. If your R2 URL differs from `pub-96a817cca5de440db5e3364bbb57f3ed.r2.dev`, update `docs/seed-test-data.sql` and `docs/migrations/add-lms-course-saisseru.sql` with your base URL before running migrations.
7. **CORS:** If your FE loads media directly from R2, add your FE origin in bucket **Settings** → **CORS policy**.

---

### Step 2: Complete AWS Credit Tasks (~1–2 hours)

1. [AWS Console Home](https://console.aws.amazon.com/) → **Explore AWS** widget.
2. Complete in order: AWS Budgets → Bedrock → Lambda → RDS → EC2.
3. For **RDS** and **EC2**, use the same region (e.g. `ap-south-1`).

---

### Step 3: Create RDS MySQL Database (Private)

1. **RDS** → **Create database**.
2. **Engine:** MySQL 8.0. **Template:** Free tier.
3. **DB instance identifier:** `quantum-edu-staging`.
4. **Master username:** `admin` (or your choice). **Master password:** set and save.
5. **Instance configuration:** db.t3.micro.
6. **Storage:** 20 GB gp2 (free tier).
7. **Connectivity:**
   - **Publicly accessible:** **No** (private; only reachable from within VPC).
   - **VPC:** Default (same VPC as EC2).
   - **Subnet group:** Default.
   - **Security group:** Create new `quantum-edu-rds-sg` (inbound rules added in Step 8 after EC2 exists).
8. **Database name:** `quantum_education` (recommended; creates DB on first boot).
9. Create database. Wait 5–10 min.
10. **RDS** → your DB → **Connectivity & security** → **Connect using** → select **Endpoint**. Note the **Endpoint** (private DNS, e.g. `xxx.xxxxx.ap-south-1.rds.amazonaws.com`) and **Port** (3306).

---

### Step 4: Create ECR Repository and Push Image

**Run from `quantum-edu-be` directory.** On Apple Silicon (M1/M2/M3), use `--platform linux/amd64` so the image runs on EC2 (x86).

```bash
cd quantum-edu-be

# 1. Create ECR repo (region must match EC2)
aws ecr create-repository --repository-name quantum-edu-be-staging --region ap-south-1

# 2. Get account ID, registry, and full URI
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="${ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com"
ECR_URI="${ECR_REGISTRY}/quantum-edu-be-staging"

# 3. Login to ECR (use ECR_REGISTRY, not ECR_URI)
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin $ECR_REGISTRY

# 4. Build (Apple Silicon: add --platform linux/amd64)
docker build --platform linux/amd64 -t quantum-edu-be-staging .

# 5. Tag and push (use ${ECR_URI} braces to avoid shell parsing issues)
docker tag quantum-edu-be-staging:latest ${ECR_URI}:latest
docker push ${ECR_URI}:latest
```

---

### Step 5: Create IAM Role for EC2 (ECR Access)

1. **IAM** → **Roles** → **Create role**.
2. **Trusted entity:** AWS service → **EC2**.
3. **Permissions:** Attach `AmazonEC2ContainerRegistryReadOnly`.
4. **Role name:** `quantum-edu-ecr-read`.
5. Create role.

---

### Step 6: Launch EC2 Instance (Ubuntu)

**Important:** Use the same VPC as RDS (default VPC) so EC2 can reach RDS privately.

1. **EC2** → **Launch Instance**.
2. **Name:** `quantum-edu-be-staging`.
3. **AMI:** Ubuntu Server 22.04 LTS (or 24.04).
4. **Instance type:** t2.micro (free tier).
5. **Key pair:** Create new or select existing. Download `.pem` and `chmod 400 your-key.pem`.
6. **Network settings:** Create security group `quantum-edu-sg`:
   - SSH (22) from Your IP
   - Custom TCP 8080 from 0.0.0.0/0 (or restrict to FE)
   - HTTP (80), HTTPS (443) optional for future Nginx
7. **Storage:** 8 GB gp3.
8. **Advanced details** → **IAM instance profile:** Select `quantum-edu-ecr-read` (from Step 5).
9. **User data** (optional, installs Docker and AWS CLI on boot):

```bash
#!/bin/bash
apt update -y
apt install -y docker.io awscli
systemctl start docker
systemctl enable docker
usermod -aG docker ubuntu
```

10. Launch. Note **Public IPv4 address**. Default SSH user is **`ubuntu`** (not `ec2-user`).

---

### Step 7: Update RDS Security Group for EC2

1. **RDS** → your DB → **VPC security groups** → click `quantum-edu-rds-sg` (or the SG you created).
2. **Edit inbound rules** → **Add rule:**
   - Type: MySQL/Aurora
   - Port: 3306
   - Source: Custom → select `quantum-edu-sg` (EC2 security group)
3. Save. RDS is now reachable only from your EC2 instance.

---

### Step 8: Run Database Migrations from EC2

RDS is private, so migrations must run from EC2 (same VPC).

**8a. Copy migration files to EC2** (from your machine):

```bash
scp -i your-key.pem -r docs/ ubuntu@<EC2_PUBLIC_IP>:~/quantum-edu-be-docs/
```

**8b. SSH to EC2 and run migrations:**

```bash
ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>

# Install MySQL client (Ubuntu)
sudo apt update
sudo apt install -y mysql-client

# Set variables (use your RDS endpoint, user, password)
RDS_HOST="<your-db>.xxxxx.ap-south-1.rds.amazonaws.com"
RDS_USER="admin"
RDS_PASS="<your-password>"
RDS_DB="quantum_education"

# Run migrations (order matters)
mysql -h $RDS_HOST -P 3306 -u $RDS_USER -p"$RDS_PASS" $RDS_DB < ~/quantum-edu-be-docs/schema.sql
mysql -h $RDS_HOST -P 3306 -u $RDS_USER -p"$RDS_PASS" $RDS_DB < ~/quantum-edu-be-docs/migrations/add-product-is-free.sql
mysql -h $RDS_HOST -P 3306 -u $RDS_USER -p"$RDS_PASS" $RDS_DB < ~/quantum-edu-be-docs/seed-test-data.sql
mysql -h $RDS_HOST -P 3306 -u $RDS_USER -p"$RDS_PASS" $RDS_DB < ~/quantum-edu-be-docs/migrations/add-modules-for-locked-flow.sql
mysql -h $RDS_HOST -P 3306 -u $RDS_USER -p"$RDS_PASS" $RDS_DB < ~/quantum-edu-be-docs/migrations/add-lms-course-saisseru.sql
mysql -h $RDS_HOST -P 3306 -u $RDS_USER -p"$RDS_PASS" $RDS_DB < ~/quantum-edu-be-docs/migrations/add-quiz-test-data-saisseru.sql
```

**Alternative:** If your repo is public, clone on EC2: `git clone <your-repo-url>`, then run `mysql ... < repo/docs/schema.sql` etc.

---

### Step 9: Run Container on EC2

```bash
# SSH to EC2 (Ubuntu user)
ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>

# If Docker not installed (skip if user data ran):
sudo apt update
sudo apt install -y docker.io
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ubuntu
# Log out and back in: exit, then ssh again

# Install AWS CLI if not present (for ECR login)
sudo apt install -y awscli

# No aws configure needed — IAM role provides credentials
# Generate JWT secret locally first: openssl rand -base64 32
# Set variables (replace ALL placeholders)
ECR_URI="<YOUR_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/quantum-edu-be-staging"
RDS_ENDPOINT="<your-db>.ap-south-1.rds.amazonaws.com"
RDS_DB="quantum_education"
RDS_USER="admin"
RDS_PASS="<your-password>"
JWT_SECRET="<generate-with-openssl-rand-base64-32>"

# Login to ECR (IAM role provides credentials)
aws ecr get-login-password --region ap-south-1 | sudo docker login --username AWS --password-stdin <YOUR_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com

# Pull and run (use ${ECR_URI} braces)
sudo docker pull ${ECR_URI}:latest
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
  -e APP_CORS_ORIGINS=https://quantum-education.netlify.app \
  -e VERIFICATION_BASE_URL=https://quantum-education.netlify.app/verify-email \
  -e RAZORPAY_KEY_ID=rzp_test_xxx \
  -e RAZORPAY_KEY_SECRET=xxx \
  -e RAZORPAY_WEBHOOK_SECRET=xxx \
  ${ECR_URI}:latest
```

---

### Step 10: Verify Backend Is Running

```bash
# From your machine
curl http://<EC2_PUBLIC_IP>:8080/health
# Expected: {"status":"UP"}

curl http://<EC2_PUBLIC_IP>:8080/api/v1/catalogue/getCategories
# Expected: JSON with categories
```

---

### Step 11: Configure Razorpay Webhook

1. **Razorpay Dashboard** → **Webhooks** → **Add endpoint**.
2. **URL:** `http://<EC2_PUBLIC_IP>:8080/api/v1/cart/webhook/razorpay`
3. **Events:** `payment.captured`.
4. Copy **Webhook Secret** → set as `RAZORPAY_WEBHOOK_SECRET` in the container (restart container with new env var, or add to run command and redeploy).

---

### Step 12: Update Frontend

In your FE repo (e.g. Netlify env vars):

```
VITE_API_BASE_URL=http://<EC2_PUBLIC_IP>:8080
```

Redeploy the frontend.

---

### Step 13: Redeploy After Code Changes (Manual Push)

```bash
# On your machine (from quantum-edu-be directory):
cd quantum-edu-be
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_URI="${ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com/quantum-edu-be-staging"

# Build (Apple Silicon: add --platform linux/amd64)
docker build --platform linux/amd64 -t quantum-edu-be-staging .

# Tag and push (use ${ECR_URI} braces)
docker tag quantum-edu-be-staging:latest ${ECR_URI}:latest
docker push ${ECR_URI}:latest

# On EC2: pull and restart
ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>
ECR_URI="<YOUR_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/quantum-edu-be-staging"
sudo docker stop quantum-edu-be
sudo docker rm quantum-edu-be
sudo docker pull ${ECR_URI}:latest
# Run the same docker run command as in Step 9
```

---

## Part 5: Configuration Gap Analysis (Staging)

### What Exists

| Config | Location | Status |
|--------|----------|--------|
| `application-staging.properties` | `app/src/main/resources/` | Present; tuned for Render + Aiven |
| MySQL datasource | `application-staging.properties` | Uses `MYSQL_*` env vars; defaults to Aiven |
| CORS | `application-staging.properties` | `APP_CORS_ORIGINS` with Netlify default |
| JWT | `application-staging.properties` | `JWT_SECRET` with dev default; override for staging |
| Razorpay | `application-staging.properties` | Test keys; `RAZORPAY_*` env vars |
| Email | `application-staging.properties` | Disabled by default (`APP_EMAIL_ENABLED=false`) |
| Dockerfile | repo root | Builds JAR; no `SPRING_PROFILES_ACTIVE` (set via env) |

### What's Missing or Needs Attention

| Gap | Impact | Action |
|-----|--------|--------|
| **RDS datasource URL** | Staging uses `useSSL=true&requireSSL=true` (Aiven). RDS supports SSL; if connection fails, add `allowPublicKeyRetrieval=true` to URL. | Override via `SPRING_DATASOURCE_URL` if needed, or add RDS-specific connection params to staging properties. |
| **AWS-specific defaults** | `application-staging.properties` defaults to Aiven host/port. | All `MYSQL_*` must be overridden via env vars for AWS; no code change required. |
| **`STAGING_RENDER_ENV.md`** | Documents Render + Aiven only. | Add `STAGING_AWS_ENV.md` or extend doc with AWS env vars. |
| **Razorpay webhook secret** | Hardcoded default `shivansh1234` in staging. | Must match Razorpay Dashboard; set `RAZORPAY_WEBHOOK_SECRET` in container. |
| **JWT secret** | Default in staging is weak. | Override with `openssl rand -base64 32` for staging. |
| **PORT** | `server.port=${PORT:8080}`. EC2 does not set `PORT`. | Default 8080 is used; no change needed. |
| **Health check** | `/health` exists. | No change; suitable for load balancers. |
| **Product `is_free`** | Schema does not include it; migration adds it. | Run `add-product-is-free.sql` before seed (see Step 8). |

### Recommended Repo Updates (Optional)

1. **`docs/STAGING_AWS_ENV.md`** — Copy of env vars for AWS staging. See [STAGING_AWS_ENV.md](./STAGING_AWS_ENV.md).
2. **Add `allowPublicKeyRetrieval` for RDS** — In `application-staging.properties`, append `&allowPublicKeyRetrieval=true` to datasource URL if RDS connection fails with "Public Key Retrieval is not allowed".
3. **Dockerfile** — No change; `SPRING_PROFILES_ACTIVE` is correctly set via env.

---

## Part 6: Alternative — ECS Fargate (More Managed)

If you prefer managed containers:

1. Create ECS cluster.
2. Create task definition (ECR image, CPU/memory, env vars).
3. Create ECS service (Fargate, 1 task).
4. Configure ALB (optional).

This uses more credits but is simpler operationally.

---

## Part 7: Environment Variables Summary

See also [STAGING_AWS_ENV.md](./STAGING_AWS_ENV.md) for a copy-paste reference.

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
| `NoCredentials` / Unable to locate credentials | Run `aws configure`; create IAM user with access keys (IAM → Users → Create user → Security credentials → Create access key). |
| `command not found: docker` | Install Docker Desktop; start it with `open -a Docker`. |
| `failed to connect to docker.sock` | Docker daemon not running. Start Docker Desktop and wait for it to be ready. |
| `unable to evaluate symlinks in Dockerfile path` | Run from `quantum-edu-be` directory: `cd quantum-edu-be` before `docker build`. |
| `registry-1.docker.io: unauthorized` | Use `ECR_REGISTRY` (not `ECR_URI`) for `docker login`. ECR_REGISTRY = host only; ECR_URI = host/repo. |
| `tag does not exist` / `quantum-edu-be-stagingatest` | Run `docker tag` before `docker push`. Use `${ECR_URI}:latest` (braces) to avoid shell parsing. |
| ECR push denied | Run `aws ecr get-login-password` piped to `docker login` with `$ECR_REGISTRY` (host only, not `$ECR_URI`). |
| DB connection failed | Check RDS security group; allow inbound 3306 from EC2 SG. |
| CORS errors | Set `APP_CORS_ORIGINS` to Netlify/FE URL. |
| Container exits | Check logs: `docker logs <container_id>`. |
| Port 8080 not reachable | Add inbound rule for 8080 in EC2 security group. |
| Media 403/404 | R2: Enable public access; check object keys and CORS. |
| RDS SSL/connection failed | RDS is private; only EC2 can connect. Ensure RDS and EC2 are in same VPC; RDS security group allows 3306 from EC2 SG. Add `allowPublicKeyRetrieval=true` to URL if needed. |
| Apple Silicon: image won't run on EC2 | Build with `--platform linux/amd64`; EC2 t2/t3 are x86. |
