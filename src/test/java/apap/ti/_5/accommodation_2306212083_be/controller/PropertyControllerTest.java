package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.service.RoomService;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyController.class)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PropertyService propertyService;

    @MockBean
    private RoomService roomService;

    @MockBean
    private RoomTypeRepository roomTypeRepository;

    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testProperty = new Property();
        testProperty.setPropertyId("PROP001");
        testProperty.setPropertyName("Test Hotel");
        testProperty.setType(1);
        testProperty.setAddress("Test Address");
        testProperty.setProvince(1);
        testProperty.setActiveStatus(1);
        testProperty.setTotalRoom(10);
        testProperty.setActiveRoom(10);
        testProperty.setIncome(5000000);
        testProperty.setOwnerName("Test Owner");
        testProperty.setOwnerId(UUID.randomUUID());
        testProperty.setCreatedDate(LocalDateTime.now());
        testProperty.setUpdatedDate(LocalDateTime.now());
        testProperty.setDescription("Test Description");

        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("RT001");
        testRoomType.setName("Deluxe");
        testRoomType.setPrice(500000);
        testRoomType.setCapacity(2);
        testRoomType.setFloor(2);
        testRoomType.setProperty(testProperty);
        testRoomType.setActiveStatus(1);

        testRoom = new Room();
        testRoom.setRoomId("PROP001-ROOM-201");
        testRoom.setName("Room 201");
        testRoom.setRoomType(testRoomType);
        testRoom.setAvailabilityStatus(1);
        testRoom.setActiveRoom(1);
    }

    @Test
    void testListAllProperties_NoParams() throws Exception {
        when(propertyService.getAllActiveProperties()).thenReturn(Arrays.asList(testProperty));
        when(roomService.getRoomsByProperty("PROP001")).thenReturn(Arrays.asList(testRoom));

        mockMvc.perform(get("/api/property"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].propertyId").value("PROP001"))
                .andExpect(jsonPath("$.data[0].roomCount").value(1));

        verify(propertyService, times(1)).getAllActiveProperties();
        verify(roomService, times(1)).getRoomsByProperty("PROP001");
    }

    @Test
    void testListAllProperties_WithNameParam() throws Exception {
        when(propertyService.searchProperties("Test", null, null)).thenReturn(Arrays.asList(testProperty));
        when(roomService.getRoomsByProperty("PROP001")).thenReturn(Arrays.asList(testRoom));

        mockMvc.perform(get("/api/property")
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].propertyName").value("Test Hotel"));

        verify(propertyService, times(1)).searchProperties("Test", null, null);
    }

    @Test
    void testListAllProperties_WithTypeParam() throws Exception {
        when(propertyService.searchProperties(null, 1, null)).thenReturn(Arrays.asList(testProperty));
        when(roomService.getRoomsByProperty("PROP001")).thenReturn(Arrays.asList(testRoom));

        mockMvc.perform(get("/api/property")
                        .param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(propertyService, times(1)).searchProperties(null, 1, null);
    }

    @Test
    void testListAllProperties_WithProvinceParam() throws Exception {
        when(propertyService.searchProperties(null, null, 1)).thenReturn(Arrays.asList(testProperty));
        when(roomService.getRoomsByProperty("PROP001")).thenReturn(Arrays.asList(testRoom));

        mockMvc.perform(get("/api/property")
                        .param("province", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(propertyService, times(1)).searchProperties(null, null, 1);
    }

    @Test
    void testGetRoomTypesByProperty_Success() throws Exception {
        when(roomTypeRepository.findByProperty_PropertyIdAndActiveStatus("PROP001", 1))
                .thenReturn(Arrays.asList(testRoomType));

        mockMvc.perform(get("/api/property/PROP001/room-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].roomTypeId").value("RT001"));

        verify(roomTypeRepository, times(1)).findByProperty_PropertyIdAndActiveStatus("PROP001", 1);
    }

    @Test
    void testGetRoomTypesByProperty_Error() throws Exception {
        when(roomTypeRepository.findByProperty_PropertyIdAndActiveStatus("PROP001", 1))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/property/PROP001/room-types"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Database error"));
    }

    @Test
    void testGetPropertyDetail_NoDateParams() throws Exception {
        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        when(roomService.getRoomTypesByProperty("PROP001")).thenReturn(Arrays.asList(testRoomType));
        when(roomService.getRoomsByProperty("PROP001")).thenReturn(Arrays.asList(testRoom));

        mockMvc.perform(get("/api/property/PROP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.property.propertyId").value("PROP001"));

        verify(propertyService, times(1)).getPropertyById("PROP001");
        verify(roomService, times(1)).getRoomTypesByProperty("PROP001");
        verify(roomService, times(1)).getRoomsByProperty("PROP001");
    }

    @Test
    void testGetPropertyDetail_WithDateParams() throws Exception {
        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        when(roomService.getRoomTypesByProperty("PROP001")).thenReturn(Arrays.asList(testRoomType));
        when(roomService.getAvailableRoomsByPropertyAndDate("PROP001", "2025-12-01", "2025-12-05"))
                .thenReturn(Arrays.asList(testRoom));

        mockMvc.perform(get("/api/property/PROP001")
                        .param("checkIn", "2025-12-01")
                        .param("checkOut", "2025-12-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(roomService, times(1)).getAvailableRoomsByPropertyAndDate("PROP001", "2025-12-01", "2025-12-05");
        verify(roomService, never()).getRoomsByProperty(anyString());
    }

    @Test
    void testGetPropertyDetail_NotFound() throws Exception {
        when(propertyService.getPropertyById("PROP999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/property/PROP999"))
                .andExpect(status().isInternalServerError());

        verify(propertyService, times(1)).getPropertyById("PROP999");
    }

    @Test
    void testCreateProperty_WithRoomTypes() throws Exception {
        Map<String, Object> roomType1 = new HashMap<>();
        roomType1.put("name", "Deluxe");
        roomType1.put("price", 500000);
        roomType1.put("capacity", 2);
        roomType1.put("facility", "AC, TV");
        roomType1.put("floor", 2);
        roomType1.put("description", "Deluxe room");
        roomType1.put("roomCount", 5);

        Map<String, Object> request = new HashMap<>();
        request.put("propertyName", "New Hotel");
        request.put("type", 1);
        request.put("address", "New Address");
        request.put("province", 1);
        request.put("description", "Test Description");
        request.put("ownerName", "Test Owner");
        request.put("ownerId", UUID.randomUUID().toString());
        request.put("roomTypes", Arrays.asList(roomType1));

        when(propertyService.createProperty(any(Property.class), anyList(), anyList()))
                .thenReturn(testProperty);

        mockMvc.perform(post("/api/property/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Property created successfully"));

        verify(propertyService, times(1)).createProperty(any(Property.class), anyList(), anyList());
    }

    @Test
    void testCreateProperty_WithoutRoomTypes() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("propertyName", "New Hotel");
        request.put("type", 1);
        request.put("address", "New Address");
        request.put("province", 1);
        request.put("description", "Test Description");
        request.put("ownerName", "Test Owner");
        request.put("ownerId", UUID.randomUUID().toString());

        when(propertyService.createProperty(any(Property.class), anyList(), anyList()))
                .thenReturn(testProperty);

        mockMvc.perform(post("/api/property/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(propertyService, times(1)).createProperty(any(Property.class), anyList(), anyList());
    }

    @Test
    void testCreateProperty_Error() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("propertyName", "New Hotel");
        request.put("type", 1);
        request.put("address", "New Address");
        request.put("province", 1);
        request.put("ownerId", UUID.randomUUID().toString());

        when(propertyService.createProperty(any(Property.class), anyList(), anyList()))
                .thenThrow(new RuntimeException("Validation error"));

        mockMvc.perform(post("/api/property/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation error"));
    }

    @Test
    void testGetPropertyForUpdate_Success() throws Exception {
        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        when(roomService.getRoomTypesByProperty("PROP001")).thenReturn(Arrays.asList(testRoomType));

        mockMvc.perform(get("/api/property/update/PROP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.property.propertyId").value("PROP001"))
                .andExpect(jsonPath("$.roomTypes[0].roomTypeId").value("RT001"));

        verify(propertyService, times(1)).getPropertyById("PROP001");
        verify(roomService, times(1)).getRoomTypesByProperty("PROP001");
    }

    @Test
    void testGetPropertyForUpdate_NotFound() throws Exception {
        when(propertyService.getPropertyById("PROP999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/property/update/PROP999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Property not found"));
    }

    @Test
    void testUpdateProperty_WithExistingRoomTypes() throws Exception {
        Map<String, Object> roomType1 = new HashMap<>();
        roomType1.put("roomTypeId", "RT001");
        roomType1.put("name", "Super Deluxe");
        roomType1.put("price", 750000);
        roomType1.put("capacity", 3);
        roomType1.put("facility", "AC, TV, Mini Bar");
        roomType1.put("floor", 2);
        roomType1.put("description", "Updated description");

        Map<String, Object> request = new HashMap<>();
        request.put("propertyId", "PROP001");
        request.put("propertyName", "Updated Hotel");
        request.put("address", "Updated Address");
        request.put("description", "Updated Description");
        request.put("province", 2);
        request.put("roomTypes", Arrays.asList(roomType1));

        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyService.updatePropertyWithRoomTypes(any(Property.class), anyList(), anyMap()))
                .thenReturn(testProperty);

        mockMvc.perform(put("/api/property/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Property updated successfully"));

        verify(propertyService, times(1)).getPropertyById("PROP001");
        verify(propertyService, times(1)).updatePropertyWithRoomTypes(any(Property.class), anyList(), anyMap());
    }

    @Test
    void testUpdateProperty_WithNewRoomTypes() throws Exception {
        Map<String, Object> newRoomType = new HashMap<>();
        newRoomType.put("name", "Suite");
        newRoomType.put("price", 1000000);
        newRoomType.put("capacity", 4);
        newRoomType.put("facility", "AC, TV, Kitchen");
        newRoomType.put("floor", 3);
        newRoomType.put("description", "Luxury suite");
        newRoomType.put("roomCount", 3);

        Map<String, Object> request = new HashMap<>();
        request.put("propertyId", "PROP001");
        request.put("propertyName", "Updated Hotel");
        request.put("address", "Updated Address");
        request.put("description", "Updated Description");
        request.put("province", 1);
        request.put("roomTypes", Arrays.asList(newRoomType));

        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyService.updatePropertyWithRoomTypes(any(Property.class), anyList(), anyMap()))
                .thenReturn(testProperty);

        mockMvc.perform(put("/api/property/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(propertyService, times(1)).updatePropertyWithRoomTypes(any(Property.class), anyList(), anyMap());
    }

    @Test
    void testUpdateProperty_WithoutRoomTypes() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("propertyId", "PROP001");
        request.put("propertyName", "Updated Hotel");
        request.put("address", "Updated Address");
        request.put("description", "Updated Description");
        request.put("province", 1);

        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyService.updatePropertyWithRoomTypes(any(Property.class), anyList(), anyMap()))
                .thenReturn(testProperty);

        mockMvc.perform(put("/api/property/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(propertyService, times(1)).updatePropertyWithRoomTypes(any(Property.class), anyList(), anyMap());
    }

    @Test
    void testUpdateProperty_PropertyNotFound() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("propertyId", "PROP999");
        request.put("propertyName", "Updated Hotel");
        request.put("address", "Updated Address");

        when(propertyService.getPropertyById("PROP999")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/property/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Property not found"));
    }

    @Test
    void testUpdateProperty_Error() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("propertyId", "PROP001");
        request.put("propertyName", "Updated Hotel");

        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        when(propertyService.updatePropertyWithRoomTypes(any(Property.class), anyList(), anyMap()))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/api/property/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Update failed"));
    }

    @Test
    void testDeleteProperty_Success() throws Exception {
        doNothing().when(propertyService).softDeleteProperty("PROP001");

        mockMvc.perform(delete("/api/property/delete/PROP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Property deleted successfully"));

        verify(propertyService, times(1)).softDeleteProperty("PROP001");
    }

    @Test
    void testDeleteProperty_Error() throws Exception {
        doThrow(new RuntimeException("Cannot delete property with future bookings"))
                .when(propertyService).softDeleteProperty("PROP001");

        mockMvc.perform(delete("/api/property/delete/PROP001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot delete property with future bookings"));
    }

    @Test
    void testAddRoomTypes_Success() throws Exception {
        Map<String, Object> roomType1 = new HashMap<>();
        roomType1.put("name", "Presidential Suite");
        roomType1.put("price", 2000000);
        roomType1.put("capacity", 6);
        roomType1.put("facility", "All facilities");
        roomType1.put("floor", 10);
        roomType1.put("description", "Presidential suite");
        roomType1.put("roomCount", 2);

        Map<String, Object> request = new HashMap<>();
        request.put("propertyId", "PROP001");
        request.put("roomTypes", Arrays.asList(roomType1));

        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        doNothing().when(roomService).addRoomType(any(Property.class), any(RoomType.class), anyInt());

        mockMvc.perform(post("/api/property/updateroom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Room types added successfully"));

        verify(propertyService, times(1)).getPropertyById("PROP001");
        verify(roomService, times(1)).addRoomType(any(Property.class), any(RoomType.class), eq(2));
    }

    @Test
    void testAddRoomTypes_WithDefaultRoomCount() throws Exception {
        Map<String, Object> roomType1 = new HashMap<>();
        roomType1.put("name", "Standard");
        roomType1.put("price", 300000);
        roomType1.put("capacity", 2);
        roomType1.put("facility", "AC, TV");
        roomType1.put("floor", 1);
        roomType1.put("description", "Standard room");
        // No roomCount specified, should default to 1

        Map<String, Object> request = new HashMap<>();
        request.put("propertyId", "PROP001");
        request.put("roomTypes", Arrays.asList(roomType1));

        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        doNothing().when(roomService).addRoomType(any(Property.class), any(RoomType.class), anyInt());

        mockMvc.perform(post("/api/property/updateroom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(roomService, times(1)).addRoomType(any(Property.class), any(RoomType.class), eq(1));
    }

    @Test
    void testAddRoomTypes_PropertyNotFound() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("propertyId", "PROP999");
        request.put("roomTypes", new ArrayList<>());

        when(propertyService.getPropertyById("PROP999")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/property/updateroom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Property not found"));
    }

    @Test
    void testAddRoomTypes_Error() throws Exception {
        Map<String, Object> roomType1 = new HashMap<>();
        roomType1.put("name", "Deluxe");
        roomType1.put("price", 500000);
        roomType1.put("capacity", 2);
        roomType1.put("floor", 2);

        Map<String, Object> request = new HashMap<>();
        request.put("propertyId", "PROP001");
        request.put("roomTypes", Arrays.asList(roomType1));

        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));
        doThrow(new RuntimeException("Failed to add room type"))
                .when(roomService).addRoomType(any(Property.class), any(RoomType.class), anyInt());

        mockMvc.perform(post("/api/property/updateroom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to add room type"));
    }

    @Test
    void testAddRoomTypes_WithNullRoomTypes() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("propertyId", "PROP001");
        request.put("roomTypes", null);

        when(propertyService.getPropertyById("PROP001")).thenReturn(Optional.of(testProperty));

        mockMvc.perform(post("/api/property/updateroom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(roomService, never()).addRoomType(any(), any(), anyInt());
    }
}