package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.service.RoomService;
import apap.ti._5.accommodation_2306212083_be.util.PropertyRoomTypeValidator;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyControllerTest {

    @Mock
    private PropertyService propertyService;

    @Mock
    private RoomService roomService;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private PropertyRoomTypeValidator roomTypeValidator;

    @InjectMocks
    private PropertyController propertyController;

    private UserPrincipal ownerUser;
    private UserPrincipal superadminUser;
    private UserPrincipal customerUser;
    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        customerUser = UserPrincipal.builder()
                .userId("11111111-1111-1111-1111-111111111111")
                .username("customer")
                .role("Customer")
                .build();

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
        testProperty.setType(0);
        testProperty.setAddress("Test Address");
        testProperty.setProvince(31);
        testProperty.setDescription("Test Description");
        testProperty.setActiveStatus(1);
        testProperty.setOwnerId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        testProperty.setOwnerName("Test Owner");

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
        testRoom.setRoomType(testRoomType);
    }

    @Test
    void listProperties_AsOwner_ReturnsOwnProperties() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);

            List<Property> properties = Arrays.asList(testProperty);
            when(propertyService.getPropertiesByOwner(any(UUID.class))).thenReturn(properties);
            when(roomService.getRoomsByProperty(anyString())).thenReturn(Arrays.asList(testRoom));

            ResponseEntity<Map<String, Object>> response = propertyController.listProperties(null, null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
            assertNotNull(response.getBody().get("data"));
        }
    }

    @Test
    void listProperties_AsOwner_WithFilters() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);

            List<Property> properties = Arrays.asList(testProperty);
            when(propertyService.getPropertiesByOwner(any(UUID.class))).thenReturn(properties);
            when(roomService.getRoomsByProperty(anyString())).thenReturn(Arrays.asList(testRoom));

            ResponseEntity<Map<String, Object>> response = propertyController.listProperties("Test", 0, 31);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void listProperties_AsSuperadmin_ReturnsAllProperties() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);

            List<Property> properties = Arrays.asList(testProperty);
            when(propertyService.getAllActiveProperties()).thenReturn(properties);
            when(roomService.getRoomsByProperty(anyString())).thenReturn(Arrays.asList(testRoom));

            ResponseEntity<Map<String, Object>> response = propertyController.listProperties(null, null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void listProperties_AsSuperadmin_WithFilters() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);

            List<Property> properties = Arrays.asList(testProperty);
            when(propertyService.searchProperties(anyString(), anyInt(), anyInt())).thenReturn(properties);
            when(roomService.getRoomsByProperty(anyString())).thenReturn(Arrays.asList(testRoom));

            ResponseEntity<Map<String, Object>> response = propertyController.listProperties("Test", 0, 31);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getRoomTypesByProperty_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);

            List<RoomType> roomTypes = Arrays.asList(testRoomType);
            when(roomTypeRepository.findByProperty_PropertyIdAndActiveStatus("prop-001", 1))
                    .thenReturn(roomTypes);

            ResponseEntity<Map<String, Object>> response = propertyController.getRoomTypesByProperty("prop-001");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getRoomTypesByProperty_AsOwner_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
            when(roomTypeRepository.findByProperty_PropertyIdAndActiveStatus("prop-001", 1))
                    .thenReturn(Arrays.asList(testRoomType));

            ResponseEntity<Map<String, Object>> response = propertyController.getRoomTypesByProperty("prop-001");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getRoomTypesByProperty_AsOwner_AccessDenied() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            UserPrincipal otherOwner = UserPrincipal.builder()
                    .userId("44444444-4444-4444-4444-444444444444")
                    .role("Accommodation Owner")
                    .build();
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(otherOwner);

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));

            ResponseEntity<Map<String, Object>> response = propertyController.getRoomTypesByProperty("prop-001");

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getRoomTypesByProperty_NotFound() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);

            when(propertyService.getPropertyById("invalid")).thenReturn(Optional.empty());

            ResponseEntity<Map<String, Object>> response = propertyController.getRoomTypesByProperty("invalid");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void detailProperty_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
            when(roomService.getRoomTypesByProperty("prop-001")).thenReturn(Arrays.asList(testRoomType));
            when(roomService.getRoomsByProperty("prop-001")).thenReturn(Arrays.asList(testRoom));

            ResponseEntity<Map<String, Object>> response = propertyController.detailProperty("prop-001", null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void detailProperty_WithDateFilter() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
            when(roomService.getRoomTypesByProperty("prop-001")).thenReturn(Arrays.asList(testRoomType));
            when(roomService.getAvailableRoomsByPropertyAndDate(anyString(), anyString(), anyString()))
                    .thenReturn(Arrays.asList(testRoom));

            ResponseEntity<Map<String, Object>> response = propertyController.detailProperty(
                    "prop-001", "2025-02-01", "2025-02-05");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void detailProperty_AsOwner_AccessDenied() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            UserPrincipal otherOwner = UserPrincipal.builder()
                    .userId("44444444-4444-4444-4444-444444444444")
                    .role("Accommodation Owner")
                    .build();
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(otherOwner);

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));

            ResponseEntity<Map<String, Object>> response = propertyController.detailProperty("prop-001", null, null);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }
    }

    @Test
    void createProperty_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            Map<String, Object> request = new HashMap<>();
            request.put("propertyName", "New Hotel");
            request.put("type", 0);
            request.put("address", "New Address");
            request.put("province", 31);
            request.put("description", "New Description");

            when(propertyService.createProperty(any(Property.class), anyList(), anyList()))
                    .thenReturn(testProperty);

            ResponseEntity<Map<String, Object>> response = propertyController.createProperty(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void createProperty_AsSuperadmin_WithOwnerId() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(true);

            Map<String, Object> request = new HashMap<>();
            request.put("propertyName", "New Hotel");
            request.put("type", 0);
            request.put("address", "New Address");
            request.put("province", 31);
            request.put("description", "New Description");
            request.put("ownerId", "22222222-2222-2222-2222-222222222222");
            request.put("ownerName", "Test Owner");

            when(propertyService.createProperty(any(Property.class), anyList(), anyList()))
                    .thenReturn(testProperty);

            ResponseEntity<Map<String, Object>> response = propertyController.createProperty(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void createProperty_WithRoomTypes() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            Map<String, Object> roomTypeData = new HashMap<>();
            roomTypeData.put("name", "Deluxe Room");
            roomTypeData.put("price", 500000);
            roomTypeData.put("capacity", 2);
            roomTypeData.put("facility", "AC, TV");
            roomTypeData.put("floor", 1);
            roomTypeData.put("description", "Nice room");
            roomTypeData.put("roomCount", 5);

            Map<String, Object> request = new HashMap<>();
            request.put("propertyName", "New Hotel");
            request.put("type", 0);
            request.put("address", "New Address");
            request.put("province", 31);
            request.put("description", "New Description");
            request.put("roomTypes", Arrays.asList(roomTypeData));

            when(propertyService.createProperty(any(Property.class), anyList(), anyList()))
                    .thenReturn(testProperty);

            ResponseEntity<Map<String, Object>> response = propertyController.createProperty(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void createProperty_NotAuthenticated() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(null);

            Map<String, Object> request = new HashMap<>();
            request.put("propertyName", "New Hotel");
            request.put("type", 0);
            request.put("address", "New Address");
            request.put("province", 31);
            request.put("description", "New Description");

            ResponseEntity<Map<String, Object>> response = propertyController.createProperty(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getPropertyForUpdate_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
            when(roomService.getRoomTypesByProperty("prop-001")).thenReturn(Arrays.asList(testRoomType));

            ResponseEntity<Map<String, Object>> response = propertyController.getPropertyForUpdate("prop-001");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getPropertyForUpdate_AccessDenied() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            UserPrincipal otherOwner = UserPrincipal.builder()
                    .userId("44444444-4444-4444-4444-444444444444")
                    .role("Accommodation Owner")
                    .build();
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(otherOwner);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));

            ResponseEntity<Map<String, Object>> response = propertyController.getPropertyForUpdate("prop-001");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void updateProperty_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            Map<String, Object> request = new HashMap<>();
            request.put("propertyId", "prop-001");
            request.put("propertyName", "Updated Hotel");
            request.put("address", "Updated Address");
            request.put("description", "Updated Description");
            request.put("province", 32);

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
            when(propertyService.updatePropertyWithRoomTypes(any(Property.class), anyList(), anyMap()))
                    .thenReturn(testProperty);

            ResponseEntity<Map<String, Object>> response = propertyController.updateProperty(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void updateProperty_WithRoomTypes() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(true);

            Map<String, Object> roomTypeData = new HashMap<>();
            roomTypeData.put("roomTypeId", "rt-001");
            roomTypeData.put("name", "Superior Room");
            roomTypeData.put("price", 600000);
            roomTypeData.put("capacity", 3);
            roomTypeData.put("facility", "AC, TV, WiFi");
            roomTypeData.put("floor", 2);
            roomTypeData.put("description", "Nice room");
            roomTypeData.put("roomCount", 3);

            Map<String, Object> request = new HashMap<>();
            request.put("propertyId", "prop-001");
            request.put("propertyName", "Updated Hotel");
            request.put("address", "Updated Address");
            request.put("description", "Updated Description");
            request.put("roomTypes", Arrays.asList(roomTypeData));

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
            when(propertyService.updatePropertyWithRoomTypes(any(Property.class), anyList(), anyMap()))
                    .thenReturn(testProperty);

            ResponseEntity<Map<String, Object>> response = propertyController.updateProperty(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void deleteProperty_Success() {
        doNothing().when(propertyService).softDeleteProperty("prop-001");

        ResponseEntity<Map<String, Object>> response = propertyController.deleteProperty("prop-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        verify(propertyService).softDeleteProperty("prop-001");
    }

    @Test
    void deleteProperty_Error() {
        doThrow(new RuntimeException("Error")).when(propertyService).softDeleteProperty("prop-001");

        ResponseEntity<Map<String, Object>> response = propertyController.deleteProperty("prop-001");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void addRoomTypes_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            Map<String, Object> roomTypeData = new HashMap<>();
            roomTypeData.put("name", "Family Room");
            roomTypeData.put("price", 800000);
            roomTypeData.put("capacity", 4);
            roomTypeData.put("facility", "AC, TV");
            roomTypeData.put("floor", 2);
            roomTypeData.put("description", "Large room");
            roomTypeData.put("roomCount", 3);

            Map<String, Object> request = new HashMap<>();
            request.put("propertyId", "prop-001");
            request.put("roomTypes", Arrays.asList(roomTypeData));

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
            when(roomTypeValidator.isValidRoomType(anyInt(), anyString())).thenReturn(true);

            ResponseEntity<Map<String, Object>> response = propertyController.addRoomTypes(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void addRoomTypes_InvalidRoomType() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isSuperadmin).thenReturn(false);

            Map<String, Object> roomTypeData = new HashMap<>();
            roomTypeData.put("name", "Invalid Type");
            roomTypeData.put("price", 800000);
            roomTypeData.put("capacity", 4);
            roomTypeData.put("facility", "AC, TV");
            roomTypeData.put("floor", 2);
            roomTypeData.put("description", "Large room");

            Map<String, Object> request = new HashMap<>();
            request.put("propertyId", "prop-001");
            request.put("roomTypes", Arrays.asList(roomTypeData));

            when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
            when(roomTypeValidator.isValidRoomType(anyInt(), anyString())).thenReturn(false);
            when(roomTypeValidator.getPropertyTypeString(anyInt())).thenReturn("Hotel");
            when(roomTypeValidator.getValidRoomTypes(anyInt())).thenReturn(
                    new HashSet<>(Arrays.asList("Single Room", "Double Room")));

            ResponseEntity<Map<String, Object>> response = propertyController.addRoomTypes(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    @Test
    void getValidRoomTypes_Success() {
        when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
        when(roomTypeValidator.getValidRoomTypes(0)).thenReturn(
                new HashSet<>(Arrays.asList("Single Room", "Double Room", "Deluxe Room")));
        when(roomTypeValidator.getPropertyTypeString(0)).thenReturn("Hotel");

        ResponseEntity<Map<String, Object>> response = propertyController.getValidRoomTypes("prop-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertNotNull(response.getBody().get("validRoomTypes"));
    }

    @Test
    void getValidRoomTypes_PropertyNotFound() {
        when(propertyService.getPropertyById("invalid")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = propertyController.getValidRoomTypes("invalid");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }
}
