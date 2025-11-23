package apap.ti._5.accommodation_2306212083_be.model;

import apap.ti._5.accommodation_2306212083_be.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a support ticket for booking-related issues.
 * Tickets can reference bookings from any of the 5 travel services.
 */
@Entity
@Table(name = "support_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {

    /**
     * Unique ticket ID with format: ST-{SERVICE_CODE}-{YEAR}-{SEQUENCE}
     * Example: ST-ACC-2025-0001
     */
    @Id
    @Column(name = "ticket_id", nullable = false, length = 50)
    private String ticketId;

    /**
     * ID of the user who created the ticket
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * ID of the admin assigned to handle this ticket (nullable if not yet assigned)
     */
    @Column(name = "assigned_admin_id")
    private Long assignedAdminId;

    /**
     * The external service where the booking was made
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "external_service_source", nullable = false, length = 20)
    private ServiceSource externalServiceSource;

    /**
     * The booking ID from the external service
     */
    @Column(name = "external_booking_id", nullable = false, length = 100)
    private String externalBookingId;

    /**
     * Subject/title of the ticket
     */
    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    /**
     * Detailed description of the issue
     */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Current status of the ticket
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    /**
     * Priority level of the ticket
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    /**
     * Category of the issue
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private Category category;

    /**
     * Timestamp when the ticket was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the ticket was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Timestamp when the ticket was closed (nullable if still open)
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /**
     * List of messages associated with this ticket
     */
    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TicketMessage> messages = new ArrayList<>();

    /**
     * List of progress updates associated with this ticket
     */
    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SupportProgress> progressUpdates = new ArrayList<>();

    /**
     * Add a message to this ticket
     * @param message The message to add
     */
    public void addMessage(TicketMessage message) {
        messages.add(message);
        message.setSupportTicket(this);
    }

    /**
     * Add a progress update to this ticket
     * @param progress The progress update to add
     */
    public void addProgress(SupportProgress progress) {
        progressUpdates.add(progress);
        progress.setSupportTicket(this);
    }
}
