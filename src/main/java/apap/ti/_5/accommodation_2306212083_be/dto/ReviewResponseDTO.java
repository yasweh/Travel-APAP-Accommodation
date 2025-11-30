package apap.ti._5.accommodation_2306212083_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private UUID reviewId;
    private String bookingId;
    private String propertyId;
    private UUID customerId;
    private Double overallRating;
    private Integer cleanlinessRating;
    private Integer facilityRating;
    private Integer serviceRating;
    private Integer valueRating;
    private String comment;
    private LocalDateTime createdDate;

    // Additional info for display
    private String propertyName;
    private String customerName;
}
