# Quantum Education — AWS production guide

This is the **only** production deployment guide for the platform: **EC2 + Docker (API) + nginx + Certbot + Gunicorn (admin) + RDS + Amplify (Next.js)**. Staging remains documented in **`AWS_DEPLOYMENT_GUIDE.md`**.

| Layer | Production |
|--------|------------|
| **API** | Ubuntu **EC2**, **Docker** (Spring Boot **:8080** on all interfaces). **nginx** on **443** proxies `api.yourdomain.com` → `127.0.0.1:8080`. **No ALB** in the default path. |
| **Admin** | Same EC2 (typical): **Gunicorn** `127.0.0.1:8000`, **nginx** `admin.yourdomain.com` → proxy. **No ALB.** |
| **Frontend** | **AWS Amplify** (`quantum_edu_fe`). |
| **Data** | **RDS MySQL** (private): `quantum_education` + `admin_django`. |
| **Media** | **Cloudflare R2** (unchanged). |
| **Releases** | **`./deploy.prod.sh`** + **`deploy.prod.env`** in **`quantum-edu-be`** and **`quantum-edu-admin`**. |

---

## Table of contents

1. [What is Certbot?](#1-what-is-certbot)
2. [Architecture diagram](#2-architecture-diagram)
3. [Code and configuration changes](#3-code-and-configuration-changes)
4. [Local setup — `deploy.prod.env`](#4-local-setup--deployprodenv)
5. [AWS — RDS MySQL](#5-aws--rds-mysql)
6. [AWS — ECR](#6-aws--ecr)
7. [AWS — IAM role for EC2](#7-aws--iam-role-for-ec2)
8. [AWS — Security groups](#8-aws--security-groups)
9. [AWS — Elastic IP and EC2](#9-aws--elastic-ip-and-ec2)
10. [AWS — Allow EC2 to reach RDS](#10-aws--allow-ec2-to-reach-rds)
11. [On EC2 — Docker, nginx, Certbot](#11-on-ec2--docker-nginx-certbot)
12. [On EC2 — nginx for API and Admin](#12-on-ec2--nginx-for-api-and-admin)
13. [DNS](#13-dns)
14. [Database schema and Django](#14-database-schema-and-django)
15. [Deploy backend](#15-deploy-backend)
16. [Deploy admin](#16-deploy-admin)
17. [Amplify (frontend)](#17-amplify-frontend)
18. [Secrets and `deploy.prod.env`](#18-secrets-and-deployprodenv)
19. [Go-live checklist](#19-go-live-checklist)
20. [Reference: staging AWS walkthrough](#20-reference-staging-aws-walkthrough)
21. [Appendix — Optional ALB + Auto Scaling](#appendix--optional-alb--auto-scaling)

---

## 1. What is Certbot?

**Certbot** is the standard client for **Let’s Encrypt**. It requests **free TLS certificates**, proves you control the domain (usually HTTP on **port 80**), installs certs into **nginx** (`python3-certbot-nginx`), and renews them automatically (~90-day lifetime). You need **DNS A records** pointing at your **Elastic IP** before Certbot succeeds.

---

## 2. Architecture diagram

```
                    HTTPS
Internet ──────────► EC2 (Elastic IP)
                     nginx :443
                       api.yourdomain.com    → 127.0.0.1:8080 (Spring in Docker)
                       admin.yourdomain.com → 127.0.0.1:8000 (Gunicorn)
                     :80 for Certbot / redirect

                     MySQL :3306
                     ─────────► RDS (private) quantum_education | admin_django

Amplify: www → Next.js, NEXT_PUBLIC_API_URL=https://api.yourdomain.com
```

Bind **Gunicorn to `127.0.0.1:8000`** only; do not publish **8000** on the security group.

---

## 3. Code and configuration changes

Apply before or during cutover. **Do not commit secrets.**

### 3.1 Backend (`quantum-edu-be`)

| Item | What to do |
|------|------------|
| **Health / smoke tests** | Prefer **`spring-boot-starter-actuator`** + **`/actuator/health`** in prod, or any stable **GET** returning **200** behind `https://api…` (see `curl` in §15). |
| **RDS TLS** | Set **`useSSL=true`** (and CA options if required) on **`application-prod.properties`** JDBC URL — [RDS MySQL SSL](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/MySQL.Concepts.SSLSupport.html). |
| **CORS** | **`APP_CORS_ORIGINS`** in **`deploy.prod.env`**: Amplify URL(s) + custom domain, comma-separated. Update **`application-prod.properties`** fallback if it still mentions Netlify. |
| **Email / verify** | **`VERIFICATION_BASE_URL`**, **`EMAIL_FROM`**, **`MAIL_*`**, **`JWT_SECRET`** via **`deploy.prod.env`** / container env. |
| **Listen address** | Spring must listen **`0.0.0.0:8080`** inside Docker so **nginx** can reach **`127.0.0.1:8080`**. |

### 3.2 Frontend (`quantum_edu_fe`)

| Item | What to do |
|------|------------|
| **`NEXT_PUBLIC_API_URL`** | Amplify env: **`https://api.yourdomain.com`**. Rebuild after changes. |
| **`next.config.ts`** | Add prod **R2** host to `images.remotePatterns` if the bucket URL differs. |
| **`amplify.yml`** | Add at repo root if Amplify does not build Next.js 16 correctly. Example: |

```yaml
version: 1
frontend:
  phases:
    preBuild:
      commands:
        - npm ci
    build:
      commands:
        - npm run build
  artifacts:
    baseDirectory: .next
    files:
      - '**/*'
  cache:
    paths:
      - node_modules/**/*
```

Set **Node 20+** in Amplify **Build image settings** if builds fail. See [Amplify Next.js](https://docs.aws.amazon.com/amplify/latest/userguide/deploy-nextjs-app.html).

### 3.3 Admin (`quantum-edu-admin`)

| Item | What to do |
|------|------------|
| **MySQL SSL** | Match RDS in **`quantum_admin/settings/prod.py`** (mirror **`staging.py`** `ssl_mode` / options if needed). |
| **Settings module** | **`deploy.prod.env`** has **`DJANGO_SETTINGS_MODULE=quantum_admin.settings.prod`** ( **`deploy.prod.sh`** writes **`gunicorn.env`**). |
| **`ALLOWED_HOSTS`** | **`admin.yourdomain.com`** (comma-separated). |

---

## 4. Local setup — `deploy.prod.env`

**Backend**

```bash
cd quantum-edu-be
cp deploy.prod.env.example deploy.prod.env
chmod 600 deploy.prod.env
# Fill ECR_URI, EC2 Elastic IP, RDS, JWT, APP_CORS_ORIGINS, VERIFICATION_BASE_URL, LIVE Razorpay, mail…
```

**Admin**

```bash
cd quantum-edu-admin
cp deploy.prod.env.example deploy.prod.env
chmod 600 deploy.prod.env
# Same EC2_HOST if one server; ALLOWED_HOSTS; R2 keys if needed
```

---

## 5. AWS — RDS MySQL

In the AWS console, follow the same flow as **`AWS_DEPLOYMENT_GUIDE.md` → Step 3 (Create RDS MySQL)** with **production** values:

| | Staging (reference) | Production |
|--|---------------------|------------|
| Identifier | e.g. `quantum-edu-staging` | e.g. `quantum-edu-mysql-production` |
| Template | Free tier | **Production** or right-sized |
| Class | `db.t3.micro` | e.g. **`db.t3.small`**+ |
| Public access | **No** | **No** |
| VPC | Same as EC2 | **Same VPC as prod EC2** |
| Initial DB name | `quantum_education` | `quantum_education` |

Note **Endpoint** and port **3306**. After **Available**, create **`admin_django`** if missing (§14). Load **`quantum_education`** schema (e.g. **`docs/schema.sql`** from EC2 — same idea as staging **Step 8**).

---

## 6. AWS — ECR

Same as staging **`AWS_DEPLOYMENT_GUIDE.md` → Step 4**, but create repository **`quantum-edu-be-production`**:

```bash
aws ecr create-repository --repository-name quantum-edu-be-production --region YOUR_REGION
```

Put the full URI in **`deploy.prod.env`** as **`ECR_URI`**.

---

## 7. AWS — IAM role for EC2

Same as staging **Step 5**: role for **EC2** with **`AmazonEC2ContainerRegistryReadOnly`**. Add **`AmazonSSMManagedInstanceCore`** if you use **Session Manager** (recommended). Attach profile on the **prod EC2** at launch.

---

## 8. AWS — Security groups

### `sg-quantum-prod-app` (EC2)

| Inbound | Port | Source |
|---------|------|--------|
| SSH | 22 | **Your IP** (or skip if SSM-only) |
| HTTP | 80 | `0.0.0.0/0` (Certbot) |
| HTTPS | 443 | `0.0.0.0/0` |

**Do not** open **8080** or **8000** to the world.

### `sg-quantum-prod-rds` (RDS)

| Inbound | Port | Source |
|---------|------|--------|
| MySQL | 3306 | **`sg-quantum-prod-app`** only |

---

## 9. AWS — Elastic IP and EC2

1. **EC2 → Elastic IPs → Allocate**.
2. **Launch instance** (see staging **`AWS_DEPLOYMENT_GUIDE.md` → Step 6** for the full click-path):

| Field | Production |
|-------|------------|
| AMI | **Ubuntu 22.04 / 24.04 LTS** |
| Type | e.g. **t3.small** |
| Key pair | Prod key, `chmod 400` |
| VPC / subnet | Same as RDS; **public** subnet + auto-assign public IP |
| Security group | **`sg-quantum-prod-app`** |
| Storage | ≥ **30 GiB** gp3 |
| IAM profile | ECR read role (§7) |

3. **Associate Elastic IP** to the instance.
4. Set **`EC2_HOST`** in both **`deploy.prod.env`** files.

Optional **user data** (install Docker — staging Step 6 snippet):

```bash
#!/bin/bash
apt update -y
apt install -y docker.io awscli
systemctl start docker
systemctl enable docker
usermod -aG docker ubuntu
```

---

## 10. AWS — Allow EC2 to reach RDS

**RDS → your DB → VPC security groups → `sg-quantum-prod-rds` → Inbound:** **MySQL 3306** from **`sg-quantum-prod-app`**.

---

## 11. On EC2 — Docker, nginx, Certbot

```bash
ssh -i /path/to/prod.pem ubuntu@YOUR_ELASTIC_IP

sudo apt update
sudo apt install -y docker.io nginx certbot python3-certbot-nginx awscli
sudo systemctl enable --now docker
sudo usermod -aG docker ubuntu
# Re-login for docker group

sudo systemctl enable --now nginx
```

---

## 12. On EC2 — nginx for API and Admin

Create **`/etc/nginx/sites-available/quantum-edu`**:

```nginx
server {
    listen 80;
    server_name api.yourdomain.com;
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
    }
}

server {
    listen 80;
    server_name admin.yourdomain.com;
    client_max_body_size 150M;
    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo ln -sf /etc/nginx/sites-available/quantum-edu /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
```

**DNS:** §13 — point **`api`** and **`admin`** **A** records to the **Elastic IP**.

**TLS:**

```bash
sudo certbot --nginx -d api.yourdomain.com -d admin.yourdomain.com
```

---

## 13. DNS

| Hostname | Type | Value |
|----------|------|--------|
| `api.yourdomain.com` | **A** | Elastic IP |
| `admin.yourdomain.com` | **A** | Same IP (one EC2) |
| `www` / apex | Per **Amplify** | CNAME / ANAME as Amplify shows |

---

## 14. Database schema and Django

```bash
sudo apt install -y mysql-client
mysql -h YOUR_RDS_ENDPOINT -P 3306 -u admin -p \
  -e "CREATE DATABASE IF NOT EXISTS admin_django CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Apply **`quantum_education`** schema per staging **Step 8** (`schema.sql` or your process). **Django** only migrates **`admin_django`**:

```bash
# After admin deploy
cd ~/quantum_edu_admin && source .venv/bin/activate && set -a && source gunicorn.env && set +a
python manage.py migrate --database=default
python manage.py createsuperuser
```

Spring owns **`quantum_education`** schema (`admin_app` router does not migrate that DB).

---

## 15. Deploy backend

**On your laptop** (Docker + `aws configure`):

```bash
cd quantum-edu-be
./deploy.prod.sh
```

Optional: `DEPLOY_PROD_ENV_FILE=/path/to.env ./deploy.prod.sh`

Verify:

```bash
curl -sS -o /dev/null -w "%{http_code}\n" https://api.yourdomain.com/actuator/health
```

---

## 16. Deploy admin

**On your laptop:**

```bash
cd quantum-edu-admin
./deploy.prod.sh
```

**One-time systemd:** [`quantum-edu-admin/docs/DJANGO_AWS_DEPLOYMENT_GUIDE.md`](../../quantum-edu-admin/docs/DJANGO_AWS_DEPLOYMENT_GUIDE.md) — **`quantum-edu-admin.service`**, `systemctl enable --now quantum-edu-admin`.

---

## 17. Amplify (frontend)

1. Amplify → **Host web app** → Git → **`quantum_edu_fe`**.
2. **`NEXT_PUBLIC_API_URL=https://api.yourdomain.com`**.
3. Custom domain; ensure **`APP_CORS_ORIGINS`** lists that origin.
4. **R2 bucket CORS** in Cloudflare: allow FE origins if media loads from R2.

---

## 18. Secrets and `deploy.prod.env`

- **`deploy.prod.env`** is **gitignored** — never commit.
- Use **different** passwords / JWT / Django secret / Razorpay keys than **staging**.
- Prefer **`chmod 600`** on the env file.

---

## 19. Go-live checklist

- [ ] RDS private; **3306** only from app SG
- [ ] **8080 / 8000** not open on internet-facing SG
- [ ] **HTTPS** works for **api** and **admin**
- [ ] **`SPRING_PROFILES_ACTIVE=prod`**
- [ ] **`quantum_admin.settings.prod`** for admin
- [ ] Amplify **`NEXT_PUBLIC_API_URL`** correct
- [ ] **Live** Razorpay when taking real payments
- [ ] `sudo certbot renew --dry-run`

---

## 20. Reference: staging AWS walkthrough

For **click-by-click** detail on RDS/ECR/EC2 (screens and ordering), use **[`AWS_DEPLOYMENT_GUIDE.md`](./AWS_DEPLOYMENT_GUIDE.md)** and treat production as the **same flow** with the **naming, sizing, and security group rules** in this guide.

**Operations cheatsheet:** [`EC2_COMMANDS_REFERENCE.md`](./EC2_COMMANDS_REFERENCE.md).

---

## Appendix — Optional ALB + Auto Scaling

If you later need **horizontal scaling** or **ACM-only TLS without nginx on the instance**, use an **Application Load Balancer** + **target group** on **8080** + **Auto Scaling Group**. That path is **more expensive and more work** (VPC across AZs, launch templates, instance refresh). The default in **this** document is **single (or few) EC2 + nginx + Certbot** to stay close to staging.

---

*Single production guide — EC2 + nginx + Certbot + Amplify; **`deploy.prod.sh`** for API and admin.*
