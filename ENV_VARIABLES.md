# Environment Variables Reference

## Backend Configuration

### Database (PostgreSQL)

| Variable | Dev Default | Production | Description |
|----------|-------------|------------|-------------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/accommodation_db` | From ConfigMap | Database connection URL |
| `DATABASE_USERNAME` | `postgres` | From ConfigMap | Database username |
| `DATABASE_PASSWORD` | `postgres` | From Secret | Database password |

### Profile Service (Authentication)

| Variable | Default | Description |
|----------|---------|-------------|
| `PROFILE_SERVICE_URL` | `https://2306219575-be.hafizmuh.site` | Profile Service backend URL for token validation |

### JWT Configuration

| Variable | Dev Default | Production | Description |
|----------|-------------|------------|-------------|
| `JWT_SECRET_KEY` | `your-super-secret-key-change-this-in-production-min-256-bits` | From Secret | Secret key untuk JWT signing (min 256 bits) |

### CORS Configuration

| Variable | Dev Default | Production | Description |
|----------|-------------|------------|-------------|
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | From ConfigMap | Comma-separated allowed origins |

### Spring Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile (dev/prod) |
| `SERVER_PORT` | `8080` | Server port |
| `DDL_AUTO` | `update` | Hibernate DDL mode (create/update/validate) |

## Frontend Configuration

### Environment Files

| File | Purpose | When Used |
|------|---------|-----------|
| `.env.development` | Local development | `npm run dev` |
| `.env.production` | Production build | `npm run build` |
| `.env.local` | Local overrides (gitignored) | Always (highest priority) |

### Variables

| Variable | Dev Default | Prod Default | Description |
|----------|-------------|--------------|-------------|
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | `https://2306212083-be.hafizmuh.site/api` | Accommodation backend API |
| `VITE_PROFILE_SERVICE_URL` | `https://2306219575-be.hafizmuh.site` | `https://2306219575-be.hafizmuh.site` | Profile Service backend |
| `VITE_PROFILE_FRONTEND_URL` | `https://2306219575-fe.hafizmuh.site` | `https://2306219575-fe.hafizmuh.site` | Profile Service frontend |

## Usage Examples

### Backend - Override via Command Line

```bash
# Linux/Mac
export DATABASE_URL="jdbc:postgresql://custom-host:5432/custom_db"
export DATABASE_USERNAME="custom_user"
export DATABASE_PASSWORD="custom_pass"
./gradlew bootRun --args='--spring.profiles.active=dev'

# Windows CMD
set DATABASE_URL=jdbc:postgresql://custom-host:5432/custom_db
set DATABASE_USERNAME=custom_user
set DATABASE_PASSWORD=custom_pass
gradlew.bat bootRun --args="--spring.profiles.active=dev"

# Windows PowerShell
$env:DATABASE_URL="jdbc:postgresql://custom-host:5432/custom_db"
$env:DATABASE_USERNAME="custom_user"
$env:DATABASE_PASSWORD="custom_pass"
.\gradlew.bat bootRun --args="--spring.profiles.active=dev"
```

### Backend - Override via application-dev.yml

Edit `src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/accommodation_db
    username: your_username
    password: your_password

profile:
  service:
    base-url: https://2306219575-be.hafizmuh.site

jwt:
  secret-key: your-custom-secret-key-min-256-bits

cors:
  allowed:
    origins: http://localhost:5173,http://localhost:3000,http://localhost:8081
```

### Frontend - Create .env.local

Create `accommodation-2306212083-fe/.env.local` (gitignored):

```env
# Override untuk development lokal Anda
VITE_API_BASE_URL=http://localhost:8080/api
VITE_PROFILE_SERVICE_URL=https://2306219575-be.hafizmuh.site
VITE_PROFILE_FRONTEND_URL=https://2306219575-fe.hafizmuh.site
```

### Frontend - Use Different Port

```env
# .env.local
VITE_API_BASE_URL=http://localhost:8081/api
```

