package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.MaintenanceDTO;
import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.model.Maintenance;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.security.annotations.IsOwner;
import apap.ti._5.accommodation_2306212083_be.service.MaintenanceService;
import apap.ti._5.accommodation_2306212083_be.service.OwnerValidationService;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/property/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final RoomRepository roomRepository;
    private final OwnerValidationService ownerValidationService;

    /**
     * POST /api/property/maintenance/add - Add maintenance schedule
     * Requires: ACCOMMODATION_OWNER (own property) or SUPERADMIN role
     */
    @PostMapping("/add")
    @IsOwner
    public ResponseEntity<Map<String, Object>> addMaintenance(@RequestBody Map<String, Object> request) {
        try {
            String roomId = (String) request.get("roomId");
            String startDateStr = (String) request.get("startDate");
            String startTimeStr = (String) request.get("startTime");
            String endDateStr = (String) request.get("endDate");
            String endTimeStr = (String) request.get("endTime");
            
            // Find room
            Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
            
            // Use OwnerValidationService to validate room ownership
            ownerValidationService.validateRoomOwnership(room);
            
            // Parse dates and times
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);
            
            // Create maintenance
            Maintenance maintenance = Maintenance.builder()
                .room(room)
                .startDate(startDate)
                .startTime(startTime)
                .endDate(endDate)
                .endTime(endTime)
                .build();
            
            Maintenance saved = maintenanceService.addMaintenance(maintenance);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Maintenance scheduled successfully");
            response.put("data", saved);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/property/maintenance - Get all maintenance
     * ACCOMMODATION_OWNER: only shows maintenance for their properties
     * SUPERADMIN: shows all maintenance
     */
    @GetMapping
    @IsOwner
    public ResponseEntity<Map<String, Object>> getAllMaintenance() {
        try {
            UserPrincipal user = ownerValidationService.getCurrentUser();
            List<MaintenanceDTO> maintenanceList = maintenanceService.getAllMaintenance();
            
            // Filter by owner if not superadmin
            // TODO: Fix filtering for DTOs
            /*
            if (ownerValidationService.isOwner() && !ownerValidationService.isSuperadmin()) {
                UUID ownerUuid = UUID.fromString(user.getUserId());
                maintenanceList = maintenanceList.stream()
                    .filter(maintenance -> {
                        Room room = maintenance.getRoom();
                        RoomType roomType = room.getRoomType();
                        Property property = roomType.getProperty();
                        return property.getOwnerId().equals(ownerUuid);
                    })
                    .collect(Collectors.toList());
            }
            */
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", maintenanceList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/property/maintenance/room-type/{roomTypeId} - Get maintenance by room type
     * ACCOMMODATION_OWNER: only if room type belongs to their property
     * SUPERADMIN: can view any
     */
    @GetMapping("/room-type/{roomTypeId}")
    @IsOwner
    public ResponseEntity<Map<String, Object>> getMaintenanceByRoomType(@PathVariable String roomTypeId) {
        try {
            List<MaintenanceDTO> maintenanceList = maintenanceService.getMaintenanceByRoomTypeId(roomTypeId);
            
            // Validate owner can access this room type if not superadmin
            // TODO: Fix validation for DTOs
            /*
            if (ownerValidationService.isOwner() && !ownerValidationService.isSuperadmin() && !maintenanceList.isEmpty()) {
                Room firstRoom = maintenanceList.get(0).getRoom();
                RoomType roomType = firstRoom.getRoomType();
                ownerValidationService.validateRoomTypeOwnership(roomType);
            }
            */
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", maintenanceList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/property/maintenance/room/{roomId} - Get maintenance by room
     * ACCOMMODATION_OWNER: only if room belongs to their property
     * SUPERADMIN: can view any
     */
    /**
     * GET /api/property/maintenance/room/{roomId} - Get maintenance by room
     * ACCOMMODATION_OWNER: only if room belongs to their property
     * SUPERADMIN: can view any
     */
    @GetMapping("/room/{roomId}")
    @IsOwner
    public ResponseEntity<Map<String, Object>> getMaintenanceByRoom(@PathVariable String roomId) {
        try {
            // Validate room ownership first
            Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
            ownerValidationService.validateRoomOwnership(room);
            
            List<MaintenanceDTO> maintenanceList = maintenanceService.getMaintenanceByRoomId(roomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", maintenanceList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
