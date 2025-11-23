# RBAC Testing Quick Reference

## Test User Setup

Before testing, ensure you have test users with these roles in the Profile Service:

```json
// SUPERADMIN user
{
  "userId": "uuid-1",
  "username": "superadmin",
  "role": "SUPERADMIN"
}

// ACCOMMODATION_OWNER user
{
  "userId": "uuid-2", 
  "username": "owner1",
  "role": "ACCOMMODATION_OWNER"
}

// Another ACCOMMODATION_OWNER user
{
  "userId": "uuid-3",
  "username": "owner2", 
  "role": "ACCOMMODATION_OWNER"
}

// CUSTOMER user
{
  "userId": "uuid-4",
  "username": "customer1",
  "role": "CUSTOMER"
}
```

---

## Test Scenarios

### Scenario 1: Property Management

#### Test 1.1 - Owner Creates Property
```bash
# Login as owner1 (uuid-2)
POST /api/property/create
{
  "propertyName": "Owner1 Hotel",
  "type": 1,
  "address": "123 Street",
  "province": 1,
  "description": "Test property",
  "ownerName": "Owner One",
  "ownerId": "uuid-2"
}
# Expected: ✅ Success (201 Created)
```

#### Test 1.2 - Owner Views Own Property
```bash
# Login as owner1 (uuid-2)
GET /api/property/update/{property-id-owned-by-owner1}
# Expected: ✅ Success (200 OK)
```

#### Test 1.3 - Owner Tries to View Another Owner's Property
```bash
# Login as owner1 (uuid-2)
GET /api/property/update/{property-id-owned-by-owner2}
# Expected: ❌ 403 Forbidden
# Message: "You don't have permission to modify this property"
```

#### Test 1.4 - Owner Tries to Delete Property
```bash
# Login as owner1 (uuid-2)
DELETE /api/property/delete/{property-id}
# Expected: ❌ 403 Forbidden
```

#### Test 1.5 - Superadmin Deletes Property
```bash
# Login as superadmin (uuid-1)
DELETE /api/property/delete/{any-property-id}
# Expected: ✅ Success (200 OK)
```

---

### Scenario 2: Booking Management

#### Test 2.1 - Customer Creates Booking
```bash
# Login as customer1 (uuid-4)
POST /api/bookings/create
{
  "roomId": "room-id-1",
  "customerId": "uuid-4",
  "customerName": "Customer One",
  "customerEmail": "customer1@example.com",
  "customerPhone": "081234567890",
  "checkInDate": "2025-12-01T14:00:00",
  "checkOutDate": "2025-12-03T12:00:00",
  "capacity": 2,
  "isBreakfast": true
}
# Expected: ✅ Success (201 Created)
```

#### Test 2.2 - Customer Views Own Bookings
```bash
# Login as customer1 (uuid-4)
GET /api/bookings
# Expected: ✅ Success - Shows only bookings where customerId = uuid-4
```

#### Test 2.3 - Customer Tries to View Another Customer's Booking
```bash
# Login as customer1 (uuid-4)
GET /api/bookings/{booking-id-of-customer2}
# Expected: ❌ 403 Forbidden
# Message: "You can only access your own bookings"
```

#### Test 2.4 - Customer Pays Own Booking
```bash
# Login as customer1 (uuid-4)
PUT /api/bookings/pay/{own-booking-id}
# Expected: ✅ Success (200 OK)
```

#### Test 2.5 - Customer Tries to Pay Another's Booking
```bash
# Login as customer1 (uuid-4)
PUT /api/bookings/pay/{booking-id-of-customer2}
# Expected: ❌ 403 Forbidden
```

#### Test 2.6 - Owner Views Bookings for Their Property
```bash
# Login as owner1 (uuid-2)
GET /api/bookings
# Expected: ✅ Success - Shows only bookings for properties owned by uuid-2
```

#### Test 2.7 - Owner Tries to View Booking for Another Owner's Property
```bash
# Login as owner1 (uuid-2)
GET /api/bookings/{booking-id-for-owner2-property}
# Expected: ❌ 403 Forbidden
# Message: "You can only access bookings for your properties"
```

#### Test 2.8 - Superadmin Views All Bookings
```bash
# Login as superadmin (uuid-1)
GET /api/bookings
# Expected: ✅ Success - Shows ALL bookings
```

---

### Scenario 3: Maintenance Management

