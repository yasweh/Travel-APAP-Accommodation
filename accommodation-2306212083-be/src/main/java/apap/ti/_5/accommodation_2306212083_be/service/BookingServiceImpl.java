package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.BookingRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.BookingResponseDTO;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306212083_be.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final AccommodationBookingRepository bookingRepository;
    private final RoomRepository roomRepository;
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
        return bookingRepository.findAll();
    }

    @Override
    public List<AccommodationBooking> getBookingsByCustomer(UUID customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    @Override
    public List<AccommodationBooking> getBookingsByStatus(Integer status) {
        return bookingRepository.findByStatus(status);
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
    
    @Override
    public BookingResponseDTO createBookingWithRoom(String roomId, BookingRequestDTO request) {
        // Validate room exists
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        // Validate dates
        validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());
        
        // Validate maintenance conflict
        if (maintenanceService.hasMaintenanceConflict(roomId, request.getCheckInDate(), request.getCheckOutDate())) {
            throw new RuntimeException("Room has maintenance scheduled during selected dates");
        }
        
        // Validate capacity
        RoomType roomType = room.getRoomType();
        if (request.getCapacity() > roomType.getCapacity()) {
            throw new RuntimeException("Capacity exceeds room type capacity");
        }
        
        // Calculate price
        long days = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        int basePrice = roomType.getPrice() * (int) days;
        int breakfastCost = request.getAddOnBreakfast() ? 50000 * (int) days : 0;
        int totalPrice = basePrice + breakfastCost;
        
        // Create booking
        AccommodationBooking booking = AccommodationBooking.builder()
            .bookingId(idGenerator.generateBookingId(roomId))
            .room(room)
            .checkInDate(request.getCheckInDate())
            .checkOutDate(request.getCheckOutDate())
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
            .build();
        
        AccommodationBooking saved = bookingRepository.save(booking);
        
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
        
        // Check if has extra_pay or refund
        if (existing.getExtraPay() > 0 || existing.getRefund() > 0) {
            throw new RuntimeException("Cannot update booking with extra payment or refund");
        }
        
        // Validate dates
        validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());
        
        // Validate maintenance conflict
        if (maintenanceService.hasMaintenanceConflict(existing.getRoom().getRoomId(), 
                request.getCheckInDate(), request.getCheckOutDate())) {
            throw new RuntimeException("Room has maintenance scheduled during selected dates");
        }
        
        // Calculate new price
        Room room = existing.getRoom();
        RoomType roomType = room.getRoomType();
        long days = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        int basePrice = roomType.getPrice() * (int) days;
        int breakfastCost = request.getAddOnBreakfast() ? 50000 * (int) days : 0;
        int newTotalPrice = basePrice + breakfastCost;
        
        int oldTotalPrice = existing.getTotalPrice();
        
        // Update fields
        existing.setCheckInDate(request.getCheckInDate());
        existing.setCheckOutDate(request.getCheckOutDate());
        existing.setTotalDays((int) days);
        existing.setTotalPrice(newTotalPrice);
        existing.setIsBreakfast(request.getAddOnBreakfast());
        existing.setCapacity(request.getCapacity());
        
        // Calculate refund if price decreased
        if (newTotalPrice < oldTotalPrice) {
            existing.setRefund(oldTotalPrice - newTotalPrice);
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
    public void payBookingById(String id) {
        AccommodationBooking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        String propertyId = booking.getRoom().getRoomType().getProperty().getPropertyId();
        
        if (booking.getExtraPay() > 0) {
            // Pay extra payment
            propertyService.updatePropertyIncome(propertyId, booking.getExtraPay());
            booking.setExtraPay(0);
        } else {
            // Pay full booking
            booking.setStatus(1);
            propertyService.updatePropertyIncome(propertyId, booking.getTotalPrice());
        }
        
        bookingRepository.save(booking);
    }

    @Override
    public void cancelBookingById(String id) {
        AccommodationBooking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        String propertyId = booking.getRoom().getRoomType().getProperty().getPropertyId();
        
        if (booking.getStatus() == 0) {
            if (booking.getExtraPay() > 0) {
                propertyService.updatePropertyIncome(propertyId, -booking.getTotalPrice());
                propertyService.updatePropertyIncome(propertyId, booking.getExtraPay());
                booking.setExtraPay(0);
            }
        } else if (booking.getStatus() == 1) {
            propertyService.updatePropertyIncome(propertyId, -booking.getTotalPrice());
        } else if (booking.getStatus() == 3) {
            propertyService.updatePropertyIncome(propertyId, -booking.getTotalPrice());
            propertyService.updatePropertyIncome(propertyId, -booking.getRefund());
        }
        
        booking.setStatus(2);
        bookingRepository.save(booking);
    }

    @Override
    public void refundBookingById(String id) {
        AccommodationBooking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (booking.getRefund() > 0) {
            String propertyId = booking.getRoom().getRoomType().getProperty().getPropertyId();
            propertyService.updatePropertyIncome(propertyId, -booking.getRefund());
            booking.setStatus(3);
            bookingRepository.save(booking);
        }
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
}
