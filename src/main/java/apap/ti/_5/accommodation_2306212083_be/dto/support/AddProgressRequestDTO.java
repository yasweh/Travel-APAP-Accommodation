package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adding a progress update to a support ticket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProgressRequestDTO {

    /**
     * Description of the progress action taken
     */
    @NotBlank(message = "Progress text is required")
    private String text;

    /**
     * Type of action taken
     */
    @NotNull(message = "Action type is required")
    private ActionType actionType;
}
