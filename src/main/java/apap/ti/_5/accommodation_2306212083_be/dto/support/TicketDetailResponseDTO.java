package apap.ti._5.accommodation_2306212083_be.dto.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for detailed support ticket responses including messages and progress.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TicketDetailResponseDTO extends TicketResponseDTO {

    /**
     * List of messages in this ticket
     */
    private List<MessageResponseDTO> messages;

    /**
     * List of progress updates in this ticket
     */
    private List<ProgressResponseDTO> progress;

    @Builder(builderMethodName = "detailBuilder")
    public TicketDetailResponseDTO(
            String ticketId,
            String userId,
            String assignedAdminId,
            apap.ti._5.accommodation_2306212083_be.enums.ServiceSource externalServiceSource,
            String externalBookingId,
            String subject,
            String description,
            apap.ti._5.accommodation_2306212083_be.enums.TicketStatus status,
            apap.ti._5.accommodation_2306212083_be.enums.Priority priority,
            apap.ti._5.accommodation_2306212083_be.enums.Category category,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt,
            java.time.LocalDateTime closedAt,
            BookingInfoDTO bookingInfo,
            Integer unreadMessageCount,
            List<MessageResponseDTO> messages,
            List<ProgressResponseDTO> progress) {
        super(ticketId, userId, assignedAdminId, externalServiceSource, externalBookingId,
                subject, description, status, priority, category, createdAt, updatedAt,
                closedAt, bookingInfo, unreadMessageCount);
        this.messages = messages;
        this.progress = progress;
    }
}
