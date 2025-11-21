package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeRepository roomTypeRepository;
    private final PropertyService propertyService;

    /**
     * GET /api/room-types - Get all room types
     */
    @GetMapping("/room-types")
    public ResponseEntity<Map<String, Object>> getAllRoomTypes() {
        try {
            List<RoomType> roomTypes = roomTypeRepository.findByActiveStatus(1); // Only active
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roomTypes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/room-types/{id} - Get room type by ID with its rooms
     */
    @GetMapping("/room-types/{id}")
    public ResponseEntity<Map<String, Object>> getRoomTypeById(@PathVariable String id) {
        try {
            RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room type not found"));
            
            // Build response with listRoom exposed
            Map<String, Object> roomTypeData = new HashMap<>();
            roomTypeData.put("roomTypeId", roomType.getRoomTypeId());
            roomTypeData.put("name", roomType.getName());
            roomTypeData.put("price", roomType.getPrice());
            roomTypeData.put("description", roomType.getDescription());
            roomTypeData.put("capacity", roomType.getCapacity());
            roomTypeData.put("facility", roomType.getFacility());
            roomTypeData.put("floor", roomType.getFloor());
            roomTypeData.put("activeStatus", roomType.getActiveStatus());
            roomTypeData.put("createdDate", roomType.getCreatedDate());
            roomTypeData.put("updatedDate", roomType.getUpdatedDate());
            
            // Include listRoom
            List<Map<String, Object>> roomsList = roomType.getListRoom().stream()
                .map(room -> {
                    Map<String, Object> roomData = new HashMap<>();
                    roomData.put("roomId", room.getRoomId());
                    roomData.put("name", room.getName());
                    roomData.put("availabilityStatus", room.getAvailabilityStatus());
                    roomData.put("activeRoom", room.getActiveRoom());
                    roomData.put("maintenanceStart", room.getMaintenanceStart());
                    roomData.put("maintenanceEnd", room.getMaintenanceEnd());
                    roomData.put("createdDate", room.getCreatedDate());
                    roomData.put("updatedDate", room.getUpdatedDate());
                    return roomData;
                })
                .collect(Collectors.toList());
            
            roomTypeData.put("listRoom", roomsList);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roomTypeData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * GET /api/roomtype/{idProperty} - Get room types for property
     */
    @GetMapping("/roomtype/{idProperty}")
    public ResponseEntity<Map<String, Object>> getRoomTypes(@PathVariable String idProperty) {
        try {
            Property property = propertyService.getPropertyById(idProperty)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            List<RoomType> roomTypes = roomTypeRepository.findByProperty_PropertyIdAndActiveStatus(idProperty, 1);
            
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
}
