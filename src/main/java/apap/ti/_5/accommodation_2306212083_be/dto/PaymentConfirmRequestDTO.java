package apap.ti._5.accommodation_2306212083_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for receiving payment confirmation callback from Bill Service
 * Endpoint: POST /api/accommodation/payment/confirm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequestDTO {
    
    /**
     * The booking ID (serviceReferenceId in Bill Service)
     */
    private String serviceReferenceId;
    
    /**
     * The customer ID who made the payment
     */
    private String customerId;
}
