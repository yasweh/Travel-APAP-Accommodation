package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * API Response wrapper for Flight Booking service
 * The API returns an object with data array, not direct array
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightBookingApiResponse {
    
    @JsonProperty("status")
    private Integer status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private List<FlightBookingDTO> data;
    
    @JsonProperty("timestamp")
    private String timestamp;
}
