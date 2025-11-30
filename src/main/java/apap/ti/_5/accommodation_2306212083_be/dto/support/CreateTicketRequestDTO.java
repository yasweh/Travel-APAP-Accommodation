package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new support ticket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequestDTO {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotNull(message = "Service source is required")
    private ServiceSource serviceSource;
    
    @NotBlank(message = "External booking ID is required")
    private String externalBookingId;
    
    @NotBlank(message = "Initial message is required")
    private String initialMessage;
}
