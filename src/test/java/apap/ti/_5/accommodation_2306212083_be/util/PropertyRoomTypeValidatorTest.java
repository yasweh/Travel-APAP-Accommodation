package apap.ti._5.accommodation_2306212083_be.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PropertyRoomTypeValidatorTest {

    private PropertyRoomTypeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PropertyRoomTypeValidator();
    }

    @Test
    void testIsValidRoomType_Hotel_Valid() {
        assertTrue(validator.isValidRoomType(0, "Single Room"));
        assertTrue(validator.isValidRoomType(0, "Double Room"));
        assertTrue(validator.isValidRoomType(0, "Suite"));
    }

    @Test
    void testIsValidRoomType_Hotel_Invalid() {
        assertFalse(validator.isValidRoomType(0, "Luxury")); // Villa type
        assertFalse(validator.isValidRoomType(0, "Studio")); // Apartemen type
        assertFalse(validator.isValidRoomType(0, "Invalid Type"));
    }

    @Test
    void testIsValidRoomType_Villa_Valid() {
        assertTrue(validator.isValidRoomType(1, "Luxury"));
        assertTrue(validator.isValidRoomType(1, "Beachfront"));
        assertTrue(validator.isValidRoomType(1, "Pool Villa"));
    }

    @Test
    void testIsValidRoomType_Villa_Invalid() {
        assertFalse(validator.isValidRoomType(1, "Single Room")); // Hotel type
        assertFalse(validator.isValidRoomType(1, "Studio")); // Apartemen type
    }

    @Test
    void testIsValidRoomType_Apartemen_Valid() {
        assertTrue(validator.isValidRoomType(2, "Studio"));
        assertTrue(validator.isValidRoomType(2, "One Bedroom"));
        assertTrue(validator.isValidRoomType(2, "Penthouse"));
    }

    @Test
    void testIsValidRoomType_Apartemen_Invalid() {
        assertFalse(validator.isValidRoomType(2, "Single Room")); // Hotel type
        assertFalse(validator.isValidRoomType(2, "Luxury")); // Villa type
    }

    @Test
    void testIsValidRoomType_NullParameters() {
        assertFalse(validator.isValidRoomType(null, "Single Room"));
        assertFalse(validator.isValidRoomType(0, null));
        assertFalse(validator.isValidRoomType(null, null));
    }

    @Test
    void testIsValidRoomType_InvalidPropertyType() {
        assertFalse(validator.isValidRoomType(99, "Single Room"));
        assertFalse(validator.isValidRoomType(-1, "Luxury"));
    }

    @Test
    void testGetValidRoomTypes_Hotel() {
        Set<String> validTypes = validator.getValidRoomTypes(0);
        
        assertNotNull(validTypes);
        assertEquals(6, validTypes.size());
        assertTrue(validTypes.contains("Single Room"));
        assertTrue(validTypes.contains("Double Room"));
        assertTrue(validTypes.contains("Suite"));
    }

    @Test
    void testGetValidRoomTypes_Villa() {
        Set<String> validTypes = validator.getValidRoomTypes(1);
        
        assertNotNull(validTypes);
        assertEquals(4, validTypes.size());
        assertTrue(validTypes.contains("Luxury"));
        assertTrue(validTypes.contains("Pool Villa"));
    }

    @Test
    void testGetValidRoomTypes_Apartemen() {
        Set<String> validTypes = validator.getValidRoomTypes(2);
        
        assertNotNull(validTypes);
        assertEquals(4, validTypes.size());
        assertTrue(validTypes.contains("Studio"));
        assertTrue(validTypes.contains("Penthouse"));
    }

    @Test
    void testGetValidRoomTypes_InvalidType() {
        Set<String> validTypes = validator.getValidRoomTypes(99);
        
        assertNotNull(validTypes);
        assertTrue(validTypes.isEmpty());
    }

    @Test
    void testGetValidRoomTypes_Null() {
        Set<String> validTypes = validator.getValidRoomTypes(null);
        
        assertNotNull(validTypes);
        assertTrue(validTypes.isEmpty());
    }

    @Test
    void testGetPropertyTypeString() {
        assertEquals("Hotel", validator.getPropertyTypeString(0));
        assertEquals("Villa", validator.getPropertyTypeString(1));
        assertEquals("Apartemen", validator.getPropertyTypeString(2));
        assertEquals("Unknown", validator.getPropertyTypeString(99));
        assertEquals("Unknown", validator.getPropertyTypeString(null));
    }

    @Test
    void testValidateRoomTypes() {
        List<String> roomTypeNames = Arrays.asList("Single Room", "Luxury", "Suite");
        Map<String, Boolean> results = validator.validateRoomTypes(0, roomTypeNames);
        
        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.get("Single Room"));
        assertFalse(results.get("Luxury"));
        assertTrue(results.get("Suite"));
    }

    @Test
    void testValidateRoomTypes_EmptyList() {
        Map<String, Boolean> results = validator.validateRoomTypes(0, Arrays.asList());
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testValidateRoomTypes_Null() {
        Map<String, Boolean> results = validator.validateRoomTypes(0, null);
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetInvalidRoomTypes() {
        List<String> roomTypeNames = Arrays.asList("Single Room", "Luxury", "Suite", "Studio");
        List<String> invalidTypes = validator.getInvalidRoomTypes(0, roomTypeNames);
        
        assertNotNull(invalidTypes);
        assertEquals(2, invalidTypes.size());
        assertTrue(invalidTypes.contains("Luxury"));
        assertTrue(invalidTypes.contains("Studio"));
    }

    @Test
    void testGetInvalidRoomTypes_AllValid() {
        List<String> roomTypeNames = Arrays.asList("Single Room", "Double Room", "Suite");
        List<String> invalidTypes = validator.getInvalidRoomTypes(0, roomTypeNames);
        
        assertNotNull(invalidTypes);
        assertTrue(invalidTypes.isEmpty());
    }

    @Test
    void testGetInvalidRoomTypes_AllInvalid() {
        List<String> roomTypeNames = Arrays.asList("Luxury", "Studio", "Penthouse");
        List<String> invalidTypes = validator.getInvalidRoomTypes(0, roomTypeNames);
        
        assertNotNull(invalidTypes);
        assertEquals(3, invalidTypes.size());
    }

    @Test
    void testGetInvalidRoomTypes_EmptyList() {
        List<String> invalidTypes = validator.getInvalidRoomTypes(0, Arrays.asList());
        
        assertNotNull(invalidTypes);
        assertTrue(invalidTypes.isEmpty());
    }

    @Test
    void testGetInvalidRoomTypes_Null() {
        List<String> invalidTypes = validator.getInvalidRoomTypes(0, null);
        
        assertNotNull(invalidTypes);
        assertTrue(invalidTypes.isEmpty());
    }
}
