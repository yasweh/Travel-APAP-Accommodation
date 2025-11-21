package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.MaintenanceDTO;
import apap.ti._5.accommodation_2306212083_be.model.*;
import apap.ti._5.accommodation_2306212083_be.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MaintenanceServiceTestImpl {

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @Mock
    private AccommodationBookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private MaintenanceServiceImpl maintenanceService;

    private Room testRoom;
    private RoomType testRoomType;
    private Property testProperty;
    private Maintenance testMaintenance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testProperty = Property.builder()
                .propertyId("PROP001")
                .propertyName("Test Hotel")
                .build();

        testRoomType = RoomType.builder()
                .roomTypeId("RT001")
                .name("Deluxe")
                .property(testProperty)
                .build();

        testRoom = Room.builder()
                .roomId("ROOM001")
                .name("101")
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(testRoomType)
                .build();

        testMaintenance = Maintenance.builder()
                .id(1L)
                .room(testRoom)
                .startDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endDate(LocalDate.now().plusDays(2))
                .endTime(LocalTime.of(17, 0))
                .activeStatus(1)
                .build();
    }

    @Test
    void testAddMaintenance() {
        when(maintenanceRepository.existsOverlappingMaintenance(
                anyString(), any(LocalDate.class), any(LocalTime.class), 
                any(LocalDate.class), any(LocalTime.class))).thenReturn(false);
        when(bookingRepository.existsBookingConflict(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(testMaintenance);

        Maintenance result = maintenanceService.addMaintenance(testMaintenance);

        assertNotNull(result);
        verify(maintenanceRepository, times(1)).save(any(Maintenance.class));
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testGetAllMaintenance() {
        when(maintenanceRepository.findByActiveStatus(1)).thenReturn(Arrays.asList(testMaintenance));

        List<MaintenanceDTO> result = maintenanceService.getAllMaintenance();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ROOM001", result.get(0).getRoomId());
        verify(maintenanceRepository, times(1)).findByActiveStatus(1);
    }

    @Test
    void testGetMaintenanceByRoomId() {
        when(maintenanceRepository.findByRoom_RoomId("ROOM001")).thenReturn(Arrays.asList(testMaintenance));

        List<MaintenanceDTO> result = maintenanceService.getMaintenanceByRoomId("ROOM001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(maintenanceRepository, times(1)).findByRoom_RoomId("ROOM001");
    }

    @Test
    void testGetMaintenanceByRoomTypeId() {
        when(maintenanceRepository.findByRoom_RoomType_RoomTypeId("RT001")).thenReturn(Arrays.asList(testMaintenance));

        List<MaintenanceDTO> result = maintenanceService.getMaintenanceByRoomTypeId("RT001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(maintenanceRepository, times(1)).findByRoom_RoomType_RoomTypeId("RT001");
    }

    @Test
    void testHasMaintenanceConflict_NoConflict() {
        when(maintenanceRepository.existsOverlappingMaintenance(
                anyString(), any(LocalDate.class), any(LocalTime.class), 
                any(LocalDate.class), any(LocalTime.class))).thenReturn(false);

        boolean result = maintenanceService.hasMaintenanceConflict(
                "ROOM001",
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(12)
        );

        assertFalse(result);
        verify(maintenanceRepository, times(1)).existsOverlappingMaintenance(
                anyString(), any(LocalDate.class), any(LocalTime.class), 
                any(LocalDate.class), any(LocalTime.class));
    }

    @Test
    void testHasMaintenanceConflict_WithConflict() {
        when(maintenanceRepository.existsOverlappingMaintenance(
                anyString(), any(LocalDate.class), any(LocalTime.class), 
                any(LocalDate.class), any(LocalTime.class))).thenReturn(true);

        LocalDateTime checkIn = LocalDateTime.of(testMaintenance.getStartDate(), LocalTime.of(8, 0));
        LocalDateTime checkOut = LocalDateTime.of(testMaintenance.getEndDate(), LocalTime.of(18, 0));

        boolean result = maintenanceService.hasMaintenanceConflict("ROOM001", checkIn, checkOut);

        assertTrue(result);
        verify(maintenanceRepository, times(1)).existsOverlappingMaintenance(
                anyString(), any(LocalDate.class), any(LocalTime.class), 
                any(LocalDate.class), any(LocalTime.class));
    }

    @Test
    void testValidateMaintenanceSchedule_Valid() {
        when(maintenanceRepository.existsOverlappingMaintenance(
                anyString(), any(LocalDate.class), any(LocalTime.class), 
                any(LocalDate.class), any(LocalTime.class))).thenReturn(false);
        when(bookingRepository.existsBookingConflict(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(false);

        assertDoesNotThrow(() -> maintenanceService.validateMaintenanceSchedule(
                "ROOM001",
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalDate.now().plusDays(2),
                LocalTime.of(17, 0)
        ));

        verify(maintenanceRepository, times(1)).existsOverlappingMaintenance(
                anyString(), any(LocalDate.class), any(LocalTime.class), 
                any(LocalDate.class), any(LocalTime.class));
    }

    @Test
    void testValidateMaintenanceSchedule_OverlapThrowsException() {
        when(maintenanceRepository.existsOverlappingMaintenance(
                anyString(), any(LocalDate.class), any(LocalTime.class), 
                any(LocalDate.class), any(LocalTime.class))).thenReturn(true);

        assertThrows(RuntimeException.class, () -> maintenanceService.validateMaintenanceSchedule(
                "ROOM001",
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalDate.now().plusDays(2),
                LocalTime.of(17, 0)
        ));
    }
}
