package apap.ti._5.accommodation_2306212083_be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Profile Service token validation response
 * Maps response from POST https://2306219575-be.hafizmuh.site/api/auth/validate-token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    
    private Integer status;
    private String message;
    private TokenData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenData {
        private Boolean valid;
        
        @JsonProperty("userId")
        private String userId;
        
        private String username;
        private String email;
        private String name;
        private String role; // ADMIN, TRAVEL_AGENT, CUSTOMER
    }
}
