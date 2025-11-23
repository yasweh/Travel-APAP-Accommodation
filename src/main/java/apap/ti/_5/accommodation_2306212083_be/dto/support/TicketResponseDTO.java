package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for basic support ticket responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponseDTO {

    /**
     * Unique ticket ID
     */
    private String ticketId;

    /**
     * User ID who created the ticket
     */
    private Long userId;

    /**
     * Admin ID assigned to this ticket (if any)
     */
    private Long assignedAdminId;

    /**
     * External service source
     */
    private ServiceSource externalServiceSource;

    /**
     * External booking ID
     */
    private String externalBookingId;

    /**
     * Ticket subject
     */
    private String subject;

    /**
     * Ticket description
     */
    private String description;

    /**
     * Current status
     */
    private TicketStatus status;

    /**
     * Priority level
     */
    private Priority priority;

    /**
     * Issue category
     */
    private Category category;

    /**
     * When the ticket was created
     */
    private LocalDateTime createdAt;

    /**
     * When the ticket was last updated
     */
    private LocalDateTime updatedAt;

    /**
     * When the ticket was closed (if closed)
     */
    private LocalDateTime closedAt;

    /**
     * Booking information from external service
     */
    private BookingInfoDTO bookingInfo;

    /**
     * Count of unread messages (for the current user)
     */
    private Integer unreadMessageCount;
}
