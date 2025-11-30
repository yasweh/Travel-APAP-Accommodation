package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.MaintenanceDTO;
import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.model.Maintenance;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306212083_be.service.MaintenanceService;
import apap.ti._5.accommodation_2306212083_be.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceControllerTest {

    @Mock
    private MaintenanceService maintenanceService;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private MaintenanceController maintenanceController;

    private UserPrincipal ownerUser;
    private UserPrincipal superadminUser;
    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;
    private MaintenanceDTO testMaintenanceDTO;

    @BeforeEach
    void setUp() {
        ownerUser = UserPrincipal.builder()
                .userId("22222222-2222-2222-2222-222222222222")
                .username("owner")
                .role("Accommodation Owner")
                .build();

        superadminUser = UserPrincipal.builder()
                .userId("33333333-3333-3333-3333-333333333333")
                .username("admin")
                .role("Superadmin")
                .build();

        testProperty = new Property();
        testProperty.setPropertyId("prop-001");
        testProperty.setPropertyName("Test Hotel");
        testProperty.setOwnerId(UUID.fromString("22222222-2222-2222-2222-222222222222"));

        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("rt-001");
        testRoomType.setProperty(testProperty);

        testRoom = new Room();
        testRoom.setRoomId("room-001");
        testRoom.setName("Room 101");
        testRoom.setRoomType(testRoomType);

        testMaintenanceDTO = MaintenanceDTO.builder()
                .maintenanceId(1L)
                .roomId("room-001")
                .roomName("Room 101")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build();
    }

    @Test
    void addMaintenance_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            Map<String, Object> request = new HashMap<>();
            request.put("roomId", "room-001");
            request.put("startDate", "2025-02-01");
            request.put("startTime", "10:00");
            request.put("endDate", "2025-02-02");
            request.put("endTime", "15:00");

            when(roomRepository.findById("room-001")).thenReturn(Optional.of(testRoom));
            when(maintenanceService.addMaintenance(any(Maintenance.class))).thenReturn(new Maintenance());

            ResponseEntity<Map<String, Object>> response = maintenanceController.addMaintenance(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void addMaintenance_AsSuperadmin_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(true);

            Map<String, Object> request = new HashMap<>();
            request.put("roomId", "room-001");
            request.put("startDate", "2025-02-01");
            request.put("startTime", "10:00");
            request.put("endDate", "2025-02-02");
            request.put("endTime", "15:00");

            when(roomRepository.findById("room-001")).thenReturn(Optional.of(testRoom));
            when(maintenanceService.addMaintenance(any(Maintenance.class))).thenReturn(new Maintenance());

            ResponseEntity<Map<String, Object>> response = maintenanceController.addMaintenance(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void addMaintenance_RoomNotFound() {
        Map<String, Object> request = new HashMap<>();
        request.put("roomId", "invalid");
        request.put("startDate", "2025-02-01");
        request.put("startTime", "10:00");
        request.put("endDate", "2025-02-02");
        request.put("endTime", "15:00");

        when(roomRepository.findById("invalid")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = maintenanceController.addMaintenance(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void addMaintenance_AccessDenied() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            UserPrincipal otherOwner = UserPrincipal.builder()
                    .userId("44444444-4444-4444-4444-444444444444")
                    .role("Accommodation Owner")
                    .build();
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(otherOwner);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            Map<String, Object> request = new HashMap<>();
            request.put("roomId", "room-001");
            request.put("startDate", "2025-02-01");
            request.put("startTime", "10:00");
            request.put("endDate", "2025-02-02");
            request.put("endTime", "15:00");

            when(roomRepository.findById("room-001")).thenReturn(Optional.of(testRoom));

            ResponseEntity<Map<String, Object>> response = maintenanceController.addMaintenance(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getAllMaintenance_AsSuperadmin_ReturnsAll() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<MaintenanceDTO> maintenanceList = Arrays.asList(testMaintenanceDTO);
            when(maintenanceService.getAllMaintenance()).thenReturn(maintenanceList);

            ResponseEntity<Map<String, Object>> response = maintenanceController.getAllMaintenance();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getAllMaintenance_AsOwner_ReturnsFiltered() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(true);

            List<MaintenanceDTO> maintenanceList = new ArrayList<>();
            maintenanceList.add(testMaintenanceDTO);
            when(maintenanceService.getAllMaintenance()).thenReturn(maintenanceList);
            when(roomRepository.findById("room-001")).thenReturn(Optional.of(testRoom));

            ResponseEntity<Map<String, Object>> response = maintenanceController.getAllMaintenance();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getAllMaintenance_Error() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            when(maintenanceService.getAllMaintenance()).thenThrow(new RuntimeException("Error"));

            ResponseEntity<Map<String, Object>> response = maintenanceController.getAllMaintenance();

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getMaintenanceByRoomType_Success() {
        List<MaintenanceDTO> maintenanceList = Arrays.asList(testMaintenanceDTO);
        when(maintenanceService.getMaintenanceByRoomTypeId("rt-001")).thenReturn(maintenanceList);

        ResponseEntity<Map<String, Object>> response = maintenanceController.getMaintenanceByRoomType("rt-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void getMaintenanceByRoomType_Error() {
        when(maintenanceService.getMaintenanceByRoomTypeId("invalid"))
                .thenThrow(new RuntimeException("Error"));

        ResponseEntity<Map<String, Object>> response = maintenanceController.getMaintenanceByRoomType("invalid");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void getMaintenanceByRoom_AsSuperadmin_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(true);

            when(roomRepository.findById("room-001")).thenReturn(Optional.of(testRoom));
            when(maintenanceService.getMaintenanceByRoomId("room-001"))
                    .thenReturn(Arrays.asList(testMaintenanceDTO));

            ResponseEntity<Map<String, Object>> response = maintenanceController.getMaintenanceByRoom("room-001");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getMaintenanceByRoom_AsOwner_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            when(roomRepository.findById("room-001")).thenReturn(Optional.of(testRoom));
            when(maintenanceService.getMaintenanceByRoomId("room-001"))
                    .thenReturn(Arrays.asList(testMaintenanceDTO));

            ResponseEntity<Map<String, Object>> response = maintenanceController.getMaintenanceByRoom("room-001");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getMaintenanceByRoom_AsOwner_AccessDenied() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            UserPrincipal otherOwner = UserPrincipal.builder()
                    .userId("44444444-4444-4444-4444-444444444444")
                    .role("Accommodation Owner")
                    .build();
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(otherOwner);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            when(roomRepository.findById("room-001")).thenReturn(Optional.of(testRoom));

            ResponseEntity<Map<String, Object>> response = maintenanceController.getMaintenanceByRoom("room-001");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getMaintenanceByRoom_RoomNotFound() {
        when(roomRepository.findById("invalid")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = maintenanceController.getMaintenanceByRoom("invalid");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }
}
