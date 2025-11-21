package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, String> {
    List<Property> findByActiveStatus(Integer activeStatus);
    List<Property> findByOwnerIdAndActiveStatus(UUID ownerId, Integer activeStatus);
    Optional<Property> findByPropertyIdAndActiveStatus(String propertyId, Integer activeStatus);
    List<Property> findByPropertyNameContainingIgnoreCaseAndActiveStatus(String name, Integer activeStatus);
    List<Property> findByTypeAndActiveStatus(Integer type, Integer activeStatus);
    List<Property> findByProvinceAndActiveStatus(Integer province, Integer activeStatus);
    
    /**
     * Check if property has future bookings (for soft delete validation)
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM Property p " +
           "JOIN p.listRoomType rt " +
           "JOIN rt.listRoom r " +
           "JOIN r.bookings b " +
           "WHERE p.propertyId = :propertyId " +
           "AND b.checkInDate > :currentDate " +
           "AND b.status IN (0, 1)")
    boolean hasFutureBookings(@Param("propertyId") String propertyId, @Param("currentDate") LocalDateTime currentDate);
}
