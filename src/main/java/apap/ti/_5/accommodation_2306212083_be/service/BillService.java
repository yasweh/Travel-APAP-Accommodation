package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.external.BillRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.external.BillResponseDTO;
import apap.ti._5.accommodation_2306212083_be.dto.external.CustomerBillDTO;
import apap.ti._5.accommodation_2306212083_be.dto.external.CustomerBillsResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for interacting with external Bill service
 * Bill service URL: http://2306211660-be.hafizmuh.site/api/bill
 * 
 * Payload yang dikirim:
 * - customerId: UUID customer
 * - serviceName: "Accommodation" (capitalized)
 * - serviceReferenceId: booking ID
 * - description: "Accommodation Bill" (hardcoded)
 * - amount: total price dari booking
 */
@Service
@Slf4j
public class BillService {

    private static final String BILL_SERVICE_BASE_URL = "http://2306211660-be.hafizmuh.site/api/bill";
    private static final String CREATE_BILL_URL = BILL_SERVICE_BASE_URL + "/create";
    private static final String GET_CUSTOMER_BILLS_URL = BILL_SERVICE_BASE_URL + "/customer";
    
    // Hardcoded values
    private static final String SERVICE_NAME = "Accommodation";
    private static final String DESCRIPTION = "Accommodation Bill";

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
                    log.info("Got JWT token from HTTP request header for Bill service");
                    return token;
                }
            }
        } catch (Exception e) {
            log.debug("Could not get token from request: {}", e.getMessage());
        }
        
        // Fallback to Security Context
        String token = apap.ti._5.accommodation_2306212083_be.util.SecurityUtil.getCurrentToken();
        if (token != null && !token.isEmpty()) {
            log.info("Got JWT token from Security Context for Bill service");
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
            log.debug("Bearer token added to Bill service request");
        } else {
            log.error("No JWT token found - Bill service call will fail authentication!");
        }
        
        return headers;
    }

    /**
     * Create a bill in the external Bill service
     * Called automatically when a booking is created
     * 
     * @param customerId Customer UUID
     * @param bookingId Booking ID (service reference)
     * @param amount Total price from booking
     * @return BillResponseDTO if successful, null if failed
     */
    public BillResponseDTO createBill(String customerId, String bookingId, BigDecimal amount) {
        log.info("========== CREATING BILL ==========");
        log.info("Target URL: {}", CREATE_BILL_URL);
        log.info("Customer ID: {}", customerId);
        log.info("Booking ID (serviceReferenceId): {}", bookingId);
        log.info("Service Name: {}", SERVICE_NAME);
        log.info("Description: {}", DESCRIPTION);
        log.info("Amount: {}", amount);
        log.info("====================================");
        
        try {
            BillRequestDTO request = BillRequestDTO.builder()
                .customerId(customerId)
                .serviceName(SERVICE_NAME)
                .serviceReferenceId(bookingId)
                .description(DESCRIPTION)
                .amount(amount)
                .build();
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<BillRequestDTO> entity = new HttpEntity<>(request, headers);
            
            log.info("Sending POST to Bill Service: {}", CREATE_BILL_URL);
            log.info("Request Body: {}", request);
            
            ResponseEntity<BillResponseDTO> response = restTemplate.exchange(
                CREATE_BILL_URL,
                HttpMethod.POST,
                entity,
                BillResponseDTO.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Bill created successfully!");
                log.info("Response Status: {}", response.getStatusCode());
                log.info("Response Body: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("❌ Bill creation failed with status: {}", response.getStatusCode());
                log.error("Response Body: {}", response.getBody());
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Error creating bill for booking {}", bookingId);
            log.error("Exception Type: {}", e.getClass().getSimpleName());
            log.error("Exception Message: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Create a bill for a booking
     * 
     * @param customerId Customer UUID as string
     * @param bookingId Booking ID
     * @param totalPrice Total booking price (MUST be accurate from booking)
     * @return BillResponseDTO if successful, null if failed
     */
    public BillResponseDTO createBillForBooking(String customerId, String bookingId, int totalPrice) {
        return createBill(customerId, bookingId, BigDecimal.valueOf(totalPrice));
    }

    /**
     * Get all bills for the current customer from Bill Service
     * Used to sync payment status with bookings
     * 
     * @return List of CustomerBillDTO, empty list if failed
     */
    public List<CustomerBillDTO> getCustomerBills() {
        log.info("========== GETTING CUSTOMER BILLS ==========");
        log.info("Target URL: {}", GET_CUSTOMER_BILLS_URL);
        
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<CustomerBillsResponseDTO> response = restTemplate.exchange(
                GET_CUSTOMER_BILLS_URL,
                HttpMethod.GET,
                entity,
                CustomerBillsResponseDTO.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                CustomerBillsResponseDTO body = response.getBody();
                log.info("✅ Got {} bills from Bill Service", body.getData() != null ? body.getData().size() : 0);
                return body.getData() != null ? body.getData() : new ArrayList<>();
            } else {
                log.warn("Failed to get customer bills, status: {}", response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error getting customer bills: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get set of booking IDs that have been paid in Bill Service
     * Filters only Accommodation bills with PAID status
     * 
     * @return Set of booking IDs (serviceReferenceId) that are PAID
     */
    public Set<String> getPaidAccommodationBookingIds() {
        List<CustomerBillDTO> bills = getCustomerBills();
        
        Set<String> paidBookingIds = bills.stream()
            .filter(CustomerBillDTO::isAccommodationBill)
            .filter(CustomerBillDTO::isPaid)
            .map(CustomerBillDTO::getServiceReferenceId)
            .collect(Collectors.toSet());
        
        log.info("Found {} PAID Accommodation bookings in Bill Service: {}", paidBookingIds.size(), paidBookingIds);
        return paidBookingIds;
    }
}
