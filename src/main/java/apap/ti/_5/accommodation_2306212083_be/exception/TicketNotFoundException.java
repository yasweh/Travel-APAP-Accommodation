package apap.ti._5.accommodation_2306212083_be.exception;

/**
 * Exception thrown when a ticket is not found
 */
public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String message) {
        super(message);
    }
}
