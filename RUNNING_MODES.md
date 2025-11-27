# Running Backend: Development vs Production

Complete guide untuk menjalankan Accommodation Backend dalam mode Development atau Production.

## 🔀 Quick Reference

| Aspect | Development | Production |
|--------|-------------|------------|
| **Profile** | `dev` | `prod` |
| **Command** | `run-dev.bat/sh` | `run-prod.bat/sh` |
| **Database** | Local PostgreSQL | Remote PostgreSQL (production) |
| **Config File** | `application-dev.yml` | `application-prod.yml` |
| **Env Variables** | Optional (has defaults) | **Required** (no defaults for secrets) |
| **Logging** | DEBUG level | INFO/WARN level |
| **SQL Logging** | Enabled | Disabled |
| **Port** | 8080 (default) | 8080 or custom |
| **CORS** | localhost:5173, localhost:3000 | hafizmuh.site domains |

---

## 🟢 DEVELOPMENT MODE

### Prerequisites:
- ✅ Java 17+
- ✅ PostgreSQL installed locally
- ✅ Database `accommodation_db` created

### Setup Database (One-time):

```bash
# Login to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE accommodation_db;

# Exit
\q
```

### Configuration:

File: `src/main/resources/application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/accommodation_db
    username: postgres
    password: postgres  # ← Change if needed
```

**Default Values:**
- Database: `localhost:5432/accommodation_db`
- Username: `postgres`
- Password: `postgres`
- Auth: Profile Service (remote)
- CORS: `localhost:5173`, `localhost:3000`

### Running:

#### Option 1: Using Script (Recommended)

**Windows:**
```cmd
cd accommodation-2306212083-be
run-dev.bat
```

**Linux/Mac:**
```bash
cd accommodation-2306212083-be
chmod +x run-dev.sh
./run-dev.sh
```

#### Option 2: Manual Gradle Command

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### Option 3: Run JAR Directly

```bash
# Build first
./gradlew clean build

# Run JAR
java -jar build/libs/accommodation-2306212083-be-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Verify:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Should return:
# {"status":"UP"}
```

### Override Config (Optional):

```bash
# Custom database
export DATABASE_URL="jdbc:postgresql://192.168.1.100:5432/test_db"
export DATABASE_USERNAME="testuser"
export DATABASE_PASSWORD="testpass"

# Run
./gradlew bootRun --args='--spring.profiles.active=dev'
```

---

## 🔴 PRODUCTION MODE

### Prerequisites:
- ✅ Java 17+
- ✅ Production PostgreSQL database
- ✅ All production environment variables set
- ✅ JWT secret key generated
- ✅ Production CORS domains configured

### Setup Environment Variables:

#### Step 1: Copy Template

**Windows:**
```cmd
copy set-prod-env.bat.example set-prod-env.bat
```

**Linux/Mac:**
```bash
cp set-prod-env.sh.example set-prod-env.sh
chmod +x set-prod-env.sh
```

#### Step 2: Edit Variables

Edit `set-prod-env.bat` (Windows) or `set-prod-env.sh` (Linux/Mac):

```bash
# DATABASE Configuration
export DATABASE_URL_PROD="jdbc:postgresql://prod-host:5432/accommodation_prod"
export DATABASE_USERNAME="prod_user"
export DATABASE_PASSWORD="strong_password_here"

# JWT Configuration (Generate: openssl rand -base64 64)
export JWT_SECRET_KEY="your-256-bit-secret-key-here"

# CORS Configuration
export CORS_ALLOWED_ORIGINS="https://2306212083-fe.hafizmuh.site,https://www.yourdomain.com"

# Profile Service (optional override)
export PROFILE_SERVICE_URL="https://2306219575-be.hafizmuh.site"
```

#### Step 3: Generate JWT Secret

```bash
# Linux/Mac/Git Bash
openssl rand -base64 64

# PowerShell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
```

### Configuration:

