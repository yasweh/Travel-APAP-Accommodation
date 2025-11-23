package apap.ti._5.accommodation_2306212083_be.dto.support;

import apap.ti._5.accommodation_2306212083_be.enums.Category;
import apap.ti._5.accommodation_2306212083_be.enums.Priority;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new support ticket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketRequestDTO {

    /**
     * The service where the booking was made
     */
    @NotNull(message = "External service source is required")
    private ServiceSource externalServiceSource;

    /**
     * The booking ID from the external service
     */
    @NotBlank(message = "External booking ID is required")
    @Size(max = 100, message = "Booking ID cannot exceed 100 characters")
    private String externalBookingId;

    /**
     * Subject/title of the ticket
     */
    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    private String subject;

    /**
     * Detailed description of the issue
     */
    @NotBlank(message = "Description is required")
    private String description;

    /**
     * Priority level (defaults to MEDIUM if not provided)
     */
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    /**
     * Category of the issue
     */
    @NotNull(message = "Category is required")
    private Category category;
}
