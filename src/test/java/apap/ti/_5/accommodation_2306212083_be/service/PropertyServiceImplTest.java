package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.PropertyStatisticsDTO;
import apap.ti._5.accommodation_2306212083_be.model.*;
import apap.ti._5.accommodation_2306212083_be.repository.*;
import apap.ti._5.accommodation_2306212083_be.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

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
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();

        testProperty = new Property();
        testProperty.setPropertyId("PROP001");
        testProperty.setPropertyName("Test Hotel");
        testProperty.setType(1);
        testProperty.setProvince(1);
        testProperty.setAddress("Test Address");
        testProperty.setDescription("Test Description");
        testProperty.setOwnerId(ownerId);
        testProperty.setActiveStatus(1);
        testProperty.setIncome(0);
        testProperty.setTotalRoom(0);
        testProperty.setActiveRoom(0);

        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("RT001");
        testRoomType.setName("Suite");
        testRoomType.setPrice(100000);
        testRoomType.setCapacity(2);
        testRoomType.setFacility("AC, TV");
        testRoomType.setFloor(1);
        testRoomType.setProperty(testProperty);
        testRoomType.setActiveStatus(1);

        testRoom = new Room();
        testRoom.setRoomId("ROOM001");
        testRoom.setName("Room 101");
        testRoom.setRoomType(testRoomType);
        testRoom.setAvailabilityStatus(1);
        testRoom.setActiveRoom(1);

        testBooking = AccommodationBooking.builder()
                .bookingId("BOOK001")
                .room(testRoom)
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(3))
                .totalPrice(200000)
                .status(1) // Confirmed
                .activeStatus(1)
                .build();
    }

    // ========== CREATE PROPERTY TESTS ==========

    @Test
    void createProperty_Success() {
        List<RoomType> roomTypes = Arrays.asList(testRoomType);
        List<List<Integer>> roomCounts = Arrays.asList(Arrays.asList(2));

        when(propertyRepository.count()).thenReturn(0L);
        when(idGenerator.generatePropertyId(anyInt(), any(UUID.class), anyInt())).thenReturn("PROP001");
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);
        when(idGenerator.generateRoomTypeId(any(Property.class), anyString(), anyInt())).thenReturn("RT001");
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Property result = propertyService.createProperty(testProperty, roomTypes, roomCounts);

        assertNotNull(result);
        verify(propertyRepository, times(2)).save(any(Property.class));
        verify(roomTypeRepository).save(any(RoomType.class));
        verify(roomRepository, times(2)).save(any(Room.class));
    }

    // ========== GET PROPERTY TESTS ==========

    @Test
    void getPropertyById_Success() {
        when(propertyRepository.findByPropertyIdAndActiveStatus("PROP001", 1))
                .thenReturn(Optional.of(testProperty));

        Optional<Property> result = propertyService.getPropertyById("PROP001");

        assertTrue(result.isPresent());
        assertEquals("PROP001", result.get().getPropertyId());
    }

    @Test
    void getPropertyById_NotFound() {
        when(propertyRepository.findByPropertyIdAndActiveStatus("INVALID", 1))
                .thenReturn(Optional.empty());

        Optional<Property> result = propertyService.getPropertyById("INVALID");

        assertFalse(result.isPresent());
    }

    @Test
    void getAllActiveProperties_Success() {
        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.getAllActiveProperties();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ========== UPDATE PROPERTY TESTS ==========

    @Test
    void updateProperty_Success() {
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        Property result = propertyService.updateProperty(testProperty);

        assertNotNull(result);
        verify(propertyRepository).save(testProperty);
    }

    // ========== DELETE PROPERTY TESTS ==========

    @Test
    void deleteProperty_Success() {
        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        propertyService.deleteProperty("PROP001");

        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void deleteProperty_NotFound() {
        when(propertyRepository.findById("INVALID")).thenReturn(Optional.empty());

        propertyService.deleteProperty("INVALID");

        verify(propertyRepository, never()).save(any(Property.class));
    }

    // ========== GET PROPERTIES BY OWNER TESTS ==========

    @Test
    void getPropertiesByOwner_Success() {
        when(propertyRepository.findByOwnerIdAndActiveStatus(ownerId, 1))
                .thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.getPropertiesByOwner(ownerId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ========== SEARCH PROPERTIES TESTS ==========

    @Test
    void searchProperties_ByName() {
        when(propertyRepository.findByPropertyNameContainingIgnoreCaseAndActiveStatus("Test", 1))
                .thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.searchProperties("Test", null, null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void searchProperties_ByType() {
        when(propertyRepository.findByTypeAndActiveStatus(1, 1))
                .thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.searchProperties(null, 1, null);

        assertFalse(result.isEmpty());
    }

    @Test
    void searchProperties_ByProvince() {
        when(propertyRepository.findByProvinceAndActiveStatus(1, 1))
                .thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.searchProperties(null, null, 1);

        assertFalse(result.isEmpty());
    }

    @Test
    void searchProperties_NoFilters() {
        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.searchProperties(null, null, null);

        assertFalse(result.isEmpty());
    }

    @Test
    void searchProperties_EmptyName() {
        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));

        List<Property> result = propertyService.searchProperties("", null, null);

        assertFalse(result.isEmpty());
    }

    // ========== UPDATE PROPERTY INCOME TESTS ==========

    @Test
    void updatePropertyIncome_Success() {
        testProperty.setIncome(100000);
        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        propertyService.updatePropertyIncome("PROP001", 50000);

        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void updatePropertyIncome_NotFound() {
        when(propertyRepository.findById("INVALID")).thenReturn(Optional.empty());

        propertyService.updatePropertyIncome("INVALID", 50000);

        verify(propertyRepository, never()).save(any(Property.class));
    }

    // ========== UPDATE PROPERTY WITH ROOM TYPES TESTS ==========

    @Test
    void updatePropertyWithRoomTypes_Success() {
        Map<String, Integer> roomCountMap = new HashMap<>();
        roomCountMap.put("Suite_1", 2);

        RoomType newRoomType = new RoomType();
        newRoomType.setName("Suite");
        newRoomType.setFloor(1);
        newRoomType.setPrice(150000);
        newRoomType.setCapacity(3);

        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001")).thenReturn(Arrays.asList(testRoom));
        when(roomTypeRepository.findByProperty_PropertyIdAndNameAndFloor(anyString(), anyString(), anyInt()))
                .thenReturn(null);
        when(idGenerator.generateRoomTypeId(any(Property.class), anyString(), anyInt())).thenReturn("RT002");
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Property result = propertyService.updatePropertyWithRoomTypes(testProperty, 
                Arrays.asList(newRoomType), roomCountMap);

        assertNotNull(result);
    }

    @Test
    void updatePropertyWithRoomTypes_NotFound() {
        when(propertyRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                propertyService.updatePropertyWithRoomTypes(testProperty, null, null));
    }

    @Test
    void updatePropertyWithRoomTypes_ExistingRoomType() {
        testRoomType.setRoomTypeId("RT001");
        Map<String, Integer> roomCountMap = new HashMap<>();

        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001")).thenReturn(Collections.emptyList());
        when(roomTypeRepository.findById("RT001")).thenReturn(Optional.of(testRoomType));
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);

        Property result = propertyService.updatePropertyWithRoomTypes(testProperty, 
                Arrays.asList(testRoomType), roomCountMap);

        assertNotNull(result);
        verify(roomTypeRepository).save(any(RoomType.class));
    }

    @Test
    void updatePropertyWithRoomTypes_ExistingRoomTypeByNameAndFloor() {
        RoomType newRoomType = new RoomType();
        newRoomType.setName("Suite");
        newRoomType.setFloor(1);
        newRoomType.setPrice(150000);

        Map<String, Integer> roomCountMap = new HashMap<>();
        roomCountMap.put("Suite_1", 1);

        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001")).thenReturn(Collections.emptyList());
        when(roomTypeRepository.findByProperty_PropertyIdAndNameAndFloor("PROP001", "Suite", 1))
                .thenReturn(testRoomType);
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Property result = propertyService.updatePropertyWithRoomTypes(testProperty, 
                Arrays.asList(newRoomType), roomCountMap);

        assertNotNull(result);
    }

    // ========== SOFT DELETE PROPERTY TESTS ==========

    @Test
    void softDeleteProperty_Success() {
        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.hasFutureBookings(anyString(), any(LocalDateTime.class))).thenReturn(false);
        when(roomTypeRepository.findByProperty_PropertyId("PROP001")).thenReturn(Arrays.asList(testRoomType));
        when(roomRepository.findByRoomType_RoomTypeId("RT001")).thenReturn(Arrays.asList(testRoom));
        when(bookingRepository.findByRoom_RoomId("ROOM001")).thenReturn(Arrays.asList(testBooking));
        when(maintenanceRepository.findByRoom_RoomId("ROOM001")).thenReturn(Collections.emptyList());

        propertyService.softDeleteProperty("PROP001");

        verify(propertyRepository).save(any(Property.class));
        verify(roomTypeRepository).save(any(RoomType.class));
        verify(roomRepository).save(any(Room.class));
        verify(bookingRepository).save(any(AccommodationBooking.class));
    }

    @Test
    void softDeleteProperty_NotFound() {
        when(propertyRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                propertyService.softDeleteProperty("INVALID"));
    }

    @Test
    void softDeleteProperty_HasFutureBookings() {
        when(propertyRepository.findById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.hasFutureBookings(anyString(), any(LocalDateTime.class))).thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
                propertyService.softDeleteProperty("PROP001"));
    }

    // ========== GET MONTHLY STATISTICS TESTS ==========

    @Test
    void getMonthlyStatistics_Success() {
        testBooking.setCheckInDate(LocalDateTime.of(2025, 11, 15, 14, 0));
        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));
        when(bookingRepository.findByRoom_RoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(testBooking));

        List<PropertyStatisticsDTO> result = propertyService.getMonthlyStatistics(11, 2025);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("PROP001", result.get(0).getPropertyId());
    }

    @Test
    void getMonthlyStatistics_NoBookings() {
        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));
        when(bookingRepository.findByRoom_RoomType_Property_PropertyId("PROP001"))
                .thenReturn(Collections.emptyList());

        List<PropertyStatisticsDTO> result = propertyService.getMonthlyStatistics(11, 2025);

        assertTrue(result.isEmpty());
    }

    @Test
    void getMonthlyStatistics_WrongMonth() {
        testBooking.setCheckInDate(LocalDateTime.of(2025, 10, 15, 14, 0)); // October
        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));
        when(bookingRepository.findByRoom_RoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(testBooking));

        List<PropertyStatisticsDTO> result = propertyService.getMonthlyStatistics(11, 2025); // November

        assertTrue(result.isEmpty());
    }

    @Test
    void getMonthlyStatistics_UnconfirmedBooking() {
        testBooking.setStatus(0); // Waiting for payment
        testBooking.setCheckInDate(LocalDateTime.of(2025, 11, 15, 14, 0));
        when(propertyRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testProperty));
        when(bookingRepository.findByRoom_RoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(testBooking));

        List<PropertyStatisticsDTO> result = propertyService.getMonthlyStatistics(11, 2025);

        assertTrue(result.isEmpty());
    }
}
