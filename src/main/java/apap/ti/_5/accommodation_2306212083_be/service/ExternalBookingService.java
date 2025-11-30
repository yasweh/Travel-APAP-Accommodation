package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.external.*;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.exception.ExternalServiceException;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service to handle external API calls to fetch booking data from 5 services
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
        try {
            // USE LOCAL DATABASE instead of external API
            log.info("Fetching accommodation bookings from LOCAL database");
            
            List<AccommodationBooking> localBookings = accommodationBookingRepository.findAll();
            
            // Convert to DTO format
            return localBookings.stream()
                .filter(b -> b.getActiveStatus() == 1) // Only active bookings
                .map(this::convertToAccommodationDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error fetching accommodation bookings from local DB: {}", e.getMessage());
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
                }
            }
        }
        
        return dto;
    }
    
    private List<InsurancePolicyDTO> fetchInsurancePolicies(String baseUrl, UUID userId) {
        try {
            ResponseEntity<List<InsurancePolicyDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<InsurancePolicyDTO>>() {}
            );
            
            List<InsurancePolicyDTO> policies = response.getBody();
            
            // Filter by userId - DISABLED for debug
            // if (policies != null && userId != null) {
            //     return policies.stream()
            //         .filter(p -> p.getUserId() != null && p.getUserId().equals(userId.toString()))
            //         .toList();
            // }
            
            return policies;
        } catch (Exception e) {
            log.error("Error fetching insurance policies: {}", e.getMessage());
            throw new ExternalServiceException("Failed to fetch insurance policies", e);
        }
    }
    
    private InsurancePolicyDTO fetchInsurancePolicyById(String baseUrl, String policyId) {
        try {
            ResponseEntity<List<InsurancePolicyDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<InsurancePolicyDTO>>() {}
            );
            
            List<InsurancePolicyDTO> policies = response.getBody();
            if (policies != null) {
                return policies.stream()
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
        try {
            ResponseEntity<List<FlightBookingDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FlightBookingDTO>>() {}
            );
            
            List<FlightBookingDTO> bookings = response.getBody();
            
            // Filter by userId - DISABLED for debug
            // if (bookings != null && userId != null) {
            //     return bookings.stream()
            //         .filter(b -> b.getUserId() != null && b.getUserId().equals(userId))
            //         .toList();
            // }
            
            return bookings;
        } catch (Exception e) {
            log.error("Error fetching flight bookings: {}", e.getMessage());
            throw new ExternalServiceException("Failed to fetch flight bookings", e);
        }
    }
    
    private FlightBookingDTO fetchFlightBookingById(String baseUrl, String bookingId) {
        try {
            ResponseEntity<List<FlightBookingDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FlightBookingDTO>>() {}
            );
            
            List<FlightBookingDTO> bookings = response.getBody();
            if (bookings != null) {
                return bookings.stream()
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
        try {
            ResponseEntity<List<RentalVehicleDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RentalVehicleDTO>>() {}
            );
            
            List<RentalVehicleDTO> vehicles = response.getBody();
            
            // Filter by userId - DISABLED for debug
            // if (vehicles != null && userId != null) {
            //     return vehicles.stream()
            //         .filter(v -> v.getUserId() != null && v.getUserId().equals(userId.toString()))
            //         .toList();
            // }
            
            return vehicles;
        } catch (Exception e) {
            log.error("Error fetching rental vehicles: {}", e.getMessage());
            throw new ExternalServiceException("Failed to fetch rental vehicles", e);
        }
    }
    
    private RentalVehicleDTO fetchRentalVehicleById(String baseUrl, String vehicleId) {
        try {
            ResponseEntity<List<RentalVehicleDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RentalVehicleDTO>>() {}
            );
            
            List<RentalVehicleDTO> vehicles = response.getBody();
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
        try {
            ResponseEntity<List<TourPackageDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TourPackageDTO>>() {}
            );
            
            List<TourPackageDTO> packages = response.getBody();
            
            // Filter by userId - DISABLED for debug
            // if (packages != null && userId != null) {
            //     return packages.stream()
            //         .filter(p -> p.getUserId() != null && p.getUserId().equals(userId.toString()))
            //         .toList();
            // }
            
            return packages;
        } catch (Exception e) {
            log.error("Error fetching tour packages: {}", e.getMessage());
            throw new ExternalServiceException("Failed to fetch tour packages", e);
        }
    }
    
    private TourPackageDTO fetchTourPackageById(String baseUrl, String packageId) {
        try {
            ResponseEntity<List<TourPackageDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TourPackageDTO>>() {}
            );
            
            List<TourPackageDTO> packages = response.getBody();
            if (packages != null) {
                return packages.stream()
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
