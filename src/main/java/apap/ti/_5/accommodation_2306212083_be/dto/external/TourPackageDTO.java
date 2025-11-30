package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Tour Package from external service
 * Endpoint: GET https://2306219575-be.hafizmuh.site/api/packages
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
    private LocalDateTime startDate;
    
    @JsonProperty("endDate")
    private LocalDateTime endDate;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("destination")
    private String destination;
    
    @JsonProperty("description")
    private String description;
}
