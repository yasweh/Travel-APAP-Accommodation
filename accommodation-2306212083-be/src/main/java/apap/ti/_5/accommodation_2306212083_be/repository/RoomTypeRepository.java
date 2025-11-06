package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, String> {
    List<RoomType> findByProperty_PropertyId(String propertyId);
    List<RoomType> findByProperty_PropertyIdOrderByFloorAsc(String propertyId);
    
    /**
     * Check if room type with same name and floor exists in property
     */
    boolean existsByProperty_PropertyIdAndNameAndFloor(String propertyId, String name, Integer floor);
}
