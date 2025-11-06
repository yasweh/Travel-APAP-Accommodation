package apap.ti._5.accommodation_2306212083_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {
    
    private String roomId;
    private String propertyId; // For cascading dropdown
    private String roomTypeId; // For cascading dropdown
    
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    private Boolean addOnBreakfast;
    private Integer capacity;
}
