package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.BookingRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.BookingResponseDTO;
import apap.ti._5.accommodation_2306212083_be.model.*;
import apap.ti._5.accommodation_2306212083_be.repository.*;
import apap.ti._5.accommodation_2306212083_be.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BookingServiceTestImpl {

    @Mock
    private AccommodationBookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PropertyService propertyService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MaintenanceService maintenanceService;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Room testRoom;
    private RoomType testRoomType;
    private Property testProperty;
    private AccommodationBooking testBooking;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testProperty = Property.builder()
                .propertyId("PROP001")
                .propertyName("Test Hotel")
                .address("Test Address")
                .province(1)
                .type(1)
                .income(0)
                .activeStatus(1)
                .build();

        testRoomType = RoomType.builder()
                .roomTypeId("RT001")
                .name("Deluxe")
                .price(500000)
                .capacity(2)
                .facility("AC, TV")
                .floor(1)
                .property(testProperty)
                .activeStatus(1)
                .build();

        testRoom = Room.builder()
                .roomId("ROOM001")
                .name("101")
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(testRoomType)
                .build();

        testCustomer = Customer.builder()
                .customerId(UUID.randomUUID())
                .name("Test Customer")
                .email("test@example.com")
                .phone("081234567890")
                .build();

        testBooking = AccommodationBooking.builder()
                .bookingId("BOOK001")
                .room(testRoom)
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(3))
                .totalDays(2)
                .totalPrice(1000000)
                .status(0)
                .customerId(testCustomer.getCustomerId())
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .customerPhone("081234567890")
                .isBreakfast(false)
                .capacity(2)
                .refund(0)
                .extraPay(0)
                .activeStatus(1)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
    }

    // ========== BASIC CRUD TESTS ==========

    @Test
    void testGetAllBookingsAsDTO() {
        when(bookingRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testBooking));

        List<BookingResponseDTO> result = bookingService.getAllBookingsAsDTO();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BOOK001", result.get(0).getBookingId());
        verify(bookingRepository, times(1)).findByActiveStatus(1);
    }

    @Test
    void testGetBookingById() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        Optional<AccommodationBooking> result = bookingService.getBookingById("BOOK001");

        assertTrue(result.isPresent());
        assertEquals("BOOK001", result.get().getBookingId());
        verify(bookingRepository, times(1)).findById("BOOK001");
    }

    @Test
    void testGetBookingDetailDTO() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        BookingResponseDTO result = bookingService.getBookingDetail("BOOK001");

        assertNotNull(result);
        assertEquals("BOOK001", result.getBookingId());
        assertEquals("Test Hotel", result.getPropertyName());
        verify(bookingRepository, times(1)).findById("BOOK001");
    }

    @Test
    void testGetBookingDetailNotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookingService.getBookingDetail("INVALID"));
    }

    @Test
    void testGetAllBookings() {
        when(bookingRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testBooking));

        List<AccommodationBooking> result = bookingService.getAllBookings();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findByActiveStatus(1);
    }

    @Test
    void testGetBookingsByCustomer() {
        when(bookingRepository.findByCustomerId(any(UUID.class)))
                .thenReturn(Arrays.asList(testBooking));

        List<AccommodationBooking> result = bookingService.getBookingsByCustomer(testCustomer.getCustomerId());

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findByCustomerId(any(UUID.class));
    }

    @Test
    void testGetBookingsByCustomerWithInactive() {
        AccommodationBooking inactiveBooking = AccommodationBooking.builder()
                .bookingId("BOOK002")
                .activeStatus(0)
                .build();
        
        when(bookingRepository.findByCustomerId(any(UUID.class)))
                .thenReturn(Arrays.asList(testBooking, inactiveBooking));

        List<AccommodationBooking> result = bookingService.getBookingsByCustomer(testCustomer.getCustomerId());

        assertEquals(1, result.size());
        assertEquals("BOOK001", result.get(0).getBookingId());
    }

    @Test
    void testGetBookingsByStatus() {
        when(bookingRepository.findByStatus(0)).thenReturn(Arrays.asList(testBooking));

        List<AccommodationBooking> result = bookingService.getBookingsByStatus(0);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findByStatus(0);
    }

    @Test
    void testGetBookingsByStatusWithInactive() {
        AccommodationBooking inactiveBooking = AccommodationBooking.builder()
                .bookingId("BOOK002")
                .status(0)
                .activeStatus(0)
                .build();
        
        when(bookingRepository.findByStatus(0))
                .thenReturn(Arrays.asList(testBooking, inactiveBooking));

        List<AccommodationBooking> result = bookingService.getBookingsByStatus(0);

        assertEquals(1, result.size());
        assertEquals("BOOK001", result.get(0).getBookingId());
    }

    // ========== CREATE BOOKING TESTS ==========

    @Test
    void testCreateBooking() {
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(idGenerator.generateBookingId(anyString())).thenReturn("BOOK002");
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        testBooking.setRoom(testRoom);
        AccommodationBooking result = bookingService.createBooking(testBooking);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testCreateBookingWithRoom() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate("2025-11-10");
        request.setCheckOutDate("2025-11-12");
        request.setCustomerId(testCustomer.getCustomerId());
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findByRoom_RoomId(anyString())).thenReturn(new ArrayList<>());
        when(idGenerator.generateBookingId(anyString())).thenReturn("BOOK002");
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        BookingResponseDTO result = bookingService.createBookingWithRoom("ROOM001", request);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    @Test
    void testCreateBookingWithRoomAndBreakfast() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate("2025-11-10");
        request.setCheckOutDate("2025-11-12");
        request.setCustomerId(testCustomer.getCustomerId());
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(true); // With breakfast

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findByRoom_RoomId(anyString())).thenReturn(new ArrayList<>());
        when(idGenerator.generateBookingId(anyString())).thenReturn("BOOK002");
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        BookingResponseDTO result = bookingService.createBookingWithRoom("ROOM001", request);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    @Test
    void testCreateBookingWithNewCustomer() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate("2025-11-10");
        request.setCheckOutDate("2025-11-12");
        request.setCustomerId(UUID.randomUUID());
        request.setCustomerName("New Customer");
        request.setCustomerEmail("new@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findByRoom_RoomId(anyString())).thenReturn(new ArrayList<>());
        when(idGenerator.generateBookingId(anyString())).thenReturn("BOOK002");
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        BookingResponseDTO result = bookingService.createBookingWithRoom("ROOM001", request);

        assertNotNull(result);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testCreateBookingRoomNotFound() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("INVALID");

        when(roomRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, 
            () -> bookingService.createBookingWithRoom("INVALID", request));
    }

    @Test
    void testCreateBookingMaintenanceConflict() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate("2025-11-10");
        request.setCheckOutDate("2025-11-12");
        request.setCustomerId(testCustomer.getCustomerId());
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThrows(RuntimeException.class, 
            () -> bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void testCreateBookingCapacityExceeded() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate("2025-11-10");
        request.setCheckOutDate("2025-11-12");
        request.setCustomerId(testCustomer.getCustomerId());
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(5); // Exceeds capacity
        request.setAddOnBreakfast(false);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(RuntimeException.class, 
            () -> bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void testCreateBookingWithSelection() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate("2025-11-10");
        request.setCheckOutDate("2025-11-12");
        request.setCustomerId(testCustomer.getCustomerId());
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findByRoom_RoomId(anyString())).thenReturn(new ArrayList<>());
        when(idGenerator.generateBookingId(anyString())).thenReturn("BOOK002");
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        BookingResponseDTO result = bookingService.createBookingWithSelection(request);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    // ========== UPDATE BOOKING TESTS ==========

    @Test
    void testUpdateBooking() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking updated = AccommodationBooking.builder()
                .bookingId("BOOK001")
                .checkInDate(LocalDateTime.now().plusDays(2))
                .checkOutDate(LocalDateTime.now().plusDays(4))
                .totalPrice(1200000)
                .build();

        AccommodationBooking result = bookingService.updateBooking("BOOK001", updated);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    @Test
    void testUpdateBookingStatusZeroPriceIncrease() {
        testBooking.setStatus(0);
        testBooking.setTotalPrice(1000000);
        
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking updated = AccommodationBooking.builder()
                .checkInDate(testBooking.getCheckInDate())
                .checkOutDate(testBooking.getCheckOutDate())
                .totalPrice(1200000)
                .capacity(2)
                .build();

        AccommodationBooking result = bookingService.updateBooking("BOOK001", updated);

        assertNotNull(result);
        assertEquals(1200000, result.getTotalPrice());
    }

    @Test
    void testUpdateBookingStatusOnePriceIncrease() {
        testBooking.setStatus(1);
        testBooking.setTotalPrice(1000000);
        
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking updated = AccommodationBooking.builder()
                .checkInDate(testBooking.getCheckInDate())
                .checkOutDate(testBooking.getCheckOutDate())
                .totalPrice(1200000)
                .capacity(2)
                .build();

        AccommodationBooking result = bookingService.updateBooking("BOOK001", updated);

        assertNotNull(result);
        assertEquals(200000, result.getExtraPay());
    }

    @Test
    void testUpdateBookingStatusOnePriceDecrease() {
        testBooking.setStatus(1);
        testBooking.setTotalPrice(1000000);
        
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking updated = AccommodationBooking.builder()
                .checkInDate(testBooking.getCheckInDate())
                .checkOutDate(testBooking.getCheckOutDate())
                .totalPrice(800000)
                .capacity(2)
                .build();

        AccommodationBooking result = bookingService.updateBooking("BOOK001", updated);

        assertNotNull(result);
        assertEquals(200000, result.getRefund());
        assertEquals(3, result.getStatus());
    }

    @Test
    void testUpdateBookingWithExtraPay() {
        testBooking.setStatus(0);
        testBooking.setExtraPay(100000);
        
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        AccommodationBooking updated = AccommodationBooking.builder()
                .checkInDate(testBooking.getCheckInDate())
                .checkOutDate(testBooking.getCheckOutDate())
                .totalPrice(1200000)
                .capacity(2)
                .build();

        assertThrows(RuntimeException.class, 
            () -> bookingService.updateBooking("BOOK001", updated));
    }

    @Test
    void testUpdateBookingFromDTO() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCheckInDate("2025-11-11");
        request.setCheckOutDate("2025-11-13");
        request.setCustomerId(testBooking.getCustomerId());
        request.setCustomerName("Updated Name");
        request.setCustomerEmail("updated@example.com");
        request.setCustomerPhone("081234567891");
        request.setCapacity(2);
        request.setAddOnBreakfast(true);

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findByRoom_RoomId(anyString())).thenReturn(Arrays.asList(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        BookingResponseDTO result = bookingService.updateBookingFromDTO("BOOK001", request);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    @Test
    void testUpdateBookingFromDTOCancelled() {
        testBooking.setStatus(2);
        BookingRequestDTO request = new BookingRequestDTO();

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, 
            () -> bookingService.updateBookingFromDTO("BOOK001", request));
    }

    @Test
    void testUpdateBookingFromDTORefundRequested() {
        testBooking.setStatus(3);
        BookingRequestDTO request = new BookingRequestDTO();

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, 
            () -> bookingService.updateBookingFromDTO("BOOK001", request));
    }

    @Test
    void testUpdateBookingFromDTODone() {
        testBooking.setStatus(4);
        BookingRequestDTO request = new BookingRequestDTO();

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, 
            () -> bookingService.updateBookingFromDTO("BOOK001", request));
    }

    @Test
    void testUpdateBookingFromDTOPriceIncrease() {
        testBooking.setStatus(1);
        testBooking.setTotalPrice(1000000);
        
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCheckInDate("2025-11-11");
        request.setCheckOutDate("2025-11-14"); // 3 days instead of 2
        request.setCustomerId(testBooking.getCustomerId());
        request.setCustomerName("Test");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findByRoom_RoomId(anyString())).thenReturn(Arrays.asList(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        BookingResponseDTO result = bookingService.updateBookingFromDTO("BOOK001", request);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    @Test
    void testUpdateBookingFromDTOPriceDecrease() {
        testBooking.setStatus(1);
        testBooking.setTotalPrice(1500000);
        
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCheckInDate("2025-11-11");
        request.setCheckOutDate("2025-11-12"); // 1 day instead of 2
        request.setCustomerId(testBooking.getCustomerId());
        request.setCustomerName("Test");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findByRoom_RoomId(anyString())).thenReturn(Arrays.asList(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        BookingResponseDTO result = bookingService.updateBookingFromDTO("BOOK001", request);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    // ========== PAYMENT TESTS ==========

    @Test
    void testPayBooking() {
        testBooking.setStatus(0);
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking result = bookingService.payBooking("BOOK001");

        assertNotNull(result);
        assertEquals(1, result.getStatus());
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    @Test
    void testPayBookingInvalidStatus() {
        testBooking.setStatus(1);
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, () -> bookingService.payBooking("BOOK001"));
    }

    @Test
    void testPayBookingById() {
        testBooking.setStatus(0);
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        doNothing().when(propertyService).updatePropertyIncome(anyString(), anyInt());

        bookingService.payBookingById("BOOK001");

        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
        verify(propertyService, times(1)).updatePropertyIncome(anyString(), anyInt());
    }

    @Test
    void testPayBookingByIdWithExtraPay() {
        testBooking.setStatus(1);
        testBooking.setExtraPay(100000);
        
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        doNothing().when(propertyService).updatePropertyIncome(anyString(), anyInt());

        bookingService.payBookingById("BOOK001");

        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
        verify(propertyService, times(1)).updatePropertyIncome(anyString(), eq(100000));
    }

    // ========== CANCEL TESTS ==========

    @Test
    void testCancelBooking() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        AccommodationBooking result = bookingService.cancelBooking("BOOK001");

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testCancelBookingStatusOne() {
        testBooking.setStatus(1);
        testBooking.setTotalPrice(1000000);
        
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        doNothing().when(propertyService).updatePropertyIncome(anyString(), anyInt());

        AccommodationBooking result = bookingService.cancelBooking("BOOK001");

        assertNotNull(result);
        verify(propertyService, times(1)).updatePropertyIncome(anyString(), eq(-1000000));
    }

    @Test
    void testCancelBookingStatusOneWithExtraPay() {
        testBooking.setStatus(1);
        testBooking.setTotalPrice(1000000);
        testBooking.setExtraPay(200000);
        
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        doNothing().when(propertyService).updatePropertyIncome(anyString(), anyInt());

        AccommodationBooking result = bookingService.cancelBooking("BOOK001");

        assertNotNull(result);
        verify(propertyService, times(1)).updatePropertyIncome(anyString(), eq(-800000));
    }

    @Test
    void testCancelBookingStatusThree() {
        testBooking.setStatus(3);
        testBooking.setTotalPrice(1000000);
        testBooking.setRefund(100000);
        
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        doNothing().when(propertyService).updatePropertyIncome(anyString(), anyInt());

        AccommodationBooking result = bookingService.cancelBooking("BOOK001");

        assertNotNull(result);
        verify(propertyService, times(1)).updatePropertyIncome(anyString(), eq(-1100000));
    }

    @Test
    void testCancelBookingById() {
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        bookingService.cancelBookingById("BOOK001");

        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
        assertEquals(2, testBooking.getStatus());
    }

    // ========== REFUND TESTS ==========

    @Test
    void testRefundBooking() {
        testBooking.setStatus(1);
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        AccommodationBooking result = bookingService.refundBooking("BOOK001");

        assertNotNull(result);
        assertEquals(3, result.getStatus());
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    @Test
    void testRefundBookingInvalidStatus() {
        testBooking.setStatus(0);
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, () -> bookingService.refundBooking("BOOK001"));
    }

    @Test
    void testRefundBookingById() {
        testBooking.setStatus(3);
        testBooking.setRefund(100000);
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        doNothing().when(propertyService).updatePropertyIncome(anyString(), anyInt());

        bookingService.refundBookingById("BOOK001");

        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
        verify(propertyService, times(1)).updatePropertyIncome(anyString(), eq(-100000));
        assertEquals(4, testBooking.getStatus());
    }

    @Test
    void testRefundBookingByIdInvalidStatus() {
        testBooking.setStatus(1);
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, () -> bookingService.refundBookingById("BOOK001"));
    }

    @Test
    void testRefundBookingByIdNoRefundAmount() {
        testBooking.setStatus(3);
        testBooking.setRefund(0);
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(RuntimeException.class, () -> bookingService.refundBookingById("BOOK001"));
    }

    // ========== AUTO CHECK-IN TESTS ==========

    @Test
    void testAutoCheckInBookings() {
        testBooking.setStatus(1);
        testBooking.setCheckInDate(LocalDateTime.now().minusHours(1));
        testBooking.setExtraPay(0);
        
        when(bookingRepository.findBookingsToCheckIn(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        doNothing().when(propertyService).updatePropertyIncome(anyString(), any(Integer.class));

        bookingService.autoCheckInBookings();

        verify(bookingRepository, times(1)).findBookingsToCheckIn(any(LocalDateTime.class));
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
        verify(propertyService, times(1)).updatePropertyIncome(anyString(), anyInt());
    }

    @Test
    void testAutoCheckInBookingsWithExtraPay() {
        testBooking.setStatus(1);
        testBooking.setCheckInDate(LocalDateTime.now().minusHours(1));
        testBooking.setExtraPay(100000);
        
        when(bookingRepository.findBookingsToCheckIn(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testBooking));
        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        bookingService.autoCheckInBookings();

        verify(bookingRepository, times(1)).findBookingsToCheckIn(any(LocalDateTime.class));
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    @Test
    void testAutoCheckInBookingsWithRefund() {
        testBooking.setStatus(1);
        testBooking.setCheckInDate(LocalDateTime.now().minusHours(1));
        testBooking.setExtraPay(0);
        testBooking.setRefund(100000);
        testBooking.setTotalPrice(1000000);
        
        when(bookingRepository.findBookingsToCheckIn(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        doNothing().when(propertyService).updatePropertyIncome(anyString(), anyInt());

        bookingService.autoCheckInBookings();

        verify(propertyService, times(1)).updatePropertyIncome(anyString(), eq(900000));
    }

    // ========== ROOM AVAILABILITY TESTS ==========

    @Test
    void testIsRoomAvailableForDates() {
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001")).thenReturn(new ArrayList<>());

        boolean result = bookingService.isRoomAvailableForDates(
                "ROOM001",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(7)
        );

        assertTrue(result);
        verify(roomRepository, times(1)).findById("ROOM001");
    }

    @Test
    void testIsRoomAvailableForDatesRoomNotFound() {
        when(roomRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, 
            () -> bookingService.isRoomAvailableForDates("INVALID", 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));
    }

    @Test
    void testIsRoomAvailableForDatesInactiveRoom() {
        testRoom.setActiveRoom(0);
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001")).thenReturn(new ArrayList<>());

        boolean result = bookingService.isRoomAvailableForDates(
                "ROOM001",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(7)
        );

        assertFalse(result);
    }

    @Test
    void testIsRoomAvailableForDatesWithBookingConflict() {
        AccommodationBooking conflictBooking = AccommodationBooking.builder()
                .bookingId("BOOK002")
                .checkInDate(LocalDateTime.now().plusDays(4))
                .checkOutDate(LocalDateTime.now().plusDays(6))
                .status(1)
                .activeStatus(1)
                .build();
        
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001"))
                .thenReturn(Arrays.asList(conflictBooking));

        boolean result = bookingService.isRoomAvailableForDates(
                "ROOM001",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(7)
        );

        assertFalse(result);
    }

    @Test
    void testIsRoomAvailableForDatesWithCancelledBooking() {
        AccommodationBooking cancelledBooking = AccommodationBooking.builder()
                .bookingId("BOOK002")
                .checkInDate(LocalDateTime.now().plusDays(4))
                .checkOutDate(LocalDateTime.now().plusDays(6))
                .status(2) // Cancelled
                .activeStatus(1)
                .build();
        
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001"))
                .thenReturn(Arrays.asList(cancelledBooking));

        boolean result = bookingService.isRoomAvailableForDates(
                "ROOM001",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(7)
        );

        assertTrue(result); // Should be available since booking is cancelled
    }

    @Test
    void testIsRoomAvailableForDatesWithInactiveBooking() {
        AccommodationBooking inactiveBooking = AccommodationBooking.builder()
                .bookingId("BOOK002")
                .checkInDate(LocalDateTime.now().plusDays(4))
                .checkOutDate(LocalDateTime.now().plusDays(6))
                .status(1)
                .activeStatus(0) // Inactive
                .build();
        
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001"))
                .thenReturn(Arrays.asList(inactiveBooking));

        boolean result = bookingService.isRoomAvailableForDates(
                "ROOM001",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(7)
        );

        assertTrue(result); // Should be available since booking is inactive
    }

    @Test
    void testIsRoomAvailableForDatesWithMaintenanceConflict() {
        testRoom.setMaintenanceStart(LocalDateTime.now().plusDays(4));
        testRoom.setMaintenanceEnd(LocalDateTime.now().plusDays(6));
        
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001")).thenReturn(new ArrayList<>());

        boolean result = bookingService.isRoomAvailableForDates(
                "ROOM001",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(7)
        );

        assertFalse(result); // Should not be available due to maintenance
    }

    @Test
    void testIsRoomAvailableForDatesNoConflict() {
        AccommodationBooking pastBooking = AccommodationBooking.builder()
                .bookingId("BOOK002")
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(3))
                .status(1)
                .activeStatus(1)
                .build();
        
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001"))
                .thenReturn(Arrays.asList(pastBooking));

        boolean result = bookingService.isRoomAvailableForDates(
                "ROOM001",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(7)
        );

        assertTrue(result); // Should be available, no date overlap
    }

    // ========== DATE VALIDATION TESTS ==========

    @Test
    void testCreateBookingWithPastCheckInDate() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate("2024-01-01"); // Past date
        request.setCheckOutDate("2025-11-12");
        request.setCustomerId(testCustomer.getCustomerId());
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));

        assertThrows(RuntimeException.class, 
            () -> bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void testCreateBookingWithCheckOutBeforeCheckIn() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate("2025-11-12");
        request.setCheckOutDate("2025-11-10"); // Before check-in
        request.setCustomerId(testCustomer.getCustomerId());
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));

        assertThrows(RuntimeException.class, 
            () -> bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void testCreateBookingWithSameDates() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate("2025-11-10");
        request.setCheckOutDate("2025-11-10"); // Same date
        request.setCustomerId(testCustomer.getCustomerId());
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));

        assertThrows(RuntimeException.class, 
            () -> bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void testCreateBookingWithEmptyDate() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate(""); // Empty
        request.setCheckOutDate("2025-11-12");
        request.setCustomerId(testCustomer.getCustomerId());
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));

        assertThrows(RuntimeException.class, 
            () -> bookingService.createBookingWithRoom("ROOM001", request));
    }

    @Test
    void testCreateBookingWithInvalidDateFormat() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setRoomId("ROOM001");
        request.setCheckInDate("invalid-date");
        request.setCheckOutDate("2025-11-12");
        request.setCustomerId(testCustomer.getCustomerId());
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));

        assertThrows(RuntimeException.class, 
            () -> bookingService.createBookingWithRoom("ROOM001", request));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void testGetBookingByIdNotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        Optional<AccommodationBooking> result = bookingService.getBookingById("INVALID");

        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateBookingNotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        AccommodationBooking updated = new AccommodationBooking();

        assertThrows(RuntimeException.class, 
            () -> bookingService.updateBooking("INVALID", updated));
    }

    @Test
    void testPayBookingNotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, 
            () -> bookingService.payBooking("INVALID"));
    }

    @Test
    void testCancelBookingNotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, 
            () -> bookingService.cancelBooking("INVALID"));
    }

    @Test
    void testRefundBookingNotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, 
            () -> bookingService.refundBooking("INVALID"));
    }

    @Test
    void testUpdateBookingFromDTONotFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        BookingRequestDTO request = new BookingRequestDTO();

        assertThrows(RuntimeException.class, 
            () -> bookingService.updateBookingFromDTO("INVALID", request));
    }

    @Test
    void testUpdateBookingFromDTOMaintenanceConflict() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCheckInDate("2025-11-10");
        request.setCheckOutDate("2025-11-12");
        request.setCustomerId(testBooking.getCustomerId());
        request.setCustomerName("Test");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThrows(RuntimeException.class, 
            () -> bookingService.updateBookingFromDTO("BOOK001", request));
    }

    @Test
    void testUpdateBookingStatusZeroSamePrice() {
        testBooking.setStatus(0);
        testBooking.setTotalPrice(1000000);
        
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCheckInDate("2025-11-11");
        request.setCheckOutDate("2025-11-13");
        request.setCustomerId(testBooking.getCustomerId());
        request.setCustomerName("Test");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findByRoom_RoomId(anyString())).thenReturn(Arrays.asList(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        BookingResponseDTO result = bookingService.updateBookingFromDTO("BOOK001", request);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }

    @Test
    void testUpdateBookingStatusOneSamePrice() {
        testBooking.setStatus(1);
        testBooking.setTotalPrice(1000000);
        
        BookingRequestDTO request = new BookingRequestDTO();
        request.setCheckInDate("2025-11-11");
        request.setCheckOutDate("2025-11-13");
        request.setCustomerId(testBooking.getCustomerId());
        request.setCustomerName("Test");
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("081234567890");
        request.setCapacity(2);
        request.setAddOnBreakfast(false);

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCustomer));
        when(maintenanceService.hasMaintenanceConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findByRoom_RoomId(anyString())).thenReturn(Arrays.asList(testBooking));
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(testBooking);

        BookingResponseDTO result = bookingService.updateBookingFromDTO("BOOK001", request);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(AccommodationBooking.class));
    }
}