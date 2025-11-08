package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class RoomTypeControllerTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private PropertyService propertyService;

    @InjectMocks
    private RoomTypeController roomTypeController;

    private RoomType testRoomType1;
    private RoomType testRoomType2;
    private Property testProperty;
    private Room testRoom1;
    private Room testRoom2;

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

        testRoom1 = Room.builder()
                .roomId("ROOM001")
                .name("101")
                .availabilityStatus(1)
                .activeRoom(1)
                .maintenanceStart(null)
                .maintenanceEnd(null)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        testRoom2 = Room.builder()
                .roomId("ROOM002")
                .name("102")
                .availabilityStatus(1)
                .activeRoom(1)
                .maintenanceStart(null)
                .maintenanceEnd(null)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        testRoomType1 = RoomType.builder()
                .roomTypeId("RT001")
                .name("Deluxe")
                .price(500000)
                .description("Deluxe room with city view")
                .capacity(2)
                .facility("AC, TV, WiFi")
                .floor(1)
                .property(testProperty)
                .activeStatus(1)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .listRoom(Arrays.asList(testRoom1, testRoom2))
                .build();

        testRoomType2 = RoomType.builder()
                .roomTypeId("RT002")
                .name("Suite")
                .price(1000000)
                .description("Luxury suite with ocean view")
                .capacity(4)
                .facility("AC, TV, WiFi, Balcony")
                .floor(2)
                .property(testProperty)
                .activeStatus(1)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .listRoom(new ArrayList<>())
                .build();

        // Set bidirectional relationship
        testRoom1.setRoomType(testRoomType1);
        testRoom2.setRoomType(testRoomType1);
    }

    @Test
    void testGetAllRoomTypes_Success() {
        // Arrange
        List<RoomType> roomTypes = Arrays.asList(testRoomType1, testRoomType2);
        when(roomTypeRepository.findByActiveStatus(1)).thenReturn(roomTypes);

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getAllRoomTypes();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        
        @SuppressWarnings("unchecked")
        List<RoomType> data = (List<RoomType>) response.getBody().get("data");
        assertEquals(2, data.size());
        assertEquals("RT001", data.get(0).getRoomTypeId());
        assertEquals("RT002", data.get(1).getRoomTypeId());
        
        verify(roomTypeRepository, times(1)).findByActiveStatus(1);
    }

    @Test
    void testGetAllRoomTypes_EmptyList() {
        // Arrange
        when(roomTypeRepository.findByActiveStatus(1)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getAllRoomTypes();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        
        @SuppressWarnings("unchecked")
        List<RoomType> data = (List<RoomType>) response.getBody().get("data");
        assertEquals(0, data.size());
        
        verify(roomTypeRepository, times(1)).findByActiveStatus(1);
    }

    @Test
    void testGetAllRoomTypes_Exception() {
        // Arrange
        when(roomTypeRepository.findByActiveStatus(1))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getAllRoomTypes();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Database connection error", response.getBody().get("message"));
        
        verify(roomTypeRepository, times(1)).findByActiveStatus(1);
    }

    @Test
    void testGetRoomTypeById_Success() {
        // Arrange
        when(roomTypeRepository.findById("RT001")).thenReturn(Optional.of(testRoomType1));

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypeById("RT001");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertEquals("RT001", data.get("roomTypeId"));
        assertEquals("Deluxe", data.get("name"));
        assertEquals(500000, data.get("price"));
        assertEquals("Deluxe room with city view", data.get("description"));
        assertEquals(2, data.get("capacity"));
        assertEquals("AC, TV, WiFi", data.get("facility"));
        assertEquals(1, data.get("floor"));
        assertEquals(1, data.get("activeStatus"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("listRoom");
        assertEquals(2, rooms.size());
        assertEquals("ROOM001", rooms.get(0).get("roomId"));
        assertEquals("101", rooms.get(0).get("name"));
        assertEquals(1, rooms.get(0).get("availabilityStatus"));
        assertEquals(1, rooms.get(0).get("activeRoom"));
        
        verify(roomTypeRepository, times(1)).findById("RT001");
    }

    @Test
    void testGetRoomTypeById_WithEmptyRoomList() {
        // Arrange
        when(roomTypeRepository.findById("RT002")).thenReturn(Optional.of(testRoomType2));

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypeById("RT002");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("listRoom");
        assertEquals(0, rooms.size());
        
        verify(roomTypeRepository, times(1)).findById("RT002");
    }

    @Test
    void testGetRoomTypeById_NotFound() {
        // Arrange
        when(roomTypeRepository.findById("RT999")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypeById("RT999");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Room type not found", response.getBody().get("message"));
        
        verify(roomTypeRepository, times(1)).findById("RT999");
    }

    @Test
    void testGetRoomTypeById_Exception() {
        // Arrange
        when(roomTypeRepository.findById("RT001"))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypeById("RT001");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Database error", response.getBody().get("message"));
        
        verify(roomTypeRepository, times(1)).findById("RT001");
    }

    @Test
    void testGetRoomTypes_Success() {
        // Arrange
        List<RoomType> roomTypes = Arrays.asList(testRoomType1, testRoomType2);
        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        when(roomTypeRepository.findByProperty_PropertyIdAndActiveStatus("PROP001", 1))
                .thenReturn(roomTypes);

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypes("PROP001");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        
        Property property = (Property) response.getBody().get("property");
        assertEquals("PROP001", property.getPropertyId());
        assertEquals("Test Hotel", property.getPropertyName());
        
        @SuppressWarnings("unchecked")
        List<RoomType> returnedRoomTypes = (List<RoomType>) response.getBody().get("roomTypes");
        assertEquals(2, returnedRoomTypes.size());
        assertEquals("RT001", returnedRoomTypes.get(0).getRoomTypeId());
        assertEquals("RT002", returnedRoomTypes.get(1).getRoomTypeId());
        
        verify(propertyService, times(1)).getPropertyById("PROP001");
        verify(roomTypeRepository, times(1)).findByProperty_PropertyIdAndActiveStatus("PROP001", 1);
    }

    @Test
    void testGetRoomTypes_EmptyRoomTypeList() {
        // Arrange
        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        when(roomTypeRepository.findByProperty_PropertyIdAndActiveStatus("PROP001", 1))
                .thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypes("PROP001");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        
        @SuppressWarnings("unchecked")
        List<RoomType> returnedRoomTypes = (List<RoomType>) response.getBody().get("roomTypes");
        assertEquals(0, returnedRoomTypes.size());
        
        verify(propertyService, times(1)).getPropertyById("PROP001");
        verify(roomTypeRepository, times(1)).findByProperty_PropertyIdAndActiveStatus("PROP001", 1);
    }

    @Test
    void testGetRoomTypes_PropertyNotFound() {
        // Arrange
        when(propertyService.getPropertyById("PROP999")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypes("PROP999");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Property not found", response.getBody().get("message"));
        
        verify(propertyService, times(1)).getPropertyById("PROP999");
        verify(roomTypeRepository, never()).findByProperty_PropertyIdAndActiveStatus(anyString(), anyInt());
    }

    @Test
    void testGetRoomTypes_Exception() {
        // Arrange
        when(propertyService.getPropertyById("PROP001"))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypes("PROP001");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Service unavailable", response.getBody().get("message"));
        
        verify(propertyService, times(1)).getPropertyById("PROP001");
        verify(roomTypeRepository, never()).findByProperty_PropertyIdAndActiveStatus(anyString(), anyInt());
    }

    @Test
    void testGetRoomTypeById_WithMaintenanceDates() {
        // Arrange
        LocalDateTime maintenanceStart = LocalDateTime.now().minusDays(1);
        LocalDateTime maintenanceEnd = LocalDateTime.now().plusDays(1);
        
        testRoom1.setMaintenanceStart(maintenanceStart);
        testRoom1.setMaintenanceEnd(maintenanceEnd);
        
        when(roomTypeRepository.findById("RT001")).thenReturn(Optional.of(testRoomType1));

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypeById("RT001");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("listRoom");
        assertEquals(maintenanceStart, rooms.get(0).get("maintenanceStart"));
        assertEquals(maintenanceEnd, rooms.get(0).get("maintenanceEnd"));
        
        verify(roomTypeRepository, times(1)).findById("RT001");
    }

    @Test
    void testGetRoomTypeById_VerifyAllRoomFields() {
        // Arrange
        when(roomTypeRepository.findById("RT001")).thenReturn(Optional.of(testRoomType1));

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypeById("RT001");

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("listRoom");
        Map<String, Object> room = rooms.get(0);
        
        assertTrue(room.containsKey("roomId"));
        assertTrue(room.containsKey("name"));
        assertTrue(room.containsKey("availabilityStatus"));
        assertTrue(room.containsKey("activeRoom"));
        assertTrue(room.containsKey("maintenanceStart"));
        assertTrue(room.containsKey("maintenanceEnd"));
        assertTrue(room.containsKey("createdDate"));
        assertTrue(room.containsKey("updatedDate"));
        
        verify(roomTypeRepository, times(1)).findById("RT001");
    }

    @Test
    void testGetRoomTypeById_VerifyAllRoomTypeFields() {
        // Arrange
        when(roomTypeRepository.findById("RT001")).thenReturn(Optional.of(testRoomType1));

        // Act
        ResponseEntity<Map<String, Object>> response = roomTypeController.getRoomTypeById("RT001");

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        
        assertTrue(data.containsKey("roomTypeId"));
        assertTrue(data.containsKey("name"));
        assertTrue(data.containsKey("price"));
        assertTrue(data.containsKey("description"));
        assertTrue(data.containsKey("capacity"));
        assertTrue(data.containsKey("facility"));
        assertTrue(data.containsKey("floor"));
        assertTrue(data.containsKey("activeStatus"));
        assertTrue(data.containsKey("createdDate"));
        assertTrue(data.containsKey("updatedDate"));
        assertTrue(data.containsKey("listRoom"));
        
        verify(roomTypeRepository, times(1)).findById("RT001");
    }
}