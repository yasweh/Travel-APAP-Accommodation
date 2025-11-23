package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for support progress responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressResponseDTO {

    /**
     * Progress ID
     */
    private Long progressId;

    /**
     * Ticket ID this progress belongs to
     */
    private String ticketId;

    /**
     * ID of the user who created this progress
     */
    private Long userId;

    /**
     * Name of the user
     */
    private String userName;

    /**
     * Progress description
     */
    private String text;

    /**
     * Role of the person creating progress
     */
    private String role;

    /**
     * Type of action taken
     */
    private ActionType actionType;

    /**
     * When the progress was created
     */
    private LocalDateTime createdAt;
}