File: `src/main/resources/application-prod.yml`

**All sensitive values come from environment variables:**
- ✅ `DATABASE_URL_PROD` - Required
- ✅ `DATABASE_USERNAME` - Required
- ✅ `DATABASE_PASSWORD` - Required
- ✅ `JWT_SECRET_KEY` - Required
- ✅ `CORS_ALLOWED_ORIGINS` - Required

### Running:

#### Step 1: Set Environment Variables

**Windows:**
```cmd
set-prod-env.bat
```

**Linux/Mac:**
```bash
source set-prod-env.sh
```

#### Step 2: Run Application

**Windows:**
```cmd
run-prod.bat
```

**Linux/Mac:**
```bash
./run-prod.sh
```

#### Manual Command:

```bash
# Ensure env vars are set first!
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Verify:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Check environment
curl http://localhost:8080/actuator/info
```

---

## 🐳 DOCKER DEPLOYMENT (Production)

### Using Docker Compose:

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  accommodation-be:
    image: muhammadyahya32/accommodation-be:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL_PROD=jdbc:postgresql://db:5432/accommodation_prod
      - DATABASE_USERNAME=postgres
      - DATABASE_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET_KEY=${JWT_SECRET}
      - CORS_ALLOWED_ORIGINS=https://2306212083-fe.hafizmuh.site
      - PROFILE_SERVICE_URL=https://2306219575-be.hafizmuh.site
    depends_on:
      - db
    restart: unless-stopped

  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=accommodation_prod
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data:
```

Run:
```bash
# Set secrets
export DB_PASSWORD="your_db_password"
export JWT_SECRET="your_jwt_secret"

# Start
docker-compose up -d
```

### Using Docker Run:

```bash
docker run -d \
  --name accommodation-be \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL_PROD="jdbc:postgresql://your-host:5432/accommodation_prod" \
  -e DATABASE_USERNAME="your_user" \
  -e DATABASE_PASSWORD="your_password" \
  -e JWT_SECRET_KEY="your_secret_key" \
  -e CORS_ALLOWED_ORIGINS="https://2306212083-fe.hafizmuh.site" \
  muhammadyahya32/accommodation-be:latest
```

---

## ☸️ KUBERNETES DEPLOYMENT (K3s)

Already configured in `.gitlab-ci.yml` for automatic deployment.

### Manual K8s Deploy:

```bash
# Apply ConfigMap (non-sensitive config)
kubectl apply -f k8s/configmap.yaml

# Apply Secret (sensitive data)
kubectl apply -f k8s/secret.yaml

# Apply Deployment
kubectl apply -f k8s/deployment.yaml

# Apply Service
kubectl apply -f k8s/service.yaml

# Apply Ingress
kubectl apply -f k8s/ingress.yaml

# Check status
kubectl get pods
kubectl logs -f deployment/accommodation-be
```

---

## 🔧 Environment Variables Reference

### Required in Production:

| Variable | Example | Source |
|----------|---------|--------|
| `DATABASE_URL_PROD` | `jdbc:postgresql://host:5432/db` | GitLab CI/CD or manual |
| `DATABASE_USERNAME` | `accommodation_user` | GitLab CI/CD or manual |
| `DATABASE_PASSWORD` | `************` | GitLab CI/CD Secret |
| `JWT_SECRET_KEY` | `************` | GitLab CI/CD Secret |
| `CORS_ALLOWED_ORIGINS` | `https://fe.site,https://www.site` | GitLab CI/CD |

### Optional (Has Defaults):

| Variable | Default | Description |
|----------|---------|-------------|
| `PROFILE_SERVICE_URL` | `https://2306219575-be.hafizmuh.site` | Profile Service backend |
| `SERVER_PORT` | `8080` | Application port |

---

## 📊 Switching Between Modes

### Scenario 1: Local Development → Production Test

```bash
# 1. Run in dev mode
./run-dev.bat

# 2. Setup prod env vars
set-prod-env.bat  # Edit with test production values

# 3. Switch to prod mode
run-prod.bat
```

