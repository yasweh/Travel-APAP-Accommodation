package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Insurance Policy from external service
 * Endpoint: GET http://2306240061-be.hafizmuh.site/api/policy
 * Response format: { status, message, timestamp, data: [...] }
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
    private String startDate;
    
    @JsonProperty("service")
    private String service;  // ACCOMMODATION, FLIGHT, etc.
    
    @JsonProperty("totalPrice")
    private Integer totalPrice;
    
    @JsonProperty("totalCoverage")
    private Integer totalCoverage;
    
    @JsonProperty("status")
    private String status;  // CREATED, ACTIVE, etc.
    
    @JsonProperty("orderedPlans")
    private List<OrderedPlanDTO> orderedPlans;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("updatedAt")
    private String updatedAt;
    
    /**
     * Nested DTO for ordered insurance plans
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderedPlanDTO {
        private String id;
        private String status;
        private String expiredDate;
        private InsurancePlanDTO insurancePlan;
        private Integer claimsCount;
    }
    
    /**
     * Nested DTO for insurance plan details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InsurancePlanDTO {
        private String id;
        private String planName;
        private Integer price;
    }
}
