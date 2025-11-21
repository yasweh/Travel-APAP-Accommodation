package apap.ti._5.accommodation_2306212083_be.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accommodation_booking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationBooking {

    @Id
    @Column(name = "booking_id", length = 50)
    private String bookingId;

    @Column(name = "check_in_date", nullable = false)
    private LocalDateTime checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDateTime checkOutDate;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 0; // 0=Waiting, 1=Confirmed, 2=Cancelled, 3=Request Refund, 4=Done

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "is_breakfast", nullable = false)
    @Builder.Default
    private Boolean isBreakfast = false;

    @Column(name = "refund", nullable = false)
    @Builder.Default
    private Integer refund = 0;

    @Column(name = "extra_pay", nullable = false)
    @Builder.Default
    private Integer extraPay = 0;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "active_status", nullable = false)
    @Builder.Default
    private Integer activeStatus = 1; // 0=Deleted, 1=Active

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Customer customer;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    public String getStatusString() {
        switch (this.status) {
            case 0: return "Waiting for Payment";
            case 1: return "Payment Confirmed";
            case 2: return "Cancelled";
            case 3: return "Request Refund";
            case 4: return "Done";
            default: return "Unknown";
        }
    }
}
