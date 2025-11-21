package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomService {
    // Room operations
    Optional<Room> getRoomById(String id);
    List<Room> getRoomsByProperty(String propertyId);
    List<Room> getAvailableRooms();
    List<Room> getAvailableRoomsByPropertyAndDate(String propertyId, String checkIn, String checkOut);
    Room setRoomMaintenance(String roomId, LocalDateTime start, LocalDateTime end);
    
    // RoomType operations
    RoomType createRoomType(RoomType roomType);
    Optional<RoomType> getRoomTypeById(String id);
    List<RoomType> getRoomTypesByProperty(String propertyId);
    void addRoomType(Property property, RoomType roomType, Integer roomCount);
}
