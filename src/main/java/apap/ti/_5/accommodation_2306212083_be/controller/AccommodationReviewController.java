package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.CreateReviewRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.ReviewResponseDTO;
import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.service.AccommodationReviewService;
import apap.ti._5.accommodation_2306212083_be.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Accommodation Review operations
 * - Customer: Can only manage their own reviews
 * - Accommodation Owner: Can see reviews for their properties
 * - Superadmin: Can see and manage all reviews
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class AccommodationReviewController {

    private final AccommodationReviewService reviewService;

    /**
     * GET /api/reviews
     * Get all reviews (from all properties and bookings)
     */
    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        log.info("GET /api/reviews - Fetching all reviews");
        
        List<ReviewResponseDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/property/{propertyId}
     * Get all reviews for a specific property
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByProperty(@PathVariable String propertyId) {
        log.info("GET /api/reviews/property/{}", propertyId);
        
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByProperty(propertyId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/my-reviews
     * Get all reviews by authenticated customer
     * Customer: Own reviews, Owner: Reviews for their properties, Superadmin: All
     */
    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReviewResponseDTO>> getMyReviews() {
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        UUID userId = UUID.fromString(currentUser.getUserId());
        String role = currentUser.getRole();
        
        log.info("GET /api/reviews/my-reviews - userId={}, role={}", userId, role);
        
        List<ReviewResponseDTO> reviews;
        
        if (SecurityUtil.isCustomer()) {
            // Customer: Only their own reviews
            reviews = reviewService.getReviewsByCustomer(userId);
        } else if (SecurityUtil.isAccommodationOwner()) {
            // Accommodation Owner: Reviews for their properties
            reviews = reviewService.getReviewsForOwnerProperties(userId);
        } else {
            // Superadmin: All reviews
            reviews = reviewService.getAllReviews();
        }
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/customer
     * Get all reviews by a specific customer (kept for backward compatibility)
     * Query param: customerId (required)
     */
    @GetMapping("/customer")
    @PreAuthorize("hasAuthority('Superadmin')")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByCustomer(@RequestParam UUID customerId) {
        log.info("GET /api/reviews/customer - customerId={}", customerId);
        
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByCustomer(customerId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/{reviewId}
     * Get review detail
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> getReviewDetail(@PathVariable UUID reviewId) {
        log.info("GET /api/reviews/{}", reviewId);
        
        ReviewResponseDTO review = reviewService.getReviewDetail(reviewId);
        return ResponseEntity.ok(review);
    }

    /**
     * POST /api/reviews
     * Create a new review (authenticated user only)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDTO> createReview(@Valid @RequestBody CreateReviewRequestDTO request) {
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        UUID customerId = UUID.fromString(currentUser.getUserId());
        
        log.info("POST /api/reviews - Creating review for booking: {} by customer: {}", 
                request.getBookingId(), customerId);
        
        ReviewResponseDTO review = reviewService.createReview(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    /**
     * DELETE /api/reviews/{reviewId}
     * Delete a review (soft delete)
     * Customer: Own reviews only, Owner: Reviews for their properties, Superadmin: All
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable UUID reviewId) {
        UserPrincipal currentUser = SecurityUtil.getCurrentUser();
        UUID customerId = UUID.fromString(currentUser.getUserId());
        
        log.info("DELETE /api/reviews/{} - customerId={}, role={}", reviewId, customerId, currentUser.getRole());
        
        reviewService.deleteReview(reviewId, customerId);
        return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
    }

    /**
     * Global exception handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Error in AccommodationReviewController: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}
