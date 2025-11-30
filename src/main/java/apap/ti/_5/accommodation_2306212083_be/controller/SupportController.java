package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.external.*;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.service.ExternalBookingService;
import apap.ti._5.accommodation_2306212083_be.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for Support page - aggregates bookings from all external services
 * with authenticated API calls
 */
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SupportController {

    private final ExternalBookingService externalBookingService;

    /**
     * Get all bookings from all external services
     * This endpoint requires authentication and forwards the token to external services
     */
    @GetMapping("/bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAllBookings() {
        log.info("Fetching all bookings from external services for support page");
        
        String currentUserId = SecurityUtil.getCurrentUserId();
        UUID userId = currentUserId != null ? UUID.fromString(currentUserId) : null;
        
        List<Map<String, Object>> unifiedBookings = new ArrayList<>();
        Map<String, Integer> summaryBySource = new HashMap<>();
        
        // Fetch from each service
        try {
            // Tour Packages
            List<?> tourPackages = externalBookingService.fetchBookingsByService(ServiceSource.TOUR, userId);
            for (Object pkg : tourPackages) {
                if (pkg instanceof TourPackageDTO) {
                    TourPackageDTO tour = (TourPackageDTO) pkg;
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("id", tour.getId());
                    booking.put("source", "tour");
                    booking.put("userId", tour.getUserId());
                    booking.put("serviceName", tour.getPackageName());
                    booking.put("description", tour.getDescription() != null ? tour.getDescription() : tour.getDestination());
                    booking.put("startDate", tour.getStartDate());
                    booking.put("endDate", tour.getEndDate());
                    booking.put("price", tour.getPrice());
                    booking.put("totalPrice", tour.getTotalPrice() != null ? tour.getTotalPrice() : tour.getPrice());
                    booking.put("quota", tour.getQuota());
                    booking.put("status", tour.getStatus());
                    booking.put("rawData", tour);
                    unifiedBookings.add(booking);
                }
            }
            summaryBySource.put("tour", tourPackages.size());
            log.info("Fetched {} tour packages", tourPackages.size());
        } catch (Exception e) {
            log.error("Error fetching tour packages: {}", e.getMessage());
            summaryBySource.put("tour", 0);
        }
        
        try {
            // Insurance Policies
            List<?> insurances = externalBookingService.fetchBookingsByService(ServiceSource.INSURANCE, userId);
            for (Object ins : insurances) {
                if (ins instanceof InsurancePolicyDTO) {
                    InsurancePolicyDTO policy = (InsurancePolicyDTO) ins;
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("id", policy.getId());
                    booking.put("source", "insurance");
                    booking.put("userId", policy.getUserId());
                    booking.put("serviceName", "Policy for Booking " + policy.getBookingId());
                    booking.put("description", "Service: " + policy.getService());
                    booking.put("startDate", policy.getStartDate());
                    booking.put("totalPrice", policy.getTotalPrice());
                    booking.put("status", policy.getStatus());
                    booking.put("rawData", policy);
                    unifiedBookings.add(booking);
                }
            }
            summaryBySource.put("insurance", insurances.size());
            log.info("Fetched {} insurance policies", insurances.size());
        } catch (Exception e) {
            log.error("Error fetching insurance policies: {}", e.getMessage());
            summaryBySource.put("insurance", 0);
        }
        
        try {
            // Flight Bookings
            List<?> flights = externalBookingService.fetchBookingsByService(ServiceSource.FLIGHT, userId);
            for (Object flt : flights) {
                if (flt instanceof FlightBookingDTO) {
                    FlightBookingDTO flight = (FlightBookingDTO) flt;
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("id", flight.getId() != null ? flight.getId().toString() : null);
                    booking.put("source", "flight");
                    booking.put("userId", flight.getUserId() != null ? flight.getUserId().toString() : null);
                    booking.put("serviceName", "Flight " + flight.getFlightId());
                    booking.put("description", flight.getDepartureCity() + " → " + flight.getArrivalCity() + " (" + flight.getPassengerCount() + " passengers)");
                    booking.put("startDate", flight.getDepartureTime());
                    booking.put("endDate", flight.getArrivalTime());
                    booking.put("totalPrice", flight.getTotalPrice());
                    booking.put("status", "Booked");
                    booking.put("rawData", flight);
                    unifiedBookings.add(booking);
                }
            }
            summaryBySource.put("flight", flights.size());
            log.info("Fetched {} flight bookings", flights.size());
        } catch (Exception e) {
            log.error("Error fetching flight bookings: {}", e.getMessage());
            summaryBySource.put("flight", 0);
        }
        
        try {
            // Rental Vehicles
            List<?> rentals = externalBookingService.fetchBookingsByService(ServiceSource.RENTAL, userId);
            for (Object rnt : rentals) {
                if (rnt instanceof RentalVehicleDTO) {
                    RentalVehicleDTO vehicle = (RentalVehicleDTO) rnt;
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("id", vehicle.getId());
                    booking.put("source", "rental");
                    booking.put("userId", vehicle.getUserId());
                    booking.put("serviceName", vehicle.getBrand() + " " + vehicle.getModel() + " (" + vehicle.getYear() + ")");
                    booking.put("description", "Location: " + vehicle.getLocation() + " | " + vehicle.getTransmission() + " | " + vehicle.getFuelType());
                    booking.put("price", vehicle.getPrice());
                    booking.put("status", vehicle.getStatus());
                    booking.put("rawData", vehicle);
                    unifiedBookings.add(booking);
                }
            }
            summaryBySource.put("rental", rentals.size());
            log.info("Fetched {} rental vehicles", rentals.size());
        } catch (Exception e) {
            log.error("Error fetching rental vehicles: {}", e.getMessage());
            summaryBySource.put("rental", 0);
        }
        
        try {
            // Accommodation Bookings (local)
            List<?> accommodations = externalBookingService.fetchBookingsByService(ServiceSource.ACCOMMODATION, userId);
            for (Object acc : accommodations) {
                if (acc instanceof AccommodationBookingDTO) {
                    AccommodationBookingDTO accom = (AccommodationBookingDTO) acc;
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("id", accom.getBookingId());
                    booking.put("source", "accommodation");
                    booking.put("userId", accom.getCustomerId() != null ? accom.getCustomerId().toString() : null);
                    booking.put("serviceName", accom.getPropertyName());
                    booking.put("description", "Room: " + accom.getRoomName() + " - " + accom.getRoomTypeName());
                    booking.put("startDate", accom.getCheckInDate() != null ? accom.getCheckInDate().toString() : null);
                    booking.put("endDate", accom.getCheckOutDate() != null ? accom.getCheckOutDate().toString() : null);
                    booking.put("totalPrice", accom.getTotalPrice());
                    booking.put("status", getAccommodationStatus(accom.getStatus()));
                    booking.put("rawData", accom);
                    unifiedBookings.add(booking);
                }
            }
            summaryBySource.put("accommodation", accommodations.size());
            log.info("Fetched {} accommodation bookings", accommodations.size());
        } catch (Exception e) {
            log.error("Error fetching accommodation bookings: {}", e.getMessage());
            summaryBySource.put("accommodation", 0);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", unifiedBookings);
        response.put("total", unifiedBookings.size());
        response.put("summaryBySource", summaryBySource);
        
        log.info("Total bookings fetched: {}", unifiedBookings.size());
        
        return ResponseEntity.ok(response);
    }
    
    private String getAccommodationStatus(Integer status) {
        if (status == null) return "Unknown";
        switch (status) {
            case 0: return "Waiting";
            case 1: return "Confirmed";
            case 2: return "Done";
            case 3: return "Cancelled";
            default: return "Unknown";
        }
    }
}
