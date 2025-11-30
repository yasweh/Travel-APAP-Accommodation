package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for updating ticket status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDTO {
    
    @NotNull(message = "Status is required")
    private TicketStatus status;
    
    @NotNull(message = "Updated by user ID is required")
    private UUID updatedBy;
    
    private String reason;  // Optional reason for status change
}
