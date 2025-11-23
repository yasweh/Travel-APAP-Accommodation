package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.support.*;
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
 * REST Controller for user-facing support ticket operations.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/support-tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;
    private final TicketMessageService ticketMessageService;

    /**
     * Create a new support ticket
     * @param request Ticket creation request
     * @return Created ticket
     */
    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(@Valid @RequestBody CreateTicketRequestDTO request) {
        TicketResponseDTO ticket = supportTicketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    /**
     * Get all tickets for the current user
     * @return List of user's tickets
     */
    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets() {
        List<TicketResponseDTO> tickets = supportTicketService.getMyTickets();
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
     * Add a message to a ticket
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

    /**
     * Get all messages for a ticket
     * @param ticketId The ticket ID
     * @return List of messages
     */
    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable String ticketId) {
        List<MessageResponseDTO> messages = ticketMessageService.getMessages(ticketId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark messages as read
     * @param ticketId The ticket ID
     * @return Success response
     */
    @PutMapping("/{ticketId}/messages/mark-read")
    public ResponseEntity<Map<String, String>> markMessagesAsRead(@PathVariable String ticketId) {
        ticketMessageService.markMessagesAsRead(ticketId);
        return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    }

    /**
     * Close a ticket (user can close their own tickets)
     * @param ticketId The ticket ID
     * @return Success response
     */
    @PutMapping("/{ticketId}/close")
    public ResponseEntity<Map<String, String>> closeTicket(@PathVariable String ticketId) {
        supportTicketService.closeTicket(ticketId);
        return ResponseEntity.ok(Map.of("message", "Ticket closed successfully"));
    }
}
