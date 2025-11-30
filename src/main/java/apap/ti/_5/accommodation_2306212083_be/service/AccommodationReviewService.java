package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.CreateReviewRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.ReviewResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AccommodationReviewService {

    /**
     * Get all reviews (from all properties and bookings)
     */
    List<ReviewResponseDTO> getAllReviews();

    /**
     * Get all reviews for a property
     */
    List<ReviewResponseDTO> getReviewsByProperty(String propertyId);

    /**
     * Get all reviews by a customer
     */
    List<ReviewResponseDTO> getReviewsByCustomer(UUID customerId);

    /**
     * Get review detail by ID
     */
    ReviewResponseDTO getReviewDetail(UUID reviewId);

    /**
     * Create a new review
     * Validates booking ownership and completion status
     */
    ReviewResponseDTO createReview(CreateReviewRequestDTO request, UUID customerId);

    /**
     * Delete review (soft delete)
     */
    void deleteReview(UUID reviewId, UUID customerId);
}
