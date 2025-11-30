package apap.ti._5.accommodation_2306212083_be.dto.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for adding progress entry to ticket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProgressRequestDTO {
    
    @NotNull(message = "Performed by user ID is required")
    private UUID performedBy;
    
    @NotBlank(message = "Description is required")
    private String description;
}
