package apap.ti._5.accommodation_2306212083_be.dto.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for generic booking information from external services.
 * This structure can accommodate different booking types.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingInfoDTO {

    /**
     * Booking ID from the external service
     */
    private String bookingId;

    /**
     * User ID who made the booking
     */
    private Long userId;

    /**
     * Current status of the booking
     */
    private String status;

    /**
     * When the booking was created
     */
    private String createdAt;

    /**
     * Service-specific details (flexible structure)
     * For accommodation: propertyName, roomType, checkInDate, checkOutDate
     * For flight: flightId, departureDate, passengers
     * For vehicle: vehicleType, rentalDuration
     * etc.
     */
    private Map<String, Object> details;

    /**
     * Total price/cost of the booking
     */
    private Integer totalPrice;
}
