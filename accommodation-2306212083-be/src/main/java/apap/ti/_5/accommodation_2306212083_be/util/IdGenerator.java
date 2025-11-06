package apap.ti._5.accommodation_2306212083_be.util;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdGenerator {

    /**
     * Generate Property ID
     * Format: {PREFIX}-{LAST_4_UUID}-{COUNTER}
     * Example: APT-0000-004
     */
    public String generatePropertyId(Integer type, UUID ownerId, int counter) {
        String prefix;
        switch (type) {
            case 0: prefix = "HOT"; break;
            case 1: prefix = "VIL"; break;
            case 2: prefix = "APT"; break;
            default: prefix = "UNK"; break;
        }
        
        String uuidPart = ownerId.toString().replace("-", "").substring(28);
        String counterPart = String.format("%03d", counter);
        
        return prefix + "-" + uuidPart + "-" + counterPart;
    }

    /**
     * Generate RoomType ID
     * Format: {COUNTER_PROPERTY}–{ROOM_TYPE_NAME}–{FLOOR}
     * Example: 003–Single Room–2
     */
    public String generateRoomTypeId(Property property, String roomTypeName, Integer floor) {
        String propertyCounter = property.getPropertyId().substring(property.getPropertyId().lastIndexOf("-") + 1);
        return propertyCounter + "–" + roomTypeName + "–" + floor;
    }

    /**
     * Generate Room ID
     * Format: {PROPERTY_ID}-{UNIT_NUMBER}
     * Example: APT-0000-004-101
     */
    public String generateRoomId(String propertyId, Integer floor, Integer unitNumber) {
        String roomNumber = String.format("%d%02d", floor, unitNumber);
        return propertyId + "-" + roomNumber;
    }

    /**
     * Generate Booking ID
     * Format: BOOK-{last3DigitsOfRoomID}-{YYMMdd}-{HHmm}-{ss}.{SS}
     * Example: Room ID APT-0000-004-101 → BOOK-101-251024-1438-12.45
     */
    public String generateBookingId(String roomId) {
        // Extract last 3 digits from room ID (e.g., "101" from "APT-0000-004-101")
        String[] parts = roomId.split("-");
        String last3Digits = parts[parts.length - 1]; // Gets "101"
        
        // Get current time components
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String yymmdd = String.format("%02d%02d%02d", 
            now.getYear() % 100, 
            now.getMonthValue(), 
            now.getDayOfMonth()
        );
        String hhmm = String.format("%02d%02d", now.getHour(), now.getMinute());
        String ss = String.format("%02d", now.getSecond());
        String ms = String.format("%02d", now.getNano() / 10000000); // Get centiseconds
        
        return String.format("BOOK-%s-%s-%s-%s.%s", last3Digits, yymmdd, hhmm, ss, ms);
    }
}
