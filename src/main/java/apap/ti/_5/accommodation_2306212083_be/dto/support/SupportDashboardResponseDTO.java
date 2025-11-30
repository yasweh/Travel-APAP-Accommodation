package apap.ti._5.accommodation_2306212083_be.dto.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dashboard response containing all bookings from external services and existing tickets
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportDashboardResponseDTO {
    
    // Bookings from external services
    private List<?> accommodationBookings;
    private List<?> flightBookings;
    private List<?> rentalBookings;
    private List<?> tourBookings;
    private List<?> insuranceBookings;
    
    // Existing support tickets
    private List<TicketResponseDTO> supportTickets;
}
