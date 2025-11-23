package apap.ti._5.accommodation_2306212083_be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to access a ticket they don't own.
 * Admins are allowed to access any ticket.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedTicketAccessException extends RuntimeException {

    public UnauthorizedTicketAccessException(String ticketId) {
        super("You are not authorized to access ticket: " + ticketId);
    }

    public UnauthorizedTicketAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
