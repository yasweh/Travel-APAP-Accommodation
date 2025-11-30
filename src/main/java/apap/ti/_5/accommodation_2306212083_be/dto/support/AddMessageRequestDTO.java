package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for adding a message to ticket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMessageRequestDTO {
    
    @NotNull(message = "Sender ID is required")
    private UUID senderId;
    
    @NotNull(message = "Sender type is required")
    private SenderType senderType;
    
    @NotBlank(message = "Message is required")
    private String message;
}
