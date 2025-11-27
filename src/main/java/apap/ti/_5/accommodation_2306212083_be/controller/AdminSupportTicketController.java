package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.support.*;
import apap.ti._5.accommodation_2306212083_be.service.SupportProgressService;
import apap.ti._5.accommodation_2306212083_be.service.SupportTicketService;
import apap.ti._5.accommodation_2306212083_be.service.TicketMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for admin-facing support ticket operations.
 * All endpoints require SUPERADMIN role.
 */
@RestController
@RequestMapping("/api/admin/support-tickets")
@RequiredArgsConstructor
public class AdminSupportTicketController {

    private final SupportTicketService supportTicketService;
    private final TicketMessageService ticketMessageService;
    private final SupportProgressService supportProgressService;

    /**
     * Get all support tickets (admin view)
     * @return List of all tickets
     */
    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        List<TicketResponseDTO> tickets = supportTicketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get detailed information about a specific ticket
     * @param ticketId The ticket ID
     * @return Ticket details including messages and progress
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailResponseDTO> getTicketDetail(@PathVariable String ticketId) {
        TicketDetailResponseDTO ticket = supportTicketService.getTicketDetail(ticketId);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Assign a ticket to an admin
     * @param ticketId The ticket ID
     * @param body Request body containing adminId
     * @return Success response
     */
    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<Map<String, String>> assignTicket(
            @PathVariable String ticketId,
            @RequestBody Map<String, String> body) {
        String adminId = body.get("adminId");
        supportTicketService.assignTicket(ticketId, adminId);
        return ResponseEntity.ok(Map.of("message", "Ticket assigned successfully"));
    }

    /**
     * Update ticket status
     * @param ticketId The ticket ID
     * @param request Status update request
     * @return Success response
     */
    @PutMapping("/{ticketId}/status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable String ticketId,
            @Valid @RequestBody UpdateStatusRequestDTO request) {
        supportTicketService.updateTicketStatus(ticketId, request);
        return ResponseEntity.ok(Map.of("message", "Ticket status updated successfully"));
    }

    /**
     * Add a progress update to a ticket
     * @param ticketId The ticket ID
     * @param request Progress request
     * @return Created progress update
     */
    @PostMapping("/{ticketId}/progress")
    public ResponseEntity<ProgressResponseDTO> addProgress(
            @PathVariable String ticketId,
            @Valid @RequestBody AddProgressRequestDTO request) {
        ProgressResponseDTO progress = supportProgressService.addProgress(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(progress);
    }

    /**
     * Get all progress updates for a ticket
     * @param ticketId The ticket ID
     * @return List of progress updates
     */
    @GetMapping("/{ticketId}/progress")
    public ResponseEntity<List<ProgressResponseDTO>> getProgress(@PathVariable String ticketId) {
        List<ProgressResponseDTO> progress = supportProgressService.getProgress(ticketId);
        return ResponseEntity.ok(progress);
    }

    /**
     * Add a message to a ticket (admin can message any ticket)
     * @param ticketId The ticket ID
     * @param request Message request
     * @return Created message
     */
    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<MessageResponseDTO> addMessage(
            @PathVariable String ticketId,
            @Valid @RequestBody AddMessageRequestDTO request) {
        MessageResponseDTO message = ticketMessageService.addMessage(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}
