package apap.ti._5.accommodation_2306212083_be.repository;

import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, String> {
    List<RoomType> findByProperty_PropertyId(String propertyId);
    List<RoomType> findByProperty_PropertyIdOrderByFloorAsc(String propertyId);
    List<RoomType> findByActiveStatus(Integer activeStatus);
    List<RoomType> findByProperty_PropertyIdAndActiveStatus(String propertyId, Integer activeStatus);
    List<RoomType> findByProperty_PropertyIdAndActiveStatusOrderByFloorAsc(String propertyId, Integer activeStatus);
    
    /**
     * Check if room type with same name and floor exists in property
     */
    boolean existsByProperty_PropertyIdAndNameAndFloor(String propertyId, String name, Integer floor);
    
    /**
     * Find room type by property, name and floor
     */
    RoomType findByProperty_PropertyIdAndNameAndFloor(String propertyId, String name, Integer floor);
}
