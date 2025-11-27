# Security Temporarily Disabled

## Status: SECURITY & RBAC DISABLED ⚠️

Fitur security (login & RBAC) telah dinonaktifkan sementara untuk fokus development fitur support ticket.

## Yang Telah Dinonaktifkan

### 1. SecurityConfig.java
- **Method security disabled**: `@EnableMethodSecurity` di-comment
- **JWT filter disabled**: Filter JWT tidak lagi aktif
- **All endpoints are public**: Semua endpoint sekarang `permitAll()`
- **Original config**: Disimpan dalam block comment untuk restore nanti

### 2. OwnerValidationService.java
- **All validation methods disabled**: Semua method validasi return tanpa throw error
- **getCurrentUser()**: Return `null`
- **isCustomer/isOwner/isSuperadmin()**: Return `false`
- **validateOwnership()**: No-op (tidak melakukan apa-apa)
- **validateBookingAccess()**: No-op
- **validateBookingOwnership()**: No-op
- **validateRoomTypeOwnership()**: No-op
- **validateRoomOwnership()**: No-op
- **setOwnerForNewProperty()**: No-op
- **setCustomerForNewBooking()**: No-op
- **Original logic**: Disimpan dalam block comment untuk restore nanti

### 3. BookingController.java
- **@PreAuthorize annotations**: Di-comment pada endpoint:
  - `GET /api/bookings` - List bookings
  - `GET /api/bookings/{id}` - Booking detail

## Cara Menggunakan (Development Mode)

### Testing Endpoints Tanpa Authentication

Semua endpoint sekarang bisa diakses tanpa login/token:

```bash
# List all bookings (previously requires auth)
curl http://localhost:8080/api/bookings

# Get booking detail (previously requires auth)
curl http://localhost:8080/api/bookings/{bookingId}

# Create property (previously requires Owner/Admin)
curl -X POST http://localhost:8080/api/property/create \
  -H "Content-Type: application/json" \
  -d '{...}'

# Support tickets (focus development area)
curl -X POST http://localhost:8080/api/support-tickets \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": "...",
    "subject": "Test ticket",
    "description": "Testing support feature"
  }'
```

### Important Notes for Development

1. **Manual ID Assignment**: Karena `setCustomerForNewBooking()` dan `setOwnerForNewProperty()` disabled:
   - `customerId` dan `ownerId` harus di-set manual di request body atau di service layer
   - Pastikan UUID valid untuk testing

2. **No User Context**: `getCurrentUser()` return null, jadi:
   - Filtering by user tidak akan bekerja
   - Pastikan handle null check di logic yang pakai user context

3. **Access Control Bypassed**: 
   - Semua user bisa akses semua data
   - Testing harus dilakukan manual untuk memastikan logic bisnis tetap benar

## Cara Restore Security (Setelah Development Selesai)

### Step 1: SecurityConfig.java
```java
// Uncomment this line:
@EnableMethodSecurity(prePostEnabled = true)

// Hapus config permitAll() dan uncomment block comment:
/* ORIGINAL SECURITY CONFIGURATION */
```

### Step 2: OwnerValidationService.java
Untuk setiap method, uncomment block comment `/* ORIGINAL CODE */` dan hapus temporary return statement.

Contoh untuk `validateOwnership()`:
```java
public void validateOwnership(String propertyId) {
    // HAPUS line ini:
    // return;
    
    // UNCOMMENT block ini:
    /* ORIGINAL CODE - Uncomment to restore
    Authentication authentication = ...
    ... original validation logic ...
    */
}
```

### Step 3: BookingController.java
Uncomment semua `@PreAuthorize` annotations:
```java
// Change from:
// @PreAuthorize("hasAnyRole('CUSTOMER', 'ACCOMMODATION_OWNER', 'SUPERADMIN')")

// Back to:
@PreAuthorize("hasAnyRole('CUSTOMER', 'ACCOMMODATION_OWNER', 'SUPERADMIN')")
```

### Step 4: Testing Checklist After Restore
- [ ] Login berhasil dan dapat JWT token
- [ ] Customer hanya bisa lihat booking sendiri
- [ ] Owner hanya bisa lihat/edit property sendiri
- [ ] Superadmin bisa akses semua
- [ ] Unauthorized request dapat 401/403 response
- [ ] Support ticket RBAC berfungsi sesuai spec

## Files yang Dimodifikasi

### Backend
```
accommodation-2306212083-be/
├── src/main/java/.../
│   ├── config/
│   │   └── SecurityConfig.java (MODIFIED - security disabled)
│   ├── service/
│   │   └── OwnerValidationService.java (MODIFIED - validations disabled)
│   └── controller/
│       └── BookingController.java (MODIFIED - @PreAuthorize commented)
└── SECURITY_DISABLED.md (THIS FILE)
```

### Frontend
```
accommodation-2306212083-fe/
├── src/
│   ├── router/
│   │   └── index.ts (MODIFIED - navigation guard disabled)
│   └── components/
│       └── RoleGuard.vue (MODIFIED - always shows content)
```

## Frontend Changes

### router/index.ts
Navigation guard di-bypass, semua route bisa diakses:
```typescript
router.beforeEach(async (to, from, next) => {
  next() // Always allow
  return
  /* Original auth guard logic commented */
})
```

### RoleGuard.vue  
Component selalu render content:
```typescript
const hasPermission = computed(() => {
  return true // Always show
  /* Original role check commented */
})
```

## Development Focus

Dengan security disabled, fokus sekarang adalah:

✅ **Support Ticket Feature**
- Create ticket dari booking page
- List tickets for customer
- Admin manage tickets
- Add messages to tickets
- Mark messages as read
- Close tickets

## Troubleshooting

### Error: NullPointerException di User Context
**Cause**: Code masih expect `getCurrentUser()` return UserPrincipal  
**Fix**: Tambahkan null check atau mock user data untuk testing

### Error: customerId/ownerId is null
**Cause**: Auto-assignment disabled  
**Fix**: Set manual di request atau di service layer

### Frontend: Login page masih muncul
**Cause**: Frontend routing masih check auth  
**Fix**: Temporary bypass auth check di frontend router atau disable RoleGuard

---

**IMPORTANT**: Jangan commit disabled security ke production! File ini dan semua perubahan security hanya untuk development lokal.
