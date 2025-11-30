package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.MaintenanceDTO;
import apap.ti._5.accommodation_2306212083_be.model.*;
import apap.ti._5.accommodation_2306212083_be.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceImplTest {

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @Mock
    private AccommodationBookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private MaintenanceServiceImpl maintenanceService;

    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;
    private Maintenance testMaintenance;

    @BeforeEach
    void setUp() {
        testProperty = new Property();
        testProperty.setPropertyId("PROP001");
        testProperty.setPropertyName("Test Hotel");
        testProperty.setOwnerId(UUID.randomUUID());

        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("RT001");
        testRoomType.setName("Suite");
        testRoomType.setProperty(testProperty);

        testRoom = new Room();
        testRoom.setRoomId("ROOM001");
        testRoom.setName("Room 101");
        testRoom.setRoomType(testRoomType);
        testRoom.setAvailabilityStatus(1);
        testRoom.setActiveRoom(1);

        testMaintenance = new Maintenance();
        testMaintenance.setId(1L);
        testMaintenance.setRoom(testRoom);
        testMaintenance.setStartDate(LocalDate.now().plusDays(1));
        testMaintenance.setStartTime(LocalTime.of(9, 0));
        testMaintenance.setEndDate(LocalDate.now().plusDays(3));
        testMaintenance.setEndTime(LocalTime.of(17, 0));
        testMaintenance.setActiveStatus(1);
    }

    // ========== ADD MAINTENANCE TESTS ==========

    @Test
    void addMaintenance_Success() {
        when(maintenanceRepository.existsOverlappingMaintenance(anyString(), any(), any(), any(), any()))
                .thenReturn(false);
        when(bookingRepository.existsBookingConflict(anyString(), any(), any()))
                .thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(testMaintenance);

        Maintenance result = maintenanceService.addMaintenance(testMaintenance);

        assertNotNull(result);
        verify(roomRepository).save(any(Room.class));
        verify(maintenanceRepository).save(any(Maintenance.class));
    }

    @Test
    void addMaintenance_OverlappingMaintenance() {
        when(maintenanceRepository.existsOverlappingMaintenance(anyString(), any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
                maintenanceService.addMaintenance(testMaintenance));
    }

    @Test
    void addMaintenance_BookingConflict() {
        when(maintenanceRepository.existsOverlappingMaintenance(anyString(), any(), any(), any(), any()))
                .thenReturn(false);
        when(bookingRepository.existsBookingConflict(anyString(), any(), any()))
                .thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
                maintenanceService.addMaintenance(testMaintenance));
    }

    // ========== GET MAINTENANCE TESTS ==========

    @Test
    void getAllMaintenance_Success() {
        when(maintenanceRepository.findByActiveStatus(1))
                .thenReturn(Arrays.asList(testMaintenance));

        List<MaintenanceDTO> result = maintenanceService.getAllMaintenance();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getMaintenanceByRoomId_Success() {
        when(maintenanceRepository.findByRoom_RoomId("ROOM001"))
                .thenReturn(Arrays.asList(testMaintenance));

        List<MaintenanceDTO> result = maintenanceService.getMaintenanceByRoomId("ROOM001");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getMaintenanceByRoomId_InactiveFiltered() {
        testMaintenance.setActiveStatus(0);
        when(maintenanceRepository.findByRoom_RoomId("ROOM001"))
                .thenReturn(Arrays.asList(testMaintenance));

        List<MaintenanceDTO> result = maintenanceService.getMaintenanceByRoomId("ROOM001");

        assertTrue(result.isEmpty());
    }

    @Test
    void getMaintenanceByRoomTypeId_Success() {
        when(maintenanceRepository.findByRoom_RoomType_RoomTypeId("RT001"))
                .thenReturn(Arrays.asList(testMaintenance));

        List<MaintenanceDTO> result = maintenanceService.getMaintenanceByRoomTypeId("RT001");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ========== VALIDATE MAINTENANCE SCHEDULE TESTS ==========

    @Test
    void validateMaintenanceSchedule_Valid() {
        when(maintenanceRepository.existsOverlappingMaintenance(anyString(), any(), any(), any(), any()))
                .thenReturn(false);
        when(bookingRepository.existsBookingConflict(anyString(), any(), any()))
                .thenReturn(false);

        assertDoesNotThrow(() -> 
                maintenanceService.validateMaintenanceSchedule(
                        "ROOM001",
                        LocalDate.now().plusDays(1),
                        LocalTime.of(9, 0),
                        LocalDate.now().plusDays(2),
                        LocalTime.of(17, 0)));
    }

    @Test
    void validateMaintenanceSchedule_EndBeforeStart() {
        assertThrows(RuntimeException.class, () -> 
                maintenanceService.validateMaintenanceSchedule(
                        "ROOM001",
                        LocalDate.now().plusDays(2),
                        LocalTime.of(9, 0),
                        LocalDate.now().plusDays(1), // End before start
                        LocalTime.of(17, 0)));
    }

    @Test
    void validateMaintenanceSchedule_EndTimeBeforeStartTime_SameDay() {
        assertThrows(RuntimeException.class, () -> 
                maintenanceService.validateMaintenanceSchedule(
                        "ROOM001",
                        LocalDate.now().plusDays(1),
                        LocalTime.of(17, 0), // Later start time
                        LocalDate.now().plusDays(1), // Same day
                        LocalTime.of(9, 0))); // Earlier end time
    }

    // ========== HAS MAINTENANCE CONFLICT TESTS ==========

    @Test
    void hasMaintenanceConflict_NoConflict() {
        when(maintenanceRepository.existsOverlappingMaintenance(anyString(), any(), any(), any(), any()))
                .thenReturn(false);

        boolean result = maintenanceService.hasMaintenanceConflict(
                "ROOM001",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(7));

        assertFalse(result);
    }

    @Test
    void hasMaintenanceConflict_HasConflict() {
        when(maintenanceRepository.existsOverlappingMaintenance(anyString(), any(), any(), any(), any()))
                .thenReturn(true);

        boolean result = maintenanceService.hasMaintenanceConflict(
                "ROOM001",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3));

        assertTrue(result);
    }
}
