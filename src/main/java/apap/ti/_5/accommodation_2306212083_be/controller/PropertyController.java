package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.service.RoomService;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306212083_be.util.PropertyRoomTypeValidator;
import apap.ti._5.accommodation_2306212083_be.util.SecurityUtil;
import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/property")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final RoomService roomService;
    private final RoomTypeRepository roomTypeRepository;
    private final PropertyRoomTypeValidator roomTypeValidator;

    /**
     * GET /api/property - List all properties (filtered by owner for Accommodation Owner)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listProperties(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer province) {
        
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        List<Property> properties;
        
        // Filter by owner for Accommodation Owner role
        if ("Accommodation Owner".equals(currentUser.getRole())) {
            UUID ownerId = UUID.fromString(currentUser.getUserId());
            properties = propertyService.getPropertiesByOwner(ownerId);
            
            // Apply additional filters if provided
            if (name != null || type != null || province != null) {
                properties = properties.stream()
                    .filter(p -> (name == null || p.getPropertyName().toLowerCase().contains(name.toLowerCase())))
                    .filter(p -> (type == null || p.getType().equals(type)))
                    .filter(p -> (province == null || p.getProvince().equals(province)))
                    .toList();
            }
        } else {
            // Superadmin and Customer can see all properties
            if (name != null || type != null || province != null) {
                properties = propertyService.searchProperties(name, type, province);
            } else {
                properties = propertyService.getAllActiveProperties();
            }
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
            // Validate ownership for Accommodation Owner
            UserPrincipal currentUser = SecurityUtil.getCurrentUser();
            if ("Accommodation Owner".equals(currentUser.getRole())) {
                Optional<Property> property = propertyService.getPropertyById(id);
                if (property.isEmpty()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Property not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                
                UUID currentUserId = UUID.fromString(currentUser.getUserId());
                if (!property.get().getOwnerId().equals(currentUserId)) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Access denied: You don't own this property");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
            }
            
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
        
        // Validate ownership for Accommodation Owner
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        if ("Accommodation Owner".equals(currentUser.getRole())) {
            UUID currentUserId = UUID.fromString(currentUser.getUserId());
            if (!property.getOwnerId().equals(currentUserId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Access denied: You don't own this property");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        }
        
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
     * Requires: ACCOMMODATION_OWNER or SUPERADMIN role
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Accommodation Owner')")
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
            
            // Auto-set owner information from authenticated user
            UserPrincipal currentUser = SecurityUtil.getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("User not authenticated");
            }
            
            // Superadmin can assign property to specific owner if ownerId provided
            if (SecurityUtil.isSuperadmin() && request.containsKey("ownerId")) {
                String ownerIdStr = (String) request.get("ownerId");
                property.setOwnerId(UUID.fromString(ownerIdStr));
                // For superadmin assigning to another user, still need ownerName from request
                property.setOwnerName((String) request.get("ownerName"));
            } else {
                // For Accommodation Owner, auto-set their own ID and name
                property.setOwnerId(UUID.fromString(currentUser.getUserId()));
                property.setOwnerName(currentUser.getName());
            }
            
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
     * Requires: ACCOMMODATION_OWNER (own property) or SUPERADMIN
     */
    @GetMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Accommodation Owner')")
    public ResponseEntity<Map<String, Object>> getPropertyForUpdate(@PathVariable String id) {
        try {
            // Validate ownership - Superadmin can access all, Owner only their own
            Property property = propertyService.getPropertyById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            if (!SecurityUtil.isSuperadmin()) {
                UserPrincipal currentUser = SecurityUtil.getCurrentUser();
                if (!property.getOwnerId().toString().equals(currentUser.getUserId())) {
                    throw new RuntimeException("Access denied: You can only update your own properties");
                }
            }
            
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
     * Requires: ACCOMMODATION_OWNER (own property) or SUPERADMIN
     */
    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Accommodation Owner')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> updateProperty(@RequestBody Map<String, Object> request) {
        try {
            // Extract property ID
            String propertyId = (String) request.get("propertyId");
            
            // Get existing property
            Property existingProperty = propertyService.getPropertyById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            // Validate ownership - Superadmin can update all, Owner only their own
            if (!SecurityUtil.isSuperadmin()) {
                UserPrincipal currentUser = SecurityUtil.getCurrentUser();
                if (!existingProperty.getOwnerId().toString().equals(currentUser.getUserId())) {
                    throw new RuntimeException("Access denied: You can only update your own properties");
                }
            }
            
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
     * Requires: SUPERADMIN role only
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('Superadmin')")
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
     * Requires: ACCOMMODATION_OWNER (own property) or SUPERADMIN
     */
    @PostMapping("/updateroom")
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Accommodation Owner')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> addRoomTypes(@RequestBody Map<String, Object> request) {
        try {
            String propertyId = (String) request.get("propertyId");
            
            // Get existing property
            Property existingProperty = propertyService.getPropertyById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            // Validate ownership - Superadmin can update all, Owner only their own
            if (!SecurityUtil.isSuperadmin()) {
                UserPrincipal currentUser = SecurityUtil.getCurrentUser();
                if (!existingProperty.getOwnerId().toString().equals(currentUser.getUserId())) {
                    throw new RuntimeException("Access denied: You can only update your own properties");
                }
            }
            
            // Extract new room types
            List<RoomType> newRoomTypes = new ArrayList<>();
            List<List<Integer>> roomCounts = new ArrayList<>();
            List<String> invalidRoomTypes = new ArrayList<>();
            
            if (request.containsKey("roomTypes") && request.get("roomTypes") != null) {
                List<Map<String, Object>> roomTypesData = (List<Map<String, Object>>) request.get("roomTypes");
                for (Map<String, Object> rtData : roomTypesData) {
                    String roomTypeName = (String) rtData.get("name");
                    
                    // Validate room type against property type
                    if (!roomTypeValidator.isValidRoomType(existingProperty.getType(), roomTypeName)) {
                        invalidRoomTypes.add(roomTypeName);
                    }
                    
                    RoomType rt = new RoomType();
                    rt.setName(roomTypeName);
                    rt.setPrice((Integer) rtData.get("price"));
                    rt.setCapacity((Integer) rtData.get("capacity"));
                    rt.setFacility((String) rtData.get("facility"));
                    rt.setFloor((Integer) rtData.get("floor"));
                    rt.setDescription((String) rtData.get("description"));
                    rt.setProperty(existingProperty);
                    newRoomTypes.add(rt);
                    
                    // Get room count for this room type
                    Integer roomCount = (Integer) rtData.getOrDefault("roomCount", 1);
                    roomCounts.add(Arrays.asList(roomCount));
                }
            }
            
            // If there are invalid room types, return error
            if (!invalidRoomTypes.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid room types for property type " + 
                    roomTypeValidator.getPropertyTypeString(existingProperty.getType()));
                errorResponse.put("invalidRoomTypes", invalidRoomTypes);
                errorResponse.put("validRoomTypes", roomTypeValidator.getValidRoomTypes(existingProperty.getType()));
                errorResponse.put("propertyType", existingProperty.getType());
                errorResponse.put("propertyTypeString", roomTypeValidator.getPropertyTypeString(existingProperty.getType()));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // Add room types using service
            for (int i = 0; i < newRoomTypes.size(); i++) {
                RoomType rt = newRoomTypes.get(i);
                roomService.addRoomType(existingProperty, rt, roomCounts.get(i).get(0));
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

    /**
     * GET /api/property/{propertyId}/valid-room-types - Get valid room types for property
     * Returns the list of valid room types based on the property's type
     */
    @GetMapping("/{propertyId}/valid-room-types")
    public ResponseEntity<Map<String, Object>> getValidRoomTypes(@PathVariable String propertyId) {
        try {
            Property property = propertyService.getPropertyById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            Set<String> validRoomTypes = roomTypeValidator.getValidRoomTypes(property.getType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("propertyId", property.getPropertyId());
            response.put("propertyName", property.getPropertyName());
            response.put("propertyType", property.getType());
            response.put("propertyTypeString", roomTypeValidator.getPropertyTypeString(property.getType()));
            response.put("validRoomTypes", validRoomTypes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