#### Test 3.1 - Owner Schedules Maintenance for Own Property's Room
```bash
# Login as owner1 (uuid-2)
POST /api/property/maintenance/add
{
  "roomId": "room-id-in-owner1-property",
  "startDate": "2025-12-01",
  "startTime": "09:00:00",
  "endDate": "2025-12-01",
  "endTime": "17:00:00"
}
# Expected: ✅ Success (201 Created)
```

#### Test 3.2 - Owner Tries to Schedule Maintenance for Another Owner's Room
```bash
# Login as owner1 (uuid-2)
POST /api/property/maintenance/add
{
  "roomId": "room-id-in-owner2-property",
  "startDate": "2025-12-01",
  "startTime": "09:00:00",
  "endDate": "2025-12-01",
  "endTime": "17:00:00"
}
# Expected: ❌ 403 Forbidden
# Message: "You can only schedule maintenance for rooms in your own properties"
```

#### Test 3.3 - Owner Views Maintenance for Own Properties
```bash
# Login as owner1 (uuid-2)
GET /api/property/maintenance
# Expected: ✅ Success - Shows only maintenance for owner1's properties
```

#### Test 3.4 - Customer Tries to Schedule Maintenance
```bash
# Login as customer1 (uuid-4)
POST /api/property/maintenance/add
{...}
# Expected: ❌ 403 Forbidden
```

---

### Scenario 4: Statistics

#### Test 4.1 - Owner Views Statistics for Own Properties
```bash
# Login as owner1 (uuid-2)
GET /api/bookings/chart?month=11&year=2025
# Expected: ✅ Success - Shows only statistics for owner1's properties
```

#### Test 4.2 - Customer Tries to View Statistics
```bash
# Login as customer1 (uuid-4)
GET /api/bookings/chart?month=11&year=2025
# Expected: ❌ 403 Forbidden
```

#### Test 4.3 - Superadmin Views All Statistics
```bash
# Login as superadmin (uuid-1)
GET /api/bookings/chart?month=11&year=2025
# Expected: ✅ Success - Shows statistics for ALL properties
```

---

### Scenario 5: Room Type Access

#### Test 5.1 - Public Views All Room Types
```bash
# No authentication
GET /api/room-types
# Expected: ✅ Success - Shows all active room types
```

#### Test 5.2 - Owner Views Room Type from Own Property
```bash
# Login as owner1 (uuid-2)
GET /api/room-types/{room-type-id-in-owner1-property}
# Expected: ✅ Success
```

#### Test 5.3 - Owner Tries to View Room Type from Another Owner's Property
```bash
# Login as owner1 (uuid-2)
GET /api/room-types/{room-type-id-in-owner2-property}
# Expected: ❌ 403 Forbidden
# Message: "You can only view room types from your own properties"
```

#### Test 5.4 - Public Views Room Type (Unauthenticated)
```bash
# No authentication
GET /api/room-types/{any-room-type-id}
# Expected: ✅ Success - Public can view all active room types
```

---

## Expected Response Formats

### Success Response (200/201)
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Forbidden Response (403)
```json
{
  "success": false,
  "message": "You can only access your own bookings",
  "error": "Forbidden",
  "hint": "Customers can only access their own bookings",
  "timestamp": "2025-11-22T10:30:00",
  "status": 403
}
```

### Not Found Response (404)
```json
{
  "success": false,
  "message": "Resource not found",
  "error": "Resource Not Found",
  "timestamp": "2025-11-22T10:30:00",
  "status": 404
}
```

---

## Automated Test Template

