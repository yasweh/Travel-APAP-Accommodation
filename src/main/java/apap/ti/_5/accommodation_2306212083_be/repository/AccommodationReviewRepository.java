package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.AccommodationReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccommodationReviewRepository extends JpaRepository<AccommodationReview, UUID> {

    /**
     * Find all active reviews (all properties, all bookings)
     */
    @Query("SELECT r FROM AccommodationReview r WHERE r.activeStatus = 1 ORDER BY r.createdDate DESC")
    List<AccommodationReview> findAllActiveReviews();

    /**
     * Find all reviews for a specific property (active only)
     */
    @Query("SELECT r FROM AccommodationReview r WHERE r.propertyId = :propertyId AND r.activeStatus = 1 ORDER BY r.createdDate DESC")
    List<AccommodationReview> findByPropertyId(@Param("propertyId") String propertyId);

    /**
     * Find all reviews by a specific customer (active only)
     */
    @Query("SELECT r FROM AccommodationReview r WHERE r.customerId = :customerId AND r.activeStatus = 1 ORDER BY r.createdDate DESC")
    List<AccommodationReview> findByCustomerId(@Param("customerId") UUID customerId);

    /**
     * Find review by booking ID
     */
    @Query("SELECT r FROM AccommodationReview r WHERE r.bookingId = :bookingId AND r.activeStatus = 1")
    Optional<AccommodationReview> findByBookingId(@Param("bookingId") String bookingId);

    /**
     * Find review by ID (active only)
     */
    @Query("SELECT r FROM AccommodationReview r WHERE r.reviewId = :reviewId AND r.activeStatus = 1")
    Optional<AccommodationReview> findByIdAndActiveStatus(@Param("reviewId") UUID reviewId);

    /**
     * Check if review exists for booking
     */
    @Query("SELECT COUNT(r) > 0 FROM AccommodationReview r WHERE r.bookingId = :bookingId AND r.activeStatus = 1")
    boolean existsByBookingId(@Param("bookingId") String bookingId);
}
