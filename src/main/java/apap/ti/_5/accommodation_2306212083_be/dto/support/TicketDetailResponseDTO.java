package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for detailed ticket view (includes messages, progress, and external booking data)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponseDTO {
    
    private UUID id;
    private UUID userId;
    private String subject;
    private TicketStatus status;
    private ServiceSource serviceSource;
    private String externalBookingId;
    private String initialMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related data
    private List<MessageResponseDTO> messages;
    private List<ProgressResponseDTO> progressEntries;
    
    // External booking data (dynamic based on service source)
    private Object externalBookingData;
    private Boolean externalBookingDataAvailable;
}
