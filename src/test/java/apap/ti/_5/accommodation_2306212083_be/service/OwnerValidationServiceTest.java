package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306212083_be.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerValidationServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private AccommodationBookingRepository bookingRepository;

    @InjectMocks
    private OwnerValidationService ownerValidationService;

    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;
    private AccommodationBooking testBooking;

    @BeforeEach
    void setUp() {
        testProperty = new Property();
        testProperty.setPropertyId("prop-001");
        testProperty.setPropertyName("Test Hotel");
        testProperty.setOwnerId(UUID.fromString("22222222-2222-2222-2222-222222222222"));

        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("rt-001");
        testRoomType.setProperty(testProperty);

        testRoom = new Room();
        testRoom.setRoomId("room-001");
        testRoom.setRoomType(testRoomType);

        testBooking = new AccommodationBooking();
        testBooking.setBookingId("booking-001");
        testBooking.setCustomerId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        testBooking.setRoom(testRoom);
    }

    // All validation methods are disabled (return immediately)
    // Testing to ensure they don't throw exceptions

    @Test
    void validateOwnership_DoesNotThrowException() {
        assertDoesNotThrow(() -> ownerValidationService.validateOwnership("prop-001"));
    }

    @Test
    void validateBookingPropertyOwnership_DoesNotThrowException() {
        assertDoesNotThrow(() -> ownerValidationService.validateBookingPropertyOwnership("booking-001"));
    }

    @Test
    void getCurrentUser_ReturnsNull() {
        UserPrincipal result = ownerValidationService.getCurrentUser();
        assertNull(result);
    }

    @Test
    void isSuperadmin_ReturnsFalse() {
        assertFalse(ownerValidationService.isSuperadmin());
    }

    @Test
    void isOwner_ReturnsFalse() {
        assertFalse(ownerValidationService.isOwner());
    }

    @Test
    void isCustomer_ReturnsFalse() {
        assertFalse(ownerValidationService.isCustomer());
    }

    @Test
    void validateBookingOwnership_DoesNotThrowException() {
        assertDoesNotThrow(() -> ownerValidationService.validateBookingOwnership("booking-001"));
    }

    @Test
    void validateBookingAccess_DoesNotThrowException() {
        assertDoesNotThrow(() -> ownerValidationService.validateBookingAccess("booking-001"));
    }

    @Test
    void validateRoomTypeOwnership_DoesNotThrowException() {
        assertDoesNotThrow(() -> ownerValidationService.validateRoomTypeOwnership(testRoomType));
    }

    @Test
    void validateRoomOwnership_DoesNotThrowException() {
        assertDoesNotThrow(() -> ownerValidationService.validateRoomOwnership(testRoom));
    }

    @Test
    void setOwnerForNewProperty_DoesNotThrowException() {
        assertDoesNotThrow(() -> ownerValidationService.setOwnerForNewProperty(testProperty));
    }

    @Test
    void setCustomerForNewBooking_DoesNotThrowException() {
        assertDoesNotThrow(() -> ownerValidationService.setCustomerForNewBooking(testBooking));
    }
}
