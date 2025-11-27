# GitLab CI/CD Variables Configuration

Complete list of required and optional GitLab CI/CD variables untuk automatic deployment.

## 📋 How to Set Variables

1. Go to GitLab project: **Settings → CI/CD → Variables**
2. Click **Add Variable** untuk setiap entry dibawah
3. Mark sensitive variables as **Masked** dan **Protected**
4. For SSH keys, use **File** type

---

## 🔐 REQUIRED Variables

### Docker Registry

| Key | Value Example | Type | Masked | Protected | Description |
|-----|---------------|------|--------|-----------|-------------|
| `DOCKER_USERNAME` | `muhammadyahya32` | Variable | ✅ Yes | ✅ Yes | Docker Hub username |
| `DOCKER_PASSWORD` | `dckr_pat_xxxxx` | Variable | ✅ Yes | ✅ Yes | Docker Hub access token |
| `DOCKER_IMAGE_NAME` | `muhammadyahya32/accommodation-be` | Variable | ❌ No | ✅ Yes | Full Docker image name |

**How to get Docker token:**
```bash
1. Login to hub.docker.com
2. Account Settings → Security → Access Tokens
3. Create new token with Read/Write permissions
4. Copy token (starts with dckr_pat_...)
```

---

### Database Configuration

| Key | Value Example | Type | Masked | Protected | Description |
|-----|---------------|------|--------|-----------|-------------|
| `DATABASE_URL_PROD` | `jdbc:postgresql://postgres.internal:5432/accommodation_prod` | Variable | ❌ No | ✅ Yes | Production database JDBC URL |
| `DATABASE_USERNAME` | `accommodation_user` | Variable | ❌ No | ✅ Yes | Database username |
| `DATABASE_PASSWORD` | `************` | Variable | ✅ Yes | ✅ Yes | Database password |

**Database URL Format:**
```
jdbc:postgresql://HOST:PORT/DATABASE_NAME
```

**Examples:**
- Cloud: `jdbc:postgresql://prod-db.aws.com:5432/accommodation_prod`
- Local K3s: `jdbc:postgresql://postgres-service:5432/accommodation_prod`
- External: `jdbc:postgresql://103.127.136.41:5432/accommodation_prod`

---

### JWT Configuration

| Key | Value Example | Type | Masked | Protected | Description |
|-----|---------------|------|--------|-----------|-------------|
| `JWT_SECRET_KEY` | `base64_encoded_secret_min_256_bits` | Variable | ✅ Yes | ✅ Yes | JWT signing secret key |

**Generate secure secret:**

```bash
# Linux/Mac/Git Bash
openssl rand -base64 64

# PowerShell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(48))

# Online
# https://www.random.org/strings/ (64 chars, alphanumeric)
```

**Requirements:**
- Minimum 256 bits (32 bytes)
- Base64 encoded recommended
- **NEVER** commit to repository
- Rotate regularly (every 90 days)

---

### CORS Configuration

| Key | Value Example | Type | Masked | Protected | Description |
|-----|---------------|------|--------|-----------|-------------|
| `CORS_ALLOWED_ORIGINS` | `https://2306212083-fe.hafizmuh.site,https://www.example.com` | Variable | ❌ No | ✅ Yes | Comma-separated allowed frontend URLs |

**Format:**
```
https://domain1.com,https://domain2.com,https://subdomain.domain3.com
```

**Rules:**
- Must include protocol (`https://`)
- No trailing slashes
- Comma-separated (no spaces)
- Include all frontend domains that will access API

**Example:**
```
https://2306212083-fe.hafizmuh.site,https://accommodation.example.com,https://www.example.com
```

---

### SSH Deployment

| Key | Value Example | Type | Masked | Protected | Description |
|-----|---------------|------|--------|-----------|-------------|
| `EC2_SSH_KEY` | `-----BEGIN OPENSSH PRIVATE KEY-----...` | **File** | ✅ Yes | ✅ Yes | Private SSH key untuk EC2 |
| `EC2_HOST` | `103.127.136.41` | Variable | ❌ No | ✅ Yes | EC2 server IP address |
| `EC2_USER` | `ubuntu` | Variable | ❌ No | ✅ Yes | SSH username |

