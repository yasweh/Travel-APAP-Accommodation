package apap.ti._5.accommodation_2306212083_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for payment callback from Bill service
 * When user pays in Bill service, it calls back to our endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackDTO {
    private String bookingId;   // serviceReferenceId from bill
    private String userId;      // customerId who paid
    private String billId;      // optional: the bill ID from bill service
    private String status;      // optional: payment status
}
