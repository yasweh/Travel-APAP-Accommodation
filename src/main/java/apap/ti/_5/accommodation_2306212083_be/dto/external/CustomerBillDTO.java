package apap.ti._5.accommodation_2306212083_be.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for bill data from Bill Service
 * Response from GET http://2306211660-be.hafizmuh.site/api/bill/customer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBillDTO {
    private String id;
    private String customerId;
    private String serviceName;
    private String serviceReferenceId;  // This is the booking ID for Accommodation
    private String description;
    private BigDecimal amount;
    private String status;  // "PAID" or "UNPAID"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paymentTimestamp;
    
    /**
     * Check if this bill is for accommodation service
     */
    public boolean isAccommodationBill() {
        return "Accommodation".equalsIgnoreCase(serviceName);
    }
    
    /**
     * Check if this bill has been paid
     */
    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(status);
    }
}
