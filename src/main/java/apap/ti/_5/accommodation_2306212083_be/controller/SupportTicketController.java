package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.support.*;
import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import apap.ti._5.accommodation_2306212083_be.service.SupportTicketService;
import apap.ti._5.accommodation_2306212083_be.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Support Ticket operations
 * All endpoints pass userId as request parameter to filter data
 */
@RestController
@RequestMapping("/api/support-tickets")
@Slf4j
public class SupportTicketController {
    
    private final SupportTicketService supportTicketService;
    
    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }
    
    /**
     * GET /api/support-tickets
     * Get all tickets with optional filters
     * - Customer: Only their own tickets
     * - Accommodation Owner: Tickets for their properties
     * - Superadmin: All tickets
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) ServiceSource serviceSource) {
        
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        UUID userId = UUID.fromString(currentUser.getUserId());
        
        log.info("GET /api/support-tickets - userId={}, role={}, status={}, serviceSource={}", 
                userId, currentUser.getRole(), status, serviceSource);
        
        List<TicketResponseDTO> tickets;
        
        if (SecurityUtil.isCustomer()) {
            // Customer: Only their own tickets
            tickets = supportTicketService.getAllTickets(userId, status, serviceSource);
        } else if (SecurityUtil.isAccommodationOwner()) {
            // Accommodation Owner: Filter by properties they own (pass null userId to get all, filter in service)
            tickets = supportTicketService.getAllTicketsForOwner(userId, status, serviceSource);
        } else {
            // Superadmin: All tickets (pass null userId)
            tickets = supportTicketService.getAllTickets(null, status, serviceSource);
        }
        
        return ResponseEntity.ok(tickets);
    }
    
    /**
     * GET /api/support-tickets/{id}
     * Get detailed ticket info including messages, progress, and external booking data
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketDetailResponseDTO> getTicketDetail(@PathVariable UUID id) {
        
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        UUID userId = UUID.fromString(currentUser.getUserId());
        String role = currentUser.getRole();
        
        log.info("GET /api/support-tickets/{} - userId={}, role={}", id, userId, role);
        
        // Pass role to service for proper access control
        TicketDetailResponseDTO ticket = supportTicketService.getTicketDetailWithAccess(id, userId, role);
        return ResponseEntity.ok(ticket);
    }
    
    /**
     * POST /api/support-tickets
     * Create a new support ticket
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponseDTO> createTicket(@Valid @RequestBody CreateTicketRequestDTO request) {
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        UUID userId = UUID.fromString(currentUser.getUserId());
        
        // Override userId from request with authenticated user's ID
        request.setUserId(userId);
        
        log.info("POST /api/support-tickets - Creating ticket for userId={}", userId);
        
        TicketResponseDTO ticket = supportTicketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }
    
    /**
     * PATCH /api/support-tickets/{id}/status
     * Update ticket status
     * Customer can only close tickets, Admin/Owner can change to any status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponseDTO> updateTicketStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequestDTO request) {
        
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        UUID userId = UUID.fromString(currentUser.getUserId());
        String role = currentUser.getRole();
        
        log.info("PATCH /api/support-tickets/{}/status - Updating to {} by userId={}, role={}", 
                id, request.getStatus(), userId, role);
        
        // Set updatedBy to current user
        request.setUpdatedBy(userId);
        
        TicketResponseDTO ticket = supportTicketService.updateTicketStatusWithPermissionCheck(id, request, userId, role);
        return ResponseEntity.ok(ticket);
    }
    
    /**
     * DELETE /api/support-tickets/{id}
     * Soft delete a ticket (only allowed if status is OPEN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteTicket(@PathVariable UUID id) {
        
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        UUID userId = UUID.fromString(currentUser.getUserId());
        
        log.info("DELETE /api/support-tickets/{} - userId={}", id, userId);
        
        supportTicketService.deleteTicket(id, userId);
        return ResponseEntity.ok(Map.of("message", "Ticket deleted successfully"));
    }
    
    /**
     * POST /api/support-tickets/{id}/progress
     * Add progress entry to ticket
     * Only Admin or Accommodation Owner (who owns the property) can add progress
     */
    @PostMapping("/{id}/progress")
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Accommodation Owner')")
    public ResponseEntity<ProgressResponseDTO> addProgress(
            @PathVariable UUID id,
            @Valid @RequestBody AddProgressRequestDTO request) {
        
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        UUID userId = UUID.fromString(currentUser.getUserId());
        String role = currentUser.getRole();
        
        log.info("POST /api/support-tickets/{}/progress - userId={}, role={}", id, userId, role);
        
        // Set performedBy to current user
        request.setPerformedBy(userId);
        
        ProgressResponseDTO progress = supportTicketService.addProgressWithPermissionCheck(id, request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(progress);
    }
    
    /**
     * DELETE /api/support-tickets/{ticketId}/progress/{progressId}
     * Soft delete progress entry
     */
    @DeleteMapping("/{ticketId}/progress/{progressId}")
    public ResponseEntity<Map<String, String>> deleteProgress(
            @PathVariable UUID ticketId,
            @PathVariable UUID progressId) {
        
        log.info("DELETE /api/support-tickets/{}/progress/{}", ticketId, progressId);
        
        supportTicketService.deleteProgress(ticketId, progressId);
        return ResponseEntity.ok(Map.of("message", "Progress entry deleted successfully"));
    }
    
    /**
     * POST /api/support-tickets/{id}/messages
     * Add message to ticket
     */
    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponseDTO> addMessage(
            @PathVariable UUID id,
            @Valid @RequestBody AddMessageRequestDTO request) {
        
        log.info("POST /api/support-tickets/{}/messages", id);
        
        MessageResponseDTO message = supportTicketService.addMessage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
    
    /**
     * GET /api/support-tickets/{id}/messages
     * Get all messages for a ticket
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getTicketMessages(@PathVariable UUID id) {
        log.info("GET /api/support-tickets/{}/messages", id);
        
        List<MessageResponseDTO> messages = supportTicketService.getTicketMessages(id);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * PUT /api/support-tickets/{id}/messages/mark-read
     * Mark messages as read
     * Query params: userId (required)
     */
    @PutMapping("/{id}/messages/mark-read")
    public ResponseEntity<Map<String, String>> markMessagesAsRead(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        
        log.info("PUT /api/support-tickets/{}/messages/mark-read - userId={}", id, userId);
        
        supportTicketService.markMessagesAsRead(id, userId);
        return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    }
    
    /**
     * GET /api/support-tickets/bookings
     * Fetch available bookings for a user from external services
     * Query params: serviceSource (required), userId (required)
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<?>> getAvailableBookings(
            @RequestParam ServiceSource serviceSource,
            @RequestParam UUID userId) {
        
        log.info("GET /api/support-tickets/bookings - serviceSource={}, userId={}", serviceSource, userId);
        
        List<?> bookings = supportTicketService.getAvailableBookings(serviceSource, userId);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * GET /api/support-tickets/dashboard
     * Get comprehensive dashboard data: all bookings from 5 external services + user's support tickets
     * Query params: userId (required)
     */
    @GetMapping("/dashboard")
    public ResponseEntity<SupportDashboardResponseDTO> getDashboard(@RequestParam UUID userId) {
        log.info("GET /api/support-tickets/dashboard - userId={}", userId);
        
        SupportDashboardResponseDTO dashboard = supportTicketService.getDashboardData(userId);
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Global exception handler for this controller
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Error in SupportTicketController: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}
