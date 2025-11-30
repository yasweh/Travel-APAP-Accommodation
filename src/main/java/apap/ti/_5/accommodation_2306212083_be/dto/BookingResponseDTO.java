package apap.ti._5.accommodation_2306212083_be.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String roomTypeId;
    private String roomTypeName;
    private String propertyId;
    private String propertyName;
    
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkInDate;
    
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
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
    
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedDate;
}
