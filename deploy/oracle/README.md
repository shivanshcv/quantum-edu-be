# Zero-cost production deployment

```
Browser ──► Vercel (Next.js frontend)            https://<project>.vercel.app
   │
   ├──► Oracle Cloud Always Free ARM VM (Docker Compose)
   │      Caddy (HTTPS, Let's Encrypt)
   │        ├─ https://<api>.duckdns.org    ─► backend  (Spring Boot, :8080)
   │        └─ https://<admin>.duckdns.org  ─► admin    (Django + gunicorn, :8000)
   │      MySQL 8.4 (quantum_education + admin_django, nightly backups)
   │
   └──► Cloudflare R2 public bucket (thumbnails, videos, PDFs)
Gmail SMTP for verification emails · Razorpay in Test Mode for checkout
```

| Piece | Service | Free limit that matters |
|---|---|---|
| Frontend | Vercel Hobby | Non-commercial use (portfolio is fine) |
| Backend, admin, DB | Oracle Always Free `VM.Standard.A1.Flex` | 4 OCPU / 24 GB total, 200 GB block storage |
| Hostnames | DuckDNS | 5 subdomains |
| Media | Cloudflare R2 | 10 GB storage, zero egress fees |
| Email | Gmail App Password | ~500 emails/day |
| Payments | Razorpay Test Mode | No KYC, no real money |

## Files in this folder

| File | Purpose |
|---|---|
| `docker-compose.yml` | mysql, backend, admin, caddy |
| `Caddyfile` | HTTPS reverse proxy for both DuckDNS hostnames |
| `mysql/init/*.sh` | First boot only: create both databases + app user, load `docs/schema.sql`, load sample data |
| `seed/catalog.json` | Source of truth for the demo catalogue, learners, orders, progress |
| `../seed/build.py` | `sql` → `seed/sample-data.sql`; `media` → `seed/media/out/` (thumbnails, avatars, narrated videos, PDFs) |
| `init-env.sh` | Creates `../../deploy.prod.env` with random secrets |
| `../upload-media.sh` | Uploads `deploy/seed/media/out` to R2 under `seed/` and sets bucket CORS |
| `bootstrap-vm.sh` | Run once on the VM by `deploy.prod.sh --bootstrap` (Docker, firewall, swap, log rotation, backup cron) |
| `backup-db.sh` | Nightly `mysqldump` (7-day retention, optional copy to a private R2 bucket) |

`../../deploy.prod.sh` (in the backend repo root) does the deploy from your laptop.

---

## 0. Decide names first

The frontend needs the API URL at build time, and the backend needs the frontend URL for CORS. You choose both names yourself, so decide them up front:

| Value | Example | Where it is used |
|---|---|---|
| API hostname | `shivansh-qe-api.duckdns.org` | `API_DOMAIN`, Vercel `NEXT_PUBLIC_API_URL`, Razorpay webhook |
| Admin hostname | `shivansh-qe-admin.duckdns.org` | `ADMIN_DOMAIN` |
| Vercel project name | `quantum-edu` → `https://quantum-edu.vercel.app` | `FRONTEND_URL` |

If the Vercel name is taken, Vercel appends a suffix. Use whatever URL Vercel actually gives you as `FRONTEND_URL`.

Create the config file now and fill it in as you go through the accounts below:

```bash
cd quantum-edu-be
./deploy/oracle/init-env.sh       # writes deploy.prod.env with random passwords/secrets
```

If an old AWS `deploy.prod.env` exists, it is moved to `deploy.prod.env.aws-backup`.

---

## 1. Oracle Cloud (VM for backend + admin + MySQL)

### 1.1 Account

1. Sign up at <https://signup.cloud.oracle.com/>. A card is needed for identity verification; Always Free resources are never charged.
2. **Home region cannot be changed later.** Pick one close to India, such as **India West (Mumbai)** or **India South (Hyderabad)**. Ampere (ARM) capacity varies by region; if your region is constantly "out of capacity", see 1.3.

### 1.2 Create the VM

Console → **Compute → Instances → Create instance**:

| Setting | Value |
|---|---|
| Name | `quantum-edu` |
| Image | **Canonical Ubuntu 24.04** (the aarch64 build is picked automatically for Ampere) |
| Shape | **Ampere → VM.Standard.A1.Flex**, **2 OCPU, 8 GB** memory |
| Networking | "Create new virtual cloud network" + "Create new public subnet", **Assign a public IPv4 address: Yes** |
| SSH keys | "Generate a key pair for me" → **download the private key** |
| Boot volume | Default (~47 GB) is fine |

