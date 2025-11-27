package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service untuk validasi ownership dan akses berbasis role
 * Memastikan Accommodation Owner hanya bisa akses data milik mereka sendiri
 */
@Service
@RequiredArgsConstructor
public class OwnershipValidationService {

    private final PropertyService propertyService;
    private final BookingService bookingService;
    private final RoomRepository roomRepository;

    /**
     * Get current authenticated user
     */
    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        throw new AccessDeniedException("User not authenticated");
    }

    /**
     * Check if current user is Superadmin
     */
    public boolean isSuperadmin() {
        UserPrincipal user = getCurrentUser();
        return "SUPERADMIN".equals(user.getRole());
    }

    /**
     * Check if current user is Accommodation Owner
     */
    public boolean isOwner() {
        UserPrincipal user = getCurrentUser();
        return "ACCOMMODATION_OWNER".equals(user.getRole());
    }

    /**
     * Check if current user is Customer
     */
    public boolean isCustomer() {
        UserPrincipal user = getCurrentUser();
        return "CUSTOMER".equals(user.getRole());
    }

    /**
     * Validate property ownership
     * Accommodation Owner hanya bisa akses property milik mereka sendiri
     * Superadmin bisa akses semua property
     * 
     * @param propertyId ID property yang akan diakses
     * @throws AccessDeniedException jika user tidak punya akses
     */
    public void validatePropertyOwnership(String propertyId) {
        UserPrincipal user = getCurrentUser();
        
        // Superadmin bisa akses semua property
        if (isSuperadmin()) {
            return;
        }
        
        // Customer tidak boleh akses property management
        if (isCustomer()) {
            throw new AccessDeniedException("Customers cannot access property management");
        }
        
        // Owner hanya bisa akses property miliknya
        if (isOwner()) {
            Property property = propertyService.getPropertyById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
            
            if (!property.getOwnerId().toString().equals(user.getUserId())) {
                throw new AccessDeniedException("You can only access your own properties");
            }
        }
    }

    /**
     * Validate room ownership through property
     * 
     * @param roomId ID room yang akan diakses
     * @throws AccessDeniedException jika user tidak punya akses
     */
    public void validateRoomOwnership(String roomId) {
        UserPrincipal user = getCurrentUser();
        
        // Superadmin bisa akses semua
        if (isSuperadmin()) {
            return;
        }
        
        // Owner hanya bisa akses room dari property miliknya
        if (isOwner()) {
            Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
            
            Property property = room.getRoomType().getProperty();
            
            if (!property.getOwnerId().toString().equals(user.getUserId())) {
                throw new AccessDeniedException("You can only access rooms from your own properties");
            }
        }
    }

    /**
     * Validate room type ownership through property
     * 
     * @param roomType RoomType object yang akan diakses
     * @throws AccessDeniedException jika user tidak punya akses
     */
    public void validateRoomTypeOwnership(RoomType roomType) {
        UserPrincipal user = getCurrentUser();
        
        // Superadmin bisa akses semua
        if (isSuperadmin()) {
            return;
        }
        
        // Owner hanya bisa akses room type dari property miliknya
        if (isOwner()) {
            Property property = roomType.getProperty();
            
            if (property == null) {
                throw new RuntimeException("Room type does not have an associated property");
            }
            
            if (!property.getOwnerId().toString().equals(user.getUserId())) {
                throw new AccessDeniedException("You can only access room types from your own properties");
            }
        }
    }

    /**
     * Validate booking access
     * - Customer hanya bisa akses booking miliknya
     * - Owner hanya bisa akses booking untuk property miliknya
     * - Superadmin bisa akses semua booking
     * 
     * @param bookingId ID booking yang akan diakses
     * @throws AccessDeniedException jika user tidak punya akses
     */
    public void validateBookingAccess(String bookingId) {
        UserPrincipal user = getCurrentUser();
        
        // Superadmin bisa akses semua
        if (isSuperadmin()) {
            return;
        }
        
        AccommodationBooking booking = bookingService.getBookingById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Customer hanya bisa akses booking miliknya
        if (isCustomer()) {
            if (!booking.getCustomerId().toString().equals(user.getUserId())) {
                throw new AccessDeniedException("You can only access your own bookings");
            }
            return;
        }
        
        // Owner hanya bisa akses booking untuk property miliknya
        if (isOwner()) {
            Room room = booking.getRoom();
            Property property = room.getRoomType().getProperty();
            
            if (!property.getOwnerId().toString().equals(user.getUserId())) {
                throw new AccessDeniedException("You can only access bookings for your own properties");
            }
        }
    }

    /**
     * Validate booking ownership for modification
     * Hanya customer yang membuat booking yang bisa modify
     * 
     * @param bookingId ID booking yang akan dimodifikasi
     * @throws AccessDeniedException jika user bukan pemilik booking
     */
    public void validateBookingOwnership(String bookingId) {
        UserPrincipal user = getCurrentUser();
        
        AccommodationBooking booking = bookingService.getBookingById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Hanya customer pemilik booking yang bisa modify
        if (!booking.getCustomerId().toString().equals(user.getUserId())) {
            throw new AccessDeniedException("You can only modify your own bookings");
        }
    }

    /**
     * Check if user owns the property
     * 
     * @param propertyId ID property
     * @return true jika user adalah owner dari property tersebut atau superadmin
     */
    public boolean ownsProperty(String propertyId) {
        UserPrincipal user = getCurrentUser();
        
        if (isSuperadmin()) {
            return true;
        }
        
        if (!isOwner()) {
            return false;
        }
        
        Property property = propertyService.getPropertyById(propertyId)
            .orElseThrow(() -> new RuntimeException("Property not found"));
        
        return property.getOwnerId().toString().equals(user.getUserId());
    }

    /**
     * Validate current user is owner when creating property
     * Set ownerId to current user
     * 
     * @param property Property object yang akan dibuat
     */
    public void setOwnerForNewProperty(Property property) {
        UserPrincipal user = getCurrentUser();
        
        // Set ownerId ke current user jika owner
        if (isOwner()) {
            property.setOwnerId(UUID.fromString(user.getUserId()));
        }
        
        // Superadmin bisa set owner manual jika ada, atau ke diri sendiri
        if (isSuperadmin() && property.getOwnerId() == null) {
            property.setOwnerId(UUID.fromString(user.getUserId()));
        }
    }

    /**
     * Validate current user is customer when creating booking
     * Set customerId to current user
     * 
     * @param booking Booking object yang akan dibuat
     */
    public void setCustomerForNewBooking(AccommodationBooking booking) {
        UserPrincipal user = getCurrentUser();
        
        // Hanya customer yang bisa create booking
        if (!isCustomer()) {
            throw new AccessDeniedException("Only customers can create bookings");
        }
        
        // Set customerId ke current user
        booking.setCustomerId(UUID.fromString(user.getUserId()));
    }
}
