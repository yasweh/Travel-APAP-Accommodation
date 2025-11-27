package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.BookingRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.BookingResponseDTO;
import apap.ti._5.accommodation_2306212083_be.dto.PropertyStatisticsDTO;
import apap.ti._5.accommodation_2306212083_be.dto.RoomDTO;
import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.security.annotations.IsCustomer;
import apap.ti._5.accommodation_2306212083_be.security.annotations.IsOwner;
import apap.ti._5.accommodation_2306212083_be.service.BookingService;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.service.OwnerValidationService;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final PropertyService propertyService;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final OwnerValidationService ownerValidationService;

    /**
     * GET /api/bookings - List all bookings with filters
     * CUSTOMER: can view own bookings
     * ACCOMMODATION_OWNER: can view bookings for their properties
     * SUPERADMIN: can view all bookings
     * 
     * TEMPORARILY DISABLED - RBAC commented for support feature development
     */
    @GetMapping
    // @PreAuthorize("hasAnyRole('CUSTOMER', 'ACCOMMODATION_OWNER', 'SUPERADMIN')") // COMMENTED - RBAC disabled
    public ResponseEntity<Map<String, Object>> listBookings(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) Integer status) {
        
        UserPrincipal user = ownerValidationService.getCurrentUser();
        List<BookingResponseDTO> bookings;
        
        if (ownerValidationService.isCustomer()) {
            // Customer can only see their own bookings
            UUID uuid = UUID.fromString(user.getUserId());
            List<AccommodationBooking> rawBookings = bookingService.getBookingsByCustomer(uuid);
            bookings = rawBookings.stream()
                .map(b -> bookingService.getBookingDetail(b.getBookingId()))
                .collect(Collectors.toList());
                
        } else if (ownerValidationService.isOwner()) {
            // Owner can only see bookings for their properties
            bookings = bookingService.getAllBookingsAsDTO().stream()
                .filter(booking -> {
                    try {
                        String propertyOwnerId = getPropertyOwnerIdFromBooking(booking.getBookingId());
                        return user.getUserId().equals(propertyOwnerId);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
                
        } else {
            // Superadmin sees all bookings
            if (customerId != null) {
                UUID uuid = UUID.fromString(customerId);
                List<AccommodationBooking> rawBookings = bookingService.getBookingsByCustomer(uuid);
                bookings = rawBookings.stream()
                    .map(b -> bookingService.getBookingDetail(b.getBookingId()))
                    .collect(Collectors.toList());
            } else if (status != null) {
                List<AccommodationBooking> rawBookings = bookingService.getBookingsByStatus(status);
                bookings = rawBookings.stream()
                    .map(b -> bookingService.getBookingDetail(b.getBookingId()))
                    .collect(Collectors.toList());
            } else {
                bookings = bookingService.getAllBookingsAsDTO();
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", bookings);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/bookings/{id} - Booking detail with conditional buttons
     * CUSTOMER: can view own booking
     * ACCOMMODATION_OWNER: can view booking for their property
     * SUPERADMIN: can view any booking
     * 
     * TEMPORARILY DISABLED - RBAC commented for support feature development
     */
    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyRole('CUSTOMER', 'ACCOMMODATION_OWNER', 'SUPERADMIN')") // COMMENTED - RBAC disabled
    public ResponseEntity<Map<String, Object>> detailBooking(@PathVariable String id) {
        try {
            // Use OwnerValidationService for access validation
            ownerValidationService.validateBookingAccess(id);
            
            BookingResponseDTO booking = bookingService.getBookingDetail(id);
            
            // Determine available actions based on status
            Map<String, Boolean> availableActions = new HashMap<>();
            availableActions.put("canPay", booking.getStatus() == 0); // Waiting -> Confirm Payment
            availableActions.put("canCancel", booking.getStatus() == 0); // Waiting -> Cancel
            availableActions.put("canRefund", booking.getStatus() == 1); // Confirmed -> Request Refund
            availableActions.put("canUpdate", booking.getStatus() == 0 || booking.getStatus() == 1); // Can update if not done/cancelled
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", booking);
            response.put("availableActions", availableActions);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * PUT /api/bookings/pay/{id} - Confirm payment (status 0 -> 1)
     * Requires: CUSTOMER role (own booking only)
     */
    @PutMapping("/pay/{id}")
    @IsCustomer
    public ResponseEntity<Map<String, Object>> payBooking(@PathVariable String id) {
        try {
            // Use OwnerValidationService for ownership validation
            ownerValidationService.validateBookingOwnership(id);
            
            bookingService.payBooking(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment confirmed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * PUT /api/bookings/cancel/{id} - Cancel booking (status 0 -> 1)
     * Requires: CUSTOMER role (own booking only)
     */
    @PutMapping("/cancel/{id}")
    @IsCustomer
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable String id) {
        try {
            // Use OwnerValidationService for ownership validation
            ownerValidationService.validateBookingOwnership(id);
            
            bookingService.cancelBooking(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking cancelled successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * PUT /api/bookings/refund/{id} - Request refund (status 1 -> 3)
     * Requires: CUSTOMER role (own booking only)
     */
    @PutMapping("/refund/{id}")
    @IsCustomer
    public ResponseEntity<Map<String, Object>> refundBooking(@PathVariable String id) {
        try {
            // Use OwnerValidationService for ownership validation
            ownerValidationService.validateBookingOwnership(id);
            
            bookingService.refundBooking(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Refund requested successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ============ NEW ENDPOINTS FOR BOOKING MANAGEMENT ============

    /**
     * GET /api/bookings/create/{idRoom} - Load room details for booking form
     */
    @GetMapping("/create/{idRoom}")
    public ResponseEntity<Map<String, Object>> getCreateBookingWithRoom(@PathVariable String idRoom) {
        try {
            Room room = roomRepository.findById(idRoom)
                .orElseThrow(() -> new RuntimeException("Room not found"));
            
            RoomType roomType = room.getRoomType();
            RoomDTO roomDTO = mapRoomToDTO(room);
            
            // Add property and room type IDs for frontend cascading
            Map<String, Object> roomData = new HashMap<>();
            roomData.put("roomId", roomDTO.getRoomId());
            roomData.put("name", roomDTO.getName());
            roomData.put("roomTypeName", roomDTO.getRoomTypeName());
            roomData.put("roomTypePrice", roomDTO.getRoomTypePrice());
            roomData.put("roomTypeCapacity", roomDTO.getRoomTypeCapacity());
            roomData.put("roomTypeFacility", roomDTO.getRoomTypeFacility());
            roomData.put("floor", roomDTO.getFloor());
            roomData.put("propertyId", roomType.getProperty().getPropertyId());
            roomData.put("roomTypeId", roomType.getRoomTypeId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roomData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * GET /api/bookings/create - Load data for cascading dropdown booking form
     */
    @GetMapping("/create")
    public ResponseEntity<Map<String, Object>> getCreateBookingData() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", propertyService.getAllActiveProperties());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/bookings/roomtypes/{propertyId} - Get room types for property (cascading dropdown)
     */
    @GetMapping("/roomtypes/{propertyId}")
    public ResponseEntity<Map<String, Object>> getRoomTypesForProperty(@PathVariable String propertyId) {
        try {
            List<RoomType> roomTypes = roomTypeRepository.findByProperty_PropertyIdAndActiveStatusOrderByFloorAsc(propertyId, 1);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roomTypes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/bookings/rooms/{propertyId}/{roomTypeId} - Get available rooms (cascading dropdown)
     * Supports optional date range filtering
     */
    @GetMapping("/rooms/{propertyId}/{roomTypeId}")
    public ResponseEntity<Map<String, Object>> getAvailableRooms(
            @PathVariable String propertyId,
            @PathVariable String roomTypeId,
            @RequestParam(required = false) String checkInDate,
            @RequestParam(required = false) String checkOutDate) {
        try {
            List<Room> rooms = roomRepository.findByRoomType_RoomTypeId(roomTypeId)
                .stream()
                .filter(room -> room.getActiveRoom() == 1) // Not soft-deleted
                .collect(Collectors.toList());
            
            // If dates provided, filter by availability
            if (checkInDate != null && checkOutDate != null) {
                LocalDateTime checkIn = LocalDateTime.parse(checkInDate + "T14:00:00");
                LocalDateTime checkOut = LocalDateTime.parse(checkOutDate + "T12:00:00");
                
                rooms = rooms.stream()
                    .filter(room -> bookingService.isRoomAvailableForDates(room.getRoomId(), checkIn, checkOut))
                    .collect(Collectors.toList());
            } else {
                // No dates, just filter availabilityStatus (old behavior)
                rooms = rooms.stream()
                    .filter(room -> room.getAvailabilityStatus() == 1)
                    .collect(Collectors.toList());
            }
            
            List<RoomDTO> roomDTOs = rooms.stream()
                .map(this::mapRoomToDTO)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roomDTOs);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * POST /api/bookings/create - Create new booking (supports both with/without room selection)
     * Requires: CUSTOMER role
     */
    @PostMapping("/create")
    @IsCustomer
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody BookingRequestDTO request) {
        try {
            BookingResponseDTO booking;
            
            if (request.getRoomId() != null && !request.getRoomId().isEmpty()) {
                // Create with specific room
                booking = bookingService.createBookingWithRoom(request.getRoomId(), request);
            } else {
                // Create with selection (should have roomId from dropdown)
                booking = bookingService.createBookingWithSelection(request);
            }
            
            // Auto-set customer ID using OwnerValidationService
            AccommodationBooking rawBooking = bookingService.getBookingById(booking.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found after creation"));
            ownerValidationService.setCustomerForNewBooking(rawBooking);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking created successfully");
            response.put("data", booking);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/bookings/update/{id} - Load booking data for update form
     * Requires: CUSTOMER role (own booking only)
     */
    @GetMapping("/update/{id}")
    @IsCustomer
    public ResponseEntity<Map<String, Object>> getUpdateBooking(@PathVariable String id) {
        try {
            // Use OwnerValidationService for ownership validation
            ownerValidationService.validateBookingOwnership(id);
            
            BookingResponseDTO booking = bookingService.getBookingDetail(id);
            
            // Check if can be updated based on status
            // Status 2=Cancelled, 3=Request Refund, 4=Done cannot be updated
            if (booking.getStatus() == 2) {
                throw new RuntimeException("Cannot update cancelled booking");
            }
            if (booking.getStatus() == 3) {
                throw new RuntimeException("Cannot update booking with pending refund request");
            }
            if (booking.getStatus() == 4) {
                throw new RuntimeException("Cannot update completed booking");
            }
            
            // Convert properties to simple DTOs to avoid Hibernate proxy issues
            List<Map<String, Object>> propertiesData = propertyService.getAllActiveProperties().stream()
                .map(p -> {
                    Map<String, Object> propData = new HashMap<>();
                    propData.put("propertyId", p.getPropertyId());
                    propData.put("propertyName", p.getPropertyName());
                    propData.put("type", p.getType());
                    propData.put("address", p.getAddress());
                    propData.put("province", p.getProvince());
                    return propData;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("booking", booking);
            response.put("properties", propertiesData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * PUT /api/bookings/update/{id} - Update booking
     * Requires: CUSTOMER role (own booking only)
     */
    @PutMapping("/update/{id}")
    @IsCustomer
    public ResponseEntity<Map<String, Object>> updateBooking(
            @PathVariable String id,
            @RequestBody BookingRequestDTO request) {
        try {
            // Use OwnerValidationService for ownership validation
            ownerValidationService.validateBookingOwnership(id);
            
            BookingResponseDTO updated = bookingService.updateBookingFromDTO(id, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking updated successfully");
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * POST /api/bookings/status/pay - Pay for booking
     * Requires: CUSTOMER role (own booking only)
     */
    @PostMapping("/status/pay")
    @IsCustomer
    public ResponseEntity<Map<String, Object>> payBookingStatus(@RequestBody Map<String, String> request) {
        try {
            String bookingId = request.get("bookingId");
            
            // Use OwnerValidationService for ownership validation
            ownerValidationService.validateBookingOwnership(bookingId);
            
            bookingService.payBookingById(bookingId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment confirmed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * POST /api/bookings/status/cancel - Cancel booking
     * Requires: CUSTOMER role (own booking only)
     */
    @PostMapping("/status/cancel")
    @IsCustomer
    public ResponseEntity<Map<String, Object>> cancelBookingStatus(@RequestBody Map<String, String> request) {
        try {
            String bookingId = request.get("bookingId");
            
            // Use OwnerValidationService for ownership validation
            ownerValidationService.validateBookingOwnership(bookingId);
            
            bookingService.cancelBookingById(bookingId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking cancelled successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * POST /api/bookings/status/refund - Request refund
     * Requires: CUSTOMER role (own booking only)
     */
    @PostMapping("/status/refund")
    @IsCustomer
    public ResponseEntity<Map<String, Object>> refundBookingStatus(@RequestBody Map<String, String> request) {
        try {
            String bookingId = request.get("bookingId");
            
            // Use OwnerValidationService for ownership validation
            ownerValidationService.validateBookingOwnership(bookingId);
            
            bookingService.refundBookingById(bookingId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Refund requested successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/bookings/chart - Get monthly statistics for chart
     * ACCOMMODATION_OWNER: Only shows statistics for their properties
     * SUPERADMIN: Shows all statistics
     */
    @GetMapping("/chart")
    @IsOwner
    public ResponseEntity<Map<String, Object>> getBookingStatistics(
            @RequestParam int month,
            @RequestParam int year) {
        try {
            UserPrincipal user = ownerValidationService.getCurrentUser();
            List<PropertyStatisticsDTO> statistics = propertyService.getMonthlyStatistics(month, year);
            
            // Filter by owner if not superadmin
            if (ownerValidationService.isOwner() && !ownerValidationService.isSuperadmin()) {
                UUID ownerUuid = UUID.fromString(user.getUserId());
                statistics = statistics.stream()
                    .filter(stat -> {
                        try {
                            Property property = propertyService.getPropertyById(stat.getPropertyId())
                                .orElse(null);
                            return property != null && property.getOwnerId().equals(ownerUuid);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ============ HELPER METHODS ============
    
    private RoomDTO mapRoomToDTO(Room room) {
        RoomType roomType = room.getRoomType();
        return RoomDTO.builder()
            .roomId(room.getRoomId())
            .name(room.getName())
            .roomTypeName(roomType.getName())
            .roomTypePrice(roomType.getPrice())
            .roomTypeCapacity(roomType.getCapacity())
            .roomTypeFacility(roomType.getFacility())
            .floor(roomType.getFloor())
            .availabilityStatus(room.getAvailabilityStatus())
            .activeRoom(room.getActiveRoom())
            .build();
    }
    
    /**
     * Get property owner ID from booking
     */
    private String getPropertyOwnerIdFromBooking(String bookingId) {
        AccommodationBooking booking = bookingService.getBookingById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Room room = booking.getRoom();
        RoomType roomType = room.getRoomType();
        Property property = roomType.getProperty();
        
        return property.getOwnerId().toString();
    }
}
