package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnershipValidationServiceTest {

    @Mock
    private PropertyService propertyService;

    @Mock
    private BookingService bookingService;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private OwnershipValidationService ownershipValidationService;

    private UserPrincipal superadminUser;
    private UserPrincipal ownerUser;
    private UserPrincipal customerUser;
    private Property testProperty;
    private Room testRoom;
    private RoomType testRoomType;
    private AccommodationBooking testBooking;
    private UUID ownerId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        superadminUser = UserPrincipal.builder()
                .userId(UUID.randomUUID().toString())
                .role("SUPERADMIN")
                .build();

        ownerUser = UserPrincipal.builder()
                .userId(ownerId.toString())
                .role("ACCOMMODATION_OWNER")
                .build();

        customerUser = UserPrincipal.builder()
                .userId(customerId.toString())
                .role("CUSTOMER")
                .build();

        testProperty = new Property();
        testProperty.setPropertyId("prop-001");
        testProperty.setOwnerId(ownerId);

        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("rt-001");
        testRoomType.setProperty(testProperty);

        testRoom = new Room();
        testRoom.setRoomId("room-001");
        testRoom.setRoomType(testRoomType);

        testBooking = new AccommodationBooking();
        testBooking.setBookingId("booking-001");
        testBooking.setCustomerId(customerId);
        testBooking.setRoom(testRoom);
    }

    private void mockSecurityContext(UserPrincipal user) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCurrentUser_Success() {
        mockSecurityContext(ownerUser);

        UserPrincipal result = ownershipValidationService.getCurrentUser();

        assertNotNull(result);
        assertEquals(ownerId.toString(), result.getUserId());
    }

    @Test
    void getCurrentUser_NotAuthenticated_ThrowsException() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.setContext(securityContext);

        assertThrows(AccessDeniedException.class, () ->
                ownershipValidationService.getCurrentUser());
    }

    @Test
    void isSuperadmin_True() {
        mockSecurityContext(superadminUser);

        assertTrue(ownershipValidationService.isSuperadmin());
    }

    @Test
    void isSuperadmin_False() {
        mockSecurityContext(ownerUser);

        assertFalse(ownershipValidationService.isSuperadmin());
    }

    @Test
    void isOwner_True() {
        mockSecurityContext(ownerUser);

        assertTrue(ownershipValidationService.isOwner());
    }

    @Test
    void isOwner_False() {
        mockSecurityContext(customerUser);

        assertFalse(ownershipValidationService.isOwner());
    }

    @Test
    void isCustomer_True() {
        mockSecurityContext(customerUser);

        assertTrue(ownershipValidationService.isCustomer());
    }

    @Test
    void isCustomer_False() {
        mockSecurityContext(ownerUser);

        assertFalse(ownershipValidationService.isCustomer());
    }

    @Test
    void validatePropertyOwnership_AsSuperadmin_NoException() {
        mockSecurityContext(superadminUser);

        assertDoesNotThrow(() ->
                ownershipValidationService.validatePropertyOwnership("prop-001"));
    }

    @Test
    void validatePropertyOwnership_AsCustomer_ThrowsException() {
        mockSecurityContext(customerUser);

        assertThrows(AccessDeniedException.class, () ->
                ownershipValidationService.validatePropertyOwnership("prop-001"));
    }

    @Test
    void validatePropertyOwnership_AsOwner_OwnProperty_NoException() {
        mockSecurityContext(ownerUser);
        when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));

        assertDoesNotThrow(() ->
                ownershipValidationService.validatePropertyOwnership("prop-001"));
    }

    @Test
    void validatePropertyOwnership_AsOwner_OtherProperty_ThrowsException() {
        mockSecurityContext(ownerUser);
        
        Property otherProperty = new Property();
        otherProperty.setPropertyId("prop-002");
        otherProperty.setOwnerId(UUID.randomUUID()); // Different owner
        
        when(propertyService.getPropertyById("prop-002")).thenReturn(Optional.of(otherProperty));

        assertThrows(AccessDeniedException.class, () ->
                ownershipValidationService.validatePropertyOwnership("prop-002"));
    }

    @Test
    void validateRoomOwnership_AsSuperadmin_NoException() {
        mockSecurityContext(superadminUser);

        assertDoesNotThrow(() ->
                ownershipValidationService.validateRoomOwnership("room-001"));
    }

    @Test
    void validateRoomOwnership_AsOwner_OwnRoom_NoException() {
        mockSecurityContext(ownerUser);
        when(roomRepository.findById("room-001")).thenReturn(Optional.of(testRoom));

        assertDoesNotThrow(() ->
                ownershipValidationService.validateRoomOwnership("room-001"));
    }

    @Test
    void validateRoomOwnership_AsOwner_OtherRoom_ThrowsException() {
        mockSecurityContext(ownerUser);
        
        Property otherProperty = new Property();
        otherProperty.setOwnerId(UUID.randomUUID());
        RoomType otherRoomType = new RoomType();
        otherRoomType.setProperty(otherProperty);
        Room otherRoom = new Room();
        otherRoom.setRoomType(otherRoomType);
        
        when(roomRepository.findById("room-002")).thenReturn(Optional.of(otherRoom));

        assertThrows(AccessDeniedException.class, () ->
                ownershipValidationService.validateRoomOwnership("room-002"));
    }

    @Test
    void validateRoomTypeOwnership_AsSuperadmin_NoException() {
        mockSecurityContext(superadminUser);

        assertDoesNotThrow(() ->
                ownershipValidationService.validateRoomTypeOwnership(testRoomType));
    }

    @Test
    void validateRoomTypeOwnership_AsOwner_OwnRoomType_NoException() {
        mockSecurityContext(ownerUser);

        assertDoesNotThrow(() ->
                ownershipValidationService.validateRoomTypeOwnership(testRoomType));
    }

    @Test
    void validateRoomTypeOwnership_AsOwner_NullProperty_ThrowsException() {
        mockSecurityContext(ownerUser);
        
        RoomType orphanRoomType = new RoomType();
        orphanRoomType.setProperty(null);

        assertThrows(RuntimeException.class, () ->
                ownershipValidationService.validateRoomTypeOwnership(orphanRoomType));
    }

    @Test
    void validateBookingAccess_AsSuperadmin_NoException() {
        mockSecurityContext(superadminUser);

        assertDoesNotThrow(() ->
                ownershipValidationService.validateBookingAccess("booking-001"));
    }

    @Test
    void validateBookingAccess_AsCustomer_OwnBooking_NoException() {
        mockSecurityContext(customerUser);
        when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));

        assertDoesNotThrow(() ->
                ownershipValidationService.validateBookingAccess("booking-001"));
    }

    @Test
    void validateBookingAccess_AsCustomer_OtherBooking_ThrowsException() {
        mockSecurityContext(customerUser);
        
        AccommodationBooking otherBooking = new AccommodationBooking();
        otherBooking.setCustomerId(UUID.randomUUID()); // Different customer
        
        when(bookingService.getBookingById("booking-002")).thenReturn(Optional.of(otherBooking));

        assertThrows(AccessDeniedException.class, () ->
                ownershipValidationService.validateBookingAccess("booking-002"));
    }

    @Test
    void validateBookingAccess_AsOwner_OwnPropertyBooking_NoException() {
        mockSecurityContext(ownerUser);
        when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));

        assertDoesNotThrow(() ->
                ownershipValidationService.validateBookingAccess("booking-001"));
    }

    @Test
    void validateBookingOwnership_AsOwner_OwnBooking_NoException() {
        // Booking owner same as current user
        UserPrincipal bookingOwner = UserPrincipal.builder()
                .userId(customerId.toString())
                .role("CUSTOMER")
                .build();
        mockSecurityContext(bookingOwner);
        when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));

        assertDoesNotThrow(() ->
                ownershipValidationService.validateBookingOwnership("booking-001"));
    }

    @Test
    void validateBookingOwnership_AsOther_ThrowsException() {
        UserPrincipal otherUser = UserPrincipal.builder()
                .userId(UUID.randomUUID().toString())
                .role("CUSTOMER")
                .build();
        mockSecurityContext(otherUser);
        when(bookingService.getBookingById("booking-001")).thenReturn(Optional.of(testBooking));

        assertThrows(AccessDeniedException.class, () ->
                ownershipValidationService.validateBookingOwnership("booking-001"));
    }

    @Test
    void ownsProperty_AsSuperadmin_ReturnsTrue() {
        mockSecurityContext(superadminUser);

        assertTrue(ownershipValidationService.ownsProperty("prop-001"));
    }

    @Test
    void ownsProperty_AsOwner_OwnProperty_ReturnsTrue() {
        mockSecurityContext(ownerUser);
        when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));

        assertTrue(ownershipValidationService.ownsProperty("prop-001"));
    }

    @Test
    void ownsProperty_AsOwner_OtherProperty_ReturnsFalse() {
        mockSecurityContext(ownerUser);
        
        Property otherProperty = new Property();
        otherProperty.setOwnerId(UUID.randomUUID());
        
        when(propertyService.getPropertyById("prop-002")).thenReturn(Optional.of(otherProperty));

        assertFalse(ownershipValidationService.ownsProperty("prop-002"));
    }

    @Test
    void ownsProperty_AsCustomer_ReturnsFalse() {
        mockSecurityContext(customerUser);

        assertFalse(ownershipValidationService.ownsProperty("prop-001"));
    }

    @Test
    void setOwnerForNewProperty_AsOwner_SetsOwnerId() {
        mockSecurityContext(ownerUser);
        
        Property newProperty = new Property();
        ownershipValidationService.setOwnerForNewProperty(newProperty);

        assertEquals(ownerId, newProperty.getOwnerId());
    }

    @Test
    void setOwnerForNewProperty_AsSuperadmin_SetsOwnerId() {
        mockSecurityContext(superadminUser);
        
        Property newProperty = new Property();
        ownershipValidationService.setOwnerForNewProperty(newProperty);

        assertNotNull(newProperty.getOwnerId());
    }

    @Test
    void setCustomerForNewBooking_AsCustomer_SetsCustomerId() {
        mockSecurityContext(customerUser);
        
        AccommodationBooking newBooking = new AccommodationBooking();
        ownershipValidationService.setCustomerForNewBooking(newBooking);

        assertEquals(customerId, newBooking.getCustomerId());
    }

    @Test
    void setCustomerForNewBooking_AsOwner_ThrowsException() {
        mockSecurityContext(ownerUser);
        
        AccommodationBooking newBooking = new AccommodationBooking();

        assertThrows(AccessDeniedException.class, () ->
                ownershipValidationService.setCustomerForNewBooking(newBooking));
    }
}
