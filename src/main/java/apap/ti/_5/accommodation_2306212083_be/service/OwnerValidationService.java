package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306212083_be.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service to validate property ownership
 * Ensures owners can only modify their own properties
 */
@Service
@RequiredArgsConstructor
public class OwnerValidationService {

    private final PropertyRepository propertyRepository;
    private final AccommodationBookingRepository bookingRepository;

    /**
     * Validate if current user owns the property
     * SUPERADMIN can access any property
     * ACCOMMODATION_OWNER can only access their own properties
     * 
     * @param propertyId Property ID to check
     * @throws AccessDeniedException if user doesn't own the property
     */
    public void validateOwnership(String propertyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new AccessDeniedException("User not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // SUPERADMIN can access any property
        if ("SUPERADMIN".equals(userPrincipal.getRole())) {
            return;
        }

        // Find property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        // Check if user owns the property
        UUID userUuid = UUID.fromString(userPrincipal.getUserId());
        if (!property.getOwnerId().equals(userUuid)) {
            throw new AccessDeniedException("You don't have permission to modify this property. Owners can only access their own properties.");
        }
    }

    /**
     * Validate if current user owns the property associated with a booking
     * 
     * @param bookingId Booking ID to check
     * @throws AccessDeniedException if user doesn't own the property
     */
    public void validateBookingPropertyOwnership(String bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new AccessDeniedException("User not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // SUPERADMIN can access any booking
        if ("SUPERADMIN".equals(userPrincipal.getRole())) {
            return;
        }

        // Find booking and its associated property
        AccommodationBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        Room room = booking.getRoom();
        RoomType roomType = room.getRoomType();
        Property property = roomType.getProperty();

        // Check if user owns the property
        UUID userUuid = UUID.fromString(userPrincipal.getUserId());
        if (!property.getOwnerId().equals(userUuid)) {
            throw new AccessDeniedException("You can only access bookings for your own properties");
        }
    }

    /**
     * Get current authenticated user
     */
    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Check if current user is Superadmin
     */
    public boolean isSuperadmin() {
        UserPrincipal user = getCurrentUser();
        return user != null && "SUPERADMIN".equals(user.getRole());
    }

    /**
     * Check if current user is Accommodation Owner
     */
    public boolean isOwner() {
        UserPrincipal user = getCurrentUser();
        return user != null && "ACCOMMODATION_OWNER".equals(user.getRole());
    }

    /**
     * Check if current user is Customer
     */
    public boolean isCustomer() {
        UserPrincipal user = getCurrentUser();
        return user != null && "CUSTOMER".equals(user.getRole());
    }

    /**
     * Validate booking ownership for customer modifications
     * Customer can only modify their own bookings
     * 
     * @param bookingId Booking ID to check
     * @throws AccessDeniedException if user doesn't own the booking
     */
    public void validateBookingOwnership(String bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new AccessDeniedException("User not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Find booking
        AccommodationBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Check if user is the customer who made the booking
        UUID userUuid = UUID.fromString(userPrincipal.getUserId());
        if (!booking.getCustomerId().equals(userUuid)) {
            throw new AccessDeniedException("You can only modify your own bookings");
        }
    }

    /**
     * Validate booking access for viewing
     * - Customer: can only view their own bookings
     * - Owner: can only view bookings for their properties
     * - Superadmin: can view all bookings
     * 
     * @param bookingId Booking ID to check
     * @throws AccessDeniedException if user doesn't have access
     */
    public void validateBookingAccess(String bookingId) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        // Superadmin can access all bookings
        if (isSuperadmin()) {
            return;
        }
        
        AccommodationBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        UUID userUuid = UUID.fromString(user.getUserId());
        
        // Customer can only access their own bookings
        if (isCustomer()) {
            if (!booking.getCustomerId().equals(userUuid)) {
                throw new AccessDeniedException("You can only access your own bookings");
            }
            return;
        }
        
        // Owner can only access bookings for their properties
        if (isOwner()) {
            Room room = booking.getRoom();
            RoomType roomType = room.getRoomType();
            Property property = roomType.getProperty();
            
            if (!property.getOwnerId().equals(userUuid)) {
                throw new AccessDeniedException("You can only access bookings for your own properties");
            }
        }
    }

    /**
     * Validate room type ownership
     * Owner can only access room types from their properties
     * 
     * @param roomType RoomType object to check
     * @throws AccessDeniedException if user doesn't own the property
     */
    public void validateRoomTypeOwnership(RoomType roomType) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        // Superadmin can access all
        if (isSuperadmin()) {
            return;
        }
        
        // Owner can only access room types from their properties
        if (isOwner()) {
            Property property = roomType.getProperty();
            UUID userUuid = UUID.fromString(user.getUserId());
            
            if (!property.getOwnerId().equals(userUuid)) {
                throw new AccessDeniedException("You can only access room types from your own properties");
            }
        }
    }

    /**
     * Validate room ownership through its property
     * 
     * @param room Room object to check
     * @throws AccessDeniedException if user doesn't own the property
     */
    public void validateRoomOwnership(Room room) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        // Superadmin can access all
        if (isSuperadmin()) {
            return;
        }
        
        // Owner can only access rooms from their properties
        if (isOwner()) {
            RoomType roomType = room.getRoomType();
            Property property = roomType.getProperty();
            UUID userUuid = UUID.fromString(user.getUserId());
            
            if (!property.getOwnerId().equals(userUuid)) {
                throw new AccessDeniedException("You can only access rooms from your own properties");
            }
        }
    }

    /**
     * Set owner for new property
     * Automatically assigns current user as owner for new properties
     * 
     * @param property Property to set owner for
     */
    public void setOwnerForNewProperty(Property property) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        // Set ownerId to current user
        UUID userUuid = UUID.fromString(user.getUserId());
        property.setOwnerId(userUuid);
    }

    /**
     * Set customer for new booking
     * Automatically assigns current user as customer for new bookings
     * 
     * @param booking Booking to set customer for
     */
    public void setCustomerForNewBooking(AccommodationBooking booking) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        // Only customers can create bookings
        if (!isCustomer()) {
            throw new AccessDeniedException("Only customers can create bookings");
        }
        
        // Set customerId to current user
        UUID userUuid = UUID.fromString(user.getUserId());
        booking.setCustomerId(userUuid);
    }
}
