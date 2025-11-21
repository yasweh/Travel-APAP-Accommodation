package apap.ti._5.accommodation_2306212083_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyStatisticsDTO {
    
    private String propertyId;
    private String propertyName;
    private Integer totalIncome;
    private Integer bookingCount;
}
