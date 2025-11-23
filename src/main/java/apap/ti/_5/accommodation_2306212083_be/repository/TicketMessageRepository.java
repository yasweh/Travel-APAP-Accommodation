package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for TicketMessage entity operations.
 */
@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    /**
     * Find all messages for a specific ticket, ordered by creation time
     * @param ticketId The ticket ID
     * @return List of messages
     */
    @Query("SELECT m FROM TicketMessage m WHERE m.supportTicket.ticketId = :ticketId ORDER BY m.createdAt ASC")
    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(@Param("ticketId") String ticketId);

    /**
     * Count unread messages in a specific ticket for a specific user
     * @param ticketId The ticket ID
     * @param userId The user ID (messages from this user are excluded)
     * @return Count of unread messages
     */
    @Query("SELECT COUNT(m) FROM TicketMessage m WHERE m.supportTicket.ticketId = :ticketId AND m.isRead = false AND m.senderId != :userId")
    Long countUnreadMessagesForTicket(@Param("ticketId") String ticketId, @Param("userId") Long userId);

    /**
     * Mark all messages in a ticket as read (except those from the current user)
     * @param ticketId The ticket ID
     * @param userId The user ID (messages from this user are not marked as read)
     */
    @Modifying
    @Query("UPDATE TicketMessage m SET m.isRead = true WHERE m.supportTicket.ticketId = :ticketId AND m.senderId != :userId AND m.isRead = false")
    void markAllMessagesAsRead(@Param("ticketId") String ticketId, @Param("userId") Long userId);

    /**
     * Find unread messages for a specific user in a ticket
     * @param ticketId The ticket ID
     * @param userId The user ID
     * @return List of unread messages
     */
    @Query("SELECT m FROM TicketMessage m WHERE m.supportTicket.ticketId = :ticketId AND m.isRead = false AND m.senderId != :userId ORDER BY m.createdAt ASC")
    List<TicketMessage> findUnreadMessages(@Param("ticketId") String ticketId, @Param("userId") Long userId);
}
