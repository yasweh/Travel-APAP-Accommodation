package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.external.*;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.exception.ExternalServiceException;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306212083_be.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service to handle external API calls to fetch booking data from 5 services
 * 
 * Follows the pattern from Insurance service for token forwarding
 */
@Service
@Slf4j
public class ExternalBookingService {
    
    private final RestTemplate restTemplate;
    private final AccommodationBookingRepository accommodationBookingRepository;
    
    public ExternalBookingService(RestTemplate restTemplate, 
                                   AccommodationBookingRepository accommodationBookingRepository) {
        this.restTemplate = restTemplate;
        this.accommodationBookingRepository = accommodationBookingRepository;
    }
    
    /**
     * Get the current JWT token from the request context
     * Supports both Authorization header and JWT_TOKEN cookie
     */
    private String getCurrentToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Try Authorization header first
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    log.info("✓ Found JWT token in Authorization header: {}...{}", 
                        authHeader.substring(0, Math.min(25, authHeader.length())),
                        authHeader.length() > 30 ? authHeader.substring(authHeader.length() - 10) : "");
                    return authHeader;
                }
                
                // Try JWT_TOKEN cookie
                if (request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if ("JWT_TOKEN".equals(cookie.getName())) {
                            String token = "Bearer " + cookie.getValue();
                            log.info("✓ Found JWT token in JWT_TOKEN cookie: {}...{}", 
                                token.substring(0, Math.min(25, token.length())),
                                token.length() > 30 ? token.substring(token.length() - 10) : "");
                            return token;
                        }
                    }
                }
                
                log.warn("✗ No JWT token found in request (checked Authorization header and JWT_TOKEN cookie)");
            } else {
                log.warn("✗ No request context available - cannot get token");
            }
        } catch (Exception e) {
            log.error("✗ Error getting current token: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Create HTTP headers WITH JWT token for authenticated requests to external services
     * This forwards the user's JWT token to external APIs
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(org.springframework.http.MediaType.APPLICATION_JSON));
        
        String token = getCurrentToken();
        if (token != null) {
            headers.set("Authorization", token);
            log.info(">>> Sending request WITH Authorization header");
        } else {
            log.warn(">>> Sending request WITHOUT Authorization header (no token available)");
        }
        
        return headers;
    }
    
    /**
     * Fetch all bookings for a user from a specific service
     */
    public List<?> fetchBookingsByService(ServiceSource serviceSource, UUID userId) {
        try {
            String url = serviceSource.getApiUrl();
            
            // Add userId filter if needed (depends on each service's API)
            log.info("Fetching bookings from {}: {}", serviceSource, url);
            
            switch (serviceSource) {
                case ACCOMMODATION:
                    return fetchAccommodationBookings(url, userId);
                case INSURANCE:
                    return fetchInsurancePolicies(url, userId);
                case FLIGHT:
                    return fetchFlightBookings(url, userId);
                case RENTAL:
                    return fetchRentalVehicles(url, userId);
                case TOUR:
                    return fetchTourPackages(url, userId);
                default:
                    throw new ExternalServiceException("Unknown service source: " + serviceSource);
            }
        } catch (Exception e) {
            log.error("Error fetching bookings from {}: {}", serviceSource, e.getMessage());
            throw new ExternalServiceException("Failed to fetch bookings from " + serviceSource, e);
        }
    }
    
    /**
     * Fetch a specific booking by ID from external service
     */
    public Object fetchBookingById(ServiceSource serviceSource, String bookingId) {
        try {
            String url = serviceSource.getApiUrl();
            log.info("Fetching booking {} from {}", bookingId, serviceSource);
            
            switch (serviceSource) {
                case ACCOMMODATION:
                    return fetchAccommodationBookingById(url, bookingId);
                case INSURANCE:
                    return fetchInsurancePolicyById(url, bookingId);
                case FLIGHT:
                    return fetchFlightBookingById(url, bookingId);
                case RENTAL:
                    return fetchRentalVehicleById(url, bookingId);
                case TOUR:
                    return fetchTourPackageById(url, bookingId);
                default:
                    throw new ExternalServiceException("Unknown service source: " + serviceSource);
            }
        } catch (Exception e) {
            log.error("Error fetching booking {} from {}: {}", bookingId, serviceSource, e.getMessage());
            return null; // Return null if booking not found
        }
    }
    
    /**
     * Validate if a booking exists in the external service
     */
    public boolean validateBookingExists(ServiceSource serviceSource, String bookingId, UUID userId) {
        try {
            Object booking = fetchBookingById(serviceSource, bookingId);
            if (booking == null) {
                return false;
            }
            
            // Additional validation: check if booking belongs to user
            return validateBookingOwnership(booking, serviceSource, userId);
        } catch (Exception e) {
            log.error("Error validating booking: {}", e.getMessage());
            return false;
        }
    }
    
    // ========== Private methods for each service ==========
    
    private List<AccommodationBookingDTO> fetchAccommodationBookings(String baseUrl, UUID userId) {
        log.info("========== FETCHING ACCOMMODATION BOOKINGS ==========");
        log.info("Using LOCAL database (not external API)");
        log.info("UserId filter: {}", userId);
        
        try {
            List<AccommodationBooking> localBookings = accommodationBookingRepository.findAll();
            log.info("Found {} total bookings in local database", localBookings.size());
            
            // Convert to DTO format and filter by userId
            List<AccommodationBookingDTO> result = localBookings.stream()
                .filter(b -> b.getActiveStatus() == 1) // Only active bookings
                .map(this::convertToAccommodationDTO)
                .collect(Collectors.toList());
            
            log.info("After active filter: {} bookings", result.size());
            
            // Log first few bookings
            for (int i = 0; i < Math.min(3, result.size()); i++) {
                log.info("Booking[{}]: id={}, customerId={}, propertyId={}", 
                    i, result.get(i).getBookingId(), result.get(i).getCustomerId(), result.get(i).getPropertyId());
            }
            
            // Filter by userId if provided
            if (userId != null) {
                result = result.stream()
                    .filter(b -> b.getCustomerId() != null && b.getCustomerId().equals(userId))
                    .toList();
                log.info("After userId filter: {} accommodation bookings", result.size());
            }
            
            return result;
                
        } catch (Exception e) {
            log.error("!!! ERROR fetching accommodation bookings !!!");
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            e.printStackTrace();
            throw new ExternalServiceException("Failed to fetch accommodation bookings", e);
        }
    }
    
    private AccommodationBookingDTO fetchAccommodationBookingById(String baseUrl, String bookingId) {
        try {
            // USE LOCAL DATABASE instead of external API
            log.info("Fetching accommodation booking by ID from LOCAL database: {}", bookingId);
            
            return accommodationBookingRepository.findById(bookingId)
                .filter(b -> b.getActiveStatus() == 1)
                .map(this::convertToAccommodationDTO)
                .orElse(null);
                
        } catch (Exception e) {
            log.error("Error fetching accommodation booking by ID from local DB: {}", e.getMessage());
            return null;
        }
    }
    
    private AccommodationBookingDTO convertToAccommodationDTO(AccommodationBooking booking) {
        AccommodationBookingDTO dto = new AccommodationBookingDTO();
        dto.setBookingId(booking.getBookingId());
        dto.setUserId(booking.getCustomerId());
        dto.setCustomerId(booking.getCustomerId());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalDays(booking.getTotalDays());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus()); // Integer status: 0=WAITING, 1=CONFIRMED, 2=CANCELLED
        dto.setIsBreakfast(booking.getIsBreakfast());
        dto.setGuests(booking.getCapacity());
        dto.setCreatedDate(booking.getCreatedDate());
        dto.setUpdatedDate(booking.getUpdatedDate());
        
        // Property and room details
        if (booking.getRoom() != null) {
            dto.setRoomName(booking.getRoom().getName());
            
            if (booking.getRoom().getRoomType() != null) {
                dto.setRoomTypeName(booking.getRoom().getRoomType().getName());
                
                if (booking.getRoom().getRoomType().getProperty() != null) {
                    dto.setPropertyName(booking.getRoom().getRoomType().getProperty().getPropertyName());
                    dto.setPropertyId(booking.getRoom().getRoomType().getProperty().getPropertyId());
                }
            }
        }
        
        return dto;
    }
    
    private List<InsurancePolicyDTO> fetchInsurancePolicies(String baseUrl, UUID userId) {
        log.info("========== FETCHING INSURANCE POLICIES ==========");
        log.info("URL: {}", baseUrl);
        log.info("UserId filter: {}", userId);
        
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Making GET request to: {}", baseUrl);
            
            // Insurance API returns: { status: 200, message: "...", timestamp: "...", data: [...] }
            ResponseEntity<InsuranceApiResponse> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                entity,
                InsuranceApiResponse.class
            );
            
            log.info("Response status: {}", response.getStatusCode());
            log.info("Response headers: {}", response.getHeaders());
            
            InsuranceApiResponse apiResponse = response.getBody();
            log.info("Response body: {}", apiResponse);
            
            if (apiResponse != null) {
                log.info("API Response - status: {}, message: {}", apiResponse.getStatus(), apiResponse.getMessage());
                if (apiResponse.getData() != null) {
                    List<InsurancePolicyDTO> policies = apiResponse.getData();
                    log.info("Received {} insurance policies from API", policies.size());
                    
                    // Log each policy for debugging
                    for (int i = 0; i < Math.min(3, policies.size()); i++) {
                        log.info("Policy[{}]: id={}, userId={}, status={}", 
                            i, policies.get(i).getId(), policies.get(i).getUserId(), policies.get(i).getStatus());
                    }
                    
                    // Filter by userId if provided
                    if (userId != null) {
                        policies = policies.stream()
                            .filter(p -> p.getUserId() != null && p.getUserId().equals(userId.toString()))
                            .toList();
                        log.info("After userId filter: {} policies", policies.size());
                    }
                    
                    return policies;
                } else {
                    log.warn("API response data is NULL");
                }
            } else {
                log.warn("API response body is NULL");
            }
            
            return Collections.emptyList();
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            log.warn("Insurance API access denied (403 Forbidden) - user may not have permission");
            return Collections.emptyList();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.warn("Insurance API timeout or connection error: {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching insurance policies: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private InsurancePolicyDTO fetchInsurancePolicyById(String baseUrl, String policyId) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Insurance API returns: { status: 200, message: "...", timestamp: "...", data: [...] }
            ResponseEntity<InsuranceApiResponse> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                entity,
                InsuranceApiResponse.class
            );
            
            InsuranceApiResponse apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.getData() != null) {
                return apiResponse.getData().stream()
                    .filter(p -> p.getId().equals(policyId) || p.getBookingId().equals(policyId))
                    .findFirst()
                    .orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching insurance policy by ID: {}", e.getMessage());
            return null;
        }
    }
    
    private List<FlightBookingDTO> fetchFlightBookings(String baseUrl, UUID userId) {
        log.info("========== FETCHING FLIGHT BOOKINGS ==========");
        log.info("URL: {}", baseUrl);
        log.info("UserId filter: {}", userId);
        
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Making GET request to: {}", baseUrl);
            
            // Try to parse as wrapped response first (API returns object with data array)
            ResponseEntity<FlightBookingApiResponse> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                entity,
                FlightBookingApiResponse.class
            );
            
            log.info("Response status: {}", response.getStatusCode());
            
            FlightBookingApiResponse apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.getData() != null) {
                List<FlightBookingDTO> bookings = apiResponse.getData();
                log.info("Received {} flight bookings from API", bookings.size());
                
                // Log first few bookings
                for (int i = 0; i < Math.min(3, bookings.size()); i++) {
                    log.info("Flight[{}]: id={}, userId={}", 
                        i, bookings.get(i).getId(), bookings.get(i).getUserId());
                }
                
                if (userId != null) {
                    bookings = bookings.stream()
                        .filter(b -> b.getUserId() != null && b.getUserId().equals(userId))
                        .toList();
                    log.info("After userId filter: {} flight bookings", bookings.size());
                }
                
                return bookings;
            }
            
            return Collections.emptyList();
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            log.warn("Flight API access denied (403 Forbidden) - user may not have permission");
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching flight bookings: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private FlightBookingDTO fetchFlightBookingById(String baseUrl, String bookingId) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FlightBookingApiResponse> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                entity,
                FlightBookingApiResponse.class
            );
            
            FlightBookingApiResponse apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.getData() != null) {
                return apiResponse.getData().stream()
                    .filter(b -> b.getId().toString().equals(bookingId))
                    .findFirst()
                    .orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching flight booking by ID: {}", e.getMessage());
            return null;
        }
    }
    
    private List<RentalVehicleDTO> fetchRentalVehicles(String baseUrl, UUID userId) {
        log.info("========== FETCHING RENTAL VEHICLES ==========");
        log.info("URL: {}", baseUrl);
        log.info("UserId filter: {}", userId);
        
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Making GET request to: {}", baseUrl);
            
            ResponseEntity<RentalVehicleApiResponse> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                entity,
                RentalVehicleApiResponse.class
            );
            
            log.info("Response status: {}", response.getStatusCode());
            
            RentalVehicleApiResponse apiResponse = response.getBody();
            List<RentalVehicleDTO> vehicles = apiResponse != null ? apiResponse.getData() : null;
            log.info("Received {} rental vehicles from API", vehicles != null ? vehicles.size() : 0);
            
            if (vehicles != null && !vehicles.isEmpty()) {
                // Log first few vehicles
                for (int i = 0; i < Math.min(3, vehicles.size()); i++) {
                    log.info("Vehicle[{}]: id={}, userId={}", 
                        i, vehicles.get(i).getId(), vehicles.get(i).getUserId());
                }
                
                if (userId != null) {
                    vehicles = vehicles.stream()
                        .filter(v -> v.getUserId() != null && v.getUserId().equals(userId.toString()))
                        .toList();
                    log.info("After userId filter: {} rental vehicles", vehicles.size());
                }
            }
            
            return vehicles != null ? vehicles : Collections.emptyList();
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            log.warn("Rental API access denied (403 Forbidden) - user may not have permission");
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching rental vehicles: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private RentalVehicleDTO fetchRentalVehicleById(String baseUrl, String vehicleId) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<RentalVehicleApiResponse> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                entity,
                RentalVehicleApiResponse.class
            );
            
            RentalVehicleApiResponse apiResponse = response.getBody();
            List<RentalVehicleDTO> vehicles = apiResponse != null ? apiResponse.getData() : null;
            if (vehicles != null) {
                return vehicles.stream()
                    .filter(v -> v.getId().equals(vehicleId))
                    .findFirst()
                    .orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching rental vehicle by ID: {}", e.getMessage());
            return null;
        }
    }
    
    private List<TourPackageDTO> fetchTourPackages(String baseUrl, UUID userId) {
        log.info("========== FETCHING TOUR PACKAGES ==========");
        log.info("URL: {}", baseUrl);
        log.info("UserId filter: {}", userId);
        
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Making GET request to: {}", baseUrl);
            
            // Tour Package API returns: { status: 200, message: "...", timestamp: "...", data: [...] }
            ResponseEntity<TourPackageApiResponse> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                entity,
                TourPackageApiResponse.class
            );
            
            log.info("Response status: {}", response.getStatusCode());
            
            TourPackageApiResponse apiResponse = response.getBody();
            log.info("Response body: {}", apiResponse);
            
            if (apiResponse != null) {
                log.info("API Response - status: {}, message: {}", apiResponse.getStatus(), apiResponse.getMessage());
                if (apiResponse.getData() != null) {
                    List<TourPackageDTO> packages = apiResponse.getData();
                    log.info("Received {} tour packages from API", packages.size());
                    
                    // Log first few packages
                    for (int i = 0; i < Math.min(3, packages.size()); i++) {
                        log.info("Package[{}]: id={}, userId={}, name={}", 
                            i, packages.get(i).getId(), packages.get(i).getUserId(), packages.get(i).getPackageName());
                    }
                    
                    // Filter by userId if provided
                    if (userId != null) {
                        packages = packages.stream()
                            .filter(p -> p.getUserId() != null && p.getUserId().equals(userId.toString()))
                            .toList();
                        log.info("After userId filter: {} tour packages", packages.size());
                    }
                    
                    return packages;
                } else {
                    log.warn("API response data is NULL");
                }
            } else {
                log.warn("API response body is NULL");
            }
            
            return Collections.emptyList();
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            log.warn("Tour API access denied (403 Forbidden) - user may not have permission");
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching tour packages: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private TourPackageDTO fetchTourPackageById(String baseUrl, String packageId) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<TourPackageApiResponse> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                entity,
                TourPackageApiResponse.class
            );
            
            TourPackageApiResponse apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.getData() != null) {
                return apiResponse.getData().stream()
                    .filter(p -> p.getId().equals(packageId))
                    .findFirst()
                    .orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching tour package by ID: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate if booking belongs to the user
     */
    private boolean validateBookingOwnership(Object booking, ServiceSource serviceSource, UUID userId) {
        if (booking == null || userId == null) {
            return false;
        }
        
        try {
            switch (serviceSource) {
                case ACCOMMODATION:
                    AccommodationBookingDTO accBooking = (AccommodationBookingDTO) booking;
                    return accBooking.getCustomerId() != null && accBooking.getCustomerId().equals(userId);
                    
                case INSURANCE:
                    InsurancePolicyDTO policy = (InsurancePolicyDTO) booking;
                    return policy.getUserId() != null && policy.getUserId().equals(userId.toString());
                    
                case FLIGHT:
                    FlightBookingDTO flightBooking = (FlightBookingDTO) booking;
                    return flightBooking.getUserId() != null && flightBooking.getUserId().equals(userId);
                    
                case RENTAL:
                    RentalVehicleDTO vehicle = (RentalVehicleDTO) booking;
                    return vehicle.getUserId() != null && vehicle.getUserId().equals(userId.toString());
                    
                case TOUR:
                    TourPackageDTO tourPackage = (TourPackageDTO) booking;
                    return tourPackage.getUserId() != null && tourPackage.getUserId().equals(userId.toString());
                    
                default:
                    return false;
            }
        } catch (ClassCastException e) {
            log.error("Error validating booking ownership: {}", e.getMessage());
            return false;
        }
    }
}
