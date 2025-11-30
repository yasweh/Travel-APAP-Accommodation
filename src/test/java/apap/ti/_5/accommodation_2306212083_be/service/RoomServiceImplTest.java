package apap.ti._5.accommodation_2306212083_be.service;

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
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private AccommodationBookingRepository bookingRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testProperty = new Property();
        testProperty.setPropertyId("PROP001");
        testProperty.setPropertyName("Test Hotel");
        testProperty.setType(1);
        testProperty.setOwnerId(UUID.randomUUID());
        testProperty.setTotalRoom(0);

        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("RT001");
        testRoomType.setName("Suite");
        testRoomType.setPrice(100000);
        testRoomType.setCapacity(2);
        testRoomType.setFloor(1);
        testRoomType.setProperty(testProperty);
        testRoomType.setActiveStatus(1);

        testRoom = new Room();
        testRoom.setRoomId("ROOM001");
        testRoom.setName("Room 101");
        testRoom.setRoomType(testRoomType);
        testRoom.setAvailabilityStatus(1);
        testRoom.setActiveRoom(1);
    }

    // ========== GET ROOM TESTS ==========

    @Test
    void getRoomById_Success() {
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));

        Optional<Room> result = roomService.getRoomById("ROOM001");

        assertTrue(result.isPresent());
        assertEquals("ROOM001", result.get().getRoomId());
    }

    @Test
    void getRoomById_NotFound() {
        when(roomRepository.findById("INVALID")).thenReturn(Optional.empty());

        Optional<Room> result = roomService.getRoomById("INVALID");

        assertFalse(result.isPresent());
    }

    @Test
    void getRoomsByProperty_Success() {
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(testRoom));

        List<Room> result = roomService.getRoomsByProperty("PROP001");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getAvailableRooms_Success() {
        when(roomRepository.findByAvailabilityStatusAndActiveRoom(1, 1))
                .thenReturn(Arrays.asList(testRoom));

        List<Room> result = roomService.getAvailableRooms();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ========== GET AVAILABLE ROOMS BY PROPERTY AND DATE TESTS ==========

    @Test
    void getAvailableRoomsByPropertyAndDate_Available() {
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(testRoom));
        when(bookingRepository.existsBookingConflict(anyString(), any(), any()))
                .thenReturn(false);

        List<Room> result = roomService.getAvailableRoomsByPropertyAndDate(
                "PROP001", "2025-12-01", "2025-12-05");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getAvailableRoomsByPropertyAndDate_HasConflict() {
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(testRoom));
        when(bookingRepository.existsBookingConflict(anyString(), any(), any()))
                .thenReturn(true);

        List<Room> result = roomService.getAvailableRoomsByPropertyAndDate(
                "PROP001", "2025-12-01", "2025-12-05");

        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableRoomsByPropertyAndDate_RoomNotActive() {
        testRoom.setActiveRoom(0);
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(testRoom));

        List<Room> result = roomService.getAvailableRoomsByPropertyAndDate(
                "PROP001", "2025-12-01", "2025-12-05");

        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableRoomsByPropertyAndDate_RoomNotAvailable() {
        testRoom.setAvailabilityStatus(0);
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001"))
                .thenReturn(Arrays.asList(testRoom));

        List<Room> result = roomService.getAvailableRoomsByPropertyAndDate(
                "PROP001", "2025-12-01", "2025-12-05");

        assertTrue(result.isEmpty());
    }

    // ========== SET ROOM MAINTENANCE TESTS ==========

    @Test
    void setRoomMaintenance_Success() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Room result = roomService.setRoomMaintenance("ROOM001", start, end);

        assertNotNull(result);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void setRoomMaintenance_NotFound() {
        when(roomRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                roomService.setRoomMaintenance("INVALID", 
                        LocalDateTime.now(), LocalDateTime.now().plusDays(1)));
    }

    // ========== ROOM TYPE TESTS ==========

    @Test
    void createRoomType_Success() {
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);

        RoomType result = roomService.createRoomType(testRoomType);

        assertNotNull(result);
        assertEquals("RT001", result.getRoomTypeId());
    }

    @Test
    void getRoomTypeById_Success() {
        when(roomTypeRepository.findById("RT001")).thenReturn(Optional.of(testRoomType));

        Optional<RoomType> result = roomService.getRoomTypeById("RT001");

        assertTrue(result.isPresent());
        assertEquals("RT001", result.get().getRoomTypeId());
    }

    @Test
    void getRoomTypeById_NotFound() {
        when(roomTypeRepository.findById("INVALID")).thenReturn(Optional.empty());

        Optional<RoomType> result = roomService.getRoomTypeById("INVALID");

        assertFalse(result.isPresent());
    }

    @Test
    void getRoomTypesByProperty_Success() {
        when(roomTypeRepository.findByProperty_PropertyIdOrderByFloorAsc("PROP001"))
                .thenReturn(Arrays.asList(testRoomType));

        List<RoomType> result = roomService.getRoomTypesByProperty("PROP001");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ========== ADD ROOM TYPE TESTS ==========

    @Test
    void addRoomType_Success() {
        when(idGenerator.generateRoomTypeId(any(Property.class), anyString(), anyInt()))
                .thenReturn("RT002");
        when(idGenerator.generateRoomId(anyString(), anyInt(), anyInt()))
                .thenReturn("ROOM002");
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        roomService.addRoomType(testProperty, testRoomType, 2);

        verify(roomTypeRepository).save(any(RoomType.class));
        verify(roomRepository, times(2)).save(any(Room.class));
        assertEquals(2, testProperty.getTotalRoom());
    }

    @Test
    void addRoomType_WithExistingRooms() {
        testProperty.setTotalRoom(5);
        when(idGenerator.generateRoomTypeId(any(Property.class), anyString(), anyInt()))
                .thenReturn("RT002");
        when(idGenerator.generateRoomId(anyString(), anyInt(), anyInt()))
                .thenReturn("ROOM002");
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        roomService.addRoomType(testProperty, testRoomType, 3);

        assertEquals(8, testProperty.getTotalRoom());
    }

    @Test
    void addRoomType_NullTotalRoom() {
        testProperty.setTotalRoom(null);
        when(idGenerator.generateRoomTypeId(any(Property.class), anyString(), anyInt()))
                .thenReturn("RT002");
        when(idGenerator.generateRoomId(anyString(), anyInt(), anyInt()))
                .thenReturn("ROOM002");
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        roomService.addRoomType(testProperty, testRoomType, 2);

        assertEquals(2, testProperty.getTotalRoom());
    }
}