**How to get SSH key:**
```bash
# If you have .pem file from AWS
cat your-key.pem

# If you have id_rsa
cat ~/.ssh/id_rsa

# Copy entire content including:
# -----BEGIN OPENSSH PRIVATE KEY-----
# ...
# -----END OPENSSH PRIVATE KEY-----
```

**Set as File type:**
1. Click **Add Variable**
2. Choose **Type: File**
3. Paste entire SSH private key content
4. Check **Masked** and **Protected**

---

## 🔄 OPTIONAL Variables

### Profile Service

| Key | Default Value | Type | Description |
|-----|---------------|------|-------------|
| `PROFILE_SERVICE_URL` | `https://2306219575-be.hafizmuh.site` | Variable | Profile Service backend URL |

**When to override:**
- Using different Profile Service instance
- Testing with staging Profile Service
- Using custom authentication service

---

### Server Configuration

| Key | Default Value | Type | Description |
|-----|---------------|------|-------------|
| `SERVER_PORT` | `8080` | Variable | Application server port |

**When to override:**
- Running multiple instances on same host
- Custom port requirements
- Load balancer configuration

---

## 📝 Complete Setup Example

### Minimal Required Setup:

```yaml
# Docker Registry
DOCKER_USERNAME = muhammadyahya32
DOCKER_PASSWORD = dckr_pat_AbCdEfGhIjKlMnOpQrStUvWxYz123456
DOCKER_IMAGE_NAME = muhammadyahya32/accommodation-be

# Database
DATABASE_URL_PROD = jdbc:postgresql://103.127.136.41:5432/accommodation_prod
DATABASE_USERNAME = postgres
DATABASE_PASSWORD = MySecureP@ssw0rd!2024

# JWT
JWT_SECRET_KEY = YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXowMTIzNDU2Nzg5YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXow

# CORS
CORS_ALLOWED_ORIGINS = https://2306212083-fe.hafizmuh.site

# SSH
EC2_SSH_KEY = <content of private key> (File type)
EC2_HOST = 103.127.136.41
EC2_USER = ubuntu
```

### With Optional Overrides:

```yaml
# ... all required variables above ...

# Optional
PROFILE_SERVICE_URL = https://staging-profile.example.com
SERVER_PORT = 8081
```

---

## 🔍 Verification

### Check Variables in Pipeline:

Add to `.gitlab-ci.yml` (for debugging only, remove after):

```yaml
verify-env:
  stage: build
  script:
    - echo "DATABASE_URL_PROD=${DATABASE_URL_PROD}"
    - echo "DATABASE_USERNAME=${DATABASE_USERNAME}"
    - echo "DATABASE_PASSWORD=[MASKED]"
    - echo "JWT_SECRET_KEY=[MASKED]"
    - echo "CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS}"
    - echo "EC2_HOST=${EC2_HOST}"
    - echo "EC2_USER=${EC2_USER}"
```

### Test SSH Connection:

```yaml
test-ssh:
  stage: build
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh
    - mkdir -p ~/.ssh
    - echo "$EC2_SSH_KEY" > ~/.ssh/id_rsa
    - chmod 600 ~/.ssh/id_rsa
    - ssh-keyscan -H "$EC2_HOST" >> ~/.ssh/known_hosts
  script:
    - ssh $EC2_USER@$EC2_HOST "echo 'SSH connection successful!'"
```

---

## 🔒 Security Best Practices

### 1. Use Masked Variables

✅ **DO:**
```
DATABASE_PASSWORD = [Masked]
JWT_SECRET_KEY = [Masked]
DOCKER_PASSWORD = [Masked]
EC2_SSH_KEY = [Masked]
```

❌ **DON'T:**
```
DATABASE_PASSWORD = visible_in_logs
```

