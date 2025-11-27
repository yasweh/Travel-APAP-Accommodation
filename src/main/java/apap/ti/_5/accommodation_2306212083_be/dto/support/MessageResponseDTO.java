package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for ticket message responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponseDTO {

    /**
     * Message ID
     */
    private Long messageId;

    /**
     * Ticket ID this message belongs to
     */
    private String ticketId;

    /**
     * ID of the message sender
     */
    private String senderId;

    /**
     * Name of the sender
     */
    private String senderName;

    /**
     * Type of sender (USER, ADMIN, VENDOR)
     */
    private SenderType senderType;

    /**
     * Message content
     */
    private String messageBody;

    /**
     * Whether the message has been read
     */
    private Boolean isRead;

    /**
     * List of attachment URLs
     */
    private List<String> attachments;

    /**
     * When the message was created
     */
    private LocalDateTime createdAt;
}
