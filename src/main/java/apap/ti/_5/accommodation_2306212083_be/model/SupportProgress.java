package apap.ti._5.accommodation_2306212083_be.model;

import apap.ti._5.accommodation_2306212083_be.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing progress updates on a support ticket.
 * Progress can be added by admins or vendors to track resolution steps.
 */
@Entity
@Table(name = "support_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportProgress {

    /**
     * Auto-generated unique progress ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    /**
     * The support ticket this progress update belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonIgnore
    private SupportTicket supportTicket;

    /**
     * ID of the admin/vendor who created this progress update
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * Name of the user who created the progress update
     */
    @Column(name = "user_name", length = 100)
    private String userName;

    /**
     * Description of the progress action taken
     */
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    /**
     * Role of the person creating the progress (e.g., "SUPERADMIN", "ACCOMMODATION_OWNER")
     */
    @Column(name = "role", nullable = false, length = 50)
    private String role;

    /**
     * Type of action taken
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActionType actionType;

    /**
     * Timestamp when the progress was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Helper method to get ticket ID without loading the entire ticket
     */
    @Transient
    public String getTicketId() {
        return supportTicket != null ? supportTicket.getTicketId() : null;
    }
}
