package apap.ti._5.accommodation_2306212083_be.model;

import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main Support Ticket Entity
 */
@Entity
@Table(name = "support_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @Column(nullable = false)
    private UUID userId;  // The user who created the ticket
    
    @NotBlank
    @Column(nullable = false, length = 255)
    private String subject;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketStatus status = TicketStatus.OPEN;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ServiceSource serviceSource;  // Which external service (accommodation, flight, etc.)
    
    @NotBlank
    @Column(nullable = false, length = 255)
    private String externalBookingId;  // The booking ID from external service
    
    @Column(length = 50)
    private String propertyId;  // Property ID for accommodation service (for owner filtering)
    
    @Column(columnDefinition = "TEXT")
    private String initialMessage;  // First message when creating ticket
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Boolean deleted = false;  // Soft delete flag
    
    // Relationships
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketMessage> messages = new ArrayList<>();
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupportProgress> progressEntries = new ArrayList<>();
    
    // Helper methods
    public void addMessage(TicketMessage message) {
        messages.add(message);
        message.setTicket(this);
    }
    
    public void addProgress(SupportProgress progress) {
        progressEntries.add(progress);
        progress.setTicket(this);
    }
}
