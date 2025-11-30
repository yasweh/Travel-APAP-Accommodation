package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import apap.ti._5.accommodation_2306212083_be.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SupportTicket entity
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    
    // Find all non-deleted tickets
    List<SupportTicket> findByDeletedFalse();
    
    // Find tickets by user ID (non-deleted)
    List<SupportTicket> findByUserIdAndDeletedFalse(UUID userId);
    
    // Find by ID and non-deleted
    Optional<SupportTicket> findByIdAndDeletedFalse(UUID id);
    
    // Find by user ID and status (non-deleted)
    List<SupportTicket> findByUserIdAndStatusAndDeletedFalse(UUID userId, TicketStatus status);
    
    // Find by user ID and service source (non-deleted)
    List<SupportTicket> findByUserIdAndServiceSourceAndDeletedFalse(UUID userId, ServiceSource serviceSource);
    
    // Find by status (non-deleted)
    List<SupportTicket> findByStatusAndDeletedFalse(TicketStatus status);
    
    // Find by service source (non-deleted)
    List<SupportTicket> findByServiceSourceAndDeletedFalse(ServiceSource serviceSource);
    
    // Check if external booking ID already has a ticket
    boolean existsByServiceSourceAndExternalBookingIdAndDeletedFalse(ServiceSource serviceSource, String externalBookingId);
    
    // Complex query with multiple optional filters
    @Query("SELECT t FROM SupportTicket t WHERE t.deleted = false " +
           "AND (:userId IS NULL OR t.userId = :userId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:serviceSource IS NULL OR t.serviceSource = :serviceSource)")
    List<SupportTicket> findByFilters(
        @Param("userId") UUID userId,
        @Param("status") TicketStatus status,
        @Param("serviceSource") ServiceSource serviceSource
    );
}
