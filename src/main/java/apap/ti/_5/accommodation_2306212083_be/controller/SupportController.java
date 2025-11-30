package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.external.*;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.service.ExternalBookingService;
import apap.ti._5.accommodation_2306212083_be.service.PropertyService;
import apap.ti._5.accommodation_2306212083_be.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Support page - aggregates bookings from all external services
 * with authenticated API calls
 * 
 * Access Rules:
 * - Superadmin: Can see ALL bookings from all services (no filter)
 * - Accommodation Owner: Can see bookings related to their properties
 * - Customer: Can see only their own bookings
 */
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Slf4j
public class SupportController {

    private final ExternalBookingService externalBookingService;
    private final PropertyService propertyService;

    /**
     * Get all bookings from all external services
     * This endpoint requires authentication and forwards the token to external services
     * 
     * Access:
     * - Superadmin: All bookings, no filter
     * - Accommodation Owner: Bookings for their properties only
     * - Customer: Their own bookings only
     */
    @GetMapping("/bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAllBookings() {
        log.info("Fetching all bookings from external services for support page");
        
        String currentUserId = SecurityUtil.getCurrentUserId();
        String currentRole = SecurityUtil.getCurrentUserRole();
        
        // Determine filter mode based on role
        boolean isSuperadmin = SecurityUtil.isSuperadmin();
        boolean isAccommodationOwner = SecurityUtil.isAccommodationOwner();
        
        log.info("========== ROLE CHECK DEBUG ==========");
        log.info("Current user ID: {}", currentUserId);
        log.info("Current role: '{}' (exact string)", currentRole);
        log.info("isSuperadmin(): {}", isSuperadmin);
        log.info("isAccommodationOwner(): {}", isAccommodationOwner);
        log.info("======================================");
        
        // For Superadmin, no userId filter (pass null)
        // For Customer, filter by userId
        // For Accommodation Owner, we'll filter accommodation bookings by property ownership
        UUID userIdFilter = null;
        if (!isSuperadmin && !isAccommodationOwner && currentUserId != null) {
            userIdFilter = UUID.fromString(currentUserId);
            log.info("Applying userId filter: {}", userIdFilter);
        } else {
            log.info("NO userId filter applied (Superadmin or Owner mode)");
        }
        
        // Get list of property IDs owned by current user (for Accommodation Owner)
        Set<String> ownedPropertyIds = new HashSet<>();
        if (isAccommodationOwner && currentUserId != null) {
            try {
                UUID ownerUuid = UUID.fromString(currentUserId);
                List<Property> ownedProperties = propertyService.getPropertiesByOwner(ownerUuid);
                ownedPropertyIds = ownedProperties.stream()
                    .map(Property::getPropertyId)
                    .collect(Collectors.toSet());
                log.info("Accommodation Owner {} owns {} properties: {}", 
                    currentUserId, ownedPropertyIds.size(), ownedPropertyIds);
            } catch (Exception e) {
                log.error("Error fetching owned properties: {}", e.getMessage());
            }
        }
        
        List<Map<String, Object>> unifiedBookings = new ArrayList<>();
        Map<String, Integer> summaryBySource = new HashMap<>();
        
        final Set<String> finalOwnedPropertyIds = ownedPropertyIds;
        final UUID finalUserIdFilter = userIdFilter;
        
        // Fetch from each service
        // For external services (Tour, Insurance, Flight, Rental):
        // - Superadmin: fetch all (no userId filter)
        // - Customer: fetch only their own bookings
        // - Accommodation Owner: for external services, they see nothing (only accommodation)
        
        // For Accommodation:
        // - Superadmin: fetch all
        // - Customer: fetch only their own
        // - Accommodation Owner: fetch bookings for their properties
        
        try {
            // Tour Packages - Superadmin sees all, Customer sees own, Owner sees nothing
            List<?> tourPackages = externalBookingService.fetchBookingsByService(ServiceSource.TOUR, finalUserIdFilter);
            for (Object pkg : tourPackages) {
                if (pkg instanceof TourPackageDTO) {
                    TourPackageDTO tour = (TourPackageDTO) pkg;
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("id", tour.getId());
                    booking.put("source", "tour");
                    booking.put("userId", tour.getUserId());
                    booking.put("serviceName", tour.getPackageName());
                    booking.put("description", "Tour Package: " + tour.getPackageName());
                    booking.put("startDate", tour.getStartDate());
                    booking.put("endDate", tour.getEndDate());
                    booking.put("price", tour.getPrice());
                    booking.put("totalPrice", tour.getPrice());
                    booking.put("quota", tour.getQuota());
                    booking.put("status", tour.getStatus());
                    booking.put("billId", tour.getBillId());
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
            // Insurance Policies - Superadmin sees all, Customer sees own, Owner sees nothing
            List<?> insurances = externalBookingService.fetchBookingsByService(ServiceSource.INSURANCE, finalUserIdFilter);
            for (Object ins : insurances) {
                if (ins instanceof InsurancePolicyDTO) {
                    InsurancePolicyDTO policy = (InsurancePolicyDTO) ins;
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("id", policy.getId());
                    booking.put("source", "insurance");
                    booking.put("userId", policy.getUserId());
                    booking.put("serviceName", "Insurance Policy - " + policy.getId());
                    
                    // Build description from ordered plans if available
                    String description = "Service: " + policy.getService();
                    if (policy.getOrderedPlans() != null && !policy.getOrderedPlans().isEmpty()) {
                        var firstPlan = policy.getOrderedPlans().get(0);
                        if (firstPlan.getInsurancePlan() != null) {
                            description += " | Plan: " + firstPlan.getInsurancePlan().getPlanName();
                        }
                    }
                    booking.put("description", description);
                    
                    booking.put("startDate", policy.getStartDate());
                    booking.put("totalPrice", policy.getTotalPrice());
                    booking.put("totalCoverage", policy.getTotalCoverage());
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
            // Flight Bookings - Superadmin sees all, Customer sees own, Owner sees nothing
            List<?> flights = externalBookingService.fetchBookingsByService(ServiceSource.FLIGHT, finalUserIdFilter);
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
            // Rental Vehicles - Superadmin sees all, Customer sees own, Owner sees nothing
            List<?> rentals = externalBookingService.fetchBookingsByService(ServiceSource.RENTAL, finalUserIdFilter);
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
            // Special handling: 
            // - Superadmin: sees all (null filter)
            // - Customer: sees own bookings only
            // - Accommodation Owner: sees bookings for their properties
            
            // For accommodation owner, we pass null to get all, then filter by property
            UUID accommodationFilter = isAccommodationOwner ? null : finalUserIdFilter;
            
            List<?> accommodations = externalBookingService.fetchBookingsByService(ServiceSource.ACCOMMODATION, accommodationFilter);
            int accommodationCount = 0;
            
            for (Object acc : accommodations) {
                if (acc instanceof AccommodationBookingDTO) {
                    AccommodationBookingDTO accom = (AccommodationBookingDTO) acc;
                    
                    // For Accommodation Owner, filter by property ownership
                    if (isAccommodationOwner && !finalOwnedPropertyIds.isEmpty()) {
                        // Get property ID from booking
                        String propertyId = accom.getPropertyId();
                        if (propertyId == null || !finalOwnedPropertyIds.contains(propertyId)) {
                            continue; // Skip if not their property
                        }
                    }
                    
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("id", accom.getBookingId());
                    booking.put("source", "accommodation");
                    booking.put("userId", accom.getCustomerId() != null ? accom.getCustomerId().toString() : null);
                    booking.put("propertyId", accom.getPropertyId());
                    booking.put("serviceName", accom.getPropertyName());
                    booking.put("description", "Room: " + accom.getRoomName() + " - " + accom.getRoomTypeName());
                    booking.put("startDate", accom.getCheckInDate() != null ? accom.getCheckInDate().toString() : null);
                    booking.put("endDate", accom.getCheckOutDate() != null ? accom.getCheckOutDate().toString() : null);
                    booking.put("totalPrice", accom.getTotalPrice());
                    booking.put("status", getAccommodationStatus(accom.getStatus()));
                    booking.put("rawData", accom);
                    unifiedBookings.add(booking);
                    accommodationCount++;
                }
            }
            summaryBySource.put("accommodation", accommodationCount);
            log.info("Fetched {} accommodation bookings (filtered from {} total)", accommodationCount, accommodations.size());
        } catch (Exception e) {
            log.error("Error fetching accommodation bookings: {}", e.getMessage());
            summaryBySource.put("accommodation", 0);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", unifiedBookings);
        response.put("total", unifiedBookings.size());
        response.put("summaryBySource", summaryBySource);
        response.put("currentRole", currentRole);
        response.put("currentUserId", currentUserId);
        
        log.info("Total bookings fetched: {} for user {} with role {}", 
            unifiedBookings.size(), currentUserId, currentRole);
        
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
