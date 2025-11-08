package apap.ti._5.accommodation_2306212083_be.util;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {

    private IdGenerator idGenerator;
    private UUID testOwnerId;
    private Property testProperty;

    @BeforeEach
    void setUp() {
        idGenerator = new IdGenerator();
        testOwnerId = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        
        testProperty = Property.builder()
            .propertyId("HOT-89abc-001")
            .propertyName("Test Hotel")
            .type(0)
            .build();
    }

    // ==================== generatePropertyId Tests ====================

    @Test
    void testGeneratePropertyIdForHotel() {
        // Arrange
        Integer type = 0;
        int counter = 1;

        // Act
        String result = idGenerator.generatePropertyId(type, testOwnerId, counter);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("HOT-"));
        assertTrue(result.endsWith("-001"));
        assertTrue(result.contains("9abc")); // Last 4 chars of UUID without dashes
    }

    @Test
    void testGeneratePropertyIdForVilla() {
        // Arrange
        Integer type = 1;
        int counter = 5;

        // Act
        String result = idGenerator.generatePropertyId(type, testOwnerId, counter);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("VIL-"));
        assertTrue(result.endsWith("-005"));
        assertTrue(result.contains("9abc"));
    }

    @Test
    void testGeneratePropertyIdForApartment() {
        // Arrange
        Integer type = 2;
        int counter = 10;

        // Act
        String result = idGenerator.generatePropertyId(type, testOwnerId, counter);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("APT-"));
        assertTrue(result.endsWith("-010"));
        assertTrue(result.contains("9abc"));
    }

    @Test
    void testGeneratePropertyIdForUnknownType() {
        // Arrange
        Integer type = 999;
        int counter = 1;

        // Act
        String result = idGenerator.generatePropertyId(type, testOwnerId, counter);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("UNK-"));
        assertTrue(result.endsWith("-001"));
    }

    @Test
    void testGeneratePropertyIdWithNegativeType() {
        // Arrange
        Integer type = -1;
        int counter = 1;

        // Act
        String result = idGenerator.generatePropertyId(type, testOwnerId, counter);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("UNK-"));
    }

    @ParameterizedTest
    @CsvSource({
        "0, HOT",
        "1, VIL",
        "2, APT",
        "3, UNK",
        "100, UNK"
    })
    void testGeneratePropertyIdWithDifferentTypes(Integer type, String expectedPrefix) {
        // Act
        String result = idGenerator.generatePropertyId(type, testOwnerId, 1);

        // Assert
        assertTrue(result.startsWith(expectedPrefix + "-"));
    }

    @Test
    void testGeneratePropertyIdWithLargeCounter() {
        // Arrange
        Integer type = 0;
        int counter = 999;

        // Act
        String result = idGenerator.generatePropertyId(type, testOwnerId, counter);

        // Assert
        assertNotNull(result);
        assertTrue(result.endsWith("-999"));
    }

    @Test
    void testGeneratePropertyIdWithZeroCounter() {
        // Arrange
        Integer type = 0;
        int counter = 0;

        // Act
        String result = idGenerator.generatePropertyId(type, testOwnerId, counter);

        // Assert
        assertNotNull(result);
        assertTrue(result.endsWith("-000"));
    }

    @Test
    void testGeneratePropertyIdExtractsLast8CharsFromUUID() {
        // Arrange
        UUID customUuid = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeee1234");
        Integer type = 0;
        int counter = 1;

        // Act
        String result = idGenerator.generatePropertyId(type, customUuid, counter);

        // Assert
        // UUID without dashes: "aaaaaaaabbbbccccddddeeeeeeee1234", substring(28) = "1234"
        assertTrue(result.contains("1234"));
    }

    @Test
    void testGeneratePropertyIdFormatConsistency() {
        // Arrange
        Integer type = 0;
        int counter = 42;

        // Act
        String result = idGenerator.generatePropertyId(type, testOwnerId, counter);

        // Assert
        String[] parts = result.split("-");
        assertEquals(3, parts.length);
        assertEquals("HOT", parts[0]);
        assertEquals(4, parts[1].length()); // UUID part should be 4 chars (last 4 of UUID without dashes)
        assertEquals("042", parts[2]); // Counter should be 3 digits
    }

    // ==================== generateRoomTypeId Tests ====================

    @Test
    void testGenerateRoomTypeId() {
        // Arrange
        String roomTypeName = "Suite";
        Integer floor = 2;

        // Act
        String result = idGenerator.generateRoomTypeId(testProperty, roomTypeName, floor);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("001"));
        assertTrue(result.contains("Suite"));
        assertTrue(result.contains("2"));
    }

    @Test
    void testGenerateRoomTypeIdWithDeluxe() {
        // Arrange
        String roomTypeName = "Deluxe";
        Integer floor = 3;

        // Act
        String result = idGenerator.generateRoomTypeId(testProperty, roomTypeName, floor);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Deluxe"));
        assertTrue(result.contains("3"));
    }

    @Test
    void testGenerateRoomTypeIdWithDifferentFloors() {
        // Arrange
        String roomTypeName = "Standard";

        // Act
        String result1 = idGenerator.generateRoomTypeId(testProperty, roomTypeName, 1);
        String result2 = idGenerator.generateRoomTypeId(testProperty, roomTypeName, 5);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.contains("1"));
        assertTrue(result2.contains("5"));
        assertNotEquals(result1, result2);
    }

    @Test
    void testGenerateRoomTypeIdExtractsPropertyCounter() {
        // Arrange
        Property property = Property.builder()
            .propertyId("VIL-xyz45-099")
            .build();
        String roomTypeName = "Presidential";
        Integer floor = 10;

        // Act
        String result = idGenerator.generateRoomTypeId(property, roomTypeName, floor);

        // Assert
        assertTrue(result.contains("099"));
    }

    @Test
    void testGenerateRoomTypeIdWithZeroFloor() {
        // Arrange
        String roomTypeName = "Ground";
        Integer floor = 0;

        // Act
        String result = idGenerator.generateRoomTypeId(testProperty, roomTypeName, floor);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("0"));
    }

    @Test
    void testGenerateRoomTypeIdWithSpecialCharactersInName() {
        // Arrange
        String roomTypeName = "Suite-Premium";
        Integer floor = 5;

        // Act
        String result = idGenerator.generateRoomTypeId(testProperty, roomTypeName, floor);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Suite-Premium"));
    }

    // ==================== generateRoomId Tests ====================

    @Test
    void testGenerateRoomId() {
        // Arrange
        String propertyId = "HOT-12345-001";
        Integer floor = 2;
        Integer unitNumber = 1;

        // Act
        String result = idGenerator.generateRoomId(propertyId, floor, unitNumber);

        // Assert
        assertNotNull(result);
        assertEquals("HOT-12345-001-201", result);
    }

    @Test
    void testGenerateRoomIdWithDifferentFloors() {
        // Arrange
        String propertyId = "VIL-xyz45-002";
        Integer unitNumber = 5;

        // Act
        String result1 = idGenerator.generateRoomId(propertyId, 1, unitNumber);
        String result2 = idGenerator.generateRoomId(propertyId, 3, unitNumber);

        // Assert
        assertEquals("VIL-xyz45-002-105", result1);
        assertEquals("VIL-xyz45-002-305", result2);
    }

    @Test
    void testGenerateRoomIdWithDifferentUnitNumbers() {
        // Arrange
        String propertyId = "APT-abc99-003";
        Integer floor = 5;

        // Act
        String result1 = idGenerator.generateRoomId(propertyId, floor, 1);
        String result2 = idGenerator.generateRoomId(propertyId, floor, 15);
        String result3 = idGenerator.generateRoomId(propertyId, floor, 99);

        // Assert
        assertEquals("APT-abc99-003-501", result1);
        assertEquals("APT-abc99-003-515", result2);
        assertEquals("APT-abc99-003-599", result3);
    }

    @Test
    void testGenerateRoomIdFormatsUnitNumberWithTwoDigits() {
        // Arrange
        String propertyId = "HOT-11111-001";
        Integer floor = 3;
        Integer unitNumber = 7;

        // Act
        String result = idGenerator.generateRoomId(propertyId, floor, unitNumber);

        // Assert
        assertTrue(result.endsWith("307"));
    }

    @Test
    void testGenerateRoomIdWithGroundFloor() {
        // Arrange
        String propertyId = "HOT-22222-001";
        Integer floor = 0;
        Integer unitNumber = 1;

        // Act
        String result = idGenerator.generateRoomId(propertyId, floor, unitNumber);

        // Assert
        assertTrue(result.endsWith("001"));
    }

    @Test
    void testGenerateRoomIdWithHighFloor() {
        // Arrange
        String propertyId = "HOT-33333-001";
        Integer floor = 15;
        Integer unitNumber = 8;

        // Act
        String result = idGenerator.generateRoomId(propertyId, floor, unitNumber);

        // Assert
        assertTrue(result.endsWith("1508"));
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, 101",
        "2, 5, 205",
        "3, 10, 310",
        "10, 25, 1025"
    })
    void testGenerateRoomIdWithVariousFloorAndUnit(Integer floor, Integer unit, String expectedSuffix) {
        // Arrange
        String propertyId = "TEST-123-001";

        // Act
        String result = idGenerator.generateRoomId(propertyId, floor, unit);

        // Assert
        assertTrue(result.endsWith(expectedSuffix));
    }

    // ==================== generateBookingId Tests ====================

    @Test
    void testGenerateBookingIdFormat() {
        // Arrange
        String roomId = "HOT-12345-001-201";

        // Act
        String result = idGenerator.generateBookingId(roomId);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("BOOK-201-"));
        assertTrue(result.contains("."));
        
        String[] mainParts = result.split("-");
        assertEquals(5, mainParts.length);
        assertEquals("BOOK", mainParts[0]);
        assertEquals("201", mainParts[1]);
    }

    @Test
    void testGenerateBookingIdExtractsLast3Digits() {
        // Arrange
        String roomId1 = "HOT-12345-001-305";
        String roomId2 = "VIL-xyz45-002-107";

        // Act
        String result1 = idGenerator.generateBookingId(roomId1);
        String result2 = idGenerator.generateBookingId(roomId2);

        // Assert
        assertTrue(result1.contains("-305-"));
        assertTrue(result2.contains("-107-"));
    }

    @Test
    void testGenerateBookingIdContainsDateComponents() {
        // Arrange
        String roomId = "HOT-12345-001-201";

        // Act
        String result = idGenerator.generateBookingId(roomId);

        // Assert
        String[] parts = result.split("-");
        assertTrue(parts.length >= 4);
        
        // Date part should be 6 digits (YYMMDD)
        String datePart = parts[2];
        assertEquals(6, datePart.length());
        assertTrue(datePart.matches("\\d{6}"));
    }

    @Test
    void testGenerateBookingIdContainsTimeComponents() {
        // Arrange
        String roomId = "HOT-12345-001-201";

        // Act
        String result = idGenerator.generateBookingId(roomId);

        // Assert
        String[] parts = result.split("-");
        assertTrue(parts.length >= 4);
        
        // Time part should be 4 digits (HHMM)
        String timePart = parts[3];
        assertEquals(4, timePart.length());
        assertTrue(timePart.matches("\\d{4}"));
    }

    @Test
    void testGenerateBookingIdContainsSecondsAndMilliseconds() {
        // Arrange
        String roomId = "HOT-12345-001-201";

        // Act
        String result = idGenerator.generateBookingId(roomId);

        // Assert
        String[] parts = result.split("-");
        String lastPart = parts[parts.length - 1];
        
        // Should contain SS.MS format
        assertTrue(lastPart.contains("."));
        String[] timeParts = lastPart.split("\\.");
        assertEquals(2, timeParts.length);
        assertEquals(2, timeParts[0].length()); // Seconds
        assertEquals(2, timeParts[1].length()); // Centiseconds
    }

    @Test
    void testGenerateBookingIdUniqueness() throws InterruptedException {
        // Arrange
        String roomId = "HOT-12345-001-201";

        // Act
        String result1 = idGenerator.generateBookingId(roomId);
        Thread.sleep(10); // Small delay to ensure different timestamp
        String result2 = idGenerator.generateBookingId(roomId);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1, result2);
    }

    @Test
    void testGenerateBookingIdWithDifferentRoomIds() {
        // Arrange
        String roomId1 = "HOT-12345-001-201";
        String roomId2 = "VIL-xyz45-002-305";

        // Act
        String result1 = idGenerator.generateBookingId(roomId1);
        String result2 = idGenerator.generateBookingId(roomId2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.contains("-201-"));
        assertTrue(result2.contains("-305-"));
    }

    @Test
    void testGenerateBookingIdWithSingleDigitRoomNumber() {
        // Arrange
        String roomId = "HOT-12345-001-5";

        // Act
        String result = idGenerator.generateBookingId(roomId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("-5-"));
    }

    @Test
    void testGenerateBookingIdMultipleCalls() {
        // Arrange
        String roomId = "HOT-12345-001-201";

        // Act
        String result1 = idGenerator.generateBookingId(roomId);
        String result2 = idGenerator.generateBookingId(roomId);
        String result3 = idGenerator.generateBookingId(roomId);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        
        // All should start with same room number
        assertTrue(result1.startsWith("BOOK-201-"));
        assertTrue(result2.startsWith("BOOK-201-"));
        assertTrue(result3.startsWith("BOOK-201-"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "HOT-12345-001-101",
        "VIL-xyz45-002-202",
        "APT-abc99-003-303",
        "HOT-11111-999-1501"
    })
    void testGenerateBookingIdWithVariousRoomIds(String roomId) {
        // Act
        String result = idGenerator.generateBookingId(roomId);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("BOOK-"));
        assertTrue(result.contains("."));
        
        String[] parts = result.split("-");
        assertEquals(5, parts.length);
    }

    @Test
    void testGenerateBookingIdCompleteFormat() {
        // Arrange
        String roomId = "HOT-12345-001-201";

        // Act
        String result = idGenerator.generateBookingId(roomId);

        // Assert
        // Format: BOOK-{last3}-{YYMMDD}-{HHMM}-{SS}.{MS}
        String pattern = "BOOK-\\d+-\\d{6}-\\d{4}-\\d{2}\\.\\d{2}";
        assertTrue(result.matches(pattern), 
            "Booking ID should match pattern: " + pattern + ", but was: " + result);
    }

    // ==================== Integration Tests ====================

    @Test
    void testGenerateAllIdsForCompleteWorkflow() {
        // Arrange
        UUID ownerId = UUID.randomUUID();
        Integer propertyType = 0;
        int propertyCounter = 1;

        // Act - Generate Property ID
        String propertyId = idGenerator.generatePropertyId(propertyType, ownerId, propertyCounter);
        
        // Create property for room type
        Property property = Property.builder()
            .propertyId(propertyId)
            .build();
        
        // Generate Room Type ID
        String roomTypeId = idGenerator.generateRoomTypeId(property, "Suite", 2);
        
        // Generate Room ID
        String roomId = idGenerator.generateRoomId(propertyId, 2, 5);
        
        // Generate Booking ID
        String bookingId = idGenerator.generateBookingId(roomId);

        // Assert
        assertNotNull(propertyId);
        assertNotNull(roomTypeId);
        assertNotNull(roomId);
        assertNotNull(bookingId);
        
        assertTrue(propertyId.startsWith("HOT-"));
        assertTrue(roomId.contains(propertyId));
        assertTrue(bookingId.startsWith("BOOK-"));
    }
}