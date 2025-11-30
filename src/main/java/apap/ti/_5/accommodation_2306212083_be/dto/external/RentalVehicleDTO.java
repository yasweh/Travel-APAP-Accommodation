package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Vehicle Rental from external service
 * Endpoint: GET http://2306203236-be.hafizmuh.site/api/vehicle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RentalVehicleDTO {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("rentalVendorId")
    private Integer rentalVendorId;
    
    @JsonProperty("brand")
    private String brand;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("year")
    private Integer year;
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("licensePlate")
    private String licensePlate;
    
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("transmission")
    private String transmission;
    
    @JsonProperty("fuelType")
    private String fuelType;
}
