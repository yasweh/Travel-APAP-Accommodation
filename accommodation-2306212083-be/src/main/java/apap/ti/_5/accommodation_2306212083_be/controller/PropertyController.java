package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.service.RoomService;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/property")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final RoomService roomService;
    private final RoomTypeRepository roomTypeRepository;

    /**
     * GET /api/property - List all properties
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listProperties(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer province) {
        
        List<Property> properties;
        if (name != null || type != null || province != null) {
            properties = propertyService.searchProperties(name, type, province);
        } else {
            properties = propertyService.getAllActiveProperties();
        }
        
        // Add room count to each property
        List<Map<String, Object>> propertiesWithRoomCount = new ArrayList<>();
        for (Property property : properties) {
            Map<String, Object> propertyMap = new HashMap<>();
            propertyMap.put("propertyId", property.getPropertyId());
            propertyMap.put("propertyName", property.getPropertyName());
            propertyMap.put("type", property.getType());
            propertyMap.put("typeString", property.getTypeString());
            propertyMap.put("address", property.getAddress());
            propertyMap.put("province", property.getProvince());
            propertyMap.put("description", property.getDescription());
            propertyMap.put("totalRoom", property.getTotalRoom());
            propertyMap.put("activeStatus", property.getActiveStatus());
            propertyMap.put("activeStatusString", property.getActiveStatusString());
            propertyMap.put("activeRoom", property.getActiveRoom());
            propertyMap.put("income", property.getIncome());
            propertyMap.put("ownerName", property.getOwnerName());
            propertyMap.put("ownerId", property.getOwnerId());
            propertyMap.put("createdDate", property.getCreatedDate());
            propertyMap.put("updatedDate", property.getUpdatedDate());
            
            // Calculate actual room count
            int roomCount = roomService.getRoomsByProperty(property.getPropertyId()).size();
            propertyMap.put("roomCount", roomCount);
            
            propertiesWithRoomCount.add(propertyMap);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", propertiesWithRoomCount);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/property/{id}/room-types - Get room types by property
     */
    @GetMapping("/{id}/room-types")
    public ResponseEntity<Map<String, Object>> getRoomTypesByProperty(@PathVariable String id) {
        try {
            List<RoomType> roomTypes = roomTypeRepository.findByProperty_PropertyIdAndActiveStatus(id, 1);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roomTypes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/property/{id} - Property detail with optional date filter
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detailProperty(
            @PathVariable String id,
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut) {
        Property property = propertyService.getPropertyById(id)
            .orElseThrow(() -> new RuntimeException("Property not found"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("property", property);
        response.put("roomTypes", roomService.getRoomTypesByProperty(id));
        
        // Get all rooms or filtered rooms
        if (checkIn != null && checkOut != null) {
            response.put("rooms", roomService.getAvailableRoomsByPropertyAndDate(id, checkIn, checkOut));
        } else {
            response.put("rooms", roomService.getRoomsByProperty(id));
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/property/create - Create new property with room types
     */
    @PostMapping("/create")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> createProperty(@RequestBody Map<String, Object> request) {
        try {
            // Extract property data
            Property property = new Property();
            property.setPropertyName((String) request.get("propertyName"));
            property.setType(((Number) request.get("type")).intValue());
            property.setAddress((String) request.get("address"));
            property.setProvince(((Number) request.get("province")).intValue());
            property.setDescription((String) request.get("description"));
            property.setOwnerName((String) request.get("ownerName"));
            property.setOwnerId(UUID.fromString((String) request.get("ownerId")));
            
            // Extract room types if provided
            List<RoomType> roomTypes = new ArrayList<>();
            List<List<Integer>> roomCounts = new ArrayList<>();
            
            if (request.containsKey("roomTypes") && request.get("roomTypes") != null) {
                List<Map<String, Object>> roomTypesData = (List<Map<String, Object>>) request.get("roomTypes");
                for (Map<String, Object> rtData : roomTypesData) {
                    RoomType rt = new RoomType();
                    rt.setName((String) rtData.get("name"));
                    rt.setPrice(((Number) rtData.get("price")).intValue());
                    rt.setCapacity(((Number) rtData.get("capacity")).intValue());
                    rt.setFacility((String) rtData.get("facility"));
                    rt.setFloor(((Number) rtData.get("floor")).intValue());
                    rt.setDescription((String) rtData.get("description"));
                    roomTypes.add(rt);
                    
                    // Extract room count for this room type
                    Object roomCountObj = rtData.getOrDefault("roomCount", 1);
                    Integer roomCount = roomCountObj instanceof Number ? ((Number) roomCountObj).intValue() : 1;
                    roomCounts.add(Arrays.asList(roomCount));
                }
            }
            
            Property created = propertyService.createProperty(property, roomTypes, roomCounts);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Property created successfully");
            response.put("data", created);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/property/update/{id} - Get property data for update form
     */
    @GetMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> getPropertyForUpdate(@PathVariable String id) {
        try {
            Property property = propertyService.getPropertyById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            List<RoomType> roomTypes = roomService.getRoomTypesByProperty(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("property", property);
            response.put("roomTypes", roomTypes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * PUT /api/property/update - Update property with room types
     */
    @PutMapping("/update")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> updateProperty(@RequestBody Map<String, Object> request) {
        try {
            // Extract property ID
            String propertyId = (String) request.get("propertyId");
            
            // Get existing property
            Property existingProperty = propertyService.getPropertyById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            // Update property fields
            existingProperty.setPropertyName((String) request.get("propertyName"));
            existingProperty.setAddress((String) request.get("address"));
            existingProperty.setDescription((String) request.get("description"));
            
            // Update province if provided
            if (request.containsKey("province")) {
                Object provinceObj = request.get("province");
                existingProperty.setProvince(provinceObj instanceof Number ? ((Number) provinceObj).intValue() : (Integer) provinceObj);
            }
            
            // Extract and update room types if provided
            List<RoomType> updatedRoomTypes = new ArrayList<>();
            Map<String, Integer> roomCountMap = new HashMap<>(); // Store roomCount for new room types
            
            if (request.containsKey("roomTypes") && request.get("roomTypes") != null) {
                List<Map<String, Object>> roomTypesData = (List<Map<String, Object>>) request.get("roomTypes");
                for (Map<String, Object> rtData : roomTypesData) {
                    RoomType rt = new RoomType();
                    
                    // Check if this is an existing room type (has roomTypeId)
                    if (rtData.containsKey("roomTypeId") && rtData.get("roomTypeId") != null) {
                        rt.setRoomTypeId((String) rtData.get("roomTypeId"));
                    }
                    
                    rt.setName((String) rtData.get("name"));
                    
                    // Handle numeric fields with proper type casting
                    Object priceObj = rtData.get("price");
                    rt.setPrice(priceObj instanceof Number ? ((Number) priceObj).intValue() : (Integer) priceObj);
                    
                    Object capacityObj = rtData.get("capacity");
                    rt.setCapacity(capacityObj instanceof Number ? ((Number) capacityObj).intValue() : (Integer) capacityObj);
                    
                    rt.setFacility((String) rtData.get("facility"));
                    
                    Object floorObj = rtData.get("floor");
                    rt.setFloor(floorObj instanceof Number ? ((Number) floorObj).intValue() : (Integer) floorObj);
                    
                    rt.setDescription((String) rtData.get("description"));
                    
                    // Store roomCount for new room types (without roomTypeId)
                    if (rtData.containsKey("roomCount") && rtData.get("roomCount") != null) {
                        Object roomCountObj = rtData.get("roomCount");
                        Integer roomCount = roomCountObj instanceof Number ? ((Number) roomCountObj).intValue() : (Integer) roomCountObj;
                        // Use a temporary key based on name+floor to identify this room type later
                        String key = rt.getName() + "_" + rt.getFloor();
                        roomCountMap.put(key, roomCount);
                    }
                    
                    updatedRoomTypes.add(rt);
                }
            }
            
            Property updated = propertyService.updatePropertyWithRoomTypes(existingProperty, updatedRoomTypes, roomCountMap);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Property updated successfully");
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * DELETE /api/property/delete/{id} - Soft delete property
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteProperty(@PathVariable String id) {
        try {
            propertyService.softDeleteProperty(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Property deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * POST /api/property/updateroom - Add room types to existing property
     */
    @PostMapping("/updateroom")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> addRoomTypes(@RequestBody Map<String, Object> request) {
        try {
            String propertyId = (String) request.get("propertyId");
            
            // Get existing property
            Property property = propertyService.getPropertyById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            // Extract new room types
            List<RoomType> newRoomTypes = new ArrayList<>();
            List<List<Integer>> roomCounts = new ArrayList<>();
            
            if (request.containsKey("roomTypes") && request.get("roomTypes") != null) {
                List<Map<String, Object>> roomTypesData = (List<Map<String, Object>>) request.get("roomTypes");
                for (Map<String, Object> rtData : roomTypesData) {
                    RoomType rt = new RoomType();
                    rt.setName((String) rtData.get("name"));
                    rt.setPrice((Integer) rtData.get("price"));
                    rt.setCapacity((Integer) rtData.get("capacity"));
                    rt.setFacility((String) rtData.get("facility"));
                    rt.setFloor((Integer) rtData.get("floor"));
                    rt.setDescription((String) rtData.get("description"));
                    rt.setProperty(property);
                    newRoomTypes.add(rt);
                    
                    // Get room count for this room type
                    Integer roomCount = (Integer) rtData.getOrDefault("roomCount", 1);
                    roomCounts.add(Arrays.asList(roomCount));
                }
            }
            
            // Add room types using service
            for (int i = 0; i < newRoomTypes.size(); i++) {
                RoomType rt = newRoomTypes.get(i);
                roomService.addRoomType(property, rt, roomCounts.get(i).get(0));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Room types added successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
