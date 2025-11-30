package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.SenderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for ticket messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    
    private UUID id;
    private UUID ticketId;
    private SenderType senderType;
    private UUID senderId;
    private String message;
    private LocalDateTime sentAt;
    private Boolean readByRecipient;
}
