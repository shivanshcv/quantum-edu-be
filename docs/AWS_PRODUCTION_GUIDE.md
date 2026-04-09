# Quantum Education — AWS production guide

This document is **self-contained**: production deployment for **two EC2 instances**—**API** (**Docker** + nginx) and **admin** (**Gunicorn** + nginx)—plus **RDS MySQL** and **Amplify (Next.js)**. No other repo guide is required to execute it.


| Layer        | Production                                                                                                                                                                                                                                                                                                |
| ------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **API**      | **Dedicated** Ubuntu **EC2** (§11): **Docker** (Spring Boot **:8080**). **nginx** on **443** → `api.yourdomain.com` → `127.0.0.1:8080`. **No ALB.**                                                                                                                                                       |
| **Admin**    | **Dedicated** small Ubuntu **EC2** (§12): **Gunicorn** **127.0.0.1:8000**, **nginx** → `admin.yourdomain.com`. **systemd** **enable**s **nginx** + **quantum-edu-admin** (§20) so **only** **EC2 → Instance state → Start** (or **Stop** when idle) is needed—**no SSH** for normal restarts. **No ALB.** |
| **Frontend** | **AWS Amplify** (`quantum_edu_fe`).                                                                                                                                                                                                                                                                       |
| **Data**     | **RDS MySQL** (private): `quantum_education` + `admin_django`.                                                                                                                                                                                                                                            |
| **Media**    | **Cloudflare R2** (outside AWS).                                                                                                                                                                                                                                                                          |
| **Releases** | `**./deploy.prod.sh`** + `**deploy.prod.env**`: `**quantum-edu-be**` uses **API** `EC2_HOST`; `**quantum-edu-admin`** uses **admin** `EC2_HOST` (second **Elastic IP**).                                                                                                                                  |


---

## Table of contents

