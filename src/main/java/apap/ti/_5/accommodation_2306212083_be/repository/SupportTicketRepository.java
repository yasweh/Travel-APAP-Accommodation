package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import apap.ti._5.accommodation_2306212083_be.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SupportTicket entity operations.
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, String> {

    /**
     * Find all tickets created by a specific user
     * @param userId The user ID
     * @return List of tickets
     */
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all tickets assigned to a specific admin
     * @param assignedAdminId The admin ID
     * @return List of tickets
     */
    List<SupportTicket> findByAssignedAdminIdOrderByCreatedAtDesc(Long assignedAdminId);

    /**
     * Find all tickets with a specific status
     * @param status The ticket status
     * @return List of tickets
     */
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    /**
     * Find all tickets (for admin view)
     * @return List of all tickets ordered by creation date
     */
    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    /**
     * Find tickets by user ID and status
     * @param userId The user ID
     * @param status The ticket status
     * @return List of tickets
     */
    List<SupportTicket> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, TicketStatus status);

    /**
     * Find ticket by external booking reference
     * @param externalServiceSource The service source
     * @param externalBookingId The booking ID
     * @return List of tickets for that booking
     */
    @Query("SELECT t FROM SupportTicket t WHERE t.externalServiceSource = :source AND t.externalBookingId = :bookingId ORDER BY t.createdAt DESC")
    List<SupportTicket> findByExternalBooking(@Param("source") apap.ti._5.accommodation_2306212083_be.enums.ServiceSource source, 
                                               @Param("bookingId") String bookingId);

    /**
     * Get the maximum sequence number for a given service and year
     * Used for ticket ID generation
     * @param serviceCode The service code (e.g., "ACC")
     * @param year The year
     * @return The maximum sequence number
     */
    @Query("SELECT MAX(CAST(SUBSTRING(t.ticketId, LENGTH(t.ticketId) - 3, 4) AS int)) " +
           "FROM SupportTicket t " +
           "WHERE t.ticketId LIKE CONCAT('ST-', :serviceCode, '-', :year, '-%')")
    Optional<Integer> findMaxSequenceForServiceAndYear(@Param("serviceCode") String serviceCode, 
                                                        @Param("year") String year);

    /**
     * Count unread messages for a user across all their tickets
     * @param userId The user ID
     * @return Count of unread messages
     */
    @Query("SELECT COUNT(m) FROM TicketMessage m WHERE m.supportTicket.userId = :userId AND m.isRead = false AND m.senderId != :userId")
    Long countUnreadMessagesForUser(@Param("userId") Long userId);
}
