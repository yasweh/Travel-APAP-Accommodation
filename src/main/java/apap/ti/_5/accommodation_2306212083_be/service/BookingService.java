package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.BookingRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.BookingResponseDTO;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingService {
    // Old methods (keeping for backward compatibility)
    AccommodationBooking createBooking(AccommodationBooking booking);
    Optional<AccommodationBooking> getBookingById(String id);
    List<AccommodationBooking> getAllBookings();
    List<AccommodationBooking> getBookingsByCustomer(UUID customerId);
    List<AccommodationBooking> getBookingsByStatus(Integer status);
    AccommodationBooking updateBooking(String id, AccommodationBooking updatedBooking);
    AccommodationBooking payBooking(String id);
    AccommodationBooking cancelBooking(String id);
    AccommodationBooking refundBooking(String id);
    void autoCheckInBookings();
    
    // New methods for DTO-based operations
    BookingResponseDTO createBookingWithRoom(String roomId, BookingRequestDTO request);
    BookingResponseDTO createBookingWithSelection(BookingRequestDTO request);
    BookingResponseDTO updateBookingFromDTO(String id, BookingRequestDTO request);
    BookingResponseDTO getBookingDetail(String id);
    List<BookingResponseDTO> getAllBookingsAsDTO();
    void payBookingById(String id);
    void cancelBookingById(String id);
    void refundBookingById(String id);
    
    // Availability check
    boolean isRoomAvailableForDates(String roomId, LocalDateTime checkIn, LocalDateTime checkOut);
}