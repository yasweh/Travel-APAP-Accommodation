# Local Development Setup

Panduan setup untuk menjalankan Accommodation Service di localhost dengan database lokal, tapi tetap menggunakan Profile Service di hafizmuh.site untuk authentication.

## 📋 Prerequisites

- **Java 17+** (OpenJDK recommended)
- **PostgreSQL 15+** 
- **Node.js 18+** dan npm/pnpm
- **Git**

## 🗄️ Database Setup

### 1. Install PostgreSQL

```bash
# Windows (using Chocolatey)
choco install postgresql

# macOS (using Homebrew)
brew install postgresql@15

# Linux (Ubuntu/Debian)
sudo apt install postgresql-15
```

### 2. Create Database

```bash
# Login sebagai postgres user
sudo -u postgres psql

# Atau di Windows
psql -U postgres
```

```sql
-- Create database
CREATE DATABASE accommodation_db;

-- Create user (optional, atau gunakan postgres)
CREATE USER accommodation_user WITH PASSWORD 'your_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE accommodation_db TO accommodation_user;

-- Exit
\q
```

### 3. Verify Connection

```bash
psql -U postgres -d accommodation_db -c "SELECT version();"
```

## 🔧 Backend Setup

### 1. Clone & Navigate

```bash
cd accommodation-2306212083-be
```

### 2. Configure Database

Edit `src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/accommodation_db
    username: postgres  # atau accommodation_user
    password: postgres  # ganti dengan password Anda
```

### 3. Build & Run

```bash
# Build project
./gradlew clean build

# Run dengan dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Atau langsung run JAR
java -jar build/libs/accommodation-2306212083-be-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

Backend akan jalan di: **http://localhost:8080**

### 4. Verify Backend

```bash
# Health check
curl http://localhost:8080/actuator/health

# Should return:
# {"status":"UP"}
```

## 🎨 Frontend Setup

### 1. Navigate to Frontend

```bash
cd accommodation-2306212083-fe
```

### 2. Install Dependencies

```bash
npm install
# atau
pnpm install
```

### 3. Configure Environment

File `.env.development` sudah dikonfigurasi untuk connect ke localhost:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_PROFILE_SERVICE_URL=https://2306219575-be.hafizmuh.site
VITE_PROFILE_FRONTEND_URL=https://2306219575-fe.hafizmuh.site
```

### 4. Run Development Server

```bash
npm run dev
# atau
pnpm dev
```

Frontend akan jalan di: **http://localhost:5173**

## 🔐 Authentication Flow

### Login Process:

1. Buka **http://localhost:5173**
2. Klik fitur yang butuh authentication (misal: Booking)
3. Akan redirect ke **https://2306219575-fe.hafizmuh.site/login**
4. Login dengan akun Anda di Profile Service
5. Setelah sukses, redirect kembali ke **http://localhost:5173**
6. JWT token tersimpan di cookie, backend Anda akan validate ke Profile Service

### Architecture:

```
┌─────────────────┐         ┌──────────────────┐
│  Frontend       │         │  Backend         │
│  localhost:5173 │◄───────►│  localhost:8080  │
└────────┬────────┘         └────────┬─────────┘
         │                           │
         │  Login/Validate Token     │
         │                           │
         └──────────┬────────────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │  Profile Service     │
         │  hafizmuh.site       │
         │  (Remote Auth)       │
         └──────────────────────┘
```

## 📡 API Testing

### Test Endpoints dengan curl:

```bash
# 1. Public endpoint (no auth required)
curl http://localhost:8080/api/property/list

# 2. Protected endpoint (dengan JWT token)
# Ambil token dari browser cookie setelah login
TOKEN="your_jwt_token_here"

curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/booking/list

# 3. Create booking
curl -X POST http://localhost:8080/api/booking/create \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "propertyId": "uuid-here",
       "checkInDate": "2024-12-01",
       "checkOutDate": "2024-12-05",
       "rooms": [...]
     }'
```

### Test dengan Postman/Bruno:

1. Import collection dari `TEST_API.md`
2. Set `baseUrl` variable ke `http://localhost:8080/api`
3. Login via Profile Service di browser
4. Copy JWT token dari cookie
5. Add Authorization header: `Bearer <token>`

## 🔍 Troubleshooting

### Backend tidak connect ke database

```bash
# Check PostgreSQL status
sudo systemctl status postgresql  # Linux
brew services list | grep postgres  # macOS

# Check port 5432
netstat -an | grep 5432
lsof -i :5432

# Test connection
psql -U postgres -d accommodation_db
```

### Port 8080 already in use

```bash
# Find process using port 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows
```

### CORS Error dari frontend

Backend sudah dikonfigurasi CORS untuk localhost:
- `http://localhost:5173`
- `http://localhost:3000`
- `http://127.0.0.1:5173`

Jika masih error, cek `CorsConfig.java` dan pastikan origin Anda terdaftar.

### JWT Token tidak valid

```bash
# Test validate token endpoint
curl -X POST https://2306219575-be.hafizmuh.site/api/auth/validate-token \
     -H "Cookie: JWT_TOKEN=your_token" \
     -H "Content-Type: application/json"
```

Token valid jika return:
```json
{
  "status": 200,
  "data": {
    "valid": true,
    "userId": "...",
    "username": "..."
  }
}
```

### Database tables tidak terbuat

Default `ddl-auto=update` hanya add missing columns. Untuk reset:

```yaml
# application-dev.yml
spring:
  jpa:
    hibernate:
      ddl-auto: create  # ⚠️ WARNING: Akan hapus semua data!
```

Atau manual via SQL:
```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

## 📦 Environment Variables

Backend dapat override config via environment variables:

```bash
# Database
export DATABASE_URL=jdbc:postgresql://localhost:5432/accommodation_db
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=your_password

# JWT
export JWT_SECRET_KEY=your-secret-key-min-256-bits

# CORS
export CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# Profile Service
export PROFILE_SERVICE_URL=https://2306219575-be.hafizmuh.site

# Run
./gradlew bootRun
```

## 🚀 Next Steps

Setelah local development jalan:

1. **Test all features** - Property, Booking, Maintenance, Support Ticket
2. **Check logs** - Lihat `logs/` atau terminal output
3. **Profile endpoints** - Test dengan user berbeda role (CUSTOMER, OWNER, ADMIN)
4. **Database check** - Query PostgreSQL untuk verify data tersimpan

## 📚 Documentation

- Backend API: `TEST_API.md`
- Support System: `SUPPORT_TICKET_DOCUMENTATION.md`
- Architecture: `ARCHITECTURE.md`

## 🆘 Need Help?

Jika ada masalah:
1. Check backend logs: `./gradlew bootRun --debug`
2. Check frontend console: Browser DevTools (F12) → Console
3. Verify database: `psql -U postgres -d accommodation_db`
4. Test Profile Service: https://2306219575-be.hafizmuh.site/api/auth/validate-token
