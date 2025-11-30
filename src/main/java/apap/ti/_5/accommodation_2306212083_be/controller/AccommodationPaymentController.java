package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.PaymentConfirmRequestDTO;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for Accommodation payment callbacks from Bill Service
 * This controller handles external callbacks from the Bill Service when payment is confirmed.
 * 
 * Endpoint: POST /api/policy/payment/confirm
 * 
 * Note: This endpoint does NOT require authentication as it's called by the Bill Service
 * with a server-to-server call. In production, you should use API Key validation or
 * IP whitelisting for security.
 */
@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
@Slf4j
public class AccommodationPaymentController {

    private final BookingService bookingService;

    /**
     * POST /api/accommodation/payment/confirm
     * 
     * Called by Bill Service when a customer confirms payment for a booking.
     * Updates the booking status from "Waiting for Payment" (0) to "Payment Confirmed" (1).
     * 
     * Request Body:
     * {
     *   "serviceReferenceId": "booking-uuid-here",
     *   "customerId": "customer-uuid-here"
     * }
     * 
     * @param request Payment confirmation request containing serviceReferenceId (bookingId) and customerId
     * @return Success/failure response with booking details
     */
    @PostMapping("/payment/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody PaymentConfirmRequestDTO request) {
        
        log.info("========== PAYMENT CONFIRM CALLBACK ==========");
        log.info("Received payment confirmation from Bill Service");
        log.info("Service Reference ID (Booking ID): {}", request.getServiceReferenceId());
        log.info("Customer ID: {}", request.getCustomerId());
        log.info("==============================================");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate required fields
            if (request.getServiceReferenceId() == null || request.getServiceReferenceId().isBlank()) {
                log.error("Missing serviceReferenceId in payment confirm request");
                response.put("success", false);
                response.put("message", "serviceReferenceId is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getCustomerId() == null || request.getCustomerId().isBlank()) {
                log.error("Missing customerId in payment confirm request");
                response.put("success", false);
                response.put("message", "customerId is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            String bookingId = request.getServiceReferenceId();
            
            // Get booking and validate
            AccommodationBooking booking = bookingService.getBookingById(bookingId)
                .orElse(null);
            
            if (booking == null) {
                log.error("Booking not found: {}", bookingId);
                response.put("success", false);
                response.put("message", "Booking not found: " + bookingId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            log.info("Found booking: id={}, customerId={}, status={}", 
                booking.getBookingId(), booking.getCustomerId(), booking.getStatus());
            
            // Validate that the customerId matches the booking owner
            try {
                UUID payingCustomerId = UUID.fromString(request.getCustomerId());
                if (!booking.getCustomerId().equals(payingCustomerId)) {
                    log.warn("Customer ID mismatch! Request: {}, Booking owner: {}", 
                        request.getCustomerId(), booking.getCustomerId());
                    response.put("success", false);
                    response.put("message", "Customer ID does not match booking owner");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
            } catch (IllegalArgumentException e) {
                log.error("Invalid customer ID format: {}", request.getCustomerId());
                response.put("success", false);
                response.put("message", "Invalid customer ID format");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if booking is in correct status for payment
            if (booking.getStatus() != 0) {
                String statusMessage = switch (booking.getStatus()) {
                    case 1 -> "Booking payment is already confirmed";
                    case 2 -> "Booking has been cancelled";
                    default -> "Booking cannot be paid in current status: " + booking.getStatus();
                };
                
                log.warn("Cannot confirm payment - {}", statusMessage);
                response.put("success", false);
                response.put("message", statusMessage);
                response.put("currentStatus", booking.getStatus());
                response.put("currentStatusString", getStatusString(booking.getStatus()));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Process payment - update status to confirmed (1)
            bookingService.payBookingById(bookingId);
            
            log.info("✅ Payment confirmed successfully for booking {} by customer {}", 
                bookingId, request.getCustomerId());
            
            // Return success response
            response.put("success", true);
            response.put("message", "Payment confirmed successfully");
            response.put("bookingId", bookingId);
            response.put("customerId", request.getCustomerId());
            response.put("previousStatus", 0);
            response.put("previousStatusString", "Waiting for Payment");
            response.put("newStatus", 1);
            response.put("newStatusString", "Payment Confirmed");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing payment confirmation: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error processing payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Convert status integer to readable string
     */
    private String getStatusString(Integer status) {
        if (status == null) return "Unknown";
        return switch (status) {
            case 0 -> "Waiting for Payment";
            case 1 -> "Payment Confirmed";
            case 2 -> "Cancelled";
            default -> "Unknown (" + status + ")";
        };
    }
}
