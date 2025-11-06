package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccommodationBookingRepository extends JpaRepository<AccommodationBooking, String> {
    List<AccommodationBooking> findByCustomerId(UUID customerId);
    List<AccommodationBooking> findByStatus(Integer status);
    List<AccommodationBooking> findByRoom_RoomId(String roomId);
    
    @Query("SELECT b FROM AccommodationBooking b WHERE b.status = 1 AND b.checkInDate <= :today")
    List<AccommodationBooking> findBookingsToCheckIn(LocalDateTime today);
    
    List<AccommodationBooking> findByRoom_RoomType_Property_PropertyId(String propertyId);
    
    /**
     * Check if room has booking conflicts during specified date range
     */
    @Query("SELECT COUNT(b) > 0 FROM AccommodationBooking b " +
           "WHERE b.room.roomId = :roomId " +
           "AND b.status IN (0, 1) " +
           "AND b.checkInDate < :endDate " +
           "AND b.checkOutDate > :startDate")
    boolean existsBookingConflict(
        @Param("roomId") String roomId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
