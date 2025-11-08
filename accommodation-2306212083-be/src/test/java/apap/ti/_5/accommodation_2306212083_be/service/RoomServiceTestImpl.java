package apap.ti._5.accommodation_2306212083_be.service;

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

class RoomServiceTestImpl {

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

    private Room testRoom;
    private RoomType testRoomType;
    private Property testProperty;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testProperty = Property.builder()
                .propertyId("PROP001")
                .propertyName("Test Hotel")
                .totalRoom(0)
                .build();

        testRoomType = RoomType.builder()
                .roomTypeId("RT001")
                .name("Deluxe")
                .price(500000)
                .capacity(2)
                .floor(1)
                .property(testProperty)
                .build();

        testRoom = Room.builder()
                .roomId("ROOM001")
                .name("101")
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(testRoomType)
                .build();
    }

    @Test
    void testGetRoomById() {
        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));

        Optional<Room> result = roomService.getRoomById("ROOM001");

        assertTrue(result.isPresent());
        assertEquals("ROOM001", result.get().getRoomId());
        verify(roomRepository, times(1)).findById("ROOM001");
    }

    @Test
    void testGetRoomsByProperty() {
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001")).thenReturn(Arrays.asList(testRoom));

        List<Room> result = roomService.getRoomsByProperty("PROP001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roomRepository, times(1)).findByRoomType_Property_PropertyId("PROP001");
    }

    @Test
    void testGetAvailableRooms() {
        when(roomRepository.findByAvailabilityStatusAndActiveRoom(1, 1)).thenReturn(Arrays.asList(testRoom));

        List<Room> result = roomService.getAvailableRooms();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roomRepository, times(1)).findByAvailabilityStatusAndActiveRoom(1, 1);
    }

    @Test
    void testGetRoomTypeById() {
        when(roomTypeRepository.findById("RT001")).thenReturn(Optional.of(testRoomType));

        Optional<RoomType> result = roomService.getRoomTypeById("RT001");

        assertTrue(result.isPresent());
        assertEquals("RT001", result.get().getRoomTypeId());
        verify(roomTypeRepository, times(1)).findById("RT001");
    }

    @Test
    void testGetRoomTypesByProperty() {
        when(roomTypeRepository.findByProperty_PropertyIdOrderByFloorAsc("PROP001")).thenReturn(Arrays.asList(testRoomType));

        List<RoomType> result = roomService.getRoomTypesByProperty("PROP001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roomTypeRepository, times(1)).findByProperty_PropertyIdOrderByFloorAsc("PROP001");
    }

    @Test
    void testCreateRoomType() {
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);

        RoomType result = roomService.createRoomType(testRoomType);

        assertNotNull(result);
        assertEquals("RT001", result.getRoomTypeId());
        verify(roomTypeRepository, times(1)).save(any(RoomType.class));
    }

    @Test
    void testSetRoomMaintenance() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        when(roomRepository.findById("ROOM001")).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Room result = roomService.setRoomMaintenance("ROOM001", start, end);

        assertNotNull(result);
        verify(roomRepository, times(1)).findById("ROOM001");
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testGetAvailableRoomsByPropertyAndDate() {
        when(roomRepository.findByRoomType_Property_PropertyId("PROP001")).thenReturn(Arrays.asList(testRoom));
        when(bookingRepository.existsBookingConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        List<Room> result = roomService.getAvailableRoomsByPropertyAndDate("PROP001", "2025-11-10", "2025-11-12");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roomRepository, times(1)).findByRoomType_Property_PropertyId("PROP001");
        verify(bookingRepository, times(1)).existsBookingConflict(anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testAddRoomType() {
        when(idGenerator.generateRoomTypeId(any(Property.class), anyString(), any(Integer.class))).thenReturn("RT002");
        when(idGenerator.generateRoomId(anyString(), any(Integer.class), any(Integer.class))).thenReturn("ROOM002");
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(testRoomType);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        roomService.addRoomType(testProperty, testRoomType, 3);

        verify(roomTypeRepository, times(1)).save(any(RoomType.class));
        verify(roomRepository, times(3)).save(any(Room.class));
    }
}
