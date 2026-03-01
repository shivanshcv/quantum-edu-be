# Deployment Guide: Backend on Railway/Render + Netlify Frontend

This guide helps you deploy `quantum-edu-be` so it works with your Netlify frontend, matching local behavior.

---

## Staging: Render + Aiven (Recommended for Staging)

**Profile:** `staging` | **JWT bypass:** Yes (same as dev, for easier testing)

### Step 1: Create Free MySQL on Aiven

1. Go to [aiven.io/free-mysql-database](https://aiven.io/free-mysql-database)
2. Sign up (no credit card)
3. Create a MySQL service (free plan)
4. In **Service overview** → **Connection information**, note:
   - **Host** (e.g. `mysql-xxx.aivencloud.com`)
   - **Port** (e.g. `12345`)
   - **User** (e.g. `avnadmin`)
   - **Password**
   - **Database** (default or create one, e.g. `defaultdb`)

**Aiven allows public connections by default.** If you need to restrict IPs, use **Settings** → **IP allowlist**.

### Step 2: Copy Local Database to Aiven (or Run Migrations)

**Option A: Copy your local MySQL database (all tables + data)**

Use `mysqldump` to export, then import into Aiven. From your machine:

```bash
# 1. Export local database (--no-create-db so we can import into defaultdb)
mysqldump -h 127.0.0.1 -u root -proot --no-create-db quantum_education > /tmp/quantum_education_dump.sql

# 2. Import into Aiven defaultdb (replace placeholders with your Aiven credentials)
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p'<AIVEN_PASSWORD>' <AIVEN_DATABASE> < /tmp/quantum_education_dump.sql
```

**One-liner** (dump and import in one go):
```bash
mysqldump -h 127.0.0.1 -u root -proot --no-create-db quantum_education | mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p'<AIVEN_PASSWORD>' <AIVEN_DATABASE>
```

**Note:** `--no-create-db` puts all tables into `defaultdb`. Keep `MYSQL_DATABASE=defaultdb` in Render.

**Option B: Run migrations from scratch (fresh schema + seed data)**

```bash
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/schema.sql
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/seed-test-data.sql
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/migrations/add-lms-course-saisseru.sql
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/migrations/add-modules-for-locked-flow.sql
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/migrations/add-user-assessment-result.sql
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/migrations/add-quiz-test-data-saisseru.sql
```

### Step 3: Deploy Backend on Render

**Important:** Render's native env has no Java/Maven. Use **Docker** (Dockerfile included).

1. Go to [render.com](https://render.com) → Sign up with GitHub
2. **New** → **Web Service**
3. Connect `quantum-edu-be` repo
4. In the setup form, find the **Environment** or **Language** dropdown (often near the top). Set it to **Docker**.
5. **Build & Deploy:**
   - **Dockerfile Path:** `./Dockerfile` (or leave blank if in repo root)
   - **Instance Type:** Free
   - Clear any **Build Command** or **Start Command** – the Dockerfile handles both
6. **Environment** → Add variables (see table below)
7. **Create Web Service** → Render will build and deploy
8. Note your service URL (e.g. `https://quantum-edu-be-staging.onrender.com`)

**If you already created a service:** Render may not let you change Language after creation. Delete the service and create a new one, selecting **Docker** as the Environment/Language when prompted.

### Step 4: Environment Variables for Staging

**All variables have defaults in `application-staging.properties`.** If you don't add any env vars, the app will use the defaults (Aiven, CORS, Razorpay test keys). Override in Render to use different values.

| Variable | Required | Default (in code) | Example / Notes |
|----------|----------|------------------|----------------|
| `SPRING_PROFILES_ACTIVE` | Yes* | *(set via Start Command)* | `staging` |
| `MYSQL_HOST` | No | `mysql-6893282-shivansh-6f29.j.aivencloud.com` | From Aiven |
| `MYSQL_PORT` | No | `25679` | From Aiven |
| `MYSQL_DATABASE` | No | `defaultdb` | From Aiven |
| `MYSQL_USER` | No | `avnadmin` | From Aiven |
| `MYSQL_PASSWORD` | No | *(default in code)* | From Aiven – override in Render (Secret) for security |
| `JWT_SECRET` | No | `quantum-edu-jwt-secret-key-min-256-bits-for-hs256-algorithm` | Override for prod |
| `APP_CORS_ORIGINS` | No | `https://quantum-education.netlify.app` | Your staging FE URL |
| `VERIFICATION_BASE_URL` | No | `https://quantum-education.netlify.app/verify-email` | FE verify-email URL |
| `RAZORPAY_KEY_ID` | No | `rzp_test_SLepofnigNDLcv` | Razorpay test key |
| `RAZORPAY_KEY_SECRET` | No | *(set in code)* | Razorpay test secret – override (Secret) |
| `RAZORPAY_WEBHOOK_SECRET` | No | `shivansh1234` | From Razorpay webhook config |
| `EMAIL_FROM` | No | `noreply@quantumedu.com` | Sender for verification emails |
| `JWT_DEV_BYPASS` | No | `true` | Allows expired tokens for staging testing |

**Copy-paste for Render** (add in Environment tab; mark secrets as Secret):

```
SPRING_PROFILES_ACTIVE=staging
MYSQL_HOST=mysql-6893282-shivansh-6f29.j.aivencloud.com
MYSQL_PORT=25679
MYSQL_DATABASE=defaultdb
MYSQL_USER=avnadmin
MYSQL_PASSWORD=<from Aiven – set as Secret>
JWT_SECRET=quantum-edu-jwt-secret-key-min-256-bits-for-hs256-algorithm
APP_CORS_ORIGINS=https://quantum-education.netlify.app
VERIFICATION_BASE_URL=https://quantum-education.netlify.app/verify-email
RAZORPAY_KEY_ID=rzp_test_SLepofnigNDLcv
RAZORPAY_KEY_SECRET=<set as Secret>
RAZORPAY_WEBHOOK_SECRET=shivansh1234
EMAIL_FROM=noreply@quantumedu.com
JWT_DEV_BYPASS=true
```

### Step 5: Razorpay Webhook for Staging

1. Razorpay Dashboard → **Webhooks** → Add endpoint
2. **URL:** `https://your-render-url.onrender.com/api/v1/cart/webhook/razorpay`
3. **Events:** `payment.captured`
4. Copy **Webhook Secret** → set as `RAZORPAY_WEBHOOK_SECRET` in Render

### Step 6: Update Frontend

Set your staging FE API base URL to the Render URL and redeploy.

---

## Config Values to Provide (Checklist)

Before deploying, gather these values:

| # | Config | Where to get it |
|---|--------|-----------------|
| 1 | `MYSQL_HOST` | Aiven service → Connection info |
| 2 | `MYSQL_PORT` | Aiven service → Connection info |
| 3 | `MYSQL_DATABASE` | Aiven service → Connection info |
| 4 | `MYSQL_USER` | Aiven service → Connection info |
| 5 | `MYSQL_PASSWORD` | Aiven service → Connection info |
| 6 | `JWT_SECRET` | Generate: `openssl rand -base64 32` |
| 7 | `APP_CORS_ORIGINS` | Your staging FE URL (e.g. Netlify preview) |
| 8 | `VERIFICATION_BASE_URL` | `https://<your-fe>/verify-email` |
| 9 | `RAZORPAY_KEY_ID` | Razorpay Dashboard → API Keys (test) |
| 10 | `RAZORPAY_KEY_SECRET` | Razorpay Dashboard → API Keys (test) |
| 11 | `RAZORPAY_WEBHOOK_SECRET` | Razorpay Dashboard → Webhooks → after creating endpoint |

---

## Option 1: Railway (Easiest, $5 Trial Credit)

**Best for:** Quick setup, minimal config. Railway gives $5 free credit (credit card required).

### 1.1 Create Railway Account
- Go to [railway.app](https://railway.app) → Sign up with GitHub
- Add payment method (you won't be charged until $5 credit is used)

### 1.2 Add MySQL Database
1. **New Project** → **Add Service** → **Database** → **MySQL**
2. Wait for MySQL to provision
3. Go to MySQL service → **Variables** tab → copy `MYSQL_URL` or note:
   - `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_DATABASE`

### 1.3 Deploy Backend
1. **Add Service** → **GitHub Repo** → select `quantum-edu-be`
2. Railway auto-detects Java/Maven
3. **Settings** → **Build**:
   - **Build Command:** `mvn clean package -DskipTests`
   - **Start Command:** `java -jar app/target/quantum-edu-be-app-0.0.1-SNAPSHOT.jar`
   - **Root Directory:** (leave empty, or `/` if needed)
4. **Settings** → **Networking** → **Generate Domain** (e.g. `quantum-edu-be-production.up.railway.app`)

### 1.4 Set Environment Variables
In your backend service → **Variables** tab, add:

| Variable | Value | Notes |
|----------|-------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Use prod profile |
| `MYSQL_HOST` | *(from MySQL service)* | Railway MySQL host |
| `MYSQL_PORT` | `3306` | |
| `MYSQL_DATABASE` | *(from MySQL service)* | |
| `MYSQL_USER` | *(from MySQL service)* | |
| `MYSQL_PASSWORD` | *(from MySQL service)* | |
| `JWT_SECRET` | *(generate strong secret)* | e.g. `openssl rand -base64 32` |
| `APP_CORS_ORIGINS` | `https://your-netlify-site.netlify.app` | Your Netlify URL |
| `EMAIL_FROM` | `noreply@yourdomain.com` | |
| `VERIFICATION_BASE_URL` | `https://your-netlify-site.netlify.app/verify-email` | Netlify FE URL |
| `RAZORPAY_KEY_ID` | *(your Razorpay key)* | |
| `RAZORPAY_KEY_SECRET` | *(your Razorpay secret)* | |
| `RAZORPAY_WEBHOOK_SECRET` | *(your webhook secret)* | |

**Link MySQL variables:** In Railway, you can reference the MySQL service's variables. Or use **Connect** on MySQL → copy the connection URL and set `MYSQL_*` vars manually.

### 1.5 Run Migrations on Railway MySQL
After MySQL is up, run your schema and seed data. Options:

**A) Railway MySQL CLI (if available):** Use the MySQL connection string from Variables.

**B) From your local machine** (if Railway MySQL is publicly reachable):
```bash
mysql -h <MYSQL_HOST> -P 3306 -u <MYSQL_USER> -p<MYSQL_PASSWORD> <MYSQL_DATABASE> < docs/schema.sql
mysql -h <MYSQL_HOST> -P 3306 -u <MYSQL_USER> -p<MYSQL_PASSWORD> <MYSQL_DATABASE> < docs/seed-test-data.sql
mysql -h <MYSQL_HOST> -P 3306 -u <MYSQL_USER> -p<MYSQL_PASSWORD> <MYSQL_DATABASE> < docs/migrations/add-lms-course-saisseru.sql
mysql -h <MYSQL_HOST> -P 3306 -u <MYSQL_USER> -p<MYSQL_PASSWORD> <MYSQL_DATABASE> < docs/migrations/add-modules-for-locked-flow.sql
```

**C) Railway MySQL is often private.** Use Railway's **Connect** to get a public URL, or run migrations from a one-off deploy/script.

### 1.6 Update Netlify Frontend
In your FE repo, set the API base URL to your Railway backend:
```
VITE_API_BASE_URL=https://quantum-edu-be-production.up.railway.app
```
(or whatever env var your FE uses for the API URL). Redeploy Netlify.

---

## Option 2: Render + Aiven (Production Profile)

Same as Staging section above, but use `SPRING_PROFILES_ACTIVE=prod` and set `JWT_DEV_BYPASS=false`. See Option 1.4 for prod env vars.

### 2.1 Create Free MySQL on Aiven
1. Go to [aiven.io/free-mysql-database](https://aiven.io/free-mysql-database)
2. Sign up (no credit card)
3. Create a MySQL service (free plan)
4. Note: **Host**, **Port**, **User**, **Password**, **Database** from the service details

### 2.2 Deploy Backend on Render
1. Go to [render.com](https://render.com) → Sign up with GitHub
2. **New** → **Web Service**
3. Connect `quantum-edu-be` repo
4. **Build & Deploy:**
   - **Build Command:** `mvn clean package -DskipTests`
   - **Start Command:** `java -Dspring.profiles.active=staging -jar app/target/quantum-edu-be-app-0.0.1-SNAPSHOT.jar` (or `prod` for production)
   - **Instance Type:** Free

5. **Environment Variables** (add all from the table in Option 1.4, using Aiven MySQL credentials)

6. **Advanced** → Generate a URL (e.g. `quantum-edu-be.onrender.com`)

### 2.3 Run Migrations
Aiven MySQL is publicly reachable. From your machine:
```bash
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/schema.sql
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/seed-test-data.sql
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/migrations/add-lms-course-saisseru.sql
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/migrations/add-modules-for-locked-flow.sql
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/migrations/add-user-assessment-result.sql
mysql -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p<AIVEN_PASSWORD> <AIVEN_DATABASE> < docs/migrations/add-quiz-test-data-saisseru.sql
```

### 2.4 Update Netlify
Set your FE API URL to the Render URL and redeploy.

---

## Option 3: Railway Backend + Aiven MySQL (Hybrid)

Use Railway's $5 credit for the backend (simpler deploys) and Aiven's free MySQL. Follow Option 1 for Railway, but use Aiven credentials for `MYSQL_*` instead of Railway MySQL.

---

## CORS: Netlify ↔ Backend

Your backend already supports `APP_CORS_ORIGINS`. Set it to your Netlify URL(s):

```
APP_CORS_ORIGINS=https://your-site.netlify.app,https://your-site.netlify.app
```

For custom domain:
```
APP_CORS_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

---

## Razorpay Webhook (Production)

1. In Razorpay Dashboard → **Webhooks** → Add endpoint
2. **URL:** `https://your-backend-url.up.railway.app/api/v1/cart/webhook/razorpay`
3. **Events:** `payment.captured`
4. Copy the **Webhook Secret** and set `RAZORPAY_WEBHOOK_SECRET` in your backend env vars

---

## Checklist Before Going Live

- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `JWT_SECRET` is a strong random value (not dev default)
- [ ] `APP_CORS_ORIGINS` includes your Netlify URL
- [ ] `VERIFICATION_BASE_URL` points to Netlify `/verify-email`
- [ ] MySQL schema + migrations applied
- [ ] Razorpay webhook URL updated for production
- [ ] `app.jwt.dev-bypass` is `false` in prod (default in prod profile)

---

## Quick Reference: Build & Start Commands

| Platform | Profile | Build | Start |
|----------|---------|-------|-------|
| Railway | prod | `mvn clean package -DskipTests` | `java -jar app/target/quantum-edu-be-app-0.0.1-SNAPSHOT.jar` |
| Render (staging) | staging | `mvn clean package -DskipTests` | `java -Dspring.profiles.active=staging -jar app/target/quantum-edu-be-app-0.0.1-SNAPSHOT.jar` |
| Render (prod) | prod | `mvn clean package -DskipTests` | `java -Dspring.profiles.active=prod -jar app/target/quantum-edu-be-app-0.0.1-SNAPSHOT.jar` |

---

## Port (Railway/Render)

Railway and Render set `PORT`. Our prod config uses `server.port=${PORT:8080}` so it works automatically.

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| CORS errors from Netlify | Add Netlify URL to `APP_CORS_ORIGINS` |
| DB connection failed | Check `MYSQL_*` vars; ensure MySQL allows connections from your host (Aiven: check allowed IPs) |
| 401 on protected routes | Ensure FE sends `Authorization: Bearer <token>` |
| Build fails | Ensure `mvn clean package` works locally; check Java version (21) |
| Render spins down | First request after 15 min idle may take ~1 min; this is expected on free tier |
