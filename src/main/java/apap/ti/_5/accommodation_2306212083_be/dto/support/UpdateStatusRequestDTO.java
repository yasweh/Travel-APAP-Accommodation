package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating the status of a support ticket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusRequestDTO {

    /**
     * New status for the ticket
     */
    @NotNull(message = "Status is required")
    private TicketStatus status;
}
