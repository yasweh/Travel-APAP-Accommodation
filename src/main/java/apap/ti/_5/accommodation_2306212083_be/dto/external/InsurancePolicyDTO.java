package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * DTO for Insurance Policy from external service
 * Endpoint: GET http://2306240061-be.hafizmuh.site/api/policy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsurancePolicyDTO {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("bookingId")
    private String bookingId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("startDate")
    private Date startDate;
    
    @JsonProperty("service")
    private String service;  // Enum as String
    
    @JsonProperty("totalPrice")
    private Integer totalPrice;
    
    @JsonProperty("totalCoverage")
    private Integer totalCoverage;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("listOrderedPlan")
    private List<Object> listOrderedPlan;  // Generic list for flexibility
}