```java
@Test
void testOwnerCanOnlyAccessOwnProperty() {
    // Setup
    String owner1Token = getTokenForUser("owner1", "ACCOMMODATION_OWNER", "uuid-2");
    String owner2PropertyId = "property-owned-by-uuid-3";
    
    // Execute
    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/property/update/" + owner2PropertyId,
        HttpMethod.GET,
        new HttpEntity<>(createHeaders(owner1Token)),
        Map.class
    );
    
    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertFalse((Boolean) response.getBody().get("success"));
    assertTrue(response.getBody().get("message").toString()
        .contains("You don't have permission"));
}

@Test
void testCustomerCanOnlyAccessOwnBooking() {
    // Setup
    String customer1Token = getTokenForUser("customer1", "CUSTOMER", "uuid-4");
    String customer2BookingId = "booking-owned-by-uuid-5";
    
    // Execute
    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/bookings/" + customer2BookingId,
        HttpMethod.GET,
        new HttpEntity<>(createHeaders(customer1Token)),
        Map.class
    );
    
    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(response.getBody().get("message").toString()
        .contains("You can only access your own bookings"));
}

@Test
void testSuperadminCanAccessEverything() {
    // Setup
    String superadminToken = getTokenForUser("superadmin", "SUPERADMIN", "uuid-1");
    
    // Test property access
    ResponseEntity<Map> propertyResponse = restTemplate.exchange(
        "/api/property/update/any-property-id",
        HttpMethod.GET,
        new HttpEntity<>(createHeaders(superadminToken)),
        Map.class
    );
    assertEquals(HttpStatus.OK, propertyResponse.getStatusCode());
    
    // Test booking access
    ResponseEntity<Map> bookingResponse = restTemplate.exchange(
        "/api/bookings",
        HttpMethod.GET,
        new HttpEntity<>(createHeaders(superadminToken)),
        Map.class
    );
    assertEquals(HttpStatus.OK, bookingResponse.getStatusCode());
    
    // Test delete property
    ResponseEntity<Map> deleteResponse = restTemplate.exchange(
        "/api/property/delete/any-property-id",
        HttpMethod.DELETE,
        new HttpEntity<>(createHeaders(superadminToken)),
        Map.class
    );
    assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
}
```

---

## Common Issues & Solutions

### Issue 1: 403 Forbidden on Valid Request
**Cause:** Token has wrong role or userId doesn't match
**Solution:** Verify JWT token contains correct role and userId

### Issue 2: Owner Can't Access Own Property
**Cause:** Property ownerId doesn't match user's userId in token
**Solution:** Ensure property.ownerId == UUID.fromString(token.userId)

### Issue 3: Customer Can't Pay Own Booking
**Cause:** Booking customerId doesn't match user's userId in token
**Solution:** Ensure booking.customerId == token.userId

### Issue 4: Public Endpoints Return 401
**Cause:** SecurityConfig not properly configured
**Solution:** Verify `.permitAll()` is set for public endpoints

---

## Checklist for Complete RBAC Testing

- [ ] SUPERADMIN can access all properties
- [ ] SUPERADMIN can access all bookings
- [ ] SUPERADMIN can delete properties
- [ ] SUPERADMIN can view all statistics
- [ ] Owner can create properties with their ownerId
- [ ] Owner can only view/edit their own properties
- [ ] Owner cannot view other owners' properties
- [ ] Owner cannot delete any properties
- [ ] Owner can only see bookings for their properties
- [ ] Owner can only see maintenance for their rooms
- [ ] Owner can only see statistics for their properties
- [ ] Customer can create bookings
- [ ] Customer can only view their own bookings
- [ ] Customer can only pay/cancel/refund their own bookings
- [ ] Customer cannot view other customers' bookings
- [ ] Customer cannot create properties
- [ ] Customer cannot view statistics
- [ ] Public (unauthenticated) can view all properties
- [ ] Public can view all room types
- [ ] Public cannot create/modify anything
- [ ] All 403 errors have clear messages with hints

---

## Test Execution Order

1. **Setup Phase:**
   - Create test users in Profile Service
   - Get JWT tokens for each role
   - Create test properties owned by different owners

2. **Property Tests:**
   - Run Scenario 1 tests
   - Verify ownership validation

3. **Booking Tests:**
   - Create bookings as customers
   - Run Scenario 2 tests
   - Verify customer and owner filtering

4. **Maintenance Tests:**
   - Run Scenario 3 tests
   - Verify room ownership validation

5. **Statistics Tests:**
   - Run Scenario 4 tests
   - Verify property filtering

6. **Room Type Tests:**
   - Run Scenario 5 tests
   - Verify owner restrictions

7. **Cleanup:**
   - Delete test bookings
   - Soft delete test properties
   - Remove test users

---

## Performance Considerations

When testing with large datasets:
- Owner viewing bookings may be slow if they have many properties
- Consider pagination for `/api/bookings` endpoint
- Statistics endpoint filters in-memory - consider database-level filtering for production

## Security Audit Checklist

- [ ] No endpoints allow privilege escalation
- [ ] All ownership checks use UUID comparison (not string)
- [ ] No SQL injection vulnerabilities in filters
- [ ] JWT validation happens before all protected endpoints
- [ ] Error messages don't leak sensitive information
- [ ] All exceptions are caught and return consistent format
- [ ] No endpoints bypass authentication
- [ ] No endpoints bypass authorization
