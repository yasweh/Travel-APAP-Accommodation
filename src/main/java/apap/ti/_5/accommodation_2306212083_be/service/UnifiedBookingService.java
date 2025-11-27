package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.BookingResponseDTO;
import apap.ti._5.accommodation_2306212083_be.dto.UnifiedBookingDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service to aggregate bookings from all 5 microservices
 * - Tour (2306219575)
 * - Rental (2306203236)
 * - Insurance (2306240061)
 * - Accommodation (2306212083) - our service
 * - Flight (2306211660)
 */
@Service
@RequiredArgsConstructor
public class UnifiedBookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(UnifiedBookingService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BookingService bookingService;
    
    // External service URLs
    private static final String TOUR_API = "https://2306219575-be.hafizmuh.site/api/packages";
    private static final String RENTAL_API = "http://2306203236-be.hafizmuh.site/api/vehicle/";
    private static final String INSURANCE_API = "http://2306240061-be.hafizmuh.site/api/policy";
    private static final String ACCOMMODATION_API = "http://2306212083-be.hafizmuh.site/api/bookings";
    private static final String FLIGHT_API = "http://2306211660-be.hafizmuh.site/api/booking";
    
    /**
     * Fetch all bookings from all 5 services
     * Returns unified list with 'source' field
     */
    public List<UnifiedBookingDTO> getAllBookingsFromAllServices() {
        List<UnifiedBookingDTO> allBookings = new ArrayList<>();
        
        // Fetch from each service in parallel (or sequentially for now)
        allBookings.addAll(fetchTourPackages());
        allBookings.addAll(fetchRentalVehicles());
        allBookings.addAll(fetchInsurancePolicies());
        allBookings.addAll(fetchAccommodationBookings());
        allBookings.addAll(fetchFlightBookings());
        
        logger.info("Fetched total {} bookings from all services", allBookings.size());
        return allBookings;
    }
    
    /**
     * Fetch tour packages
     */
    private List<UnifiedBookingDTO> fetchTourPackages() {
        List<UnifiedBookingDTO> bookings = new ArrayList<>();
        try {
            logger.info("Fetching tour packages from {}", TOUR_API);
            Object response = restTemplate.getForObject(TOUR_API, Object.class);
            
            // Parse response
            JsonNode rootNode = objectMapper.valueToTree(response);
            JsonNode dataNode = rootNode.get("data");
            
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode packageNode : dataNode) {
                    UnifiedBookingDTO dto = new UnifiedBookingDTO();
                    dto.setSource("tour");
                    dto.setId(packageNode.path("id").asText());
                    dto.setUserId(packageNode.path("userId").asText());
                    dto.setServiceName(packageNode.path("packageName").asText());
                    dto.setDescription("Tour package");
                    dto.setQuota(packageNode.path("quota").asInt());
                    dto.setPrice(packageNode.path("price").asLong());
                    dto.setStatus(packageNode.path("status").asText());
                    dto.setStartDate(packageNode.path("startDate").asText());
                    dto.setEndDate(packageNode.path("endDate").asText());
                    dto.setRawData(packageNode);
                    
                    bookings.add(dto);
                }
            }
            
            logger.info("Fetched {} tour packages", bookings.size());
        } catch (Exception e) {
            logger.error("Failed to fetch tour packages: {}", e.getMessage());
        }
        return bookings;
    }
    
    /**
     * Fetch rental vehicles
     */
    private List<UnifiedBookingDTO> fetchRentalVehicles() {
        List<UnifiedBookingDTO> bookings = new ArrayList<>();
        try {
            logger.info("Fetching rental vehicles from {}", RENTAL_API);
            Object response = restTemplate.getForObject(RENTAL_API, Object.class);
            
            // Parse response
            JsonNode rootNode = objectMapper.valueToTree(response);
            
            // Check if it's array or has 'data' field
            JsonNode dataNode = rootNode.isArray() ? rootNode : rootNode.get("data");
            
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode vehicleNode : dataNode) {
                    UnifiedBookingDTO dto = new UnifiedBookingDTO();
                    dto.setSource("rental");
                    dto.setId(vehicleNode.path("id").asText());
                    dto.setServiceName(vehicleNode.path("brand").asText() + " " + 
                                     vehicleNode.path("model").asText() + " (" + 
                                     vehicleNode.path("year").asText() + ")");
                    dto.setDescription("Vehicle: " + vehicleNode.path("type").asText());
                    dto.setPrice(vehicleNode.path("price").asLong());
                    dto.setStatus(vehicleNode.path("status").asText());
                    dto.setRawData(vehicleNode);
                    
                    bookings.add(dto);
                }
            }
            
            logger.info("Fetched {} rental vehicles", bookings.size());
        } catch (Exception e) {
            logger.error("Failed to fetch rental vehicles: {}", e.getMessage());
        }
        return bookings;
    }
    
    /**
     * Fetch insurance policies
     */
    private List<UnifiedBookingDTO> fetchInsurancePolicies() {
        List<UnifiedBookingDTO> bookings = new ArrayList<>();
        try {
            logger.info("Fetching insurance policies from {}", INSURANCE_API);
            Object response = restTemplate.getForObject(INSURANCE_API, Object.class);
            
            // Parse response
            JsonNode rootNode = objectMapper.valueToTree(response);
            JsonNode dataNode = rootNode.isArray() ? rootNode : rootNode.get("data");
            
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode policyNode : dataNode) {
                    UnifiedBookingDTO dto = new UnifiedBookingDTO();
                    dto.setSource("insurance");
                    dto.setId(policyNode.path("id").asText());
                    dto.setUserId(policyNode.path("userId").asText());
                    dto.setServiceName("Policy for Booking " + policyNode.path("bookingId").asText());
                    dto.setDescription("Service: " + policyNode.path("service").asText());
                    dto.setTotalPrice(policyNode.path("totalPrice").asInt());
                    dto.setStatus(policyNode.path("status").asText());
                    dto.setStartDate(policyNode.path("startDate").asText());
                    dto.setRawData(policyNode);
                    
                    bookings.add(dto);
                }
            }
            
            logger.info("Fetched {} insurance policies", bookings.size());
        } catch (Exception e) {
            logger.error("Failed to fetch insurance policies: {}", e.getMessage());
        }
        return bookings;
    }
    
    /**
     * Fetch accommodation bookings (from our own service)
     */
    private List<UnifiedBookingDTO> fetchAccommodationBookings() {
        List<UnifiedBookingDTO> bookings = new ArrayList<>();
        try {
            logger.info("Fetching accommodation bookings from local service");
            
            // Use our own service instead of HTTP call
            List<BookingResponseDTO> localBookings = bookingService.getAllBookingsAsDTO();
            
            for (BookingResponseDTO booking : localBookings) {
                UnifiedBookingDTO dto = new UnifiedBookingDTO();
                dto.setSource("accommodation");
                dto.setId(booking.getBookingId());
                dto.setCustomerId(booking.getCustomerId());
                dto.setServiceName(booking.getPropertyName());
                dto.setDescription("Room: " + booking.getRoomName() + " - " + booking.getRoomTypeName());
                dto.setTotalPrice(booking.getTotalPrice());
                dto.setStatus(getStatusText(booking.getStatus()));
                dto.setStatusCode(booking.getStatus());
                dto.setStartDate(booking.getCheckInDate());
                dto.setEndDate(booking.getCheckOutDate());
                dto.setRawData(booking);
                
                bookings.add(dto);
            }
            
            logger.info("Fetched {} accommodation bookings", bookings.size());
        } catch (Exception e) {
            logger.error("Failed to fetch accommodation bookings: {}", e.getMessage());
        }
        return bookings;
    }
    
    /**
     * Fetch flight bookings
     */
    private List<UnifiedBookingDTO> fetchFlightBookings() {
        List<UnifiedBookingDTO> bookings = new ArrayList<>();
        try {
            logger.info("Fetching flight bookings from {}", FLIGHT_API);
            Object response = restTemplate.getForObject(FLIGHT_API, Object.class);
            
            // Parse response
            JsonNode rootNode = objectMapper.valueToTree(response);
            JsonNode dataNode = rootNode.isArray() ? rootNode : rootNode.get("data");
            
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode flightNode : dataNode) {
                    UnifiedBookingDTO dto = new UnifiedBookingDTO();
                    dto.setSource("flight");
                    dto.setId(flightNode.path("id").asText());
                    dto.setUserId(flightNode.path("userId").asText());
                    dto.setContactEmail(flightNode.path("contactEmail").asText());
                    dto.setContactPhone(flightNode.path("contactPhone").asText());
                    dto.setServiceName("Flight " + flightNode.path("flightId").asText());
                    dto.setDescription("Passengers: " + flightNode.path("passengerCount").asInt());
                    dto.setRawData(flightNode);
                    
                    bookings.add(dto);
                }
            }
            
            logger.info("Fetched {} flight bookings", bookings.size());
        } catch (Exception e) {
            logger.error("Failed to fetch flight bookings: {}", e.getMessage());
        }
        return bookings;
    }
    
    /**
     * Helper to convert status code to text
     */
    private String getStatusText(int status) {
        switch (status) {
            case 0: return "Waiting";
            case 1: return "Confirmed";
            case 2: return "Done";
            case 3: return "Cancelled";
            default: return "Unknown";
        }
    }
}
