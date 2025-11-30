package apap.ti._5.accommodation_2306212083_be.exception;

/**
 * Exception thrown when an external service call fails
 */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
