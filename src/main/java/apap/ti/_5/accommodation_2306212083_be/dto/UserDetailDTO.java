package apap.ti._5.accommodation_2306212083_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user detail from external user service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {
    private String id;
    private String username;
    private String email;
    private String name;
    private String role;
    private String gender;
    private Long saldo;
    private String createdAt;
    private String updatedAt;
    private String phone;
    private String address;
}
