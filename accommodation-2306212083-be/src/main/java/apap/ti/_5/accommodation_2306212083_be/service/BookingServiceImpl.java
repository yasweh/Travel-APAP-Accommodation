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
        Integer oldPrice = existingBooking.getTotalPrice();
        Integer newPrice = updatedBooking.getTotalPrice();
        
        // Status 0 (Waiting for Payment) logic
        if (oldStatus == 0) {
            if (existingBooking.getExtraPay() > 0) {
                throw new RuntimeException("Cannot update booking with extra_pay > 0");
            }
            existingBooking.setTotalPrice(newPrice);
            // No income change for status 0
        }
        
        // Status 1 (Payment Confirmed) logic
        else if (oldStatus == 1) {
            if (newPrice > oldPrice) {
                // Price increased → extra_pay
                existingBooking.setExtraPay(existingBooking.getExtraPay() + (newPrice - oldPrice));
            } else if (newPrice < oldPrice) {
                // Price decreased → refund
                existingBooking.setRefund(existingBooking.getRefund() + (oldPrice - newPrice));
                existingBooking.setStatus(3); // Change to Request Refund
            }
            existingBooking.setTotalPrice(newPrice);
        }
        
        // Update other fields
        existingBooking.setCheckInDate(updatedBooking.getCheckInDate());
        existingBooking.setCheckOutDate(updatedBooking.getCheckOutDate());
        existingBooking.setCapacity(updatedBooking.getCapacity());
        
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
        } else if (oldStatus == 1 && booking.getExtraPay() > 0) {
            // Payment Confirmed with extra_pay
            int reduction = booking.getTotalPrice() - booking.getExtraPay();
            propertyService.updatePropertyIncome(propertyId, -reduction);
        } else if (oldStatus == 1) {
            // Payment Confirmed without extra_pay
            propertyService.updatePropertyIncome(propertyId, -booking.getTotalPrice());
        } else if (oldStatus == 3) {
            // Request Refund
            int reduction = booking.getTotalPrice() + booking.getRefund();
            propertyService.updatePropertyIncome(propertyId, -reduction);
        }
        
        booking.setStatus(2); // Cancelled
        
        // Release room
        Room room = booking.getRoom();
        room.setAvailabilityStatus(1);
        roomRepository.save(room);
        
        return bookingRepository.save(booking);
    }

    @Override
    public AccommodationBooking refundBooking(String id) {
        AccommodationBooking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (booking.getStatus() != 1) {
            throw new RuntimeException("Can only request refund for Payment Confirmed bookings");
        }
        
        booking.setStatus(3); // Request Refund
        return bookingRepository.save(booking);
    }

    @Override
    public void autoCheckInBookings() {
        LocalDateTime today = LocalDateTime.now();
        List<AccommodationBooking> bookingsToCheckIn = bookingRepository.findBookingsToCheckIn(today);
        
        for (AccommodationBooking booking : bookingsToCheckIn) {
            if (booking.getExtraPay() > 0) {
                // If extra_pay > 0 → Cancel
                cancelBooking(booking.getBookingId());
            } else {
                // Change to Done
                booking.setStatus(4);
                
                // Update property income
                String propertyId = booking.getRoom().getRoomType().getProperty().getPropertyId();
                int incomeAmount = booking.getTotalPrice() - booking.getRefund();
                propertyService.updatePropertyIncome(propertyId, incomeAmount);
                
                // Release room
                Room room = booking.getRoom();
                room.setAvailabilityStatus(1);
                roomRepository.save(room);
                
                bookingRepository.save(booking);
            }
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
            .refund(0)
            .extraPay(0)
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
        
        // Status validation based on business rules:
        // Status 0 (Waiting): can update, diupdate, cancel
        // Status 1 (Payment Confirmed): can diupdate and cancel (with rules)
        // Status 2 (Cancelled): cannot update
        // Status 3 (Request Refund): cannot update
        // Status 4 (Done): cannot update
        
        if (existing.getStatus() == 2) {
            throw new RuntimeException("Cannot update cancelled booking");
        }
        if (existing.getStatus() == 3) {
            throw new RuntimeException("Cannot update booking with pending refund request");
        }
        if (existing.getStatus() == 4) {
            throw new RuntimeException("Cannot update completed booking");
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
        
        // Handle price changes based on status
        if (existing.getStatus() == 0) {
            // Status 0 (Waiting): Update total price, extra_pay becomes 0 if price increased
            if (newTotalPrice > oldTotalPrice) {
                existing.setExtraPay(0); // Will be recalculated on payment
                existing.setStatus(0); // Keep status as Waiting for Payment
            }
            existing.setRefund(0); // No refund for status 0
        } else if (existing.getStatus() == 1) {
            // Status 1 (Payment Confirmed): Handle extra_pay or refund
            if (newTotalPrice > oldTotalPrice) {
                // Price increased: add to extra_pay, set refund to 0, change to waiting
                int extraPayment = newTotalPrice - oldTotalPrice;
                existing.setExtraPay(extraPayment);
                existing.setRefund(0);
                existing.setStatus(0); // Change to Waiting for Payment
            } else if (newTotalPrice < oldTotalPrice) {
                // Price decreased: add to refund
                int refundAmount = oldTotalPrice - newTotalPrice;
                existing.setRefund(refundAmount);
                existing.setStatus(3); // Change to Request Refund status
            }
            // If newTotalPrice == oldTotalPrice, no changes to extra_pay or refund
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
            
            // If there's extra pay, add it to total price and income
            if (booking.getExtraPay() > 0) {
                booking.setTotalPrice(booking.getTotalPrice() + booking.getExtraPay());
                booking.setExtraPay(0);
            }
            
            // Update property income/revenue with total price
            propertyService.updatePropertyIncome(propertyId, booking.getTotalPrice());
            
        } else if (booking.getExtraPay() > 0) {
            // Pay extra payment only (booking already confirmed)
            // Add extra pay to total price
            booking.setTotalPrice(booking.getTotalPrice() + booking.getExtraPay());
            // Add extra pay to income
            propertyService.updatePropertyIncome(propertyId, booking.getExtraPay());
            booking.setExtraPay(0);
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
    public void refundBookingById(String id) {
        AccommodationBooking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Validate status: only process refund if status is 3 (Request Refund)
        if (booking.getStatus() != 3) {
            throw new RuntimeException("Booking is not in Request Refund status");
        }
        
        // Process refund: reduce property income by refund amount
        if (booking.getRefund() > 0) {
            String propertyId = booking.getRoom().getRoomType().getProperty().getPropertyId();
            
            // Reduce property income
            propertyService.updatePropertyIncome(propertyId, -booking.getRefund());
            
            // Change status to 4 (Done/Completed) after refund is processed
            booking.setStatus(4);
            booking.setUpdatedDate(LocalDateTime.now());
            bookingRepository.save(booking);
        } else {
            throw new RuntimeException("No refund amount to process");
        }
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
            .refund(booking.getRefund())
            .extraPay(booking.getExtraPay())
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
            // Skip cancelled, refund, done bookings, and inactive bookings
            if (booking.getActiveStatus() == 0 || booking.getStatus() == 2 || booking.getStatus() == 3 || booking.getStatus() == 4) {
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
