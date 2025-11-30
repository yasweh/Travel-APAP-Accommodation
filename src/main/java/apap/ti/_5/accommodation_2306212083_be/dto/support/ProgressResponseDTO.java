package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for ticket progress/timeline entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponseDTO {
    
    private UUID id;
    private UUID ticketId;
    private ActionType actionType;
    private String description;
    private UUID performedBy;
    private LocalDateTime performedAt;
}
