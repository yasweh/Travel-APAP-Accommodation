Struktur Direktori MVC Spring Boot - Accommodation System
==========================================================

Project ini menggunakan arsitektur berlapis (layered architecture) MVC dengan requirement:
1. **Property Management** - Mengelola properti (Hotel/Villa/Apartemen), room types, dan rooms
2. **Booking Management** - Mengelola booking dengan status logic yang kompleks (Waiting → Confirmed → Done/Cancelled/Refund)
3. **Income Tracking** - Otomatis menghitung pendapatan properti berdasarkan status booking

## Struktur Package

```
apap.ti._5.accommodation_2306212083_be/
├── model/                          # JPA Entities sesuai ERD
│   ├── Property.java               # Entity properti dengan income tracking
│   ├── RoomType.java               # Entity tipe kamar (per lantai)
│   ├── Room.java                   # Entity kamar individual
│   └── AccommodationBooking.java  # Entity booking dengan status logic
│
├── repository/                     # Data Access Layer dengan custom queries
│   ├── PropertyRepository.java
│   ├── RoomTypeRepository.java
│   ├── RoomRepository.java
│   └── AccommodationBookingRepository.java
│
├── service/                        # Business Logic Layer
│   ├── PropertyService.java       # Interface
│   ├── PropertyServiceImpl.java   # Implementasi: create property + room types + rooms
│   ├── RoomService.java           # Interface
│   ├── RoomServiceImpl.java       # Implementasi: room & room type operations
│   ├── BookingService.java        # Interface
│   └── BookingServiceImpl.java    # Implementasi: status logic, income calculation
│
├── controller/                     # MVC Controllers (web views)
│   ├── PropertyController.java    # /property endpoints
│   └── BookingController.java     # /booking endpoints
│
├── rest/                          # REST API Layer (sudah ada, belum diupdate)
│   ├── controller/
│   ├── service/
│   └── dto/
│
├── util/                          # Utility Classes
│   └── IdGenerator.java           # Generate ID dengan format khusus
│
├── scheduler/                     # Scheduled Tasks
│   └── BookingScheduler.java     # Auto check-in daily at midnight
│
└── exception/                     # Exception Handling
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java
```

## Entity Relationships (sesuai ERD)

```
Property (1) ──< (Many) RoomType
RoomType (1) ──< (Many) Room
Room (1) ──< (Many) AccommodationBooking
```

### Property Fields
- propertyId: String (format: {PREFIX}-{UUID_LAST4}-{COUNTER})
- propertyName, type (0=Hotel,1=Villa,2=Apt), address, province
- totalRoom, activeStatus, income
- ownerName, ownerId (UUID)
- createdDate, updatedDate

### RoomType Fields
- roomTypeId: String (format: {COUNTER}–{NAME}–{FLOOR})
- name, price, capacity, facility, floor, description
- property (ManyToOne)

### Room Fields
- roomId: String (format: {PROPERTY_ID}-{FLOOR}{UNIT})
- name, availabilityStatus (0=Booked,1=Available)
- activeRoom (0=Maintenance,1=Active)
- maintenanceStart, maintenanceEnd
- roomType (ManyToOne)

### AccommodationBooking Fields
- bookingId: String (format: BOK-{TIMESTAMP}-{COUNTER})
- checkInDate, checkOutDate, totalDays, totalPrice
- **status**: 0=Waiting, 1=Confirmed, 2=Cancelled, 3=Refund, 4=Done
- customerId (UUID), customerName, customerEmail, customerPhone
- isBreakfast, **refund**, **extraPay**, capacity
- room (ManyToOne)

## Booking Status Logic (Core Business Logic)

### Status 0: Waiting for Payment
- **Dapat**: diupdate, dibayar (pay), dibatalkan (cancel)
- **Update**: memperbarui total harga tanpa memengaruhi income_property
- **Constraint**: tidak bisa diupdate jika masih memiliki extra_pay > 0

### Status 1: Payment Confirmed
- **Update harga baru > harga lama** → selisih masuk ke `extra_pay`
- **Update harga baru < harga lama** → selisih masuk ke `refund` + status jadi 3 (Request Refund)
- **Income**: belum memengaruhi income_property hingga transaksi selesai (Done)

### Status 2: Cancelled
- **Dari Waiting (status 0)**: tidak memengaruhi income_property
- **Dari Waiting with extra_pay**: kurangi income sebesar `total_baru - extra_pay`
- **Dari Payment Confirmed**: kurangi income sejumlah `total_price`
- **Dari Request Refund**: kurangi income sebesar `total_price + refund`
- **Effect**: room availability dikembalikan jadi Available

