package apap.ti._5.accommodation_2306212083_be.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accommodation_review")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationReview {

    @Id
    @Column(name = "review_id")
    private UUID reviewId;

    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    @Column(name = "property_id", nullable = false)
    private String propertyId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "overall_rating", nullable = false)
    private Double overallRating;

    @Column(name = "cleanliness_rating", nullable = false)
    private Integer cleanlinessRating; // 1-5

    @Column(name = "facility_rating", nullable = false)
    private Integer facilityRating; // 1-5

    @Column(name = "service_rating", nullable = false)
    private Integer serviceRating; // 1-5

    @Column(name = "value_rating", nullable = false)
    private Integer valueRating; // 1-5

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "active_status", nullable = false)
    @Builder.Default
    private Integer activeStatus = 1; // 0=Deleted, 1=Active

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", referencedColumnName = "booking_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AccommodationBooking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", referencedColumnName = "property_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Property property;

    @PrePersist
    public void prePersist() {
        if (this.reviewId == null) {
            this.reviewId = UUID.randomUUID();
        }
        this.createdDate = LocalDateTime.now();
        
        // Calculate overall rating as average
        this.overallRating = (cleanlinessRating + facilityRating + serviceRating + valueRating) / 4.0;
    }
}
