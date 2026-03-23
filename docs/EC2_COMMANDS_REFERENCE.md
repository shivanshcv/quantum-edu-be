# EC2 Instance — Important Commands Reference

Quick reference for commands used with the quantum-edu-be staging EC2 instance. Replace placeholders from your `deploy.env` as needed.

**Variables from deploy.env:**
- `EC2_HOST` — EC2 public IP (e.g. 65.0.74.204)
- `EC2_USER` — SSH user (typically `ubuntu`)
- `SSH_KEY` — Path to .pem key (e.g. ~/Downloads/cli-admin.pem)

---

## 1. SSH & Connection

| Command | Purpose |
|--------|---------|
| `ssh -i $SSH_KEY ${EC2_USER}@${EC2_HOST}` | SSH into EC2 (use values from deploy.env) |
| `ssh -i ~/Downloads/cli-admin.pem ubuntu@65.0.74.204` | Example with concrete values |
| `ssh -i $SSH_KEY -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST}` | SSH without host key prompt |

---

## 2. Docker — quantum-edu-be Container

| Command | Purpose |
|--------|---------|
| `sudo docker ps` | List running containers |
| `sudo docker ps -a` | List all containers (running + stopped) |
| `sudo docker logs quantum-edu-be --tail 50` | Last 50 log lines |
| `sudo docker logs quantum-edu-be -f` | Follow logs in real time |
| `sudo docker logs quantum-edu-be --since 5m` | Logs from last 5 minutes |
| `sudo docker stop quantum-edu-be` | Stop the backend container |
| `sudo docker start quantum-edu-be` | Start the container |
| `sudo docker restart quantum-edu-be` | Restart the container |
| `sudo docker rm quantum-edu-be` | Remove stopped container (must stop first) |
| `sudo docker inspect quantum-edu-be` | Inspect container config and env |

**One-liner from your machine (using deploy.env vars):**
```bash
ssh -i $SSH_KEY ${EC2_USER}@${EC2_HOST} 'sudo docker logs quantum-edu-be --tail 50'
```

---

## 3. Docker — Images & Cleanup

| Command | Purpose |
|--------|---------|
| `sudo docker images` | List images |
| `sudo docker pull ${ECR_URI}:latest` | Pull latest image from ECR |
| `sudo docker container prune -f` | Remove stopped containers |
| `sudo docker image prune -f` | Remove dangling images |
| `sudo docker image prune -af` | Remove all unused images |
| `sudo docker system prune -f` | Remove stopped containers + unused networks + dangling images |
| `sudo docker system prune -af` | Full prune including unused images |
| `sudo docker system prune -af --volumes` | **WARNING:** Also removes volumes (data loss risk) |

**Remove old Mailpit (when switched to Gmail):**
```bash
sudo docker stop mailpit 2>/dev/null
sudo docker rm mailpit 2>/dev/null
```

---

## 4. Docker — Network

| Command | Purpose |
|--------|---------|
| `sudo docker network ls` | List networks |
| `sudo docker network create quantum-net` | Create network (idempotent) |
| `sudo docker network inspect quantum-net` | Inspect quantum-net containers |

---

## 5. ECR Login (on EC2)

| Command | Purpose |
|--------|---------|
| `aws ecr get-login-password --region ap-south-1 \| sudo docker login --username AWS --password-stdin 632127306445.dkr.ecr.ap-south-1.amazonaws.com` | Login to ECR (replace account ID/region as needed) |

**Using deploy.env vars (run from EC2 after sourcing):**
```bash
aws ecr get-login-password --region ${AWS_REGION} | sudo docker login --username AWS --password-stdin ${ECR_REGISTRY}
```
*(Note: `ECR_REGISTRY` = host only, e.g. `632127306445.dkr.ecr.ap-south-1.amazonaws.com`)*

---

## 6. Database (MySQL/RDS)

