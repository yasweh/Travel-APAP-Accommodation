package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for Flight Booking from external service
 * Endpoint: GET http://2306211660-be.hafizmuh.site/api/booking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightBookingDTO {
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("flightId")
    private UUID flightId;
    
    @JsonProperty("userId")
    private UUID userId;
    
    @JsonProperty("contactEmail")
    private String contactEmail;
    
    @JsonProperty("contactPhone")
    private String contactPhone;
    
    @JsonProperty("passengerCount")
    private Integer passengerCount;
    
    @JsonProperty("passengers")
    private List<Object> passengers;  // Generic list for passenger details
    
    @JsonProperty("departureCity")
    private String departureCity;
    
    @JsonProperty("arrivalCity")
    private String arrivalCity;
    
    @JsonProperty("departureTime")
    private String departureTime;
    
    @JsonProperty("arrivalTime")
    private String arrivalTime;
    
    @JsonProperty("totalPrice")
    private Double totalPrice;
}
