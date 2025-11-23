package apap.ti._5.accommodation_2306212083_be.model;

import apap.ti._5.accommodation_2306212083_be.enums.SenderType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a message in a support ticket conversation.
 * Messages can be sent by users, admins, or vendors.
 */
@Entity
@Table(name = "ticket_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessage {

    /**
     * Auto-generated unique message ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    /**
     * The support ticket this message belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonIgnore
    private SupportTicket supportTicket;

    /**
     * ID of the user/admin/vendor who sent this message
     */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /**
     * Type of the sender (USER, ADMIN, or VENDOR)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 10)
    private SenderType senderType;

    /**
     * Name of the sender for display purposes
     */
    @Column(name = "sender_name", length = 100)
    private String senderName;

    /**
     * The message content
     */
    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    /**
     * Whether this message has been read by the recipient
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * JSON array of attachment URLs (optional)
     */
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    /**
     * Timestamp when the message was created
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
