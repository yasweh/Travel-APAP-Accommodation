package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.SupportProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for SupportProgress entity operations.
 */
@Repository
public interface SupportProgressRepository extends JpaRepository<SupportProgress, Long> {

    /**
     * Find all progress updates for a specific ticket, ordered by creation time
     * @param ticketId The ticket ID
     * @return List of progress updates
     */
    @Query("SELECT p FROM SupportProgress p WHERE p.supportTicket.ticketId = :ticketId ORDER BY p.createdAt ASC")
    List<SupportProgress> findByTicketIdOrderByCreatedAtAsc(@Param("ticketId") String ticketId);

    /**
     * Find progress updates created by a specific user
     * @param userId The user ID
     * @return List of progress updates
     */
    List<SupportProgress> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Count progress updates for a specific ticket
     * @param ticketId The ticket ID
     * @return Count of progress updates
     */
    @Query("SELECT COUNT(p) FROM SupportProgress p WHERE p.supportTicket.ticketId = :ticketId")
    Long countByTicketId(@Param("ticketId") String ticketId);
}
