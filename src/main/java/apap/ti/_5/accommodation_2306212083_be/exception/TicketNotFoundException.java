package apap.ti._5.accommodation_2306212083_be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested support ticket is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String ticketId) {
        super("Support ticket not found with ID: " + ticketId);
    }

    public TicketNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
