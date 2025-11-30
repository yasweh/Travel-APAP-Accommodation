package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for bill response from external Bill service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillResponseDTO {
    private String id;
    private String billId;
    private String customerId;
    private String serviceName;
    private String serviceReferenceId;
    private String description;
    private BigDecimal amount;
    private String status;
    private String createdAt;
    private String paidAt;
}