### Status 3: Request Refund
- Terjadi jika update menyebabkan harga lebih kecil dari harga awal
- Income dikurangi setelah refund disetujui atau saat check-in

### Status 4: Done
- **Trigger**: Otomatis via scheduler saat `check_in_date` tercapai dari status 1 (Payment Confirmed)
- **Condition Check**:
  - Jika masih ada `extra_pay > 0` → ubah jadi Cancelled
  - Jika ada `refund` → refund dikembalikan dan income dikurangi
- **Income Update**: `property.income += (totalPrice - refund)`
- **Effect**: room availability dikembalikan jadi Available

## ID Generator Formats

1. **Property ID**: `{PREFIX}-{LAST_4_UUID}-{COUNTER}`
   - Example: `APT-0000-004` (Apartemen), `HOT-abcd-001` (Hotel)

2. **RoomType ID**: `{COUNTER_PROPERTY}–{ROOM_TYPE_NAME}–{FLOOR}`
   - Example: `003–Single Room–2`

3. **Room ID**: `{PROPERTY_ID}-{FLOOR}{UNIT}`
   - Example: `APT-0000-004-101` (lantai 1, unit 01)

4. **Booking ID**: `BOK-{TIMESTAMP}-{COUNTER}`
   - Example: `BOK-1730000000-001`

## Endpoints Implemented

### Property Management (MVC Controller)
- `GET /property` - List semua properti (dengan filter: name, type, province)
- `GET /property/{id}` - Detail properti + room types + rooms
- `GET /property/create` - Form create properti
- `POST /property/create` - Submit create properti
- `GET /property/update/{id}` - Form update properti
- `POST /property/update/{id}` - Submit update properti
- `POST /property/{id}/delete` - Soft delete properti (activeStatus = 0)

### Booking Management (MVC Controller)
- `GET /booking` - List semua booking (filter by customerId)
- `GET /booking/{id}` - Detail booking
- `GET /booking/create` - Form create booking
- `POST /booking/create` - Submit create booking (status = 0)
- `POST /booking/update/{id}` - Update booking (dengan status logic)
- `POST /booking/pay/{id}` - Konfirmasi pembayaran (0 → 1)
- `POST /booking/cancel/{id}` - Batalkan booking (→ 2)
- `POST /booking/refund/{id}` - Request refund (1 → 3)

### Scheduled Task
- **Auto Check-in**: Berjalan setiap hari jam 00:00:00
  - Cari booking dengan status 1 (Payment Confirmed) dan checkInDate ≤ today
  - Jika `extra_pay > 0` → Cancel
  - Jika tidak → Update status ke 4 (Done) + update income + release room

## Service Layer Business Logic

### PropertyService
- `createProperty()`: Generate property ID, create room types, generate room IDs
- `updatePropertyIncome()`: Update income berdasarkan booking completion

### BookingService
- `createBooking()`: Generate booking ID, set status 0, block room
- `updateBooking()`: Implement status logic (extra_pay/refund calculation)
- `payBooking()`: Change status 0 → 1
- `cancelBooking()`: Change status → 2 + adjust property income
- `refundBooking()`: Change status 1 → 3
- `autoCheckInBookings()`: Called by scheduler, change status 1 → 4

## Database Schema Notes

- Semua ID adalah String (bukan auto-increment Long)
- UUID digunakan untuk ownerId dan customerId
- LocalDateTime untuk semua timestamp
- Integer untuk status (bukan enum) agar lebih fleksibel
- Soft delete untuk Property (activeStatus field)

## Next Steps untuk Development

1. ✅ Model entities sudah dibuat sesuai ERD
2. ✅ Repositories dengan custom queries sudah dibuat
3. ✅ Service layer dengan business logic sudah diimplementasi
4. ✅ Controllers dengan endpoints sudah dibuat
5. ✅ Scheduler untuk auto check-in sudah dibuat
6. ⏳ Perlu dibuat: Thymeleaf views (HTML templates)
7. ⏳ Perlu dibuat: Integration dengan wilayah.id API untuk provinsi
8. ⏳ Perlu dibuat: Validation annotations (@NotNull, @Size, dll)
9. ⏳ Perlu dibuat: Exception handling yang lebih spesifik
10. ⏳ Perlu dibuat: Unit tests dan integration tests
11. ⏳ REST API layer perlu diupdate sesuai requirement baru

## Important Notes

- **Minimalisir file baru**: Semua implementasi menggunakan struktur MVC yang sudah ada
- **ID Format**: Semua ID di-generate otomatis oleh IdGenerator utility
- **Status Logic**: Sangat penting untuk income calculation yang akurat
- **Scheduler**: Enabled via @EnableScheduling di main application
- **Soft Delete**: Property menggunakan activeStatus, bukan hard delete

