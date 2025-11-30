package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.CreateReviewRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.ReviewResponseDTO;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationReview;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationReviewRepository;
import apap.ti._5.accommodation_2306212083_be.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccommodationReviewServiceImpl implements AccommodationReviewService {

    private final AccommodationReviewRepository reviewRepository;
    private final AccommodationBookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviews() {
        log.info("Fetching all reviews");
        
        List<AccommodationReview> reviews = reviewRepository.findAllActiveReviews();
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByProperty(String propertyId) {
        log.info("Fetching reviews for property: {}", propertyId);
        
        List<AccommodationReview> reviews = reviewRepository.findByPropertyId(propertyId);
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByCustomer(UUID customerId) {
        log.info("Fetching reviews for customer: {}", customerId);
        
        List<AccommodationReview> reviews = reviewRepository.findByCustomerId(customerId);
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsForOwnerProperties(UUID ownerId) {
        log.info("Fetching reviews for properties owned by: {}", ownerId);
        
        // Get all active properties owned by this owner
        List<Property> properties = propertyRepository.findByOwnerIdAndActiveStatus(ownerId, 1);
        List<String> propertyIds = properties.stream()
                .map(Property::getPropertyId)
                .collect(Collectors.toList());
        
        // Get all reviews for these properties
        List<AccommodationReview> reviews = reviewRepository.findAllActiveReviews().stream()
                .filter(review -> propertyIds.contains(review.getPropertyId()))
                .collect(Collectors.toList());
        
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewDetail(UUID reviewId) {
        log.info("Fetching review detail for: {}", reviewId);
        
        AccommodationReview review = reviewRepository.findByIdAndActiveStatus(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + reviewId));
        
        return mapToResponseDTO(review);
    }

    @Override
    public ReviewResponseDTO createReview(CreateReviewRequestDTO request, UUID customerId) {
        log.info("Creating review for booking: {} by customer: {}", request.getBookingId(), customerId);
        
        // 1. Validate booking exists
        AccommodationBooking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + request.getBookingId()));
        
        // 2. Validate booking ownership - DISABLED for debug
        // if (!booking.getCustomerId().equals(customerId)) {
        //     throw new IllegalArgumentException("You can only review your own bookings");
        // }
        
        // 3. Validate booking completion status
        // Status must be "Payment Confirmed" (1) AND current date >= checkout date
        if (booking.getStatus() != 1) {
            throw new IllegalArgumentException("You can only review confirmed bookings");
        }
        
        if (LocalDateTime.now().isBefore(booking.getCheckOutDate())) {
            throw new IllegalArgumentException("You can only review bookings after checkout date");
        }
        
        // 4. Check if review already exists
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new IllegalArgumentException("Review already exists for this booking");
        }
        
        // 5. Get property ID from booking
        String propertyId = booking.getRoom().getRoomType().getProperty().getPropertyId();
        
        // 6. Create review
        AccommodationReview review = AccommodationReview.builder()
                .bookingId(request.getBookingId())
                .propertyId(propertyId)
                .customerId(customerId)
                .cleanlinessRating(request.getCleanlinessRating())
                .facilityRating(request.getFacilityRating())
                .serviceRating(request.getServiceRating())
                .valueRating(request.getValueRating())
                .comment(request.getComment())
                .activeStatus(1)
                .build();
        
        review = reviewRepository.save(review);
        
        log.info("Review created successfully with id: {}", review.getReviewId());
        
        return mapToResponseDTO(review);
    }

    @Override
    public void deleteReview(UUID reviewId, UUID customerId) {
        log.info("Deleting review: {} by customer: {}", reviewId, customerId);
        
        AccommodationReview review = reviewRepository.findByIdAndActiveStatus(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + reviewId));
        
        // Validate ownership - DISABLED for debug
        // if (!review.getCustomerId().equals(customerId)) {
        //     throw new IllegalArgumentException("You can only delete your own reviews");
        // }
        
        review.setActiveStatus(0);
        reviewRepository.save(review);
        
        log.info("Review deleted successfully");
    }

    // Helper method to map entity to DTO
    private ReviewResponseDTO mapToResponseDTO(AccommodationReview review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setReviewId(review.getReviewId());
        dto.setBookingId(review.getBookingId());
        dto.setPropertyId(review.getPropertyId());
        dto.setCustomerId(review.getCustomerId());
        dto.setOverallRating(review.getOverallRating());
        dto.setCleanlinessRating(review.getCleanlinessRating());
        dto.setFacilityRating(review.getFacilityRating());
        dto.setServiceRating(review.getServiceRating());
        dto.setValueRating(review.getValueRating());
        dto.setComment(review.getComment());
        dto.setCreatedDate(review.getCreatedDate());
        
        // Fetch additional info
        if (review.getProperty() != null) {
            dto.setPropertyName(review.getProperty().getPropertyName());
        }
        
        if (review.getBooking() != null) {
            dto.setCustomerName(review.getBooking().getCustomerName());
        }
        
        return dto;
    }
}
