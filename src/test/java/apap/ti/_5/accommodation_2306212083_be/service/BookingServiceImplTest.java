package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.BookingRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.BookingResponseDTO;
import apap.ti._5.accommodation_2306212083_be.model.*;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306212083_be.repository.CustomerRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306212083_be.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private AccommodationBookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PropertyService propertyService;

    @Mock
    private MaintenanceService maintenanceService;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;
    private AccommodationBooking testBooking;
    private Customer testCustomer;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID();

        testProperty = new Property();
        testProperty.setPropertyId("PROP001");
        testProperty.setPropertyName("Test Hotel");
        testProperty.setType(1);
        testProperty.setProvince(1);
        testProperty.setOwnerId(UUID.randomUUID());

        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("RT001");
        testRoomType.setName("Suite");
        testRoomType.setPrice(100000);
        testRoomType.setCapacity(2);
        testRoomType.setFacility("AC, TV");
        testRoomType.setFloor(1);
        testRoomType.setProperty(testProperty);

        testRoom = new Room();
        testRoom.setRoomId("ROOM001");
        testRoom.setName("101");
        testRoom.setAvailabilityStatus(1);
        testRoom.setActiveRoom(1);
        testRoom.setRoomType(testRoomType);

        testBooking = AccommodationBooking.builder()
                .bookingId("BOOK001")
                .room(testRoom)
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(3))
                .totalDays(2)
                .totalPrice(200000)
                .status(0)
                .customerId(testCustomerId)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("08123456789")
                .isBreakfast(false)
                .capacity(2)
                .activeStatus(1)
                .createdDate(LocalDateTime.now())
                .build();

        testCustomer = Customer.builder()
                .customerId(testCustomerId)
                .name("John Doe")
                .email("john@example.com")
                .phone("08123456789")
                .build();
    }

    // ========== CREATE BOOKING TESTS ==========

    @Test
    void createBooking_Success() {
        when(idGenerator.generateBookingId(anyString())).thenReturn("BOOK001");
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking result = bookingService.createBooking(testBooking);

        assertNotNull(result);
        assertEquals("BOOK001", result.getBookingId());
        verify(roomRepository).save(any(Room.class));
        verify(bookingRepository).save(any(AccommodationBooking.class));
    }

    // ========== GET BOOKING TESTS ==========

    @Test
    void getBookingById_Success() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        Optional<AccommodationBooking> result = bookingService.getBookingById("BOOK001");

        assertTrue(result.isPresent());
        assertEquals("BOOK001", result.get().getBookingId());
    }

    @Test
    void getBookingById_NotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        Optional<AccommodationBooking> result = bookingService.getBookingById("INVALID");

        assertFalse(result.isPresent());
    }

    @Test
    void getAllBookings_Success() {
        when(bookingRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testBooking));

        List<AccommodationBooking> result = bookingService.getAllBookings();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getBookingsByCustomer_Success() {
        when(bookingRepository.findByCustomerId(testCustomerId)).thenReturn(Arrays.asList(testBooking));

        List<AccommodationBooking> result = bookingService.getBookingsByCustomer(testCustomerId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getBookingsByStatus_Success() {
        when(bookingRepository.findByStatus(0)).thenReturn(Arrays.asList(testBooking));

        List<AccommodationBooking> result = bookingService.getBookingsByStatus(0);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ========== UPDATE BOOKING TESTS ==========

    @Test
    void updateBooking_Success() {
        AccommodationBooking updatedData = AccommodationBooking.builder()
                .checkInDate(LocalDateTime.now().plusDays(2))
                .checkOutDate(LocalDateTime.now().plusDays(4))
                .capacity(3)
                .totalPrice(300000)
                .build();

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking result = bookingService.updateBooking("BOOK001", updatedData);

        assertNotNull(result);
        verify(bookingRepository).save(any(AccommodationBooking.class));
    }

    @Test
    void updateBooking_NotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                bookingService.updateBooking("INVALID", testBooking));
    }

    @Test
    void updateBooking_InvalidStatus() {
        testBooking.setStatus(1); // Payment Confirmed
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, () -> 
                bookingService.updateBooking("BOOK001", testBooking));
    }

    // ========== PAY BOOKING TESTS ==========

    @Test
    void payBooking_Success() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking result = bookingService.payBooking("BOOK001");

        assertNotNull(result);
        assertEquals(1, result.getStatus());
    }

    @Test
    void payBooking_NotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                bookingService.payBooking("INVALID"));
    }

    @Test
    void payBooking_InvalidStatus() {
        testBooking.setStatus(1);
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, () -> 
                bookingService.payBooking("BOOK001"));
    }

    // ========== CANCEL BOOKING TESTS ==========

    @Test
    void cancelBooking_WaitingForPayment() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking result = bookingService.cancelBooking("BOOK001");

        assertNotNull(result);
        assertEquals(2, result.getStatus());
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void cancelBooking_PaymentConfirmed() {
        testBooking.setStatus(1);
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking result = bookingService.cancelBooking("BOOK001");

        assertNotNull(result);
        verify(propertyService).updatePropertyIncome(anyString(), anyInt());
    }

    // ========== AUTO CHECK-IN TESTS ==========

    @Test
    void autoCheckInBookings_Success() {
        when(bookingRepository.findBookingsToCheckIn(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testBooking));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        bookingService.autoCheckInBookings();

        verify(propertyService).updatePropertyIncome(anyString(), anyInt());
        verify(roomRepository).save(any(Room.class));
    }

    // ========== DTO-BASED METHODS TESTS ==========

    @Test
    void getBookingDetail_Success() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        BookingResponseDTO result = bookingService.getBookingDetail("BOOK001");

        assertNotNull(result);
        assertEquals("BOOK001", result.getBookingId());
    }

    @Test
    void getBookingDetail_NotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                bookingService.getBookingDetail("INVALID"));
    }

    @Test
    void getAllBookingsAsDTO_Success() {
        when(bookingRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testBooking));

        List<BookingResponseDTO> result = bookingService.getAllBookingsAsDTO();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void payBookingById_Success() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        bookingService.payBookingById("BOOK001");

        verify(propertyService).updatePropertyIncome(anyString(), anyInt());
        verify(bookingRepository).save(any(AccommodationBooking.class));
    }

    @Test
    void cancelBookingById_Success() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        bookingService.cancelBookingById("BOOK001");

        verify(bookingRepository).save(any(AccommodationBooking.class));
    }

    @Test
    void isRoomAvailableForDates_Available() {
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001")).thenReturn(Collections.emptyList());

        boolean result = bookingService.isRoomAvailableForDates("ROOM001", 
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(7));

        assertTrue(result);
    }

    @Test
    void isRoomAvailableForDates_NotAvailable_BookingOverlap() {
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001")).thenReturn(Arrays.asList(testBooking));

        boolean result = bookingService.isRoomAvailableForDates("ROOM001", 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertFalse(result);
    }

    @Test
    void isRoomAvailableForDates_NotAvailable_RoomDeleted() {
        testRoom.setActiveRoom(0);
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));

        boolean result = bookingService.isRoomAvailableForDates("ROOM001", 
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(7));

        assertFalse(result);
    }

    @Test
    void isRoomAvailableForDates_NotAvailable_MaintenanceOverlap() {
        testRoom.setMaintenanceStart(LocalDateTime.now().plusDays(5));
        testRoom.setMaintenanceEnd(LocalDateTime.now().plusDays(8));
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001")).thenReturn(Collections.emptyList());

        boolean result = bookingService.isRoomAvailableForDates("ROOM001", 
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(7));

        assertFalse(result);
    }

    @Test
    void isRoomAvailableForDates_Available_CancelledBooking() {
        testBooking.setStatus(2); // Cancelled
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001")).thenReturn(Arrays.asList(testBooking));

        boolean result = bookingService.isRoomAvailableForDates("ROOM001", 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertTrue(result);
    }

    @Test
    void createBookingWithRoom_Success() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate(LocalDateTime.now().plusDays(5).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(7).toLocalDate().toString());
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(), any())).thenReturn(false);
        when(idGenerator.generateBookingId(anyString())).thenReturn("BOOK002");
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        BookingResponseDTO result = bookingService.createBookingWithRoom("ROOM001", request);

        assertNotNull(result);
        verify(bookingRepository).save(any(AccommodationBooking.class));
    }

    @Test
    void createBookingWithRoom_RoomNotFound() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate(LocalDateTime.now().plusDays(5).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(7).toLocalDate().toString());

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(roomRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                bookingService.createBookingWithRoom("INVALID", request));
    }

    @Test
    void createBookingWithRoom_MaintenanceConflict() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate(LocalDateTime.now().plusDays(5).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(7).toLocalDate().toString());
        request.setCapacity(2);

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(), any())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
                bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void createBookingWithRoom_CapacityExceeded() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate(LocalDateTime.now().plusDays(5).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(7).toLocalDate().toString());
        request.setCapacity(10); // Exceeds room capacity of 2

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(), any())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> 
                bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void createBookingWithRoom_InvalidDates_CheckInBeforeNow() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate(LocalDateTime.now().minusDays(1).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(2).toLocalDate().toString());
        request.setCapacity(2);

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));

        assertThrows(RuntimeException.class, () -> 
                bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void createBookingWithRoom_InvalidDates_CheckOutBeforeCheckIn() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate(LocalDateTime.now().plusDays(5).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(3).toLocalDate().toString());
        request.setCapacity(2);

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));

        assertThrows(RuntimeException.class, () -> 
                bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void createBookingWithRoom_NewCustomer() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("New Customer");
        request.setCustomerEmail("new@example.com");
        request.setCustomerPhone("08999999999");
        request.setCheckInDate(LocalDateTime.now().plusDays(5).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(7).toLocalDate().toString());
        request.setCapacity(2);
        request.setAddOnBreakfast(true);

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(), any())).thenReturn(false);
        when(idGenerator.generateBookingId(anyString())).thenReturn("BOOK002");
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        BookingResponseDTO result = bookingService.createBookingWithRoom("ROOM001", request);

        assertNotNull(result);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateBookingFromDTO_Success() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate(LocalDateTime.now().plusDays(5).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(7).toLocalDate().toString());
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        BookingResponseDTO result = bookingService.updateBookingFromDTO("BOOK001", request);

        assertNotNull(result);
    }

    @Test
    void updateBookingFromDTO_CancelledBooking() {
        testBooking.setStatus(2);
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate(LocalDateTime.now().plusDays(5).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(7).toLocalDate().toString());

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        assertThrows(RuntimeException.class, () -> 
                bookingService.updateBookingFromDTO("BOOK001", request));
    }

    @Test
    void updateBookingFromDTO_PriceChangeForConfirmed() {
        testBooking.setStatus(1); // Confirmed
        testBooking.setTotalPrice(200000);
        
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate(LocalDateTime.now().plusDays(5).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(10).toLocalDate().toString()); // Different days = different price
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(), any())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> 
                bookingService.updateBookingFromDTO("BOOK001", request));
    }

    @Test
    void createBookingWithSelection_Success() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate(LocalDateTime.now().plusDays(5).toLocalDate().toString());
        request.setCheckOutDate(LocalDateTime.now().plusDays(7).toLocalDate().toString());
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(), any())).thenReturn(false);
        when(idGenerator.generateBookingId(anyString())).thenReturn("BOOK002");
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        BookingResponseDTO result = bookingService.createBookingWithSelection(request);

        assertNotNull(result);
    }

    @Test
    void createBookingWithRoom_EmptyDate() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate("");
        request.setCheckOutDate(LocalDateTime.now().plusDays(7).toLocalDate().toString());

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        assertThrows(RuntimeException.class, () -> 
                bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void createBookingWithRoom_InvalidDateFormat() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCustomerId(testCustomerId);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("08123456789");
        request.setCheckInDate("invalid-date");
        request.setCheckOutDate(LocalDateTime.now().plusDays(7).toLocalDate().toString());

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        assertThrows(RuntimeException.class, () -> 
                bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void isRoomAvailableForDates_RoomNotFound() {
        when(roomRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                bookingService.isRoomAvailableForDates("INVALID", 
                        LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(7)));
    }
}
