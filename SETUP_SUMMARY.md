# Local Development Configuration Summary

## ✅ Yang Sudah Dikonfigurasi

### Backend Files:

1. **`src/main/resources/application-dev.yml`** (NEW)
   - Database: `jdbc:postgresql://localhost:5432/accommodation_db`
   - Username: `postgres`
   - Password: `postgres`
   - Profile Service: `https://2306219575-be.hafizmuh.site`
   - CORS: localhost:5173, localhost:3000
   - Logging: DEBUG level untuk development

2. **`src/main/resources/application-prod.yml`** (NEW)
   - Production config dengan environment variables
   - For K8s deployment

3. **`src/main/resources/application.properties`** (UPDATED)
   - Added profile support
   - Environment variable overrides
   - Default values untuk local dev

### Frontend Files:

1. **`.env.development`** (NEW)
   ```env
   VITE_API_BASE_URL=http://localhost:8080/api
   VITE_PROFILE_SERVICE_URL=https://2306219575-be.hafizmuh.site
   VITE_PROFILE_FRONTEND_URL=https://2306219575-fe.hafizmuh.site
   ```

2. **`.env.production`** (UPDATED)
   ```env
   VITE_API_BASE_URL=https://2306212083-be.hafizmuh.site/api
   VITE_PROFILE_SERVICE_URL=https://2306219575-be.hafizmuh.site
   VITE_PROFILE_FRONTEND_URL=https://2306219575-fe.hafizmuh.site
   ```

3. **`.env.example`** (NEW)
   - Template untuk local configuration

4. **`src/stores/auth.ts`** (UPDATED)
   - Menggunakan `VITE_PROFILE_SERVICE_URL` dan `VITE_PROFILE_FRONTEND_URL`
   - Dynamic URL dari environment variables

5. **`src/services/api.ts`** (UPDATED)
   - Base URL dari `VITE_API_BASE_URL`
   - Auth redirect menggunakan env variable

6. **`src/router/index.ts`** (UPDATED)
   - Login redirect menggunakan env variable

### Helper Scripts:

1. **`run-dev.bat`** (Windows)
   - Build dan run backend dengan dev profile
   - Double-click untuk start

2. **`run-dev.sh`** (Linux/Mac)
   - Build dan run backend dengan dev profile
   - `chmod +x run-dev.sh && ./run-dev.sh`

### Documentation:

1. **`LOCAL_DEVELOPMENT.md`** (NEW)
   - Complete setup guide
   - Database setup
   - Troubleshooting
   - API testing examples

## 🚀 Quick Start

### Backend:

```bash
# Windows
cd accommodation-2306212083-be
run-dev.bat

# Linux/Mac
cd accommodation-2306212083-be
chmod +x run-dev.sh
./run-dev.sh

# Manual
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Frontend:

```bash
cd accommodation-2306212083-fe
npm install
npm run dev
```

## 🔐 Authentication Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   USER BROWSER                          │
│                 http://localhost:5173                   │
└──────────────┬──────────────────────┬───────────────────┘
               │                      │
               │ API Calls            │ Login/Validate
               │ (Accommodation)      │ (Profile Service)
               │                      │
               ▼                      ▼
┌──────────────────────┐   ┌─────────────────────────────┐
│  Accommodation BE    │   │  Profile Service (Remote)   │
│  localhost:8080      │◄──┤  2306219575-be.hafizmuh.site│
│                      │   │                             │
│  - Property APIs     │   │  - Login                    │
│  - Booking APIs      │   │  - Register                 │
│  - Maintenance APIs  │   │  - Token Validation         │
│  - Support Ticket    │   │  - User Profile             │
└──────────┬───────────┘   └─────────────────────────────┘
           │
           │ Store Data
           ▼
┌──────────────────────┐
│  PostgreSQL (Local)  │
│  localhost:5432      │
│  accommodation_db    │
└──────────────────────┘
```

## 📝 Configuration Details

### Backend Environment Variables (Optional Override):

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/accommodation_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

# Profile Service (Remote)
PROFILE_SERVICE_URL=https://2306219575-be.hafizmuh.site

# JWT
JWT_SECRET_KEY=your-secret-key-min-256-bits

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# Spring Profile
SPRING_PROFILES_ACTIVE=dev
```

### Frontend Environment Variables:

Development mode automatically loads `.env.development`:
- Backend API: `http://localhost:8080/api`
- Profile Service: `https://2306219575-be.hafizmuh.site`

Production mode loads `.env.production`:
- Backend API: `https://2306212083-be.hafizmuh.site/api`
- Profile Service: `https://2306219575-be.hafizmuh.site`

## 🔄 Workflow

### 1. First Time Setup:

```bash
# Backend
cd accommodation-2306212083-be
./run-dev.bat  # atau ./run-dev.sh

# Frontend (new terminal)
cd accommodation-2306212083-fe
npm install
npm run dev
```

### 2. Daily Development:

```bash
# Terminal 1: Backend
cd accommodation-2306212083-be
./run-dev.bat

# Terminal 2: Frontend
cd accommodation-2306212083-fe
npm run dev
```

### 3. Test Authentication:

1. Open http://localhost:5173
2. Click "Booking" atau fitur yang butuh login
3. Redirect ke https://2306219575-fe.hafizmuh.site/login
4. Login dengan akun Anda
5. Redirect kembali ke localhost dengan JWT token
6. Backend validate token ke Profile Service
7. ✅ Authenticated!

## 🧪 Testing

### Backend API:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Public endpoint
curl http://localhost:8080/api/property/list

# Protected endpoint (butuh JWT)
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/booking/list
```

### Frontend:

1. Open DevTools (F12) → Network tab
2. Check API calls ke `http://localhost:8080/api`
3. Check JWT token di Application → Cookies
4. Check auth validation di Console

## ⚙️ Profiles

### Development (`application-dev.yml`):
- ✅ Local PostgreSQL
- ✅ DEBUG logging
- ✅ Show SQL queries
- ✅ CORS for localhost
- ✅ Remote Profile Service

### Production (`application-prod.yml`):
- ✅ Production PostgreSQL (from ConfigMap/Secret)
- ✅ INFO logging
- ✅ Hide SQL queries
- ✅ CORS for deployed domains
- ✅ Remote Profile Service

## 🎯 Benefits

1. **Faster Development**: No need to deploy untuk test changes
2. **Local Debugging**: Full access to logs dan database
3. **Isolated Testing**: Test tanpa affect production
4. **Real Authentication**: Tetap pake Profile Service yang sama
5. **Easy Switching**: Switch antara local dan production dengan environment variables

## 📚 Related Documentation

- **LOCAL_DEVELOPMENT.md**: Detailed setup guide
- **TEST_API.md**: API testing examples
- **ARCHITECTURE.md**: System architecture
- **SUPPORT_TICKET_DOCUMENTATION.md**: Support system docs

## 🆘 Common Issues

### Backend won't start:
- Check PostgreSQL running: `psql -U postgres -l`
- Check port 8080 available: `lsof -i :8080`
- Check Java version: `java -version` (need 17+)

### Frontend CORS error:
- Verify backend CORS config in `application-dev.yml`
- Check backend logs for CORS errors
- Clear browser cache

### Authentication fails:
- Check Profile Service online: `curl https://2306219575-be.hafizmuh.site/api/auth/validate-token`
- Check JWT token in browser cookies
- Try logout and login again

### Database connection error:
- Create database: `psql -U postgres -c "CREATE DATABASE accommodation_db;"`
- Check credentials in `application-dev.yml`
- Test connection: `psql -U postgres -d accommodation_db`
