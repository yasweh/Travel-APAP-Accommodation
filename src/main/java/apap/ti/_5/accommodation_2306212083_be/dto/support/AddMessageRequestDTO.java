package apap.ti._5.accommodation_2306212083_be.dto.support;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for adding a message to a support ticket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddMessageRequestDTO {

    /**
     * The message content
     */
    @NotBlank(message = "Message body is required")
    private String messageBody;

    /**
     * Optional list of attachment URLs
     */
    private List<String> attachments;
}