Then run backend on port 8081:
```bash
export SERVER_PORT=8081
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Docker Container Environment

For production deployment on K8s:

```yaml
# k8s/deployment.yaml
env:
  - name: SPRING_PROFILES_ACTIVE
    value: "prod"
  - name: SPRING_DATASOURCE_URL
    valueFrom:
      configMapKeyRef:
        name: accommodation-be-config
        key: DATABASE_URL_PROD
  - name: SPRING_DATASOURCE_USERNAME
    valueFrom:
      configMapKeyRef:
        name: accommodation-be-config
        key: DATABASE_USERNAME
  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: accommodation-be-secret
        key: DATABASE_PASSWORD
  - name: JWT_SECRET_KEY
    valueFrom:
      secretKeyRef:
        name: accommodation-be-secret
        key: JWT_SECRET_KEY
  - name: CORS_ALLOWED_ORIGINS
    valueFrom:
      configMapKeyRef:
        name: accommodation-be-config
        key: CORS_ALLOWED_ORIGINS
```

## Priority Order

### Backend (Spring Boot)

1. Command line arguments: `--spring.datasource.url=...`
2. Environment variables: `DATABASE_URL=...`
3. `application-{profile}.yml`: `application-dev.yml` or `application-prod.yml`
4. `application.properties`: Default values

### Frontend (Vite)

1. `.env.local`: Local overrides (highest priority, gitignored)
2. `.env.development` or `.env.production`: Mode-specific
3. `.env`: Base values (lowest priority)

## Security Notes

### ⚠️ NEVER Commit Sensitive Data

Add to `.gitignore`:
```gitignore
# Local environment
.env.local
application-local.yml

# Sensitive configs
**/application-prod.yml
**/*-secret.yaml
```

### ✅ Use Environment Variables for Production

Instead of hardcoding:
```yaml
# ❌ BAD
password: "mySecretPassword123"

# ✅ GOOD
password: ${DATABASE_PASSWORD}
```

### ✅ Rotate Secrets Regularly

- JWT secret keys
- Database passwords
- API keys

## Quick Reference Card

### Start Local Development

```bash
# Terminal 1: Backend
cd accommodation-2306212083-be
./run-dev.bat  # Windows
./run-dev.sh   # Linux/Mac

# Terminal 2: Frontend
cd accommodation-2306212083-fe
npm run dev

# URLs
Backend:  http://localhost:8080
Frontend: http://localhost:5173
Health:   http://localhost:8080/actuator/health
```

### Change Database Connection

```bash
# Quick override
export DATABASE_URL="jdbc:postgresql://192.168.1.100:5432/test_db"
export DATABASE_USERNAME="testuser"
export DATABASE_PASSWORD="testpass"
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Test Different Profile Service

```env
# .env.local (frontend)
VITE_PROFILE_SERVICE_URL=http://localhost:8081
VITE_PROFILE_FRONTEND_URL=http://localhost:8082
```

```bash
# Backend
export PROFILE_SERVICE_URL="http://localhost:8081"
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Troubleshooting

### Environment Variable Not Working

1. **Check spelling**: `DATABASE_URL` not `DB_URL`
2. **Check export**: `export VAR=value` (Linux/Mac), `set VAR=value` (Windows)
3. **Check .env syntax**: No spaces around `=`, no quotes unless needed
4. **Restart server**: Changes require restart

### CORS Still Blocked

1. Check `CORS_ALLOWED_ORIGINS` includes your frontend URL
2. Include protocol: `http://localhost:5173` not `localhost:5173`
3. No trailing slash: `http://localhost:5173` not `http://localhost:5173/`
4. Check backend logs for actual CORS error

### JWT Validation Fails

1. Check `PROFILE_SERVICE_URL` is correct and reachable
2. Test manually: `curl https://2306219575-be.hafizmuh.site/api/auth/validate-token`
3. Check JWT token in browser cookies
4. Verify token not expired (check browser DevTools)
