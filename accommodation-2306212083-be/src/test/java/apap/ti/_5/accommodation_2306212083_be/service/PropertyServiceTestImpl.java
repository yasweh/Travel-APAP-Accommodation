package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.PropertyStatisticsDTO;
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

class PropertyServiceTestImpl {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AccommodationBookingRepository bookingRepository;

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;
    private AccommodationBooking testBooking;

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
                .ownerName("Test Owner")
                .ownerId(UUID.randomUUID())
                .totalRoom(0)
                .activeRoom(0)
                .build();

        testRoomType = RoomType.builder()
                .roomTypeId("RT001")
                .name("Deluxe")
                .price(500000)
                .capacity(2)
                .floor(2)
                .property(testProperty)
                .activeStatus(1)
                .build();

        testRoom = Room.builder()
                .roomId("PROP001-ROOM-201")
                .name("Room 201")
                .roomType(testRoomType)
                .availabilityStatus(1)
                .activeRoom(1)
                .build();

        testBooking = AccommodationBooking.builder()
                .bookingId("BOOK001")
                .room(testRoom)
                .status(4) // Done status
                .totalPrice(1000000)
                .checkInDate(LocalDateTime.of(2025, 11, 15, 14, 0))
                .checkOutDate(LocalDateTime.of(2025, 11, 17, 12, 0))
                .activeStatus(1)
                .build();
    }

    @Test
    void testGetAllActiveProperties() {
        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.getAllActiveProperties();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(propertyRepository, times(1)).findByActiveStatus(1);
    }

    @Test
    void testGetPropertyById() {
        when(propertyRepository.findByPropertyIdAndActiveStatus("PROP001", 1)).thenReturn(Optional.of(testProperty));

        Optional<Property> result = propertyService.getPropertyById("PROP001");

        assertTrue(result.isPresent());
        assertEquals("PROP001", result.get().getPropertyId());
        verify(propertyRepository, times(1)).findByPropertyIdAndActiveStatus("PROP001", 1);
    }

    @Test
    void testUpdateProperty() {
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        Property result = propertyService.updateProperty(testProperty);

        assertNotNull(result);
        assertEquals("PROP001", result.getPropertyId());
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void testUpdatePropertyIncome() {
        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        propertyService.updatePropertyIncome("PROP001", 1000000);

        verify(propertyRepository, times(1)).findById("PROP001");
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void testGetPropertiesByOwner() {
        UUID ownerId = UUID.randomUUID();
        when(propertyRepository.findByOwnerIdAndActiveStatus(ownerId, 1)).thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.getPropertiesByOwner(ownerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(propertyRepository, times(1)).findByOwnerIdAndActiveStatus(ownerId, 1);
    }

    @Test
    void testGetMonthlyStatistics_WithValidBookings() {
        // Setup bookings with status 4 (Done) in November 2025
        AccommodationBooking booking1 = AccommodationBooking.builder()
                .bookingId("BOOK001")
                .room(testRoom)
                .status(4) // Done status
                .totalPrice(1000000)
                .checkInDate(LocalDateTime.of(2025, 11, 15, 14, 0))
                .checkOutDate(LocalDateTime.of(2025, 11, 17, 12, 0))
                .activeStatus(1)
                .build();

        AccommodationBooking booking2 = AccommodationBooking.builder()
                .bookingId("BOOK002")
                .room(testRoom)
                .status(4) // Done status
                .totalPrice(1500000)
                .checkInDate(LocalDateTime.of(2025, 11, 20, 14, 0))
                .checkOutDate(LocalDateTime.of(2025, 11, 22, 12, 0))
                .activeStatus(1)
                .build();

        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));
        when(bookingRepository.findByRoom_RoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(booking1, booking2));

        List<PropertyStatisticsDTO> result = propertyService.getMonthlyStatistics(11, 2025);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2500000, result.get(0).getTotalIncome());
        assertEquals(2, result.get(0).getBookingCount());
        verify(propertyRepository, times(1)).findByActiveStatus(1);
    }

    @Test
    void testGetMonthlyStatistics_WithDifferentStatuses() {
        // Booking with status 4 (Done) - should be counted
        AccommodationBooking doneBooking = AccommodationBooking.builder()
                .bookingId("BOOK001")
                .room(testRoom)
                .status(4)
                .totalPrice(1000000)
                .checkInDate(LocalDateTime.of(2025, 11, 15, 14, 0))
                .activeStatus(1)
                .build();

        // Booking with status 1 (Confirmed) - should NOT be counted
        AccommodationBooking confirmedBooking = AccommodationBooking.builder()
                .bookingId("BOOK002")
                .room(testRoom)
                .status(1)
                .totalPrice(2000000)
                .checkInDate(LocalDateTime.of(2025, 11, 16, 14, 0))
                .activeStatus(1)
                .build();

        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));
        when(bookingRepository.findByRoom_RoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(doneBooking, confirmedBooking));

        List<PropertyStatisticsDTO> result = propertyService.getMonthlyStatistics(11, 2025);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1000000, result.get(0).getTotalIncome()); // Only done booking counted
        assertEquals(1, result.get(0).getBookingCount());
    }

    @Test
    void testGetMonthlyStatistics_WithDifferentMonths() {
        // Booking in November - should be counted
        AccommodationBooking novemberBooking = AccommodationBooking.builder()
                .bookingId("BOOK001")
                .room(testRoom)
                .status(4)
                .totalPrice(1000000)
                .checkInDate(LocalDateTime.of(2025, 11, 15, 14, 0))
                .activeStatus(1)
                .build();

        // Booking in October - should NOT be counted
        AccommodationBooking octoberBooking = AccommodationBooking.builder()
                .bookingId("BOOK002")
                .room(testRoom)
                .status(4)
                .totalPrice(2000000)
                .checkInDate(LocalDateTime.of(2025, 10, 15, 14, 0))
                .activeStatus(1)
                .build();

        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));
        when(bookingRepository.findByRoom_RoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(novemberBooking, octoberBooking));

        List<PropertyStatisticsDTO> result = propertyService.getMonthlyStatistics(11, 2025);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1000000, result.get(0).getTotalIncome());
        assertEquals(1, result.get(0).getBookingCount());
    }

    @Test
    void testGetMonthlyStatistics_WithNoIncome() {
        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));
        when(bookingRepository.findByRoom_RoomType_Property_PropertyId("PROP001"))
                .thenReturn(new ArrayList<>());

        List<PropertyStatisticsDTO> result = propertyService.getMonthlyStatistics(11, 2025);

        assertNotNull(result);
        assertEquals(0, result.size()); // Properties with no income should be filtered out
    }

    @Test
    void testGetMonthlyStatistics_MultiplePropertiesSorted() {
        Property property2 = Property.builder()
                .propertyId("PROP002")
                .propertyName("Test Hotel 2")
                .activeStatus(1)
                .build();

        Room room2 = Room.builder()
                .roomId("PROP002-ROOM-301")
                .name("Room 301")
                .roomType(testRoomType)
                .build();

        AccommodationBooking booking1 = AccommodationBooking.builder()
                .bookingId("BOOK001")
                .room(testRoom)
                .status(4)
                .totalPrice(1000000)
                .checkInDate(LocalDateTime.of(2025, 11, 15, 14, 0))
                .build();

        AccommodationBooking booking2 = AccommodationBooking.builder()
                .bookingId("BOOK002")
                .room(room2)
                .status(4)
                .totalPrice(3000000)
                .checkInDate(LocalDateTime.of(2025, 11, 16, 14, 0))
                .build();

        when(propertyRepository.findByActiveStatus(1))
                .thenReturn(Arrays.asList(testProperty, property2));
        when(bookingRepository.findByRoom_RoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(booking1));
        when(bookingRepository.findByRoom_RoomType_Property_PropertyId("PROP002"))
                .thenReturn(Arrays.asList(booking2));

        List<PropertyStatisticsDTO> result = propertyService.getMonthlyStatistics(11, 2025);

        assertNotNull(result);
        assertEquals(2, result.size());
        // Should be sorted by income descending
        assertEquals(3000000, result.get(0).getTotalIncome());
        assertEquals(1000000, result.get(1).getTotalIncome());
    }

    @Test
    void testSearchPropertiesByName() {
        when(propertyRepository.findByPropertyNameContainingIgnoreCaseAndActiveStatus("Test", 1))
                .thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.searchProperties("Test", null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(propertyRepository, times(1)).findByPropertyNameContainingIgnoreCaseAndActiveStatus("Test", 1);
    }

    @Test
    void testSearchPropertiesByType() {
        when(propertyRepository.findByTypeAndActiveStatus(1, 1)).thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.searchProperties(null, 1, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(propertyRepository, times(1)).findByTypeAndActiveStatus(1, 1);
    }

    @Test
    void testSearchPropertiesByProvince() {
        when(propertyRepository.findByProvinceAndActiveStatus(1, 1)).thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.searchProperties(null, null, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(propertyRepository, times(1)).findByProvinceAndActiveStatus(1, 1);
    }

    @Test
    void testSearchPropertiesWithEmptyName() {
        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.searchProperties("", null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(propertyRepository, times(1)).findByActiveStatus(1);
    }

    @Test
    void testDeleteProperty() {
        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        propertyService.deleteProperty("PROP001");

        verify(propertyRepository, times(1)).findById("PROP001");
        verify(propertyRepository, times(1)).save(any(Property.class));
        assertEquals(0, testProperty.getActiveStatus());
    }

    // @Test
    // void testSoftDeleteProperty_Success() {
    //     Maintenance testMaintenance = Maintenance.builder()
    //             .maintenanceId("MAINT001")
    //             .room(testRoom)
    //             .activeStatus(1)
    //             .build();

    //     when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
    //     when(propertyRepository.hasFutureBookings(anyString(), any(LocalDateTime.class))).thenReturn(false);
    //     when(roomTypeRepository.findByProperty_PropertyId("PROP001")).thenReturn(Arrays.asList(testRoomType));
    //     when(roomRepository.findByRoomType_RoomTypeId("RT001")).thenReturn(Arrays.asList(testRoom));
    //     when(bookingRepository.findByRoom_RoomId("PROP001-ROOM-201")).thenReturn(Arrays.asList(testBooking));
    //     when(maintenanceRepository.findByRoom_RoomId("PROP001-ROOM-201")).thenReturn(Arrays.asList(testMaintenance));

    //     propertyService.softDeleteProperty("PROP001");

    //     verify(propertyRepository, times(1)).findById("PROP001");
    //     verify(propertyRepository, times(1)).hasFutureBookings(anyString(), any(LocalDateTime.class));
    //     verify(bookingRepository, times(1)).save(testBooking);
    //     verify(maintenanceRepository, times(1)).save(testMaintenance);
    //     verify(roomRepository, times(1)).save(testRoom);
    //     verify(roomTypeRepository, times(1)).save(testRoomType);
    //     verify(propertyRepository, times(1)).save(testProperty);
    // }

    @Test
    void testSoftDeleteProperty_WithFutureBookings() {
        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.hasFutureBookings(anyString(), any(LocalDateTime.class))).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            propertyService.softDeleteProperty("PROP001");
        });

        assertEquals("Cannot delete property with future bookings", exception.getMessage());
        verify(propertyRepository, times(1)).hasFutureBookings(anyString(), any(LocalDateTime.class));
    }

    @Test
    void testSoftDeleteProperty_PropertyNotFound() {
        when(propertyRepository.findById("PROP001")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            propertyService.softDeleteProperty("PROP001");
        });

        assertEquals("Property not found", exception.getMessage());
    }

    @Test
    void testCreateProperty() {
        RoomType roomType = RoomType.builder()
                .name("Deluxe")
                .price(500000)
                .capacity(2)
                .floor(2)
                .build();

        when(propertyRepository.count()).thenReturn(0L);
        when(idGenerator.generatePropertyId(any(Integer.class), any(UUID.class), any(Integer.class))).thenReturn("PROP002");
        when(idGenerator.generateRoomTypeId(any(Property.class), anyString(), any(Integer.class))).thenReturn("RT002");
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);
        when(roomRepository.save(any(Room.class))).thenReturn(new Room());

        Property result = propertyService.createProperty(
                testProperty,
                Arrays.asList(roomType),
                Arrays.asList(Arrays.asList(3))
        );

        assertNotNull(result);
        verify(propertyRepository, times(2)).save(any(Property.class));
        verify(roomTypeRepository, times(1)).save(any(RoomType.class));
        verify(roomRepository, times(3)).save(any(Room.class));
    }

    @Test
    void testUpdatePropertyWithRoomTypes_UpdateExisting() {
        Property updatedProperty = Property.builder()
                .propertyId("PROP001")
                .propertyName("Updated Hotel")
                .description("Updated Description")
                .address("Updated Address")
                .province(2)
                .build();

        RoomType updatedRoomType = RoomType.builder()
                .roomTypeId("RT001")
                .name("Super Deluxe")
                .price(750000)
                .capacity(3)
                .build();

        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);
        when(roomTypeRepository.findById("RT001")).thenReturn(Optional.of(testRoomType));
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001")).thenReturn(new ArrayList<>());

        Map<String, Integer> roomCountMap = new HashMap<>();
        
        Property result = propertyService.updatePropertyWithRoomTypes(
                updatedProperty,
                Arrays.asList(updatedRoomType),
                roomCountMap
        );

        assertNotNull(result);
        verify(propertyRepository, times(1)).save(any(Property.class));
        verify(roomTypeRepository, times(1)).save(any(RoomType.class));
    }

    @Test
    void testUpdatePropertyWithRoomTypes_AddNewRoomType() {
        Property updatedProperty = Property.builder()
                .propertyId("PROP001")
                .propertyName("Updated Hotel")
                .build();

        RoomType newRoomType = RoomType.builder()
                .name("Suite")
                .price(1000000)
                .capacity(4)
                .floor(3)
                .build();

        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001")).thenReturn(Arrays.asList(testRoom));
        when(roomTypeRepository.findByProperty_PropertyIdAndNameAndFloor("PROP001", "Suite", 3)).thenReturn(null);
        when(idGenerator.generateRoomTypeId(any(Property.class), anyString(), any(Integer.class))).thenReturn("RT002");
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(newRoomType);
        when(roomRepository.save(any(Room.class))).thenReturn(new Room());

        Map<String, Integer> roomCountMap = new HashMap<>();
        roomCountMap.put("Suite_3", 2);

        Property result = propertyService.updatePropertyWithRoomTypes(
                updatedProperty,
                Arrays.asList(newRoomType),
                roomCountMap
        );

        assertNotNull(result);
        verify(roomTypeRepository, times(1)).save(any(RoomType.class));
        verify(roomRepository, times(2)).save(any(Room.class));
    }

    @Test
    void testUpdatePropertyWithRoomTypes_PropertyNotFound() {
        Property updatedProperty = Property.builder()
                .propertyId("PROP999")
                .propertyName("Non-existent Hotel")
                .build();

        when(propertyRepository.findById("PROP999")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            propertyService.updatePropertyWithRoomTypes(updatedProperty, new ArrayList<>(), new HashMap<>());
        });

        assertEquals("Property not found", exception.getMessage());
    }
}