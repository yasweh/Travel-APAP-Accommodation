package apap.ti._5.accommodation_2306212083_be.util;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdGenerator {

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

    public String generateRoomTypeId(Property property, String roomTypeName, Integer floor) {
        String propertyCounter = property.getPropertyId().substring(property.getPropertyId().lastIndexOf("-") + 1);
        return propertyCounter + "–" + roomTypeName + "–" + floor;
    }

    public String generateRoomId(String propertyId, Integer floor, Integer unitNumber) {
        String roomNumber = String.format("%d%02d", floor, unitNumber);
        return propertyId + "-" + roomNumber;
    }

    public String generateBookingId(String roomId) {
        String[] parts = roomId.split("-");
        String last3Digits = parts[parts.length - 1];

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