Why 2 OCPU / 8 GB rather than the full 4 / 24? Oracle reclaims idle Always Free instances when CPU, network **and memory** stay under 20% for 7 days. The stack uses about 3 GB, so 8 GB keeps memory use above that threshold.

Save the key on your laptop:

```bash
mv ~/Downloads/ssh-key-*.key ~/.ssh/oracle-quantum-edu.key
chmod 600 ~/.ssh/oracle-quantum-edu.key
ssh -i ~/.ssh/oracle-quantum-edu.key ubuntu@<PUBLIC_IP>     # should log in
```

In `deploy.prod.env`: `VM_HOST=<PUBLIC_IP>`, `VM_USER=ubuntu`, `SSH_KEY=~/.ssh/oracle-quantum-edu.key`.

The public IP stays the same through reboots and stop/start, for as long as the instance exists. A reserved IP (Networking → IP Management → Reserved Public IPs) is only needed if you plan to recreate the instance.

### 1.3 "Out of capacity" when creating the A1 instance

- Retry a few times over a day, and try each **Availability Domain** in the placement section.
- Or upgrade the account to **Pay As You Go** (Billing → Upgrade). Always Free resources stay free and A1 capacity is much easier to get. If you do this, create a **Budget** (Billing → Budgets) of ₹100 with an alert at 1% so any accidental charge emails you.

### 1.4 Open ports 80 and 443

Console → **Networking → Virtual cloud networks →** your VCN → **Security → Default Security List → Add Ingress Rules**:

| Source CIDR | Protocol | Destination port |
|---|---|---|
| `0.0.0.0/0` | TCP | 80 |
| `0.0.0.0/0` | TCP | 443 |
| `0.0.0.0/0` | UDP | 443 (HTTP/3, optional) |

Ubuntu images on Oracle also have a host iptables firewall that blocks these ports. `bootstrap-vm.sh` opens them for you.

---

## 2. DuckDNS (free hostnames)

1. Go to <https://www.duckdns.org/> and sign in with GitHub or Google.
2. Add two subdomains, for example `shivansh-qe-api` and `shivansh-qe-admin`.
3. For **each**, set **current ip** to the VM public IP and click **update ip**.
4. Check from your laptop: `dig +short shivansh-qe-api.duckdns.org` should print the VM IP.

In `deploy.prod.env`: `API_DOMAIN=shivansh-qe-api.duckdns.org`, `ADMIN_DOMAIN=shivansh-qe-admin.duckdns.org`, `ACME_EMAIL=<your email>`.

Caddy gets the Let's Encrypt certificates automatically on the first deploy. DNS must already point at the VM, and ports 80/443 must be open.

---

## 3. Cloudflare R2 (media)

1. Sign up at <https://dash.cloudflare.com/sign-up>. No domain is needed.
2. Left menu → **R2 Object Storage** → enable it. Cloudflare asks for a card; the free tier is 10 GB storage plus generous operation limits, and egress is always free.
3. **Create bucket** `quantum-edu-media` (location: Automatic).
   - Bucket → **Settings → Public Development URL → Enable**, then type `allow`.
   - Copy the URL, e.g. `https://pub-0123abcd....r2.dev`.
4. **Create bucket** `quantum-edu-backups`. **Do not** enable public access; it holds database dumps.
5. R2 overview → **Manage API tokens → Create Account API token**:
   - Permission: **Admin Read & Write**. This lets `upload-media.sh` also set CORS for you. "Object Read & Write" also works, but then you must paste the CORS policy by hand; the script prints it.
   - Copy the **Access Key ID** and **Secret Access Key**. They are shown once.
   - Your **Account ID** is in the S3 endpoint shown on that page: `https://<ACCOUNT_ID>.r2.cloudflarestorage.com`.

In `deploy.prod.env`:

```
R2_ACCOUNT_ID=<account id>
R2_ACCESS_KEY_ID=<access key id>
R2_SECRET_ACCESS_KEY=<secret>
R2_BUCKET_NAME=quantum-edu-media
R2_PUBLIC_URL=https://pub-0123abcd....r2.dev        # no trailing slash
BACKUP_R2_BUCKET=quantum-edu-backups
```

The `r2.dev` URL is rate-limited by Cloudflare and meant for low traffic. That is fine for a portfolio. If you ever buy a domain, connect it under bucket Settings → Custom Domains and update `R2_PUBLIC_URL` and `NEXT_PUBLIC_R2_IMAGE_HOST`.

