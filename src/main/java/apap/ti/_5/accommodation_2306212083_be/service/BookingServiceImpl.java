package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.BookingRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.BookingResponseDTO;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.model.Customer;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306212083_be.repository.CustomerRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306212083_be.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final AccommodationBookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final PropertyService propertyService;
    private final MaintenanceService maintenanceService;
    private final IdGenerator idGenerator;

    @Override
    public AccommodationBooking createBooking(AccommodationBooking booking) {
        // Generate Booking ID - use room ID from booking
        String roomId = booking.getRoom() != null ? booking.getRoom().getRoomId() : "DEFAULT";
        booking.setBookingId(idGenerator.generateBookingId(roomId));
        
        // Calculate total days
        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        booking.setTotalDays((int) days);
        
        // Set initial status: Waiting for Payment
        booking.setStatus(0);
        
        // Update room availability
        Room room = booking.getRoom();
        room.setAvailabilityStatus(0);
        roomRepository.save(room);
        
        return bookingRepository.save(booking);
    }

    @Override
    public Optional<AccommodationBooking> getBookingById(String id) {
        return bookingRepository.findById(id);
    }

    @Override
    public List<AccommodationBooking> getAllBookings() {
        return bookingRepository.findByActiveStatus(1); // Only active bookings
    }

    @Override
    public List<AccommodationBooking> getBookingsByCustomer(UUID customerId) {
        return bookingRepository.findByCustomerId(customerId).stream()
            .filter(b -> b.getActiveStatus() == 1)
            .collect(Collectors.toList());
    }

    @Override
    public List<AccommodationBooking> getBookingsByStatus(Integer status) {
        return bookingRepository.findByStatus(status).stream()
            .filter(b -> b.getActiveStatus() == 1)
            .collect(Collectors.toList());
    }

    @Override
    public AccommodationBooking updateBooking(String id, AccommodationBooking updatedBooking) {
        AccommodationBooking existingBooking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Integer oldStatus = existingBooking.getStatus();
        
        // Only allow updates for status 0 (Waiting for Payment)
        if (oldStatus != 0) {
            throw new RuntimeException("Can only update bookings with Waiting for Payment status");
        }
        
        // Update booking details
        existingBooking.setCheckInDate(updatedBooking.getCheckInDate());
        existingBooking.setCheckOutDate(updatedBooking.getCheckOutDate());
        existingBooking.setCapacity(updatedBooking.getCapacity());
        existingBooking.setTotalPrice(updatedBooking.getTotalPrice());
        
        return bookingRepository.save(existingBooking);
    }

    @Override
    public AccommodationBooking payBooking(String id) {
        AccommodationBooking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (booking.getStatus() != 0) {
            throw new RuntimeException("Can only pay for bookings with Waiting for Payment status");
        }
        
        booking.setStatus(1); // Payment Confirmed
        return bookingRepository.save(booking);
    }

    @Override
    public AccommodationBooking cancelBooking(String id) {
        AccommodationBooking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Integer oldStatus = booking.getStatus();
        String propertyId = booking.getRoom().getRoomType().getProperty().getPropertyId();
        
        // Cancel logic based on previous status
        if (oldStatus == 0) {
            // Waiting for Payment → no income impact
            // Do nothing
        } else if (oldStatus == 1) {
            // Payment Confirmed → reduce property income
            propertyService.updatePropertyIncome(propertyId, -booking.getTotalPrice());
        }
        
        booking.setStatus(2); // Cancelled
        
        // Release room
        Room room = booking.getRoom();
        room.setAvailabilityStatus(1);
        roomRepository.save(room);
        
        return bookingRepository.save(booking);
    }



    @Override
    public void autoCheckInBookings() {
        LocalDateTime today = LocalDateTime.now();
        List<AccommodationBooking> bookingsToCheckIn = bookingRepository.findBookingsToCheckIn(today);
        
        for (AccommodationBooking booking : bookingsToCheckIn) {
            // Update property income when booking is completed
            String propertyId = booking.getRoom().getRoomType().getProperty().getPropertyId();
            propertyService.updatePropertyIncome(propertyId, booking.getTotalPrice());
            
            // Release room
            Room room = booking.getRoom();
            room.setAvailabilityStatus(1);
            roomRepository.save(room);
            
            // Keep status as Payment Confirmed (1), don't change to Done
            bookingRepository.save(booking);
        }
    }

    // ============ NEW DTO-BASED METHODS ============
    
    /**
     * Convert date string (yyyy-MM-dd) to LocalDateTime at start of day (00:00:00)
     */
    private LocalDateTime parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            throw new RuntimeException("Date cannot be empty");
        }
        try {
            LocalDate date = LocalDate.parse(dateString);
            return date.atStartOfDay(); // 00:00:00
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Expected: yyyy-MM-dd");
        }
    }
    
    /**
     * Create or update customer before creating booking
     */
    private Customer ensureCustomerExists(BookingRequestDTO request) {
        UUID customerId = request.getCustomerId();
        
        // Check if customer exists
        Optional<Customer> existingCustomer = customerRepository.findById(customerId);
        
        if (existingCustomer.isPresent()) {
            // Update existing customer info
            Customer customer = existingCustomer.get();
            customer.setName(request.getCustomerName());
            customer.setEmail(request.getCustomerEmail());
            customer.setPhone(request.getCustomerPhone());
            customer.setUpdatedDate(LocalDateTime.now());
            return customerRepository.save(customer);
        } else {
            // Create new customer
            Customer newCustomer = Customer.builder()
                .customerId(customerId)
                .name(request.getCustomerName())
                .email(request.getCustomerEmail())
                .phone(request.getCustomerPhone())
                .createdDate(LocalDateTime.now())
                .build();
            return customerRepository.save(newCustomer);
        }
    }
    
    @Override
    public BookingResponseDTO createBookingWithRoom(String roomId, BookingRequestDTO request) {
        // Auto-set customerId from authenticated user if not provided
        if (request.getCustomerId() == null) {
            var currentUser = apap.ti._5.accommodation_2306212083_be.util.SecurityUtil.getCurrentUser();
            if (currentUser != null) {
                request.setCustomerId(UUID.fromString(currentUser.getUserId()));
                // Also auto-fill customer name and email if not provided
                if (request.getCustomerName() == null || request.getCustomerName().isEmpty()) {
                    request.setCustomerName(currentUser.getName());
                }
                if (request.getCustomerEmail() == null || request.getCustomerEmail().isEmpty()) {
                    request.setCustomerEmail(currentUser.getEmail());
                }
            }
        }
        
        // Ensure customer exists in database
        ensureCustomerExists(request);
        
        // Parse dates from String to LocalDateTime
        LocalDateTime checkInDate = parseDate(request.getCheckInDate());
        LocalDateTime checkOutDate = parseDate(request.getCheckOutDate());
        
        // Validate room exists
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        // Validate dates
        validateBookingDates(checkInDate, checkOutDate);
        
        // Validate maintenance conflict
        if (maintenanceService.hasMaintenanceConflict(roomId, checkInDate, checkOutDate)) {
            throw new RuntimeException("Room has maintenance scheduled during selected dates");
        }
        
        // Validate capacity
        RoomType roomType = room.getRoomType();
        if (request.getCapacity() > roomType.getCapacity()) {
            throw new RuntimeException("Capacity exceeds room type capacity");
        }
        
        // Calculate price
        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        int basePrice = roomType.getPrice() * (int) days;
        int breakfastCost = request.getAddOnBreakfast() ? 50000 * (int) days : 0;
        int totalPrice = basePrice + breakfastCost;
        
        // Create booking
        AccommodationBooking booking = AccommodationBooking.builder()
            .bookingId(idGenerator.generateBookingId(roomId))
            .room(room)
            .checkInDate(checkInDate)
            .checkOutDate(checkOutDate)
            .totalDays((int) days)
            .totalPrice(totalPrice)
            .status(0)
            .customerId(request.getCustomerId())
            .customerName(request.getCustomerName())
            .customerEmail(request.getCustomerEmail())
            .customerPhone(request.getCustomerPhone())
            .isBreakfast(request.getAddOnBreakfast())
            .capacity(request.getCapacity())
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .build();
        
        AccommodationBooking saved = bookingRepository.save(booking);
        
        // Update room availability status to 0 (Not Available/Booked)
        room.setAvailabilityStatus(0);
        room.setUpdatedDate(LocalDateTime.now());
        roomRepository.save(room);
        
        return mapToResponseDTO(saved);
    }

    @Override
    public BookingResponseDTO createBookingWithSelection(BookingRequestDTO request) {
        // Find room by roomId from request - just delegate to createBookingWithRoom
        return createBookingWithRoom(request.getRoomId(), request);
    }

    @Override
    public BookingResponseDTO updateBookingFromDTO(String id, BookingRequestDTO request) {
        AccommodationBooking existing = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Update customer data in Customer table
        ensureCustomerExists(request);
        
        // Status validation: Only allow updates for status 0 and 1
        if (existing.getStatus() == 2) {
            throw new RuntimeException("Cannot update cancelled booking");
        }
        
        // Parse dates from String to LocalDateTime
        LocalDateTime checkInDate = parseDate(request.getCheckInDate());
        LocalDateTime checkOutDate = parseDate(request.getCheckOutDate());
        
        // Validate dates
        validateBookingDates(checkInDate, checkOutDate);
        
        // Validate maintenance conflict
        if (maintenanceService.hasMaintenanceConflict(existing.getRoom().getRoomId(), 
                checkInDate, checkOutDate)) {
            throw new RuntimeException("Room has maintenance scheduled during selected dates");
        }
        
        // Calculate new price
        Room room = existing.getRoom();
        RoomType roomType = room.getRoomType();
        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        int basePrice = roomType.getPrice() * (int) days;
        int breakfastCost = request.getAddOnBreakfast() ? 50000 * (int) days : 0;
        int newTotalPrice = basePrice + breakfastCost;
        
        int oldTotalPrice = existing.getTotalPrice();
        
        // Update fields
        existing.setCheckInDate(checkInDate);
        existing.setCheckOutDate(checkOutDate);
        existing.setTotalDays((int) days);
        existing.setTotalPrice(newTotalPrice);
        existing.setIsBreakfast(request.getAddOnBreakfast());
        existing.setCapacity(request.getCapacity());
        
        // Update customer information in booking
        existing.setCustomerName(request.getCustomerName());
        existing.setCustomerEmail(request.getCustomerEmail());
        existing.setCustomerPhone(request.getCustomerPhone());
        existing.setUpdatedDate(LocalDateTime.now());
        
        // Price changes only allowed for status 0 (Waiting for Payment)
        // For status 1 (Payment Confirmed), price is locked
        if (existing.getStatus() == 1 && newTotalPrice != oldTotalPrice) {
            throw new RuntimeException("Cannot change price for confirmed bookings");
        }
        
        AccommodationBooking updated = bookingRepository.save(existing);
        
        return mapToResponseDTO(updated);
    }

    @Override
    public BookingResponseDTO getBookingDetail(String id) {
        AccommodationBooking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        return mapToResponseDTO(booking);
    }

    @Override
    public List<BookingResponseDTO> getAllBookingsAsDTO() {
        List<AccommodationBooking> bookings = getAllBookings();
        return bookings.stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void payBookingById(String id) {
        AccommodationBooking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Room room = booking.getRoom();
        String propertyId = room.getRoomType().getProperty().getPropertyId();
        
        if (booking.getStatus() == 0) {
            // Pay full booking - change status from Waiting (0) to Confirmed (1)
            booking.setStatus(1);
            
            // Update property income/revenue with total price
            propertyService.updatePropertyIncome(propertyId, booking.getTotalPrice());
        }
        
        booking.setUpdatedDate(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Override
    public void cancelBookingById(String id) {
        AccommodationBooking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Cancel: status becomes 2 (Cancelled) - sesuai class diagram
        booking.setStatus(2);
        booking.setUpdatedDate(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Override
    public boolean isRoomAvailableForDates(String roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        return isRoomAvailableForDateRange(room, checkIn, checkOut);
    }

    // ============ HELPER METHODS ============
    
    private void validateBookingDates(LocalDateTime checkIn, LocalDateTime checkOut) {
        LocalDateTime now = LocalDateTime.now();
        
        if (checkIn.isBefore(now)) {
            throw new RuntimeException("Check-in date must be today or later");
        }
        
        if (checkOut.isBefore(checkIn) || checkOut.equals(checkIn)) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }
        
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days < 1) {
            throw new RuntimeException("Minimum booking is 1 day");
        }
    }

    private BookingResponseDTO mapToResponseDTO(AccommodationBooking booking) {
        Room room = booking.getRoom();
        RoomType roomType = room.getRoomType();
        
        return BookingResponseDTO.builder()
            .bookingId(booking.getBookingId())
            .roomId(room.getRoomId())
            .roomName(room.getName())
            .roomTypeId(roomType.getRoomTypeId())
            .roomTypeName(roomType.getName())
            .propertyId(roomType.getProperty().getPropertyId())
            .propertyName(roomType.getProperty().getPropertyName())
            .checkInDate(booking.getCheckInDate())
            .checkOutDate(booking.getCheckOutDate())
            .totalDays(booking.getTotalDays())
            .totalPrice(booking.getTotalPrice())
            .status(booking.getStatus())
            .statusString(booking.getStatusString())
            .customerId(booking.getCustomerId())
            .customerName(booking.getCustomerName())
            .customerEmail(booking.getCustomerEmail())
            .customerPhone(booking.getCustomerPhone())
            .addOnBreakfast(booking.getIsBreakfast())
            .capacity(booking.getCapacity())
            .createdDate(booking.getCreatedDate())
            .updatedDate(booking.getUpdatedDate())
            .build();
    }
    
    /**
     * Check if room is available for the given date range
     * Returns true if no booking/maintenance overlaps with the requested dates
     */
    private boolean isRoomAvailableForDateRange(Room room, LocalDateTime checkIn, LocalDateTime checkOut) {
        // Check if room is soft-deleted
        if (room.getActiveRoom() == 0) {
            return false;
        }
        
        // Check booking overlaps - only check active bookings (activeStatus = 1) with status 0 or 1
        // Skip cancelled (2), refund (3), and done (4)
        List<AccommodationBooking> roomBookings = bookingRepository.findByRoom_RoomId(room.getRoomId());
        for (AccommodationBooking booking : roomBookings) {
            // Skip cancelled bookings and inactive bookings
            if (booking.getActiveStatus() == 0 || booking.getStatus() == 2) {
                continue;
            }
            
            // Check date overlap
            if (datesOverlap(checkIn, checkOut, booking.getCheckInDate(), booking.getCheckOutDate())) {
                return false;
            }
        }
        
        // Check maintenance overlaps
        if (room.getMaintenanceStart() != null && room.getMaintenanceEnd() != null) {
            if (datesOverlap(checkIn, checkOut, room.getMaintenanceStart(), room.getMaintenanceEnd())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if two date ranges overlap
     */
    private boolean datesOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}
