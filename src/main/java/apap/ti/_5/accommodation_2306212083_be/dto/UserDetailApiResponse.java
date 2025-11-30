package apap.ti._5.accommodation_2306212083_be.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * API Response wrapper for user detail from external service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailApiResponse {
    private Integer status;
    private String message;
    private UserDetailDTO data;
    private Date timestamp;
}