| Command | Purpose |
|--------|---------|
| `sudo apt update && sudo apt install -y mysql-client` | Install MySQL client on EC2 |
| `mysql -h $RDS_ENDPOINT -P 3306 -u admin -p"$RDS_PASS" quantum_education -e "SELECT 1"` | Test RDS connectivity |
| `mysql -h $RDS_ENDPOINT -P 3306 -u admin -p"$RDS_PASS" quantum_education < ~/schema.sql` | Run schema migration |

**Copy schema to EC2 from your machine:**
```bash
scp -i $SSH_KEY docs/schema.sql ${EC2_USER}@${EC2_HOST}:~/schema.sql
```

---

## 7. Health & API Checks

| Command | Purpose |
|--------|---------|
| `curl -s http://${EC2_HOST}:8080/actuator/health` | Check backend health |
| `curl -s http://${EC2_HOST}:8080/api/v1/catalogue/getCategories` | Test API (adjust host) |
| `curl -s -o /dev/null -w "%{http_code}" http://${EC2_HOST}:8080/actuator/health` | Get HTTP status only |

---

## 8. Mailpit (when using MAIL_HOST=mailpit)

| Command | Purpose |
|--------|---------|
| `sudo docker ps \| grep mailpit` | Check if Mailpit is running |
| `sudo docker start mailpit` | Start Mailpit if stopped |
| Open `http://${EC2_HOST}:8025` | Mailpit Web UI (port 8025 must be open in SG) |

---

## 9. System & Resources

| Command | Purpose |
|--------|---------|
| `df -h` | Disk usage |
| `free -m` | Memory usage |
| `top` or `htop` | Process monitor |
| `sudo systemctl status docker` | Docker service status |
| `sudo systemctl restart docker` | Restart Docker daemon |
| `uptime` | Uptime and load |

---

## 10. Deployment (from your machine)

| Command | Purpose |
|--------|---------|
| `cd quantum-edu-be && ./deploy.sh` | Full deploy (build, push, SSH, restart) |
| `mvn clean install` | Build JAR locally before deploy |

---

## 11. Cloudflare Tunnel (if used)

If cloudflared is installed as a systemd service:

| Command | Purpose |
|--------|---------|
| `sudo systemctl status cloudflared` | Tunnel status |
| `sudo systemctl start cloudflared` | Start tunnel |
| `sudo systemctl stop cloudflared` | Stop tunnel |
| `sudo systemctl restart cloudflared` | Restart tunnel |
| `sudo journalctl -u cloudflared -f` | Follow tunnel logs |

---

## 12. Security Group Ports (AWS Console)

Ensure these inbound rules exist on the EC2 security group:

| Port | Purpose |
|------|---------|
| 22 | SSH |
| 8080 | Spring Boot API |
| 8025 | Mailpit Web UI (only when using Mailpit) |

---

## 13. Common One-Liners

```bash
# Logs (from local machine)
ssh -i ~/Downloads/cli-admin.pem ubuntu@65.0.74.204 'sudo docker logs quantum-edu-be --tail 100 -f'

# Cleanup unused Docker resources (from EC2)
sudo docker container prune -f && sudo docker image prune -af

# Restart backend only (from EC2)
sudo docker restart quantum-edu-be

# Check if backend is up
curl -s http://65.0.74.204:8080/actuator/health
```

---

## Quick Reference: deploy.env Variables

| Variable | Used for |
|----------|----------|
| `EC2_HOST` | SSH target, health check URL |
| `EC2_USER` | SSH user |
| `SSH_KEY` | SSH key path |
| `AWS_REGION` | ECR, RDS region |
| `ECR_URI` | Docker image pull |
| `RDS_ENDPOINT` | DB host |
| `RDS_DB` | Database name |
| `RDS_USER` / `RDS_PASS` | DB credentials |
| `JWT_SECRET` | Auth tokens |
| `APP_CORS_ORIGINS` | Allowed origins |
| `VERIFICATION_BASE_URL` | Email verification link base |
| `MAIL_HOST` | smtp.gmail.com or mailpit |
| `MAIL_PORT` | 587 (Gmail) or 1025 (Mailpit) |
