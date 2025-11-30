package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for TicketMessage entity
 */
@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, UUID> {
    
    // Find all messages for a ticket (non-deleted, ordered by time)
    List<TicketMessage> findByTicketIdAndDeletedFalseOrderBySentAtAsc(UUID ticketId);
    
    // Count unread messages for a ticket
    long countByTicketIdAndReadByRecipientFalseAndDeletedFalse(UUID ticketId);
    
    // Find unread messages for a ticket
    List<TicketMessage> findByTicketIdAndReadByRecipientFalseAndDeletedFalse(UUID ticketId);
}
