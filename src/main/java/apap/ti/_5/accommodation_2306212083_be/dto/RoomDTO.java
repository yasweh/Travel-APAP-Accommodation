package apap.ti._5.accommodation_2306212083_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    
    private String roomId;
    private String name;
    private String roomTypeName;
    private Integer roomTypePrice;
    private Integer roomTypeCapacity;
    private String roomTypeFacility;
    private Integer floor;
    private Integer availabilityStatus;
    private Integer activeRoom;
}
