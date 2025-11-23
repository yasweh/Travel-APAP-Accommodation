package apap.ti._5.accommodation_2306212083_be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses across all endpoints
 * Returns uniform JSON structure: { success, message, error, timestamp, status }
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle AccessDeniedException (403 Forbidden)
     * User is authenticated but doesn't have required role/permission
     * Includes context for RBAC violations
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        
        // Provide more context in the error message
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = "Access denied. You don't have permission to access this resource.";
        }
        
        body.put("message", message);
        body.put("error", "Forbidden");
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        
        // Add hint for common RBAC scenarios
        if (message.contains("own")) {
            body.put("hint", "You can only access resources that belong to you");
        } else if (message.contains("property") || message.contains("properties")) {
            body.put("hint", "Accommodation owners can only access their own properties");
        } else if (message.contains("booking")) {
            body.put("hint", "Customers can only access their own bookings");
        }

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle AuthenticationException (401 Unauthorized)
     * User is not authenticated or token is invalid
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", "Authentication failed. Please login again.");
        body.put("error", "Unauthorized");
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle ResourceNotFoundException (404 Not Found)
     * Example: Booking not found, Property not found, Room not found, etc.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        body.put("error", "Resource Not Found");
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle IllegalArgumentException (400 Bad Request)
     * Example: Invalid dates, negative capacity, maintenance conflict, etc.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        body.put("error", "Invalid Input");
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle RuntimeException (500 Internal Server Error)
     * Generic fallback for business logic errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        body.put("error", "Runtime Error");
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle generic Exception (500 Internal Server Error)
     * Catch-all for any unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(
            Exception ex, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", "An unexpected error occurred: " + ex.getMessage());
        body.put("error", "Internal Server Error");
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
