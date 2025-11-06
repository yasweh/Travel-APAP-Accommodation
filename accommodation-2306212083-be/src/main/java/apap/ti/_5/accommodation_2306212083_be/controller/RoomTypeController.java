package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306212083_be.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roomtype")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeRepository roomTypeRepository;
    private final PropertyService propertyService;
    private final IdGenerator idGenerator;

    /**
     * GET /api/roomtype/{idProperty} - Get room types for property
     */
    @GetMapping("/{idProperty}")
    public ResponseEntity<Map<String, Object>> getRoomTypes(@PathVariable String idProperty) {
        try {
            Property property = propertyService.getPropertyById(idProperty)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            List<RoomType> roomTypes = roomTypeRepository.findByProperty_PropertyIdOrderByFloorAsc(idProperty);
            
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
     * POST /api/roomtype - Add new room type to property
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addRoomType(@RequestBody Map<String, Object> request) {
        try {
            String propertyId = (String) request.get("propertyId");
            String name = (String) request.get("name");
            Integer floor = (Integer) request.get("floor");
            Integer price = (Integer) request.get("price");
            Integer capacity = (Integer) request.get("capacity");
            String facility = (String) request.get("facility");
            
            // Validate property exists
            Property property = propertyService.getPropertyById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            // Check for duplicate: same property + name + floor
            if (roomTypeRepository.existsByProperty_PropertyIdAndNameAndFloor(propertyId, name, floor)) {
                throw new RuntimeException("Room type with same name and floor already exists");
            }
            
            // Create new room type
            RoomType roomType = RoomType.builder()
                .property(property)
                .name(name)
                .price(price)
                .capacity(capacity)
                .facility(facility)
                .floor(floor)
                .build();
            
            // Generate room type ID
            String roomTypeId = idGenerator.generateRoomTypeId(property, name, floor);
            roomType.setRoomTypeId(roomTypeId);
            
            RoomType saved = roomTypeRepository.save(roomType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Room type added successfully");
            response.put("data", saved);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
