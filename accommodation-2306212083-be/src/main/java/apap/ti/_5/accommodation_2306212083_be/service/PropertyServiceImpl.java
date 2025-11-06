package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.PropertyStatisticsDTO;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306212083_be.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final AccommodationBookingRepository bookingRepository;
    private final IdGenerator idGenerator;

    @Override
    public Property createProperty(Property property, List<RoomType> roomTypes, List<List<Integer>> roomCounts) {
        // Generate Property ID
        int counter = (int) propertyRepository.count() + 1;
        String propertyId = idGenerator.generatePropertyId(property.getType(), property.getOwnerId(), counter);
        property.setPropertyId(propertyId);
        
        // Save property first
        Property savedProperty = propertyRepository.save(property);
        
        // Create room types and rooms with floor-based numbering
        int totalRooms = 0;
        
        // Group room types by floor to handle enumeration correctly
        Map<Integer, Integer> floorRoomCounter = new HashMap<>();
        
        for (int i = 0; i < roomTypes.size(); i++) {
            RoomType roomType = roomTypes.get(i);
            roomType.setProperty(savedProperty);
            
            // Set timestamps
            roomType.setCreatedDate(LocalDateTime.now());
            roomType.setUpdatedDate(LocalDateTime.now());
            
            // Generate RoomType ID
            String roomTypeId = idGenerator.generateRoomTypeId(savedProperty, roomType.getName(), roomType.getFloor());
            roomType.setRoomTypeId(roomTypeId);
            
            RoomType savedRoomType = roomTypeRepository.save(roomType);
            
            // Get floor number
            Integer floor = roomType.getFloor();
            
            // Initialize counter for this floor if not exists
            if (!floorRoomCounter.containsKey(floor)) {
                floorRoomCounter.put(floor, 1);
            }
            
            // Create rooms for this room type with floor-based numbering
            List<Integer> counts = roomCounts.get(i);
            int roomCount = counts.get(0);
            
            for (int j = 0; j < roomCount; j++) {
                Room room = new Room();
                
                // Get current room number for this floor
                int roomNumber = floorRoomCounter.get(floor);
                
                // Generate room name: Room FloorXX (e.g., Room 201, Room 202, ..., Room 210, Room 211, ...)
                String roomNumber_str = String.format("%d%02d", floor, roomNumber);
                String roomName = "Room " + roomNumber_str;
                
                room.setRoomId(propertyId + "-ROOM-" + roomNumber_str);
                room.setName(roomName);
                room.setRoomType(savedRoomType);
                room.setAvailabilityStatus(1);
                room.setActiveRoom(1);
                room.setCreatedDate(LocalDateTime.now());
                room.setUpdatedDate(LocalDateTime.now());
                roomRepository.save(room);
                
                // Increment counter for this floor
                floorRoomCounter.put(floor, roomNumber + 1);
                totalRooms++;
            }
        }
        
        // Update total rooms in property
        savedProperty.setTotalRoom(totalRooms);
        savedProperty.setActiveRoom(totalRooms);
        return propertyRepository.save(savedProperty);
    }

    @Override
    public Optional<Property> getPropertyById(String id) {
        return propertyRepository.findByPropertyIdAndActiveStatus(id, 1);
    }

    @Override
    public List<Property> getAllActiveProperties() {
        return propertyRepository.findByActiveStatus(1);
    }

    @Override
    public Property updateProperty(Property property) {
        return propertyRepository.save(property);
    }

    @Override
    public void deleteProperty(String id) {
        Optional<Property> property = propertyRepository.findById(id);
        if (property.isPresent()) {
            Property p = property.get();
            p.setActiveStatus(0);
            propertyRepository.save(p);
        }
    }

    @Override
    public List<Property> getPropertiesByOwner(UUID ownerId) {
        return propertyRepository.findByOwnerIdAndActiveStatus(ownerId, 1);
    }

    @Override
    public List<Property> searchProperties(String name, Integer type, Integer province) {
        if (name != null && !name.isEmpty()) {
            return propertyRepository.findByPropertyNameContainingIgnoreCaseAndActiveStatus(name, 1);
        } else if (type != null) {
            return propertyRepository.findByTypeAndActiveStatus(type, 1);
        } else if (province != null) {
            return propertyRepository.findByProvinceAndActiveStatus(province, 1);
        }
        return getAllActiveProperties();
    }

    @Override
    public void updatePropertyIncome(String propertyId, Integer amount) {
        Optional<Property> propertyOpt = propertyRepository.findById(propertyId);
        if (propertyOpt.isPresent()) {
            Property property = propertyOpt.get();
            property.setIncome(property.getIncome() + amount);
            propertyRepository.save(property);
        }
    }

    @Override
    public Property updatePropertyWithRoomTypes(Property updatedProperty, List<RoomType> updatedRoomTypes, Map<String, Integer> roomCountMap) {
        Optional<Property> existingOpt = propertyRepository.findById(updatedProperty.getPropertyId());
        if (!existingOpt.isPresent()) {
            throw new RuntimeException("Property not found");
        }

        Property existing = existingOpt.get();
        
        // Update basic property info
        existing.setPropertyName(updatedProperty.getPropertyName());
        existing.setDescription(updatedProperty.getDescription());
        existing.setAddress(updatedProperty.getAddress());
        existing.setProvince(updatedProperty.getProvince());
        
        Property savedProperty = propertyRepository.save(existing);
        
        // Build a map of existing room numbers per floor for continuous numbering
        Map<Integer, Integer> floorRoomCounter = new HashMap<>();
        
        // Get all existing rooms for this property to find the highest room number per floor
        List<Room> existingRooms = roomRepository.findByRoomType_Property_PropertyId(savedProperty.getPropertyId());
        for (Room room : existingRooms) {
            try {
                // Extract floor and room number from room name (e.g., "Room 201" -> floor 2, number 01)
                String roomName = room.getName();
                if (roomName != null && roomName.startsWith("Room ")) {
                    // Remove "Room " prefix
                    String numberPart = roomName.substring(5); // "Room " is 5 characters
                    if (numberPart.length() >= 2) {
                        int floor = Integer.parseInt(numberPart.substring(0, numberPart.length() - 2));
                        int roomNum = Integer.parseInt(numberPart.substring(numberPart.length() - 2));
                        
                        // Update counter to next available number
                        floorRoomCounter.put(floor, Math.max(floorRoomCounter.getOrDefault(floor, 0), roomNum + 1));
                    }
                }
            } catch (NumberFormatException e) {
                // Skip rooms with non-standard naming
            }
        }
        
        // Update room types
        if (updatedRoomTypes != null && !updatedRoomTypes.isEmpty()) {
            for (RoomType roomType : updatedRoomTypes) {
                // Check if this is an existing room type (has roomTypeId) or a new one
                if (roomType.getRoomTypeId() != null && !roomType.getRoomTypeId().isEmpty()) {
                    // Update existing room type (only editable fields)
                    Optional<RoomType> existingRoomType = roomTypeRepository.findById(roomType.getRoomTypeId());
                    if (existingRoomType.isPresent()) {
                        RoomType rt = existingRoomType.get();
                        rt.setName(roomType.getName());
                        rt.setPrice(roomType.getPrice());
                        rt.setCapacity(roomType.getCapacity());
                        rt.setFacility(roomType.getFacility());
                        rt.setDescription(roomType.getDescription());
                        rt.setUpdatedDate(LocalDateTime.now());
                        roomTypeRepository.save(rt);
                    }
                } else {
                    // This is a new room type - check if room type with same name and floor already exists
                    Integer floor = roomType.getFloor();
                    
                    // Initialize counter for this floor if not exists
                    if (!floorRoomCounter.containsKey(floor)) {
                        floorRoomCounter.put(floor, 1);
                    }
                    
                    // Check if room type with same name and floor already exists
                    RoomType existingRoomType = roomTypeRepository.findByProperty_PropertyIdAndNameAndFloor(
                            savedProperty.getPropertyId(), roomType.getName(), floor);
                    
                    RoomType targetRoomType;
                    
                    if (existingRoomType != null) {
                        // Room type already exists, just add rooms to it
                        targetRoomType = existingRoomType;
                        targetRoomType.setUpdatedDate(LocalDateTime.now());
                        roomTypeRepository.save(targetRoomType);
                    } else {
                        // Create new room type
                        String roomTypeId = idGenerator.generateRoomTypeId(savedProperty, roomType.getName(), floor);
                        roomType.setRoomTypeId(roomTypeId);
                        roomType.setProperty(savedProperty);
                        roomType.setCreatedDate(LocalDateTime.now());
                        roomType.setUpdatedDate(LocalDateTime.now());
                        
                        targetRoomType = roomTypeRepository.save(roomType);
                    }
                    
                    // Get roomCount from the roomCountMap
                    String key = roomType.getName() + "_" + floor;
                    Integer roomCount = roomCountMap.getOrDefault(key, 1);
                    
                    // Create rooms for this room type with continuous numbering
                    for (int i = 0; i < roomCount; i++) {
                        Room room = new Room();
                        
                        // Get current room number for this floor
                        int roomNumber = floorRoomCounter.get(floor);
                        
                        // Generate room name: Room FloorXX (e.g., Room 201, Room 202, ..., Room 210, Room 211, ...)
                        String roomNumber_str = String.format("%d%02d", floor, roomNumber);
                        String roomName = "Room " + roomNumber_str;
                        String roomId = savedProperty.getPropertyId() + "-ROOM-" + roomNumber_str;
                        
                        room.setRoomId(roomId);
                        room.setName(roomName);
                        room.setRoomType(targetRoomType);
                        room.setAvailabilityStatus(1);
                        room.setActiveRoom(1);
                        room.setCreatedDate(LocalDateTime.now());
                        room.setUpdatedDate(LocalDateTime.now());
                        
                        roomRepository.save(room);
                        
                        // Increment counter for this floor
                        floorRoomCounter.put(floor, roomNumber + 1);
                    }
                }
            }
        }
        
        return savedProperty;
    }

    @Override
    public void softDeleteProperty(String propertyId) {
        Optional<Property> propertyOpt = propertyRepository.findById(propertyId);
        if (!propertyOpt.isPresent()) {
            throw new RuntimeException("Property not found");
        }

        // Check for future bookings
        if (propertyRepository.hasFutureBookings(propertyId, LocalDateTime.now())) {
            throw new RuntimeException("Cannot delete property with future bookings");
        }

        Property property = propertyOpt.get();
        property.setActiveStatus(0); // Soft delete - set activeStatus to 0
        propertyRepository.save(property);
    }

    @Override
    public List<PropertyStatisticsDTO> getMonthlyStatistics(int month, int year) {
        // Only get active properties (not soft deleted)
        List<Property> properties = propertyRepository.findByActiveStatus(1);
        
        return properties.stream()
            .map(property -> {
                // Calculate total income from bookings with status=2 (done) in specified month/year
                int totalIncome = bookingRepository.findByRoom_RoomType_Property_PropertyId(property.getPropertyId())
                    .stream()
                    .filter(booking -> booking.getStatus() == 2) // Done status
                    .filter(booking -> {
                        LocalDateTime checkIn = booking.getCheckInDate();
                        return checkIn.getMonthValue() == month && checkIn.getYear() == year;
                    })
                    .mapToInt(booking -> booking.getTotalPrice())
                    .sum();
                
                long bookingCount = bookingRepository.findByRoom_RoomType_Property_PropertyId(property.getPropertyId())
                    .stream()
                    .filter(booking -> booking.getStatus() == 2)
                    .filter(booking -> {
                        LocalDateTime checkIn = booking.getCheckInDate();
                        return checkIn.getMonthValue() == month && checkIn.getYear() == year;
                    })
                    .count();
                
                return PropertyStatisticsDTO.builder()
                    .propertyId(property.getPropertyId())
                    .propertyName(property.getPropertyName())
                    .totalIncome(totalIncome)
                    .bookingCount((int) bookingCount)
                    .build();
            })
            .filter(stat -> stat.getTotalIncome() > 0) // Only include properties with income
            .sorted((a, b) -> Integer.compare(b.getTotalIncome(), a.getTotalIncome())) // Sort descending
            .collect(Collectors.toList());
    }
}
