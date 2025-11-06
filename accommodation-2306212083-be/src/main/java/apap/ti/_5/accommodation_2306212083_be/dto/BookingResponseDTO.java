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
public class BookingResponseDTO {
    
    private String bookingId;
    private String roomId;
    private String roomName;
    private String propertyName;
    
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private Integer totalDays;
    private Integer totalPrice;
    
    private Integer status;
    private String statusString;
    
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    private Boolean addOnBreakfast;
    private Integer capacity;
    
    private Integer refund;
    private Integer extraPay;
    
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
