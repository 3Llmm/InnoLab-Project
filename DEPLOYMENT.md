# CTF Platform Deployment Guide

## Local Development (Docker)

```bash
docker compose up -d
# Available at http://localhost:3000
```

## Production (Automated Native Deployment)

Deployment is now **fully automated** via Jenkins. Every 5 minutes, Jenkins polls the git repository and automatically deploys when changes are detected on the main/master branch.

### How It Works

1. Jenkins polls `git pollSCM` every 5 minutes
2. When changes are detected, the `Jenkinsfile` pipeline runs:
   - **Checkout** - Pulls latest code
   - **Build Backend** - Runs `./mvnw clean package` (Java/Maven)
   - **Build Frontend** - Runs `npm ci && npm run build` (Next.js)
   - **Deploy Backend** - Copies JAR to `/opt/ctf/backend/app.jar`
   - **Deploy Frontend** - Copies `.next` to `/opt/ctf/frontend/`
   - **Deploy Terminal** - Copies `server.js` to `/opt/ctf/terminal/`
   - **Restart Services** - Restarts `ctf-backend`, `ctf-frontend`, `ctf-terminal`
   - **Health Check** - Verifies all services are running

### Pipeline File

The deployment pipeline is defined in `Jenkinsfile` in the project root. No manual intervention required.

### Viewing Deployment Status

```bash
# Check Jenkins console output for the latest build
# Or view live logs on the production server:

journalctl -u ctf-backend -f
journalctl -u ctf-frontend -f
journalctl -u ctf-terminal -f
```

## Service Management

```bash
# Check status
systemctl status ctf-backend ctf-frontend ctf-terminal

# Restart manually (if needed)
systemctl restart ctf-backend ctf-frontend ctf-terminal

# View logs
journalctl -u ctf-backend -f
journalctl -u ctf-frontend -f
journalctl -u ctf-terminal -f
```

## Health Checks

| Service      | URL                              |
|--------------|----------------------------------|
| Frontend     | http://inno1-bif3-p1-w25.cs.technikum-wien.at:3000 |
| Terminal    | http://inno1-bif3-p1-w25.cs.technikum-wien.at:3001/health |
| Backend     | http://inno1-bif3-p1-w25.cs.technikum-wien.at:8080/api/challenges |

## Troubleshooting

### Terminal not connecting
1. Check if port 3001 is accessible:
   ```bash
   curl http://localhost:3001/health
   ```
2. Check terminal logs:
   ```bash
   journalctl -u ctf-terminal -f
   ```
3. If blocked by firewall, ensure nginx proxy is configured

### Frontend not connecting to terminal
1. Hard refresh browser (Ctrl+Shift+R)
2. Check browser DevTools → Network for WebSocket errors
3. Verify `NEXT_PUBLIC_TERMINAL_URL` is set in build

### Deployment failed
1. Check Jenkins build logs
2. Check service logs: `journalctl -u ctf-backend -f`
3. Verify disk space: `df -h`
4. Verify Java/Node.js versions are correct

## File Locations

| Service   | Deploy Path      | Source Path                      |
|-----------|------------------|----------------------------------|
| Backend   | /opt/ctf/backend | ctf-backend/target/app.jar       |
| Frontend  | /opt/ctf/frontend| ctf-frontend/.next              |
| Terminal | /opt/ctf/terminal| ctf-terminal/server.js          |
| Challenges| /opt/ctf/backend/challenges | challenges/ |

## Systemd Services

- `ctf-backend.service` - Java Spring Boot app
- `ctf-frontend.service` - Next.js app
- `ctf-terminal.service` - Node.js WebSocket terminal