### Scenario 2: Test Both Modes Simultaneously

```bash
# Terminal 1: Dev on port 8080
./run-dev.sh

# Terminal 2: Prod on port 8081
export SERVER_PORT=8081
source set-prod-env.sh
./run-prod.sh
```

### Scenario 3: Deploy to Production

```bash
# Option A: GitLab CI/CD (Automatic)
git push origin feat/deploy
# Pipeline automatically deploys with prod config

# Option B: Manual Docker
docker build -t muhammadyahya32/accommodation-be:v1.0 .
docker push muhammadyahya32/accommodation-be:v1.0
# Then update K8s deployment

# Option C: Direct JAR on Server
./gradlew clean build
scp build/libs/*.jar user@server:/app/
ssh user@server "cd /app && source prod-env.sh && java -jar *.jar --spring.profiles.active=prod"
```

---

## 🧪 Testing Configuration

### Verify Dev Configuration:

```bash
# Run in dev mode
./run-dev.sh

# In another terminal
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/info
curl http://localhost:8080/api/property/list
```

### Verify Prod Configuration:

```bash
# Run in prod mode
source set-prod-env.sh
./run-prod.sh

# In another terminal
# Check health
curl http://localhost:8080/actuator/health

# Check if prod database connected
curl http://localhost:8080/actuator/metrics/jdbc.connections.active

# Test auth with prod JWT
curl -H "Authorization: Bearer YOUR_PROD_TOKEN" \
     http://localhost:8080/api/booking/list
```

---

## 🆘 Troubleshooting

### Development Mode Issues:

**Database connection failed:**
```bash
# Check PostgreSQL running
psql -U postgres -l

# Create database if missing
psql -U postgres -c "CREATE DATABASE accommodation_db;"

# Test connection
psql -U postgres -d accommodation_db -c "SELECT 1;"
```

**Port 8080 already in use:**
```bash
# Find process
lsof -i :8080  # Linux/Mac
netstat -ano | findstr :8080  # Windows

# Kill or use different port
export SERVER_PORT=8081
./run-dev.sh
```

### Production Mode Issues:

**Environment variables not set:**
```bash
# Verify all required vars
echo $DATABASE_URL_PROD
echo $JWT_SECRET_KEY

# Re-source env file
source set-prod-env.sh  # Linux/Mac
set-prod-env.bat  # Windows
```

**Authentication fails:**
```bash
# Test Profile Service connectivity
curl https://2306219575-be.hafizmuh.site/api/auth/validate-token

# Check JWT secret matches Profile Service
# Verify CORS allows your frontend domain
```

**Database schema issues:**
```bash
# In application-prod.yml, temporarily change:
spring.jpa.hibernate.ddl-auto: validate  # Check only
spring.jpa.hibernate.ddl-auto: update    # Auto-update schema
spring.jpa.hibernate.ddl-auto: create    # ⚠️ DROPS ALL DATA!
```

---

## 📚 Related Documentation

- **LOCAL_DEVELOPMENT.md** - Development setup guide
- **ENV_VARIABLES.md** - Complete environment variables reference
- **SETUP_SUMMARY.md** - Quick start summary
- **application-dev.yml** - Development configuration
- **application-prod.yml** - Production configuration + GitLab CI/CD variables

---

## ✅ Checklist

### Before Development:
- [ ] PostgreSQL installed and running
- [ ] Database `accommodation_db` created
- [ ] Java 17+ installed
- [ ] Run `./run-dev.sh` or `run-dev.bat`

### Before Production:
- [ ] Production database ready
- [ ] JWT secret key generated (256+ bits)
- [ ] All environment variables in `set-prod-env.sh/bat`
- [ ] CORS domains configured
- [ ] Test with `run-prod.sh` locally before deploy
- [ ] GitLab CI/CD variables configured for auto-deployment
