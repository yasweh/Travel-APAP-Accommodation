package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper DTO for Bill Service customer bills response
 * Response from GET http://2306211660-be.hafizmuh.site/api/bill/customer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerBillsResponseDTO {
    private int status;
    private String message;
    private String timestamp;
    private List<CustomerBillDTO> data;
}
