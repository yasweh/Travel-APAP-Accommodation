package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for ticket list view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {
    
    private UUID id;
    private UUID userId;
    private String subject;
    private TicketStatus status;
    private ServiceSource serviceSource;
    private String externalBookingId;
    private String propertyId;
    private String propertyName;
    private String customerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer unreadMessagesCount;
}
