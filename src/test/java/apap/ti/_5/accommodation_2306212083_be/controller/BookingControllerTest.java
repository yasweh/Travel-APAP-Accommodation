package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.*;
import apap.ti._5.accommodation_2306212083_be.model.*;
import apap.ti._5.accommodation_2306212083_be.service.BookingService;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import apap.ti._5.accommodation_2306212083_be.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private PropertyService propertyService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private BookingController bookingController;

    private UserPrincipal customerUser;
    private UserPrincipal ownerUser;
    private UserPrincipal superadminUser;
    private AccommodationBooking testBooking;
    private BookingResponseDTO testBookingDTO;
    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        // Create test users
        customerUser = UserPrincipal.builder()
                .userId("11111111-1111-1111-1111-111111111111")
                .username("customer")
                .email("customer@test.com")
                .name("Test Customer")
                .role("Customer")
                .build();

        ownerUser = UserPrincipal.builder()
                .userId("22222222-2222-2222-2222-222222222222")
                .username("owner")
                .email("owner@test.com")
                .name("Test Owner")
                .role("Accommodation Owner")
                .build();

        superadminUser = UserPrincipal.builder()
                .userId("33333333-3333-3333-3333-333333333333")
                .username("admin")
                .email("admin@test.com")
                .name("Test Admin")
                .role("Superadmin")
                .build();

        // Create test entities
        testProperty = new Property();
        testProperty.setPropertyId("prop-001");
        testProperty.setPropertyName("Test Hotel");
        testProperty.setOwnerId(UUID.fromString("22222222-2222-2222-2222-222222222222"));

        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("rt-001");
        testRoomType.setName("Deluxe Room");
        testRoomType.setPrice(500000);
        testRoomType.setCapacity(2);
        testRoomType.setFacility("AC, TV, WiFi");
        testRoomType.setFloor(1);
        testRoomType.setProperty(testProperty);

        testRoom = new Room();
        testRoom.setRoomId("room-001");
        testRoom.setName("Room 101");
        testRoom.setRoomType(testRoomType);
        testRoom.setAvailabilityStatus(1);
        testRoom.setActiveRoom(1);

        testBooking = new AccommodationBooking();
        testBooking.setBookingId("booking-001");
        testBooking.setCustomerId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        testBooking.setRoom(testRoom);
        testBooking.setStatus(0);

        testBookingDTO = BookingResponseDTO.builder()
                .bookingId("booking-001")
                .customerId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .status(0)
                .build();
    }

    @Test
    void listBookings_AsCustomer_ReturnsOwnBookings() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(true);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<AccommodationBooking> bookings = Arrays.asList(testBooking);
            when(bookingService.getBookingsByCustomer(any(UUID.class))).thenReturn(bookings);
            when(bookingService.getBookingDetail(anyString())).thenReturn(testBookingDTO);

            ResponseEntity<Map<String, Object>> response = bookingController.listBookings(null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
            assertNotNull(response.getBody().get("data"));
        }
    }

    @Test
    void listBookings_AsSuperadmin_ReturnsAllBookings() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<BookingResponseDTO> bookings = Arrays.asList(testBookingDTO);
            when(bookingService.getAllBookingsAsDTO()).thenReturn(bookings);

            ResponseEntity<Map<String, Object>> response = bookingController.listBookings(null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void listBookings_AsSuperadminWithCustomerIdFilter_ReturnsFilteredBookings() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<AccommodationBooking> bookings = Arrays.asList(testBooking);
            when(bookingService.getBookingsByCustomer(any(UUID.class))).thenReturn(bookings);
            when(bookingService.getBookingDetail(anyString())).thenReturn(testBookingDTO);

            ResponseEntity<Map<String, Object>> response = bookingController.listBookings(
                    "11111111-1111-1111-1111-111111111111", null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void listBookings_AsSuperadminWithStatusFilter_ReturnsFilteredBookings() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<AccommodationBooking> bookings = Arrays.asList(testBooking);
            when(bookingService.getBookingsByStatus(anyInt())).thenReturn(bookings);
            when(bookingService.getBookingDetail(anyString())).thenReturn(testBookingDTO);

            ResponseEntity<Map<String, Object>> response = bookingController.listBookings(null, 0);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void listBookings_AsOwner_ReturnsOwnerPropertyBookings() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(true);

            List<BookingResponseDTO> allBookings = Arrays.asList(testBookingDTO);
            when(bookingService.getAllBookingsAsDTO()).thenReturn(allBookings);
            when(bookingService.getBookingById(anyString())).thenReturn(Optional.of(testBooking));

            ResponseEntity<Map<String, Object>> response = bookingController.listBookings(null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void detailBooking_AsCustomer_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(true);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            when(bookingService.getBookingDetail("booking-001")).thenReturn(testBookingDTO);
            when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));

            ResponseEntity<Map<String, Object>> response = bookingController.detailBooking("booking-001");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
            assertNotNull(response.getBody().get("availableActions"));
        }
    }

    @Test
    void detailBooking_AsCustomer_AccessDenied() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            UserPrincipal otherCustomer = UserPrincipal.builder()
                    .userId("44444444-4444-4444-4444-444444444444")
                    .role("Customer")
                    .build();
            
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(otherCustomer);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(true);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            when(bookingService.getBookingDetail("booking-001")).thenReturn(testBookingDTO);
            when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));

            ResponseEntity<Map<String, Object>> response = bookingController.detailBooking("booking-001");

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void detailBooking_AsOwner_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(true);

            when(bookingService.getBookingDetail("booking-001")).thenReturn(testBookingDTO);
            when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));

            ResponseEntity<Map<String, Object>> response = bookingController.detailBooking("booking-001");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void detailBooking_AsOwner_AccessDenied() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            UserPrincipal otherOwner = UserPrincipal.builder()
                    .userId("55555555-5555-5555-5555-555555555555")
                    .role("Accommodation Owner")
                    .build();
            
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(otherOwner);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(true);

            when(bookingService.getBookingDetail("booking-001")).thenReturn(testBookingDTO);
            when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));

            ResponseEntity<Map<String, Object>> response = bookingController.detailBooking("booking-001");

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void detailBooking_BookingNotFound_ReturnsNotFound() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            when(bookingService.getBookingDetail("invalid")).thenThrow(new RuntimeException("Booking not found"));

            ResponseEntity<Map<String, Object>> response = bookingController.detailBooking("invalid");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void payBooking_Success() {
        // payBooking returns AccommodationBooking, not void
        when(bookingService.payBooking("booking-001")).thenReturn(testBooking);

        ResponseEntity<Map<String, Object>> response = bookingController.payBooking("booking-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Payment confirmed successfully", response.getBody().get("message"));
    }

    @Test
    void payBooking_Error() {
        when(bookingService.payBooking("booking-001")).thenThrow(new RuntimeException("Cannot pay"));

        ResponseEntity<Map<String, Object>> response = bookingController.payBooking("booking-001");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void cancelBooking_Success() {
        // cancelBooking returns AccommodationBooking, not void
        when(bookingService.cancelBooking("booking-001")).thenReturn(testBooking);

        ResponseEntity<Map<String, Object>> response = bookingController.cancelBooking("booking-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Booking cancelled successfully", response.getBody().get("message"));
    }

    @Test
    void cancelBooking_Error() {
        when(bookingService.cancelBooking("booking-001")).thenThrow(new RuntimeException("Cannot cancel"));

        ResponseEntity<Map<String, Object>> response = bookingController.cancelBooking("booking-001");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void getCreateBookingWithRoom_Success() {
        when(roomRepository.findById("room-001")).thenReturn(Optional.of(testRoom));

        ResponseEntity<Map<String, Object>> response = bookingController.getCreateBookingWithRoom("room-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertNotNull(response.getBody().get("data"));
    }

    @Test
    void getCreateBookingWithRoom_RoomNotFound() {
        when(roomRepository.findById("invalid")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = bookingController.getCreateBookingWithRoom("invalid");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void getCreateBookingData_Success() {
        List<Property> properties = Arrays.asList(testProperty);
        when(propertyService.getAllActiveProperties()).thenReturn(properties);

        ResponseEntity<Map<String, Object>> response = bookingController.getCreateBookingData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void getCreateBookingData_Error() {
        when(propertyService.getAllActiveProperties()).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Map<String, Object>> response = bookingController.getCreateBookingData();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void getRoomTypesForProperty_Success() {
        List<RoomType> roomTypes = Arrays.asList(testRoomType);
        when(roomTypeRepository.findByProperty_PropertyIdAndActiveStatusOrderByFloorAsc("prop-001", 1))
                .thenReturn(roomTypes);

        ResponseEntity<Map<String, Object>> response = bookingController.getRoomTypesForProperty("prop-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void getRoomTypesForProperty_Error() {
        when(roomTypeRepository.findByProperty_PropertyIdAndActiveStatusOrderByFloorAsc(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Error"));

        ResponseEntity<Map<String, Object>> response = bookingController.getRoomTypesForProperty("invalid");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void getAvailableRooms_WithoutDates_Success() {
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomRepository.findByRoomType_RoomTypeId("rt-001")).thenReturn(rooms);

        ResponseEntity<Map<String, Object>> response = bookingController.getAvailableRooms(
                "prop-001", "rt-001", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void getAvailableRooms_WithDates_Success() {
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomRepository.findByRoomType_RoomTypeId("rt-001")).thenReturn(rooms);
        when(bookingService.isRoomAvailableForDates(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        ResponseEntity<Map<String, Object>> response = bookingController.getAvailableRooms(
                "prop-001", "rt-001", "2025-02-01", "2025-02-05");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void getAvailableRooms_Error() {
        when(roomRepository.findByRoomType_RoomTypeId(anyString())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Map<String, Object>> response = bookingController.getAvailableRooms(
                "prop-001", "invalid", null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void createBooking_WithRoomId_Success() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("room-001");
        request.setCheckInDate("2025-02-01");
        request.setCheckOutDate("2025-02-03");

        when(bookingService.createBookingWithRoom(anyString(), any(BookingRequestDTO.class)))
                .thenReturn(testBookingDTO);

        ResponseEntity<Map<String, Object>> response = bookingController.createBooking(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void createBooking_WithSelection_Success() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCheckInDate("2025-02-01");
        request.setCheckOutDate("2025-02-03");

        when(bookingService.createBookingWithSelection(any(BookingRequestDTO.class)))
                .thenReturn(testBookingDTO);

        ResponseEntity<Map<String, Object>> response = bookingController.createBooking(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void createBooking_Error() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("room-001");

        when(bookingService.createBookingWithRoom(anyString(), any(BookingRequestDTO.class)))
                .thenThrow(new RuntimeException("Error"));

        ResponseEntity<Map<String, Object>> response = bookingController.createBooking(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void getUpdateBooking_AsCustomer_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(true);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            testBookingDTO = BookingResponseDTO.builder()
                    .bookingId("booking-001")
                    .status(0)
                    .build();

            when(bookingService.getBookingDetail("booking-001")).thenReturn(testBookingDTO);
            when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));
            when(propertyService.getAllActiveProperties()).thenReturn(Arrays.asList(testProperty));

            ResponseEntity<Map<String, Object>> response = bookingController.getUpdateBooking("booking-001");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getUpdateBooking_CancelledBooking_ReturnsBadRequest() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            testBookingDTO = BookingResponseDTO.builder()
                    .bookingId("booking-001")
                    .status(2) // Cancelled
                    .build();

            when(bookingService.getBookingDetail("booking-001")).thenReturn(testBookingDTO);
            when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));

            ResponseEntity<Map<String, Object>> response = bookingController.getUpdateBooking("booking-001");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void updateBooking_AsSuperadmin_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            BookingRequestDTO request = new BookingRequestDTO();
            when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));
            when(bookingService.updateBookingFromDTO("booking-001", request)).thenReturn(testBookingDTO);

            ResponseEntity<Map<String, Object>> response = bookingController.updateBooking("booking-001", request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void payBookingStatus_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(true);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));
            doNothing().when(bookingService).payBookingById("booking-001");

            Map<String, String> request = new HashMap<>();
            request.put("bookingId", "booking-001");

            ResponseEntity<Map<String, Object>> response = bookingController.payBookingStatus(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void cancelBookingStatus_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(true);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));
            doNothing().when(bookingService).cancelBookingById("booking-001");

            Map<String, String> request = new HashMap<>();
            request.put("bookingId", "booking-001");

            ResponseEntity<Map<String, Object>> response = bookingController.cancelBookingStatus(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getBookingStatistics_AsSuperadmin_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<PropertyStatisticsDTO> stats = new ArrayList<>();
            when(propertyService.getMonthlyStatistics(1, 2025)).thenReturn(stats);

            ResponseEntity<Map<String, Object>> response = bookingController.getBookingStatistics(1, 2025);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getBookingStatistics_AsOwner_FiltersOwnProperties() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(true);

            PropertyStatisticsDTO stat = PropertyStatisticsDTO.builder()
                    .propertyId("prop-001")
                    .build();
            List<PropertyStatisticsDTO> stats = new ArrayList<>(Arrays.asList(stat));
            when(propertyService.getMonthlyStatistics(1, 2025)).thenReturn(stats);
            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));

            ResponseEntity<Map<String, Object>> response = bookingController.getBookingStatistics(1, 2025);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getBookingStatistics_Error() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            when(propertyService.getMonthlyStatistics(anyInt(), anyInt())).thenThrow(new RuntimeException("Error"));

            ResponseEntity<Map<String, Object>> response = bookingController.getBookingStatistics(1, 2025);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }
}
