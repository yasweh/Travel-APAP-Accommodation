package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper DTO for Insurance Policy API response
 * The external API returns: { status: 200, message: "...", timestamp: "...", data: [...] }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsuranceApiResponse {
    
    @JsonProperty("status")
    private Integer status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("data")
    private List<InsurancePolicyDTO> data;
}
