package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306212083_be.util.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final IdGenerator idGenerator;
    private final AccommodationBookingRepository bookingRepository;

    public RoomServiceImpl(RoomRepository roomRepository, 
                          RoomTypeRepository roomTypeRepository, 
                          IdGenerator idGenerator,
                          AccommodationBookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.idGenerator = idGenerator;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Optional<Room> getRoomById(String id) {
        return roomRepository.findById(id);
    }

    @Override
    public List<Room> getRoomsByProperty(String propertyId) {
        return roomRepository.findByRoomType_Property_PropertyId(propertyId);
    }

    @Override
    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailabilityStatusAndActiveRoom(1, 1);
    }

    @Override
    public List<Room> getAvailableRoomsByPropertyAndDate(String propertyId, String checkIn, String checkOut) {
        // Parse dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate checkInDate = LocalDate.parse(checkIn, formatter);
        LocalDate checkOutDate = LocalDate.parse(checkOut, formatter);
        
        LocalDateTime startDateTime = checkInDate.atStartOfDay();
        LocalDateTime endDateTime = checkOutDate.atStartOfDay();
        
        // Get all rooms in property
        List<Room> allRooms = roomRepository.findByRoomType_Property_PropertyId(propertyId);
        
        // Filter out rooms that have booking conflicts
        List<Room> availableRooms = new ArrayList<>();
        for (Room room : allRooms) {
            // Check if room is active
            if (room.getActiveRoom() != 1 || room.getAvailabilityStatus() != 1) {
                continue;
            }
            
            // Check if room has booking conflict
            boolean hasConflict = bookingRepository.existsBookingConflict(
                room.getRoomId(),
                startDateTime,
                endDateTime
            );
            
            if (!hasConflict) {
                availableRooms.add(room);
            }
        }
        
        return availableRooms;
    }

    @Override
    public Room setRoomMaintenance(String roomId, LocalDateTime start, LocalDateTime end) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        room.setActiveRoom(0);
        room.setMaintenanceStart(start);
        room.setMaintenanceEnd(end);
        
        return roomRepository.save(room);
    }

    @Override
    public RoomType createRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    @Override
    public Optional<RoomType> getRoomTypeById(String id) {
        return roomTypeRepository.findById(id);
    }

    @Override
    public List<RoomType> getRoomTypesByProperty(String propertyId) {
        return roomTypeRepository.findByProperty_PropertyIdOrderByFloorAsc(propertyId);
    }

    @Override
    public void addRoomType(Property property, RoomType roomType, Integer roomCount) {
        // Generate RoomType ID
        String roomTypeId = idGenerator.generateRoomTypeId(property, roomType.getName(), roomType.getFloor());
        roomType.setRoomTypeId(roomTypeId);
        roomType.setProperty(property);
        
        // Save room type
        RoomType savedRoomType = roomTypeRepository.save(roomType);
        
        // Create rooms for this room type
        for (int i = 1; i <= roomCount; i++) {
            Room room = new Room();
            String roomId = idGenerator.generateRoomId(property.getPropertyId(), roomType.getFloor(), i);
            room.setRoomId(roomId);
            room.setName(roomType.getName() + " " + i);
            room.setRoomType(savedRoomType);
            roomRepository.save(room);
        }
        
        // Update total rooms in property
        int currentTotal = property.getTotalRoom() != null ? property.getTotalRoom() : 0;
        property.setTotalRoom(currentTotal + roomCount);
    }
}