### 2. Use Protected Variables

- Check **Protected** untuk semua sensitive variables
- Variables hanya tersedia di protected branches (`main`, `feat/deploy`)
- Prevents accidental exposure in feature branches

### 3. Rotate Secrets Regularly

| Secret | Rotation Schedule |
|--------|-------------------|
| JWT_SECRET_KEY | Every 90 days |
| DATABASE_PASSWORD | Every 180 days |
| DOCKER_PASSWORD | Every 365 days |
| EC2_SSH_KEY | Every 365 days or when compromised |

### 4. Separate Environments

**Staging Variables:**
```
DATABASE_URL_STAGING
JWT_SECRET_KEY_STAGING
```

**Production Variables:**
```
DATABASE_URL_PROD
JWT_SECRET_KEY_PROD
```

---

## 🐛 Troubleshooting

### Variable Not Found in Pipeline:

```bash
# Error: DATABASE_URL_PROD: unbound variable
```

**Solutions:**
1. Check variable name spelling (case-sensitive)
2. Verify variable is not expired
3. Check branch is protected (if variable is protected)
4. Verify variable scope (project vs group)

### SSH Connection Failed:

```bash
# Error: Permission denied (publickey)
```

**Solutions:**
1. Verify `EC2_SSH_KEY` contains complete private key
2. Check key format (OpenSSH vs PEM)
3. Verify `EC2_HOST` and `EC2_USER` are correct
4. Test SSH key manually:
   ```bash
   ssh -i key.pem ubuntu@103.127.136.41
   ```

### Docker Push Failed:

```bash
# Error: denied: access forbidden
```

**Solutions:**
1. Verify `DOCKER_USERNAME` and `DOCKER_PASSWORD` are correct
2. Check Docker token has write permissions
3. Verify `DOCKER_IMAGE_NAME` format: `username/image-name`
4. Test login manually:
   ```bash
   echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
   ```

### Database Connection Failed:

```bash
# Error: Connection refused
```

**Solutions:**
1. Verify `DATABASE_URL_PROD` format
2. Check database is accessible from GitLab runner
3. Verify firewall allows connections from GitLab IPs
4. Test connection:
   ```bash
   psql -h host -p 5432 -U username -d dbname
   ```

---

## 📚 Related Files

- **`.gitlab-ci.yml`** - Pipeline configuration using these variables
- **`application-prod.yml`** - Spring Boot config mapping variables
- **`k8s/deployment.yaml`** - Kubernetes deployment using ConfigMap/Secret
- **`set-prod-env.sh.example`** - Local testing template

---

## ✅ Setup Checklist

Before running pipeline:

- [ ] Docker Registry variables set (`DOCKER_USERNAME`, `DOCKER_PASSWORD`, `DOCKER_IMAGE_NAME`)
- [ ] Database variables set (`DATABASE_URL_PROD`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`)
- [ ] JWT secret generated and set (`JWT_SECRET_KEY` minimum 256 bits)
- [ ] CORS origins configured (`CORS_ALLOWED_ORIGINS` with all frontend URLs)
- [ ] SSH credentials set (`EC2_SSH_KEY` as File type, `EC2_HOST`, `EC2_USER`)
- [ ] All sensitive variables marked as **Masked**
- [ ] All variables marked as **Protected** (available only on protected branches)
- [ ] Test pipeline on `feat/deploy` branch
- [ ] Verify deployment on `https://2306212083-be.hafizmuh.site`

---

## 📞 Support

Jika ada masalah dengan GitLab CI/CD variables:

1. Check pipeline logs untuk error messages
2. Verify all variables are set correctly in GitLab UI
3. Test connection manually (SSH, Docker, Database)
4. Check `.gitlab-ci.yml` syntax
5. Review `RUNNING_MODES.md` untuk local testing

---

**Last Updated:** November 2024  
**GitLab Repository:** `accommodation-2306212083-be`  
**Protected Branch:** `feat/deploy`
