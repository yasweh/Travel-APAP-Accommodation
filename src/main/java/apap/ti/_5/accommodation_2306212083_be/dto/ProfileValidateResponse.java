package apap.ti._5.accommodation_2306212083_be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileValidateResponse {
    private Boolean valid;
    
    @JsonProperty("userId")
    private String userId;
    
    private String username;
    private String email;
    private String name;
    private String role;
}
