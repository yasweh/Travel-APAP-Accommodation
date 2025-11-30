package apap.ti._5.accommodation_2306212083_be.util;

import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Utility class to validate room types based on property type.
 * Each property type has specific allowed room types.
 */
@Component
public class PropertyRoomTypeValidator {

    // Define valid room types for each property type
    private static final Map<Integer, Set<String>> VALID_ROOM_TYPES = new HashMap<>();

    static {
        // Hotel (type = 0)
        VALID_ROOM_TYPES.put(0, new HashSet<>(Arrays.asList(
            "Single Room",
            "Double Room", 
            "Deluxe Room",
            "Superior Room",
            "Suite",
            "Family Room"
        )));

        // Villa (type = 1)
        VALID_ROOM_TYPES.put(1, new HashSet<>(Arrays.asList(
            "Luxury",
            "Beachfront",
            "Pool Villa",
            "Mountain View"
        )));

        // Apartemen (type = 2)
        VALID_ROOM_TYPES.put(2, new HashSet<>(Arrays.asList(
            "Studio",
            "One Bedroom",
            "Two Bedroom",
            "Penthouse"
        )));
    }

    /**
     * Check if a room type name is valid for a given property type
     * 
     * @param propertyType The property type (0=Hotel, 1=Villa, 2=Apartemen)
     * @param roomTypeName The room type name to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidRoomType(Integer propertyType, String roomTypeName) {
        if (propertyType == null || roomTypeName == null) {
            return false;
        }

        Set<String> validTypes = VALID_ROOM_TYPES.get(propertyType);
        if (validTypes == null) {
            return false;
        }

        return validTypes.contains(roomTypeName);
    }

    /**
     * Get all valid room types for a property type
     * 
     * @param propertyType The property type (0=Hotel, 1=Villa, 2=Apartemen)
     * @return Set of valid room type names
     */
    public Set<String> getValidRoomTypes(Integer propertyType) {
        if (propertyType == null) {
            return Collections.emptySet();
        }

        Set<String> validTypes = VALID_ROOM_TYPES.get(propertyType);
        return validTypes != null ? new HashSet<>(validTypes) : Collections.emptySet();
    }

    /**
     * Get property type string for display
     * 
     * @param propertyType The property type number
     * @return String representation of the property type
     */
    public String getPropertyTypeString(Integer propertyType) {
        if (propertyType == null) {
            return "Unknown";
        }

        switch (propertyType) {
            case 0: return "Hotel";
            case 1: return "Villa";
            case 2: return "Apartemen";
            default: return "Unknown";
        }
    }

    /**
     * Validate multiple room type names for a property type
     * 
     * @param propertyType The property type
     * @param roomTypeNames List of room type names to validate
     * @return Map with validation results - key: room type name, value: is valid
     */
    public Map<String, Boolean> validateRoomTypes(Integer propertyType, List<String> roomTypeNames) {
        Map<String, Boolean> results = new HashMap<>();
        
        if (roomTypeNames == null || roomTypeNames.isEmpty()) {
            return results;
        }

        for (String roomTypeName : roomTypeNames) {
            results.put(roomTypeName, isValidRoomType(propertyType, roomTypeName));
        }

        return results;
    }

    /**
     * Get invalid room types from a list
     * 
     * @param propertyType The property type
     * @param roomTypeNames List of room type names to check
     * @return List of invalid room type names
     */
    public List<String> getInvalidRoomTypes(Integer propertyType, List<String> roomTypeNames) {
        List<String> invalidTypes = new ArrayList<>();
        
        if (roomTypeNames == null || roomTypeNames.isEmpty()) {
            return invalidTypes;
        }

        for (String roomTypeName : roomTypeNames) {
            if (!isValidRoomType(propertyType, roomTypeName)) {
                invalidTypes.add(roomTypeName);
            }
        }

        return invalidTypes;
    }
}
