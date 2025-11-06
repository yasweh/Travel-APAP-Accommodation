package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.PropertyStatisticsDTO;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyService {
    Property createProperty(Property property, List<RoomType> roomTypes, List<List<Integer>> roomCounts);
    Optional<Property> getPropertyById(String id);
    List<Property> getAllActiveProperties();
    Property updateProperty(Property property);
    void deleteProperty(String id);
    List<Property> getPropertiesByOwner(UUID ownerId);
    List<Property> searchProperties(String name, Integer type, Integer province);
    void updatePropertyIncome(String propertyId, Integer amount);
    
    // New methods for advanced features
    Property updatePropertyWithRoomTypes(Property updatedProperty, List<RoomType> updatedRoomTypes);
    void softDeleteProperty(String propertyId);
    List<PropertyStatisticsDTO> getMonthlyStatistics(int month, int year);
}
