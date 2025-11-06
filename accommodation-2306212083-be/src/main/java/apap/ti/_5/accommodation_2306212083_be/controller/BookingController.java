package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.BookingRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.BookingResponseDTO;
import apap.ti._5.accommodation_2306212083_be.dto.PropertyStatisticsDTO;
import apap.ti._5.accommodation_2306212083_be.dto.RoomDTO;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.service.BookingService;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * GET /api/bookings - List all bookings with filters
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listBookings(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) Integer status) {
        
        List<AccommodationBooking> bookings;
        
        if (customerId != null) {
            UUID uuid = UUID.fromString(customerId);
            bookings = bookingService.getBookingsByCustomer(uuid);
        } else if (status != null) {
            bookings = bookingService.getBookingsByStatus(status);
        } else {
            bookings = bookingService.getAllBookings();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", bookings);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/bookings/{id} - Booking detail with conditional buttons
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detailBooking(@PathVariable String id) {
        try {
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

    // REMOVED: Old createBooking method - now using BookingRequestDTO version below
    // REMOVED: Old updateBooking method - now using BookingRequestDTO version below

    /**
     * PUT /api/bookings/pay/{id} - Confirm payment (status 0 -> 1)
     */
    @PutMapping("/pay/{id}")
    public ResponseEntity<Map<String, Object>> payBooking(@PathVariable String id) {
        try {
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
     * PUT /api/bookings/cancel/{id} - Cancel booking (status 0 -> 2)
     */
    @PutMapping("/cancel/{id}")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable String id) {
        try {
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
     */
    @PutMapping("/refund/{id}")
    public ResponseEntity<Map<String, Object>> refundBooking(@PathVariable String id) {
        try {
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
            response.put("properties", propertyService.getAllActiveProperties());
            
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
            List<RoomType> roomTypes = roomTypeRepository.findByProperty_PropertyIdOrderByFloorAsc(propertyId);
            
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
     */
    @GetMapping("/rooms/{propertyId}/{roomTypeId}")
    public ResponseEntity<Map<String, Object>> getAvailableRooms(
            @PathVariable String propertyId,
            @PathVariable String roomTypeId) {
        try {
            List<Room> rooms = roomRepository.findByRoomType_RoomTypeId(roomTypeId)
                .stream()
                .filter(room -> room.getAvailabilityStatus() == 1) // Available only (not booked/maintenance)
                .collect(Collectors.toList());
            
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
     */
    @PostMapping("/create")
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
     */
    @GetMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> getUpdateBooking(@PathVariable String id) {
        try {
            BookingResponseDTO booking = bookingService.getBookingDetail(id);
            
            // Check if can be updated
            if (booking.getExtraPay() > 0 || booking.getRefund() > 0) {
                throw new RuntimeException("Cannot update booking with extra payment or refund");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("booking", booking);
            response.put("properties", propertyService.getAllActiveProperties());
            
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
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateBooking(
            @PathVariable String id,
            @RequestBody BookingRequestDTO request) {
        try {
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
     */
    @PostMapping("/status/pay")
    public ResponseEntity<Map<String, Object>> payBookingStatus(@RequestBody Map<String, String> request) {
        try {
            String bookingId = request.get("bookingId");
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
     */
    @PostMapping("/status/cancel")
    public ResponseEntity<Map<String, Object>> cancelBookingStatus(@RequestBody Map<String, String> request) {
        try {
            String bookingId = request.get("bookingId");
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
     */
    @PostMapping("/status/refund")
    public ResponseEntity<Map<String, Object>> refundBookingStatus(@RequestBody Map<String, String> request) {
        try {
            String bookingId = request.get("bookingId");
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
     */
    @GetMapping("/chart")
    public ResponseEntity<Map<String, Object>> getBookingStatistics(
            @RequestParam int month,
            @RequestParam int year) {
        try {
            List<PropertyStatisticsDTO> statistics = propertyService.getMonthlyStatistics(month, year);
            
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
}