---

## 4. Gmail SMTP (verification emails)

1. Google Account → **Security** → turn on **2-Step Verification**.
2. Open <https://myaccount.google.com/apppasswords>, create an app password named `quantum-edu` and copy the 16-character password.

In `deploy.prod.env`:

```
APP_EMAIL_ENABLED=true
MAIL_USERNAME=you@gmail.com
MAIL_PASSWORD="abcd efgh ijkl mnop"     # keep the quotes
EMAIL_FROM=you@gmail.com
```

New sign-ups get a link to `${FRONTEND_URL}/verify-email?...` and must verify before they can log in. The demo accounts are already verified.

---

## 5. Razorpay (Test Mode checkout)

1. Sign up at <https://dashboard.razorpay.com/signup>. Test Mode works without KYC.
2. Toggle **Test Mode** at the top of the dashboard.
3. **Account & Settings → API Keys → Generate Test Key**. Copy the Key ID (`rzp_test_...`) and the Key Secret (shown once).
4. **Account & Settings → Webhooks → Add New Webhook**, still in Test Mode:
   - Webhook URL: `https://<API_DOMAIN>/api/v1/cart/webhook/razorpay`
   - Secret: the `RAZORPAY_WEBHOOK_SECRET` value already generated in `deploy.prod.env` (copy it from there).
   - Active events: **payment.captured**
5. **Account & Settings → Payment capture**: make sure it is **Automatic**. Course access is granted only when the `payment.captured` webhook arrives.

In `deploy.prod.env`: `RAZORPAY_KEY_ID=rzp_test_...` and `RAZORPAY_KEY_SECRET=...`.

To pay in the checkout popup, use UPI ID `success@razorpay` or a test card from <https://razorpay.com/docs/payments/payments/test-card-details/>. Free courses skip Razorpay entirely.

---

## 6. Deploy backend + admin to the VM

Run all of this from your laptop in `quantum-edu-be/`, with `quantum-edu-admin/` checked out next to it.

```bash
# 1. Media: already rendered into deploy/seed/media/out (gitignored).
#    To regenerate (macOS, needs Google Chrome + Docker):  python3 deploy/seed/build.py media
./deploy/upload-media.sh          # uploads 44 files to r2://quantum-edu-media/seed/ + sets CORS

# 2. First deploy: installs Docker, firewall rules, swap, backups; then builds and starts everything.
./deploy.prod.sh --bootstrap             # first build takes ~5-10 minutes on the ARM VM
```

On the first start, MySQL creates both databases, loads `docs/schema.sql`, and loads the sample data. Media URLs are rewritten to `${R2_PUBLIC_URL}/seed/...`. The admin container runs Django migrations and creates the superuser and the view-only demo user.

Check it:

```bash
curl https://<API_DOMAIN>/health                     # {"status":"UP"}
open https://<ADMIN_DOMAIN>/admin/                   # log in with DJANGO_SUPERUSER_* from deploy.prod.env
./deploy.prod.sh --status                            # container status + recent logs
```

If the certificate is not ready yet, check `ssh ... 'cd ~/quantum-edu/quantum-edu-be/deploy/oracle && docker compose logs caddy'`. It is almost always DNS not pointing at the VM, or port 80 closed in the security list.

---

## 7. Frontend on Vercel

### 7.1 Push the frontend to your personal GitHub

Create an empty repo `quantum_edu_fe` on github.com (private is fine), then:

```bash
cd quantum_edu_fe
git remote rename origin versotech
git remote add origin git@github.com:shivanshcv/quantum_edu_fe.git
git push -u origin main
```

### 7.2 Import into Vercel

1. Sign up at <https://vercel.com/signup> **with GitHub** (Hobby plan).
2. **Add New → Project →** import `quantum_edu_fe`. The framework is detected as Next.js; leave the build settings at their defaults.
3. Project name: the one you chose in step 0 (it becomes `<name>.vercel.app`).
4. **Environment Variables**:

| Name | Value |
|---|---|
| `NEXT_PUBLIC_API_URL` | `https://<API_DOMAIN>` |
| `NEXT_PUBLIC_R2_IMAGE_HOST` | `pub-0123abcd....r2.dev` (hostname only, no `https://`) |

5. **Deploy**. Both variables are baked in at build time, so if you change them later, **Redeploy**.

If the final Vercel URL differs from `FRONTEND_URL` in `deploy.prod.env`, fix it there, then:

