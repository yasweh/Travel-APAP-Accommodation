package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.UnifiedBookingDTO;
import apap.ti._5.accommodation_2306212083_be.service.UnifiedBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Support System
 * Aggregates bookings from all 5 services (Tour, Rental, Insurance, Accommodation, Flight)
 */
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {
    
    private final UnifiedBookingService unifiedBookingService;
    
    /**
     * GET /api/support/bookings - Get all bookings from all services
     * Query param: source (optional) - Filter by service (tour, rental, insurance, accommodation, flight)
     */
    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> getAllBookings(
            @RequestParam(required = false) String source) {
        
        try {
            List<UnifiedBookingDTO> allBookings = unifiedBookingService.getAllBookingsFromAllServices();
            
            // Filter by source if provided
            if (source != null && !source.isEmpty()) {
                allBookings = allBookings.stream()
                    .filter(booking -> source.equalsIgnoreCase(booking.getSource()))
                    .collect(Collectors.toList());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", allBookings.size());
            response.put("data", allBookings);
            
            // Add summary by source
            Map<String, Long> summaryBySource = allBookings.stream()
                .collect(Collectors.groupingBy(
                    UnifiedBookingDTO::getSource, 
                    Collectors.counting()
                ));
            response.put("summaryBySource", summaryBySource);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch bookings: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * GET /api/support/bookings/{source}/{id} - Get specific booking detail
     */
    @GetMapping("/bookings/{source}/{id}")
    public ResponseEntity<Map<String, Object>> getBookingDetail(
            @PathVariable String source,
            @PathVariable String id) {
        
        try {
            List<UnifiedBookingDTO> allBookings = unifiedBookingService.getAllBookingsFromAllServices();
            
            UnifiedBookingDTO booking = allBookings.stream()
                .filter(b -> source.equalsIgnoreCase(b.getSource()) && id.equals(b.getId()))
                .findFirst()
                .orElse(null);
            
            if (booking == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Booking not found");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", booking);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch booking: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
