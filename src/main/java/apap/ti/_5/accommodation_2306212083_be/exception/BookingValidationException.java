package apap.ti._5.accommodation_2306212083_be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when booking validation fails.
 * This can occur when:
 * - Booking does not exist in the external service
 * - User is not the owner of the booking
 * - External service is unavailable
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BookingValidationException extends RuntimeException {

    public BookingValidationException(String message) {
        super(message);
    }

    public BookingValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static BookingValidationException bookingNotFound(String serviceSource, String bookingId) {
        return new BookingValidationException(
            String.format("Booking not found: %s in service %s", bookingId, serviceSource)
        );
    }

    public static BookingValidationException notBookingOwner(String bookingId) {
        return new BookingValidationException(
            String.format("You are not the owner of booking: %s", bookingId)
        );
    }

    public static BookingValidationException serviceUnavailable(String serviceSource) {
        return new BookingValidationException(
            String.format("External service %s is currently unavailable", serviceSource)
        );
    }
}