```bash
./deploy.prod.sh --only backend          # CORS + email links
./deploy/upload-media.sh --cors   # R2 CORS for the PDF viewer
```

Pushes to `main` redeploy the frontend automatically.

---

## 8. Demo walkthrough (what reviewers will see)

| What | Login |
|---|---|
| Learner with progress | `demo@example.com` / `Demo@12345` |
| Other learners | `priya.nair@`, `rahul.verma@`, `sneha.patil@`, `mohammed.irfan@`, `divya.reddy@example.com` (same password) |
| Unverified account (shows the "verify your email" error) | `karan.singh@example.com` |
| Admin, read-only (safe to share) | `DEMO_VIEWER_USERNAME` / `DEMO_VIEWER_PASSWORD` at `https://<ADMIN_DOMAIN>/admin/` |
| Admin, full access (keep private) | `DJANGO_SUPERUSER_USERNAME` / `DJANGO_SUPERUSER_PASSWORD` |

The demo learner:
- Has completed the free "Foundations of Skin Science" course.
- Is part-way through "Certified Laser Hair Reduction Technician": module 1 done, module 2 in progress, module 3 locked.
- Has "Laser Safety Officer Essentials" not started, and one course in the cart.

The catalogue has 6 published courses in 4 categories, plus 1 unpublished draft that is visible only in the admin.

---

## Day-2 operations

```bash
./deploy.prod.sh                         # redeploy everything after code changes
./deploy.prod.sh --only backend          # or: admin | caddy | mysql
./deploy.prod.sh --status
./deploy.prod.sh --reset-db              # DESTROYS data; reloads schema + sample data
```

- **Edit the sample data:** change `deploy/seed/catalog.json`, run `python3 deploy/seed/build.py sql` (and `media` plus `upload-media.sh` if lessons changed), then `./deploy.prod.sh --reset-db`.
- **Demo password:** `catalog.json` → `demo.password_hash` must be **Argon2id** (`m=65536,t=2,p=1`, 32-byte hash), matching the backend's `Argon2PasswordEncoder`. Generate a new one with:
  ```bash
  docker run --rm python:3.12-slim sh -c "pip -q install argon2-cffi && python -c \"from argon2 import PasswordHasher as P; print(P(time_cost=2, memory_cost=65536, parallelism=1, hash_len=32, salt_len=16).hash('NewPass123'))\""
  ```
- **Backups:** these run every night at 02:30 IST into `~/quantum-edu/backups` on the VM, with a copy in `BACKUP_R2_BUCKET` if it is set. To run one manually: `ssh ... ~/quantum-edu/quantum-edu-be/deploy/oracle/backup-db.sh`.
- **Restore** (each dump contains both databases), from `deploy/oracle` on the VM: `gunzip -c ~/quantum-edu/backups/quantum-edu-<stamp>.sql.gz | docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD"'`.
- **MySQL shell:** `docker compose exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" quantum_education'`. Port 3306 is bound to localhost only; use an SSH tunnel (`ssh -L 3306:127.0.0.1:3306 ...`) for a GUI client.

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| Frontend shows network/CORS errors | `FRONTEND_URL` doesn't exactly match the Vercel URL (scheme, no trailing slash). Fix it and run `./deploy.prod.sh --only backend` |
| Images broken on the frontend | `NEXT_PUBLIC_R2_IMAGE_HOST` missing or wrong in Vercel (redeploy after changing it), or media not uploaded |
| PDF lessons don't load | R2 CORS missing: run `./deploy/upload-media.sh --cors` |
| Images show `media.invalid` | `R2_PUBLIC_URL` was empty on the first DB init. Set it, then `./deploy.prod.sh --reset-db` |
| Paid course not unlocked after paying | Webhook URL/secret/event wrong in Razorpay (Webhooks page shows delivery attempts), or payment capture is not Automatic |
| No verification email | Wrong Gmail app password, or `APP_EMAIL_ENABLED=false`; check `docker compose logs backend` |
| Admin "CSRF verification failed" | You're opening the admin on a host other than `ADMIN_DOMAIN` |
| Backend restarts with OOM | `docker stats`; the VM needs the 8 GB shape (backend 1 GB, MySQL 1.5 GB limits) |

## Shutting down the old AWS deployment

So nothing keeps billing:
- EC2 instances and their EBS volumes/snapshots.
- Elastic IPs: unattached ones are billed.
- RDS instances and snapshots, ECR images, Load Balancers.
- Amplify apps, Route 53 hosted zones.
- CloudWatch log groups.

Check **Billing → Bills** the following month.
