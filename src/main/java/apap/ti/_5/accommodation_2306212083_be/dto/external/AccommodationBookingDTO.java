package apap.ti._5.accommodation_2306212083_be.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Accommodation Booking from external service
 * Endpoint: GET http://2306212083-be.hafizmuh.site/api/bookings
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccommodationBookingDTO {
    
    @JsonProperty("bookingId")
    private String bookingId;
    
    @JsonProperty("userId")
    private UUID userId;
    
    @JsonProperty("checkInDate")
    private LocalDateTime checkInDate;
    
    @JsonProperty("checkOutDate")
    private LocalDateTime checkOutDate;
    
    @JsonProperty("totalDays")
    private Integer totalDays;
    
    @JsonProperty("totalPrice")
    private Integer totalPrice;
    
    @JsonProperty("status")
    private Integer status;
    
    @JsonProperty("isBreakfast")
    private Boolean isBreakfast;
    
    @JsonProperty("refund")
    private Integer refund;
    
    @JsonProperty("extraPay")
    private Integer extraPay;
    
    @JsonProperty("guests")
    private Integer guests;
    
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;
    
    @JsonProperty("updatedDate")
    private LocalDateTime updatedDate;
    
    @JsonProperty("propertyName")
    private String propertyName;
    
    @JsonProperty("roomName")
    private String roomName;
    
    @JsonProperty("roomTypeName")
    private String roomTypeName;
}
