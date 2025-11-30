package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.service.OwnerValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomTypeControllerTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private PropertyService propertyService;

    @Mock
    private OwnerValidationService ownerValidationService;

    @InjectMocks
    private RoomTypeController roomTypeController;

    private UserPrincipal ownerUser;
    private UserPrincipal superadminUser;
    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;

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
        testRoomType.setName("Deluxe Room");
        testRoomType.setPrice(500000);
        testRoomType.setCapacity(2);
        testRoomType.setFloor(1);
        testRoomType.setProperty(testProperty);
        testRoomType.setActiveStatus(1);

        testRoom = new Room();
        testRoom.setRoomId("room-001");
        testRoom.setName("Room 101");
        testRoom.setAvailabilityStatus(1);
        testRoom.setActiveRoom(1);
        testRoom.setRoomType(testRoomType);

        List<Room> rooms = new ArrayList<>();
        rooms.add(testRoom);
        testRoomType.setListRoom(rooms);
    }

    @Test
    void getAllRoomTypes_Success() {
        List<RoomType> roomTypes = Arrays.asList(testRoomType);
        when(roomTypeRepository.findByActiveStatus(1)).thenReturn(roomTypes);

        ResponseEntity<Map<String, Object>> response = roomTypeController.getAllRoomTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertNotNull(response.getBody().get("data"));
    }

    @Test
    void getAllRoomTypes_Error() {
        when(roomTypeRepository.findByActiveStatus(1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Map<String, Object>> response = roomTypeController.getAllRoomTypes();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void getRoomTypeById_Success_AsAnonymous() {
        // Setup mock for unauthenticated user - simply call the method
        when(roomTypeRepository.findById("rt-001")).thenReturn(Optional.of(testRoomType));

        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypeById("rt-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertNotNull(response.getBody().get("data"));
    }

    @Test
    void getRoomTypeById_Success_AsOwner() {
        // Setup mock for authenticated owner
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(ownerUser);
        
        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(roomTypeRepository.findById("rt-001")).thenReturn(Optional.of(testRoomType));
            when(ownerValidationService.isOwner()).thenReturn(true);
            when(ownerValidationService.isSuperadmin()).thenReturn(false);
            doNothing().when(ownerValidationService).validateRoomTypeOwnership(any(RoomType.class));

            ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypeById("rt-001");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getRoomTypeById_NotFound() {
        when(roomTypeRepository.findById("invalid")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypeById("invalid");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void getRoomTypes_ForProperty_Success() {
        when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
        when(roomTypeRepository.findByProperty_PropertyIdAndActiveStatus("prop-001", 1))
                .thenReturn(Arrays.asList(testRoomType));

        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypes("prop-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertNotNull(response.getBody().get("property"));
        assertNotNull(response.getBody().get("roomTypes"));
    }

    @Test
    void getRoomTypes_PropertyNotFound() {
        when(propertyService.getPropertyById("invalid")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypes("invalid");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }
}
