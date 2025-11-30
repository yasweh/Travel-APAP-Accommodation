package apap.ti._5.accommodation_2306212083_be.dto.external;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalVehicleApiResponse {
    private Integer status;
    private String message;
    private List<RentalVehicleDTO> data;
    private String timestamp;
}
