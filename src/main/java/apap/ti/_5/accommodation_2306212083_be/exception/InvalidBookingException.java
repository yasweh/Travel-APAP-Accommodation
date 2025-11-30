package apap.ti._5.accommodation_2306212083_be.exception;

/**
 * Exception thrown when a booking validation fails
 */
public class InvalidBookingException extends RuntimeException {
    public InvalidBookingException(String message) {
        super(message);
    }
}
