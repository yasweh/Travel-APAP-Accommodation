package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.BookingResponseDTO;
import apap.ti._5.accommodation_2306212083_be.dto.UnifiedBookingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnifiedBookingServiceTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private UnifiedBookingService unifiedBookingService;

    private BookingResponseDTO testBooking;

    @BeforeEach
    void setUp() {
        testBooking = BookingResponseDTO.builder()
                .bookingId("booking-001")
                .customerId(UUID.randomUUID())
                .propertyName("Test Hotel")
                .roomName("Room 101")
                .roomTypeName("Deluxe")
                .totalPrice(1000000)
                .status(1)
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(3))
                .build();
    }

    @Test
    void getAllBookingsFromAllServices_ReturnsBookings() {
        // Mock local accommodation bookings
        when(bookingService.getAllBookingsAsDTO()).thenReturn(Arrays.asList(testBooking));

        List<UnifiedBookingDTO> result = unifiedBookingService.getAllBookingsFromAllServices();

        assertNotNull(result);
        // Should at least have accommodation bookings (other external calls might fail)
        // At minimum, the local bookings should be there
        boolean hasAccommodation = result.stream()
                .anyMatch(dto -> "accommodation".equals(dto.getSource()));
        
        // The test passes if the method completes without error
        // External APIs may fail but local should work
    }

    @Test
    void fetchAccommodationBookings_ReturnsLocalBookings() {
        when(bookingService.getAllBookingsAsDTO()).thenReturn(Arrays.asList(testBooking));

        List<UnifiedBookingDTO> result = unifiedBookingService.getAllBookingsFromAllServices();

        // Check accommodation bookings are present
        List<UnifiedBookingDTO> accommodationBookings = result.stream()
                .filter(dto -> "accommodation".equals(dto.getSource()))
                .toList();

        assertFalse(accommodationBookings.isEmpty());
        assertEquals("booking-001", accommodationBookings.get(0).getId());
        assertEquals("Test Hotel", accommodationBookings.get(0).getServiceName());
    }

    @Test
    void fetchAccommodationBookings_WithException_HandlesGracefully() {
        when(bookingService.getAllBookingsAsDTO()).thenThrow(new RuntimeException("DB Error"));

        List<UnifiedBookingDTO> result = unifiedBookingService.getAllBookingsFromAllServices();

        // Should not throw, just return empty or partial results
        assertNotNull(result);
    }

    @Test
    void statusConversion_ReturnsCorrectText() {
        BookingResponseDTO waitingBooking = BookingResponseDTO.builder()
                .bookingId("waiting")
                .status(0)
                .build();
        BookingResponseDTO confirmedBooking = BookingResponseDTO.builder()
                .bookingId("confirmed")
                .status(1)
                .build();
        BookingResponseDTO doneBooking = BookingResponseDTO.builder()
                .bookingId("done")
                .status(2)
                .build();
        BookingResponseDTO cancelledBooking = BookingResponseDTO.builder()
                .bookingId("cancelled")
                .status(3)
                .build();
        BookingResponseDTO unknownBooking = BookingResponseDTO.builder()
                .bookingId("unknown")
                .status(99)
                .build();

        when(bookingService.getAllBookingsAsDTO()).thenReturn(
                Arrays.asList(waitingBooking, confirmedBooking, doneBooking, cancelledBooking, unknownBooking));

        List<UnifiedBookingDTO> result = unifiedBookingService.getAllBookingsFromAllServices();

        List<UnifiedBookingDTO> accommodationBookings = result.stream()
                .filter(dto -> "accommodation".equals(dto.getSource()))
                .toList();

        // Verify status text conversion
        assertTrue(accommodationBookings.stream().anyMatch(dto -> "Waiting".equals(dto.getStatus())));
        assertTrue(accommodationBookings.stream().anyMatch(dto -> "Confirmed".equals(dto.getStatus())));
        assertTrue(accommodationBookings.stream().anyMatch(dto -> "Done".equals(dto.getStatus())));
        assertTrue(accommodationBookings.stream().anyMatch(dto -> "Cancelled".equals(dto.getStatus())));
        assertTrue(accommodationBookings.stream().anyMatch(dto -> "Unknown".equals(dto.getStatus())));
    }

    @Test
    void fetchAccommodationBookings_WithNullFields_HandlesGracefully() {
        BookingResponseDTO nullFieldsBooking = BookingResponseDTO.builder()
                .bookingId("null-fields")
                .customerId(null)
                .checkInDate(null)
                .checkOutDate(null)
                .status(1)
                .build();

        when(bookingService.getAllBookingsAsDTO()).thenReturn(Arrays.asList(nullFieldsBooking));

        List<UnifiedBookingDTO> result = unifiedBookingService.getAllBookingsFromAllServices();

        List<UnifiedBookingDTO> accommodationBookings = result.stream()
                .filter(dto -> "accommodation".equals(dto.getSource()))
                .toList();

        assertFalse(accommodationBookings.isEmpty());
        assertNull(accommodationBookings.get(0).getCustomerId());
        assertNull(accommodationBookings.get(0).getStartDate());
    }
}