1. [Prerequisites](#1-prerequisites)
2. [What is Certbot?](#2-what-is-certbot)
3. [Architecture](#3-architecture)
4. [Code and configuration (repos)](#4-code-and-configuration-repos)
5. [Local: `deploy.prod.env](#5-local-deployprodenv)`
6. [AWS: Create RDS MySQL](#6-aws-create-rds-mysql)
7. [AWS: Create ECR repository and push an image](#7-aws-create-ecr-repository-and-push-an-image)
8. [AWS: IAM role for EC2](#8-aws-iam-role-for-ec2)
9. [AWS: Security groups](#9-aws-security-groups)
10. [AWS: Elastic IP](#10-aws-elastic-ip)
11. [AWS: Launch EC2 (API only)](#11-aws-launch-ec2-api-only)
12. [AWS: Launch EC2 (admin only, on-demand)](#12-aws-launch-ec2-admin-only-on-demand)
13. [AWS: Allow EC2 → RDS](#13-aws-allow-ec2--rds)
14. [EC2 (API): Install Docker, nginx, Certbot](#14-ec2-api-install-docker-nginx-certbot)
15. [EC2 (API): nginx for API + Certbot](#15-ec2-api-nginx-for-api--certbot)
16. [EC2 (admin): Install nginx, Certbot, MySQL client](#16-ec2-admin-install-nginx-certbot-mysql-client)
17. [EC2 (admin): nginx for Django admin + Certbot](#17-ec2-admin-nginx-for-django-admin--certbot)
18. [DNS](#18-dns)
19. [Database: schema and Django](#19-database-schema-and-django)
20. [Gunicorn systemd (admin EC2)](#20-gunicorn-systemd-admin-ec2)
21. [Deploy backend: `./deploy.prod.sh](#21-deploy-backend-deployprodsh)`
22. [Deploy admin: `./deploy.prod.sh](#22-deploy-admin-deployprodsh)`
23. [Amplify (frontend)](#23-amplify-frontend)
24. [Secrets](#24-secrets)
25. [Go-live checklist](#25-go-live-checklist)
26. [Troubleshooting](#26-troubleshooting)
27. [Appendix: Optional ALB + Auto Scaling](#appendix-optional-alb--auto-scaling)

---

## 1. Prerequisites


| Requirement                                                               | Purpose                                                                      |
| ------------------------------------------------------------------------- | ---------------------------------------------------------------------------- |
| AWS account                                                               | Billing and console access                                                   |
| IAM user with **programmatic access** (optional but used below)           | `aws configure` on your laptop for ECR/CLI                                   |
| **AWS CLI** on your laptop                                                | `aws ecr`, `aws sts`                                                         |
| **Docker** on your laptop                                                 | `docker build`, `docker push`                                                |
| **SSH client**                                                            | Deploy scripts SSH to EC2                                                    |
| `**.pem` key pair** for EC2                                               | `chmod 400` on the key                                                       |
| **Docker on EC2**                                                         | Installed via apt or user data (below)                                       |
| **MySQL client** on **API** EC2                                           | `mysql` to load `schema.sql` (§14); **admin** EC2 also has `**mysql`** (§16) |
| **Domain DNS**                                                            | A/CNAME for `api`, `admin`, and Amplify                                      |
| Repos `**quantum-edu-be`**, `**quantum-edu-admin**`, `**quantum_edu_fe**` | Source and `deploy.prod.sh`                                                  |


Pick one **AWS region** (e.g. `ap-south-1`) and use it for **RDS, ECR, EC2**.

On **Apple Silicon (M1/M2/M3)** Macs, always build with `**docker build --platform linux/amd64`** so the image runs on common x86 EC2 instance types.

---

## 2. What is Certbot?

**Certbot** is the standard client for **Let’s Encrypt**. It obtains **free TLS certificates**, proves domain control (usually HTTP on **port 80**), configures **nginx** (package `python3-certbot-nginx`), and sets up **automatic renewal** (certificates expire about every 90 days). **DNS:** `**api.…`** must point at the **API** **Elastic IP** before Certbot on the API server; `**admin.…`** at the **admin** **Elastic IP** before Certbot on the admin server (§18).

---

## 3. Architecture

```
Internet —HTTPS→ EC2 API (Elastic IP #1)
              nginx :443   api.example.com → http://127.0.0.1:8080 (Spring in Docker)
              :80 Certbot

Internet —HTTPS→ EC2 Admin (Elastic IP #2)   [can Stop when idle]
              nginx :443   admin.example.com → http://127.0.0.1:8000 (Gunicorn)
              :80 Certbot

Both EC2 instances —3306 (private)→ RDS MySQL: quantum_education, admin_django

Amplify —HTTPS→ Next.js ; NEXT_PUBLIC_API_URL=https://api.example.com
```

Do **not** open **8080** or **8000** to `0.0.0.0/0` on either security group; only **nginx** (80/443) faces the internet.

**Operations:** On the **API** server, `**deploy.prod.sh`** uses `**--restart unless-stopped**`, so after **reboot** or **Start**, Spring Boot returns without SSH. On the **admin** server, `**systemctl enable nginx`** and `**systemctl enable quantum-edu-admin**` (§20): a **non-technical** operator can use the AWS console **Instance state → Start** after a **Stop**, wait until **Running**, then open `**https://admin.…`**—no SSH. **RDS** must stay **Available** whenever you **Start** the admin instance.

---

## 4. Code and configuration (repos)

**Do not commit secrets.**

In `**quantum-edu-be`**, `**application-prod.properties**` uses `**TODO_REPLACE_***` defaults so misconfiguration is obvious; `**deploy.prod.env**` supplies real values at runtime via Docker `-e`.


| Area         | Action                                                                                                                                                                                                                                     |
| ------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Backend**  | Actuator `**/actuator/health`**; `**useSSL=true**` JDBC for RDS; `**APP_CORS_ORIGINS**`, `**VERIFICATION_BASE_URL**`, mail, **live** Razorpay env vars via `**deploy.prod.env`**.                                                          |
| **Frontend** | Amplify env `**NEXT_PUBLIC_API_URL`**. Optional `**NEXT_PUBLIC_R2_IMAGE_HOST**` for `**next.config.ts**`. Root `**amplify.yml**` with `npm ci` / `npm run build` if needed. Set **Node 20+** in Amplify build settings if the build fails. |
| **Admin**    | **`mysqlclient>=2.2.1`** on the server (Django 5.1+); RDS TLS in **`OPTIONS`** via **`mysql_rds_tls_fragment()`** (`**ssl_mode**` for mysqlclient, **`ssl`** dict for PyMySQL-only dev). `**deploy.prod.env**`: prod settings + **`ALLOWED_HOSTS**`.          |


Official references (not project docs): [RDS MySQL SSL](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/MySQL.Concepts.SSLSupport.html), [Amplify Next.js](https://docs.aws.amazon.com/amplify/latest/userguide/deploy-nextjs-app.html).

**Example `amplify.yml`** at the root of `quantum_edu_fe` (adjust if Amplify’s Next.js preset expects different artifacts):

```yaml
version: 1
frontend:
  phases:
    preBuild:
      commands:
        - nvm install 22
        - nvm use 22
        - node -v
        - npm ci --cache .npm --prefer-offline
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
      - .npm/**/*
```

---

## 5. Local: `deploy.prod.env`

**Backend (`quantum-edu-be`)**

```bash
cd quantum-edu-be
# Use the tracked example as a template, or edit the repo’s deploy.prod.env
cp deploy.prod.env.example deploy.prod.env   # if you are starting fresh
chmod 600 deploy.prod.env
```

Fill `**EC2_HOST**`, `**SSH_KEY**`, `**AWS_REGION**`, `**ECR_URI**`, `**RDS_***`, `**JWT_SECRET**`, `**APP_CORS_ORIGINS**`, `**VERIFICATION_BASE_URL**`, mail, **live** Razorpay keys.

**Admin (`quantum-edu-admin`)**

```bash
cd quantum-edu-admin
cp deploy.prod.env.example deploy.prod.env
chmod 600 deploy.prod.env
```

Set `**EC2_HOST**` to the **admin** instance’s **Elastic IP** (or DNS name resolving to it)—**not** the API server. Set `**ALLOWED_HOSTS`** to your `**admin.…**` hostname(s). Add **R2** variables only if you use uploads.

---

## 6. AWS: Create RDS MySQL

1. Open **[Amazon RDS](https://console.aws.amazon.com/rds/)** in your chosen region.
2. Click **Create database**.
3. **Choose a database creation method:** **Full configuration** (AWS may also label the alternative **Easy create**—**do not** use Easy create). Full configuration is required so you can set **VPC**, **Public access = No**, **security groups**, **gp3** storage, **backups**, and **encryption** explicitly.
4. **Engine type:** **MySQL**.
5. **Version:** **MySQL 8.0.x** (match what you use in dev).
6. **Template:** **Production** (or **Dev/Test** if you accept smaller SLA for a first cut).
7. **DB instance identifier:** e.g. `quantum-edu-mysql-production`.
8. **Master username:** e.g. `admin` (save it).
9. **Master password:** strong password (save in `**deploy.prod.env`** as `RDS_PASS`; never commit).
10. **Instance configuration:** e.g. **db.t3.small** or larger (not free-tier unless you intentionally use it).
11. **Storage:** **General Purpose SSD (gp3)**; size e.g. **50 GiB** (tune per need); enable storage autoscaling if you want.
12. **Connectivity:**
  - **VPC:** The **same VPC** as your **API** and **admin** EC2 instances (often the **default VPC**).
    - **Subnet group:** Default is fine if it spans multiple AZs.
    - **Public access:** **No**.
    - **VPC security group:** **Create new** → name it e.g. `sg-quantum-prod-rds` (you will edit inbound rules after EC2 security group exists).
13. **Initial database name:** `quantum_education` — creates the application database on first boot.
14. **Backup**, **encryption**, **maintenance window:** set per policy (production: enable backups, prefer encryption).
15. Click **Create database**. Wait until status is **Available** (several minutes).
16. Open the DB → **Connectivity & security** → copy **Endpoint** (hostname, no port) → put in `**deploy.prod.env`** as `**RDS_ENDPOINT**`. Port is **3306**.

After RDS is up, you still need a second database `**admin_django`** for Django (see §19).

---

## 7. AWS: Create ECR repository and push an image

**Create repository (CLI, from your laptop)**

```bash
aws ecr create-repository --repository-name quantum-edu-be-production --region YOUR_REGION
```

**Get account ID and build/push (Apple Silicon: add `--platform linux/amd64`)**

```bash
cd /path/to/quantum-edu-be

ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REGION=YOUR_REGION
ECR_REGISTRY="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
ECR_URI="${ECR_REGISTRY}/quantum-edu-be-production"

aws ecr get-login-password --region "${REGION}" | docker login --username AWS --password-stdin "${ECR_REGISTRY}"

docker build --platform linux/amd64 -t quantum-edu-be-production:latest .
docker tag quantum-edu-be-production:latest "${ECR_URI}:latest"
docker push "${ECR_URI}:latest"
```

Put `**ECR_URI**` (full string `ACCOUNT.dkr.ecr.REGION.amazonaws.com/quantum-edu-be-production`) into `**quantum-edu-be/deploy.prod.env**`.

**Alternative (console):** **ECR** → **Repositories** → **Create repository** → name `quantum-edu-be-production` → **Create**. URI is shown on the repository page.

---

## 8. AWS: IAM role for EC2

1. Open **[IAM](https://console.aws.amazon.com/iam/)** → **Roles** → **Create role**.
2. **Trusted entity:** **AWS service** → **EC2** → **Next**.
3. **Add permissions:** attach `**AmazonEC2ContainerRegistryReadOnly`** (lets the instance `docker pull` from ECR without static AWS keys on disk).
4. **Optional:** attach `**AmazonSSMManagedInstanceCore`** if you will use **Systems Manager Session Manager** instead of SSH from the open internet.
5. **Role name:** e.g. `quantum-edu-ecr-read-prod` → **Create role**.
6. Attach this role **only** to the **API** EC2 instance (§11). The **admin** EC2 (§12) does **not** need ECR access—leave **IAM instance profile** empty unless you use **SSM** (optional: `**AmazonSSMManagedInstanceCore`**).

---

## 9. AWS: Security groups

Create **three** security groups in the **same VPC** as RDS and EC2.

### A) `sg-quantum-prod-app` (attach to **API** EC2 only)


| Type  | Port | Source        | Note                        |
| ----- | ---- | ------------- | --------------------------- |
| SSH   | 22   | **My IP**     | Or omit if you use only SSM |
| HTTP  | 80   | **0.0.0.0/0** | Certbot HTTP-01             |
| HTTPS | 443  | **0.0.0.0/0** | Public nginx                |


**Do not** add inbound **8080** or **8000** for the world.

**Console:** **VPC** or **EC2** → **Security Groups** → **Create security group** → name `sg-quantum-prod-app` → add inbound rules above → **Create**.

### A2) `sg-quantum-prod-admin` (attach to **admin** EC2 only)

Same inbound pattern as **API** (**22** optional, **80**, **443**)—**no** **8080**/ **8000** to `0.0.0.0/0`. Name e.g. `sg-quantum-prod-admin`.

### B) `sg-quantum-prod-rds` (attach to **RDS**)

Create the group when creating RDS (§6), or **Edit** RDS’s security group afterward.


| Type         | Port | Source                                               |
| ------------ | ---- | ---------------------------------------------------- |
| MySQL/Aurora | 3306 | **Custom** → `**sg-quantum-prod-app`** (API EC2)     |
| MySQL/Aurora | 3306 | **Custom** → `**sg-quantum-prod-admin`** (admin EC2) |


No `**0.0.0.0/0**` on 3306.

---

## 10. AWS: Elastic IP

You need **two** Elastic IPs: **#1** for the **API** EC2, **#2** for the **admin** EC2. `**api.…`** DNS → **#1**; `**admin.…`** → **#2**. The **Amplify** site uses **CNAME/ANAME** targets (§23), **not** these IPs.

**Pricing:** Public IPv4 addresses are billed **per hour** while allocated—see **[Amazon VPC pricing](https://aws.amazon.com/vpc/pricing/)**. Use **one** public IPv4 per instance (EIP **or** auto-assign, not both). **[Elastic IP docs](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/elastic-ip-addresses-eip.html)**.

1. **EC2** → **Elastic IPs** → **Allocate Elastic IP address** → **Allocate** → repeat once so you have **two** unassociated EIPs (or allocate **#2** after §12).

---

## 11. AWS: Launch EC2 (API only)

This instance runs **only** the **Spring Boot** API (**Docker**) and **nginx** for `**api.…`**. It does **not** run Django admin.

1. **EC2** → **Launch instance**.
2. **Name:** e.g. `quantum-edu-api-production`.
3. **AMI:** **Ubuntu Server 22.04 LTS** or **24.04 LTS**.
4. **Instance type:** e.g. **t3.small** (or larger).
5. **Key pair:** choose or create a key; download `**.pem`**; on your laptop: `**chmod 400 key.pem**`.
6. **Network settings:**
  - **VPC:** Same as **RDS** (§6).
    - **Subnet:** A **public** subnet (route to an Internet Gateway).
    - **Auto-assign public IP:** **Enable** (you still attach **Elastic IP #1** for a stable address).
    - **Firewall:** `**sg-quantum-prod-app`** (§9).
7. **Configure storage:** at least **30 GiB** **gp3**.
8. **Advanced details** → **IAM instance profile:** role from §8 (**ECR read**).
9. **User data** (optional; installs Docker on first boot):

```bash
#!/bin/bash
set -e
apt update -y
apt install -y docker.io curl unzip
systemctl start docker
systemctl enable docker
usermod -aG docker ubuntu
curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip
unzip -q /tmp/awscliv2.zip -d /tmp
/tmp/aws/install
rm -rf /tmp/aws /tmp/awscliv2.zip
```

**(ARM instance:** use `https://awscli.amazonaws.com/awscli-exe-linux-aarch64.zip` instead.)

1. **Launch instance**. Wait until **Running**.
2. **Actions** → **Networking** → **Associate Elastic IP address** → choose **first** EIP from §10 → **Associate**.
3. Note **Elastic IP #1** → set `**EC2_HOST`** in `**quantum-edu-be/deploy.prod.env**` only.
4. SSH user: `**ubuntu**`.

---

## 12. AWS: Launch EC2 (admin only, on-demand)

Small instance for **Django admin** only. **Stop** it when no one needs the admin UI to save cost; **Start** it from the console when needed—**§20** ensures **nginx** + **Gunicorn** start automatically (no SSH for operators).

1. **EC2** → **Launch instance**.
2. **Name:** e.g. `quantum-edu-admin-production`.
3. **AMI:** Same Ubuntu **22.04** / **24.04** as API.
4. **Instance type:** e.g. `**t3.nano`** or `**t3.micro**` (low traffic admin).
5. **Key pair:** same or separate `**.pem`** as API (store paths in `**quantum-edu-admin/deploy.prod.env**` `**SSH_KEY**`).
6. **Network:**
  - **Same VPC** and **public subnet** as API.
    - **Auto-assign public IP:** **Enable**.
    - **Security group:** `**sg-quantum-prod-admin`** (§9).
7. **Storage:** e.g. **20–30 GiB** **gp3**.
8. **Advanced details** → **IAM instance profile:** leave **empty** (no ECR), or add **SSM** only if you use Session Manager.
9. **No** Docker user data required (optional: `apt update` only).
10. **Launch** → **Running** → **Associate Elastic IP address** → **second** EIP from §10.
11. Note **Elastic IP #2** → set `**EC2_HOST`** in `**quantum-edu-admin/deploy.prod.env**`.

---

## 13. AWS: Allow EC2 → RDS

If inbound rules are missing on `**sg-quantum-prod-rds**`:

1. **RDS** → your database → open `**sg-quantum-prod-rds`** (or **Modify** DB → security groups).
2. **Inbound rules** must allow **3306** from `**sg-quantum-prod-app`** **and** from `**sg-quantum-prod-admin`** (§9).
3. **Save**.

---

## 14. EC2 (API): Install Docker, nginx, Certbot

SSH to **API** **Elastic IP #1**:

```bash
ssh -i /path/to/prod.pem ubuntu@YOUR_API_ELASTIC_IP

sudo apt update
sudo apt install -y docker.io nginx certbot python3-certbot-nginx mysql-client curl unzip
sudo systemctl enable --now docker
sudo systemctl enable nginx
sudo usermod -aG docker ubuntu

# AWS CLI v2 (Ubuntu 24.04+ often has no apt package `awscli`)
curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip
unzip -q /tmp/awscliv2.zip -d /tmp
sudo /tmp/aws/install --update
rm -rf /tmp/aws /tmp/awscliv2.zip
aws --version
```

**ARM (Graviton):** use `**awscli-exe-linux-aarch64.zip`**. If `**apt install awscli**` works on your Ubuntu release, you may use that instead of the zip.

Log out and back in so `**docker**` group applies. Verify `**docker ps**` and `**aws --version**`.

---

## 15. EC2 (API): nginx for API + Certbot

Replace `**api.yourdomain.com**` with your hostname.

```bash
sudo nano /etc/nginx/sites-available/quantum-edu-api
```

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
```

```bash
sudo ln -sf /etc/nginx/sites-available/quantum-edu-api /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
```

**TLS** after `**api.…`** **A** record → **Elastic IP #1** (§18):

```bash
sudo certbot --nginx -d api.yourdomain.com
```

---

## 16. EC2 (admin): Install nginx, Certbot, MySQL client

SSH to **admin** **Elastic IP #2**:

```bash
ssh -i /path/to/admin-key.pem ubuntu@YOUR_ADMIN_ELASTIC_IP

sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx mysql-client python3-venv \
  default-libmysqlclient-dev pkg-config build-essential python3-dev
sudo systemctl enable nginx
```

Install **`python3-venv`** so `python3 -m venv .venv` works on minimal Ubuntu; **`deploy.prod.sh`** also installs **`mysqlclient`** build deps (`**default-libmysqlclient-dev**`, etc.) before **`pip install`**. Tighten RDS TLS later with **`ssl_ca`** if you require CA verification.

---

## 17. EC2 (admin): nginx for Django admin + Certbot

Replace `**admin.yourdomain.com**`.

```bash
sudo nano /etc/nginx/sites-available/quantums-edu-admin
```

```nginx
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
sudo ln -sf /etc/nginx/sites-available/quantum-edu-admin /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
```

**TLS** after `**admin.…`** **A** record → **Elastic IP #2** (§18):

```bash
sudo certbot --nginx -d admin.yourdomain.com
```

---

## 18. DNS

### 18.1 Records you need (any provider)


| Name                                     | Type            | Value                                               |
| ---------------------------------------- | --------------- | --------------------------------------------------- |
| `api.yourdomain.com`                     | **A**           | **Elastic IP #1** (API EC2)                         |
| `admin.yourdomain.com`                   | **A**           | **Elastic IP #2** (admin EC2)                       |
| `www` / apex (marketing site on Amplify) | Per **Amplify** | **Domain management** → **CNAME** / **ANAME** (§23) |


Put **API** IP in `**quantum-edu-be/deploy.prod.env`** as `**EC2_HOST**`; **admin** IP in `**quantum-edu-admin/deploy.prod.env`**. Run **Certbot** on **each** server after its DNS row propagates (§15 and §17).

### 18.2 Domain registered at GoDaddy (website on your domain)

Your domain can stay **registered** at GoDaddy; you only configure **DNS** so names point at **AWS**.

1. Sign in at **[godaddy.com](https://www.godaddy.com)** → **My Products** → your domain → **DNS** (**Manage DNS**).
2. **API — A record**
  - **Type:** **A**  
  - **Name / Host:** `api`  
  - **Value / Points to:** **Elastic IP #1** (API EC2)  
  - **TTL:** 600 seconds (or default)  
  - Save.
3. **Admin — A record**
  - **Type:** **A**  
  - **Name:** `admin`  
  - **Value:** **Elastic IP #2** (dedicated admin EC2, §12)  
  - Save.
4. **Marketing site (Amplify)** — do **not** point `www` at the EC2 Elastic IP unless you intentionally host the homepage there. Typical flow:
  - In **[AWS Amplify](https://console.aws.amazon.com/amplify/)** → your app → **Domain management** → **Add domain** → enter **apex** (`yourdomain.com`) and/or `**www`**.  
  - Amplify shows **verification** records (sometimes a **CNAME** to `verify.…`) and **routing** records (e.g. `**www`** → `**xxxxx.cloudfront.net**`).  
  - In GoDaddy **DNS**, **Add** each record **exactly** as Amplify lists (type, host, value).  
  - **Apex** (`@`): if GoDaddy offers **ANAME** / **ALIAS** to a hostname, use Amplify’s target; otherwise use **GoDaddy forwarding** from apex to `**https://www.…`** or follow **Amplify’s** doc for apex + GoDaddy (UI names vary).
5. Wait for propagation (minutes to a few hours). Check: `**dig +short api.…`** → **IP #1**; `**dig +short admin.…`** → **IP #2**.
  Visit `**https://api.…/actuator/health`** only **after** Certbot on the **API** host (§15). `**https://admin.…`** after Certbot on the **admin** host (§17).
6. **Email (optional):** If you use GoDaddy **email** or **Microsoft 365** on the same domain, do **not** delete existing **MX** / SPF records; only add/change the `**api`**, `**admin**`, and **Amplify** rows.

**Other registrars** (Cloudflare, Namecheap, Route 53, etc.): same logical records—`**api`** and `**admin**` **A** → EIP; `**www`/apex** per Amplify.

---

## 19. Database: schema and Django

**Create `admin_django`** — run on **either** EC2 (here: **admin** server after §16 if `**mysql` client** is installed):

```bash
mysql -h YOUR_RDS_ENDPOINT -P 3306 -u YOUR_RDS_USER -p \
  -e "CREATE DATABASE IF NOT EXISTS admin_django CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

**Load Java schema into `quantum_education`** — use the **API** instance (has DB tools from §14 if you install `**mysql-client`** there too, or use **admin** host with `**mysql-client`** from §16).

From your laptop (API **Elastic IP #1**):

```bash
cd /path/to/quantum-edu-be
scp -i /path/to/prod.pem docs/schema.sql ubuntu@YOUR_API_ELASTIC_IP:~/schema.sql
```

SSH to **API** host, then:

```bash
sudo apt install -y mysql-client   # if not already installed
mysql -h YOUR_RDS_ENDPOINT -P 3306 -u YOUR_RDS_USER -p quantum_education < ~/schema.sql
```

Enter the password when prompted (avoid `**mysql -pSecret**` on shared shells—shows in process list).

**Django migrations** (on admin EC2, after `**./deploy.prod.sh`** has created `**gunicorn.env**` — §22):

```bash
cd ~/quantum_edu_admin
source .venv/bin/activate
set -a && source gunicorn.env && set +a
python manage.py migrate --database=default
python manage.py createsuperuser
```

Spring Boot continues to own `**quantum_education**` tables; do not run Django migrations against that database for `admin_app` models (router blocks it).

---

## 20. Gunicorn systemd (admin EC2)

On the **admin** instance only. **Production:** bind **Gunicorn to localhost only** so only nginx reaches it.

**Goal:** After **reboot** or **Instance state → Stop** then **Start** in the AWS console, `**https://admin.…`** works with **no SSH**—suitable for a **non-technical** operator. One-time setup: `**systemctl enable nginx`** (§16) and `**systemctl enable quantum-edu-admin**` (below).

After the first `**./deploy.prod.sh**` (§22) has created `**~/quantum_edu_admin/gunicorn.env**`, SSH to the **admin** instance and create `**/etc/systemd/system/quantum-edu-admin.service`**:

```ini
[Unit]
Description=Quantum Edu Django Admin (Gunicorn)
Wants=network-online.target
After=network-online.target

[Service]
User=ubuntu
Group=ubuntu
WorkingDirectory=/home/ubuntu/quantum_edu_admin
EnvironmentFile=/home/ubuntu/quantum_edu_admin/gunicorn.env
ExecStart=/home/ubuntu/quantum_edu_admin/.venv/bin/gunicorn \
  --bind 127.0.0.1:8000 \
  --workers 2 \
  --access-logfile - \
  --error-logfile - \
  quantum_admin.wsgi:application
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable quantum-edu-admin
sudo systemctl start quantum-edu-admin
sudo systemctl status quantum-edu-admin
```

**One-time verification (recommended):** `sudo reboot`. When the instance is **Running** again, from your laptop:

```bash
curl -sS -o /dev/null -w "%{http_code}\n" https://admin.yourdomain.com/admin/
```

Expect **200**, **302**, or **401** (Django redirect/login)—not connection refused. On the server, `**systemctl is-active nginx quantum-edu-admin`** should show **active** for both.

**Note:** Repo file `**quantum-edu-admin/docs/quantum-edu-admin.service`** may bind `**0.0.0.0:8000**` for staging. **Production** should use `**127.0.0.1:8000`** as above **or** keep port 8000 off the EC2 security group (§9 already blocks public 8000).

`**Restart=always`** plus `**RestartSec=5**` helps if Gunicorn starts a few seconds before **RDS** is reachable; systemd will retry until the DB accepts connections.

**Console workflow (ongoing):** **EC2** → `**quantum-edu-admin-production`** → **Instance state** → **Stop** (save money) or **Start** (use admin). Wait until **Running** and status checks pass, then open `**https://admin.yourdomain.com/admin/`** in a browser. If it fails, `**journalctl -u quantum-edu-admin -e**` on SSH for diagnosis.

---

## 21. Deploy backend: `./deploy.prod.sh`

On your laptop (Docker running, `**aws configure**` done if you use CLI for ECR):

```bash
cd quantum-edu-be
./deploy.prod.sh
```

Optional: `**DEPLOY_PROD_ENV_FILE=/path/to/env ./deploy.prod.sh**`

Verify:

```bash
curl -sS -o /dev/null -w "%{http_code}\n" https://api.yourdomain.com/actuator/health
```

---

## 22. Deploy admin: `./deploy.prod.sh`

```bash
cd quantum-edu-admin
./deploy.prod.sh
```

Then complete **§20** once on the **admin** EC2: `**systemctl enable` + `start`** for **quantum-edu-admin**, confirm **nginx** is **enabled** (§16), and run the **reboot / curl** check. After that, code **redeploys** use `**./deploy.prod.sh`** only; **Stop**/**Start** from the console does not require SSH for services to run.

---

## 23. Amplify (frontend)

1. **[AWS Amplify console](https://console.aws.amazon.com/amplify/)** → **Host web app** → connect **Git** → repository `**quantum_edu_fe`**, branch `**main**` (or your release branch).
2. **Environment variables (required for build):**
  - `**NEXT_PUBLIC_API_URL`** = `https://api.yourdomain.com` — **must** be set before `**next build**`; App Router pages call the API during the build. Use your real **HTTPS API** origin (same as in §18 `**api`** DNS).
  - `**NEXT_PUBLIC_R2_IMAGE_HOST**` = R2 public hostname only (no `https://`), if you use `**next.config.ts**` conditional R2 host.
3. **Build:** Repo `**amplify.yml`** uses **`nvm use 22`** (Next.js 16 needs **Node ≥ 20.9**). If you override the build spec in the console, keep the same Node major or the build can fail with an engine error.
4. **Domain:** **Domain management** → add `**www`** and apex; create the DNS records Amplify shows.
5. **CORS:** `APP_CORS_ORIGINS` on the API must include your Amplify URL and custom **https** origins.
6. **Cloudflare R2:** If the browser loads media from R2, add **CORS** on the bucket allowing your FE origins.

---

## 24. Secrets

- `**deploy.prod.env`** is **gitignored**; never commit it.
- Use **production-only** passwords, **JWT**, **Django secret**, and **live** Razorpay keys.
- `**chmod 600 deploy.prod.env`** on your laptop restricts read access to your user.

---

## 25. Go-live checklist

- RDS **not** publicly accessible; **3306** from `**sg-quantum-prod-app`** **and** `**sg-quantum-prod-admin`**
- Both EC2 SGs: **no** public **8080** / **8000**
- **HTTPS** for **api** (§15) and **admin** (§17) via Certbot
- **DNS** (§18): `**api`** → **EIP #1**, `**admin`** → **EIP #2**; **Amplify** records at registrar (e.g. **GoDaddy** §18.2)
- `**deploy.prod.env`**: `**quantum-edu-be**` `EC2_HOST` = API IP; `**quantum-edu-admin**` `EC2_HOST` = admin IP
- Admin: `**nginx**` + `**quantum-edu-admin**` `**systemctl enable`d** (§20); after console **Start**, `**https://admin…`** works without SSH
- API: Docker `**--restart unless-stopped**`; after **reboot**, `**curl https://api…/actuator/health`** OK
- `**SPRING_PROFILES_ACTIVE=prod**` in Docker
- `**quantum_admin.settings.prod**` in `**gunicorn.env**`
- Amplify `**NEXT_PUBLIC_API_URL**` = `https://api…`
- Razorpay **live** keys when taking real money
- `**sudo certbot renew --dry-run`** on **both** EC2s (or cron/timer default)

---

## 26. Troubleshooting


| Symptom                             | What to check                                                                                                                                                                                                                                               |
| ----------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `**docker login` / ECR push fails** | `**aws configure`** on laptop; use **registry host only** (not `repo/path`) with `get-login-password`. Region must match repo.                                                                                                                              |
| **Image runs on Mac but not EC2**   | Rebuild with `**--platform linux/amd64`**.                                                                                                                                                                                                                  |
| **Cannot reach RDS from EC2**       | Same **VPC**; RDS SG inbound **3306** from **EC2 SG**; correct **endpoint** and credentials.                                                                                                                                                                |
| **MySQL “Public Key Retrieval”**    | Add `**allowPublicKeyRetrieval=true`** to JDBC URL if your stack requires it.                                                                                                                                                                               |
| **502 from nginx**                  | Spring container down: `**sudo docker ps`**, `**docker logs quantum-edu-be**`; Gunicorn: `**journalctl -u quantum-edu-admin -e**`. After **reboot**, if admin never comes up: `**systemctl status nginx quantum-edu-admin`** — ensure both are **enabled**. |
| **Certbot fails**                   | `**api` → EIP #1** on API host; `**admin` → EIP #2** on admin host; port **80** open on each SG.                                                                                                                                                            |
| **CORS errors in browser**          | `**APP_CORS_ORIGINS`** must list exact **https** origins (Amplify + custom).                                                                                                                                                                                |


---

## Appendix: Optional ALB + Auto Scaling

If you need **multiple API instances** or want **TLS termination only on AWS** with **ACM** (no Certbot on EC2), add an **Application Load Balancer**, **target group** on **8080**, and an **Auto Scaling Group**. That adds monthly **ALB** cost and more setup (subnets across AZs, health checks, instance refresh). The **admin** instance in this guide remains a **single** small EC2 unless you redesign it.

---

*End of production guide — **two EC2** (API + admin) + nginx + Certbot + Amplify; `**deploy.prod.sh`** per repo.*