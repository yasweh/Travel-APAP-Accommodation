package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Tour Package from external service
 * Endpoint: GET http://2306219575-be.hafizmuh.site/api/package/public/all
 * Response format: { status: 200, message: "...", timestamp: "...", data: [...] }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TourPackageDTO {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("packageName")
    private String packageName;
    
    @JsonProperty("quota")
    private Integer quota;
    
    @JsonProperty("price")
    private Long price;
    
    @JsonProperty("startDate")
    private String startDate;
    
    @JsonProperty("endDate")
    private String endDate;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("billId")
    private String billId;
}
