package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.external.BillRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.external.BillResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with external Bill service
 * Bill service URL: http://2306211660-be.hafizmuh.site/api/bill
 */
@Service
@Slf4j
public class BillService {

    private static final String BILL_SERVICE_BASE_URL = "http://2306211660-be.hafizmuh.site/api/bill";
    private static final String CREATE_BILL_URL = BILL_SERVICE_BASE_URL + "/create";

    private final RestTemplate restTemplate;

    @Autowired
    public BillService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get JWT token from current HTTP request
     */
    private String getTokenFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    log.debug("Got JWT token from HTTP request header for Bill service");
                    return token;
                }
            }
        } catch (Exception e) {
            log.debug("Could not get token from request: {}", e.getMessage());
        }
        
        // Fallback to Security Context
        String token = apap.ti._5.accommodation_2306212083_be.util.SecurityUtil.getCurrentToken();
        if (token != null && !token.isEmpty()) {
            log.debug("Got JWT token from Security Context for Bill service");
            return token;
        }
        
        log.warn("No JWT token available for Bill service calls");
        return null;
    }

    /**
     * Create HTTP headers with authentication
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        String token = getTokenFromRequest();
        if (token != null && !token.isEmpty()) {
            headers.setBearerAuth(token);
            log.info("Bearer token added to Bill service request");
        } else {
            log.warn("No JWT token found - Bill service call may fail authentication");
        }
        
        return headers;
    }

    /**
     * Create a bill in the external Bill service
     * Called automatically when a booking is created
     * 
     * @param customerId Customer UUID
     * @param bookingId Booking ID (service reference)
     * @param description Description of the booking
     * @param amount Total price
     * @return BillResponseDTO if successful, null if failed
     */
    public BillResponseDTO createBill(String customerId, String bookingId, String description, BigDecimal amount) {
        log.info("Creating bill for booking {} with amount {}", bookingId, amount);
        
        try {
            BillRequestDTO request = BillRequestDTO.builder()
                .customerId(customerId)
                .serviceName("accommodation")
                .serviceReferenceId(bookingId)
                .description(description)
                .amount(amount)
                .build();
            
            HttpEntity<BillRequestDTO> entity = new HttpEntity<>(request, createAuthHeaders());
            
            log.info("Sending POST to {} with payload: {}", CREATE_BILL_URL, request);
            
            ResponseEntity<BillResponseDTO> response = restTemplate.exchange(
                CREATE_BILL_URL,
                HttpMethod.POST,
                entity,
                BillResponseDTO.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Bill created successfully: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Bill creation failed with status: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error creating bill for booking {}: {} - {}", 
                bookingId, e.getClass().getSimpleName(), e.getMessage());
            // Don't throw - booking creation should still succeed even if bill creation fails
            return null;
        }
    }

    /**
     * Create a bill with full booking details
     * 
     * @param customerId Customer UUID as string
     * @param bookingId Booking ID
     * @param propertyName Property name for description
     * @param roomTypeName Room type name for description
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @param totalPrice Total booking price
     * @return BillResponseDTO if successful, null if failed
     */
    public BillResponseDTO createBillForBooking(
            String customerId,
            String bookingId,
            String propertyName,
            String roomTypeName,
            String checkInDate,
            String checkOutDate,
            int totalPrice) {
        
        String description = String.format(
            "Accommodation Booking: %s - %s | Check-in: %s | Check-out: %s",
            propertyName,
            roomTypeName,
            checkInDate,
            checkOutDate
        );
        
        return createBill(customerId, bookingId, description, BigDecimal.valueOf(totalPrice));
    }
}
