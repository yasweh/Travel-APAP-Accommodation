package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByRoomType_RoomTypeId(String roomTypeId);
    List<Room> findByAvailabilityStatusAndActiveRoom(Integer availabilityStatus, Integer activeRoom);
    
    @Query("SELECT r FROM Room r JOIN FETCH r.roomType rt WHERE rt.property.propertyId = :propertyId")
    List<Room> findByRoomType_Property_PropertyId(@Param("propertyId") String propertyId);
}
