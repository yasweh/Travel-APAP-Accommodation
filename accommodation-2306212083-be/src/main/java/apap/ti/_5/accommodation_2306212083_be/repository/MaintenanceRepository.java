package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
    
    List<Maintenance> findByRoom_RoomId(String roomId);
    
    /**
     * Check if maintenance schedule overlaps with existing maintenance
     */
    @Query("SELECT COUNT(m) > 0 FROM Maintenance m WHERE m.room.roomId = :roomId " +
           "AND ((m.startDate < :endDate OR (m.startDate = :endDate AND m.startTime < :endTime)) " +
           "AND (m.endDate > :startDate OR (m.endDate = :startDate AND m.endTime > :startTime)))")
    boolean existsOverlappingMaintenance(
        @Param("roomId") String roomId,
        @Param("startDate") LocalDate startDate,
        @Param("startTime") LocalTime startTime,
        @Param("endDate") LocalDate endDate,
        @Param("endTime") LocalTime endTime
    );
}
