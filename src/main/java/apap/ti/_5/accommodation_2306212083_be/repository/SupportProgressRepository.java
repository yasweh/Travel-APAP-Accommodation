package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.SupportProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for SupportProgress entity
 */
@Repository
public interface SupportProgressRepository extends JpaRepository<SupportProgress, UUID> {
    
    // Find all progress entries for a ticket (non-deleted, ordered by time)
    List<SupportProgress> findByTicketIdAndDeletedFalseOrderByPerformedAtAsc(UUID ticketId);
}
