package apap.ti._5.accommodation_2306212083_be.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating a bill in the external Bill service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillRequestDTO {
    private String customerId;
    private String serviceName;
    private String serviceReferenceId;  // This is the booking ID
    private String description;
    private BigDecimal amount;
}
