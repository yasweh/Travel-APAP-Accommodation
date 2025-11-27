package apap.ti._5.accommodation_2306212083_be.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unified DTO for bookings from all 5 services
 * Supports: Tour, Rental, Insurance, Accommodation, Flight
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnifiedBookingDTO {
    
    // Common fields
    private String id; // Booking ID from source service
    private String source; // "tour", "rental", "insurance", "accommodation", "flight"
    
    // User/Customer info
    private String userId;
    private String customerId;
    private String contactEmail;
    private String contactPhone;
    
    // Booking details - flexible to support different services
    private String serviceName; // Package name, Vehicle type, Policy name, Property name, Flight ID
    private String description;
    
    // Dates
    private String startDate; // Check-in, rental start, policy start, departure
    private String endDate; // Check-out, rental end, policy end, arrival
    private LocalDateTime createdDate;
    
    // Pricing
    private Long price;
    private Integer totalPrice;
    private Integer quota;
    
    // Status
    private String status;
    private Integer statusCode;
    
    // Service-specific data (stored as raw object for flexibility)
    private Object rawData; // Original response dari service
    
    /**
     * Constructor for Tour Package
     */
    public static UnifiedBookingDTO fromTourPackage(Object packageData) {
        UnifiedBookingDTO dto = new UnifiedBookingDTO();
        dto.setSource("tour");
        dto.setRawData(packageData);
        // Mapping akan dilakukan di service layer
        return dto;
    }
    
    /**
     * Constructor for Rental Vehicle
     */
    public static UnifiedBookingDTO fromRentalVehicle(Object vehicleData) {
        UnifiedBookingDTO dto = new UnifiedBookingDTO();
        dto.setSource("rental");
        dto.setRawData(vehicleData);
        return dto;
    }
    
    /**
     * Constructor for Insurance Policy
     */
    public static UnifiedBookingDTO fromInsurancePolicy(Object policyData) {
        UnifiedBookingDTO dto = new UnifiedBookingDTO();
        dto.setSource("insurance");
        dto.setRawData(policyData);
        return dto;
    }
    
    /**
     * Constructor for Accommodation Booking
     */
    public static UnifiedBookingDTO fromAccommodationBooking(Object bookingData) {
        UnifiedBookingDTO dto = new UnifiedBookingDTO();
        dto.setSource("accommodation");
        dto.setRawData(bookingData);
        return dto;
    }
    
    /**
     * Constructor for Flight Booking
     */
    public static UnifiedBookingDTO fromFlightBooking(Object bookingData) {
        UnifiedBookingDTO dto = new UnifiedBookingDTO();
        dto.setSource("flight");
        dto.setRawData(bookingData);
        return dto;
    }
}
