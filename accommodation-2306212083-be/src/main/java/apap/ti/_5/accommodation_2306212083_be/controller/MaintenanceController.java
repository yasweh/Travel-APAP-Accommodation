package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.model.Maintenance;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.service.MaintenanceService;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/property/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final RoomRepository roomRepository;

    /**
     * POST /api/property/maintenance/add - Add maintenance schedule
     */
    @PostMapping("/add")
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
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMaintenance() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", maintenanceService.getAllMaintenance());
            
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
     */
    @GetMapping("/room-type/{roomTypeId}")
    public ResponseEntity<Map<String, Object>> getMaintenanceByRoomType(@PathVariable String roomTypeId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", maintenanceService.getMaintenanceByRoomTypeId(roomTypeId));
            
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
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<Map<String, Object>> getMaintenanceByRoom(@PathVariable String roomId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", maintenanceService.getMaintenanceByRoomId(roomId));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
