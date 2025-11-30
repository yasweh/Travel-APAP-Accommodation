package apap.ti._5.accommodation_2306212083_be.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SenderTypeTest {

    @Test
    void values_ReturnsAllEnumValues() {
        SenderType[] values = SenderType.values();
        
        assertEquals(2, values.length);
        assertEquals(SenderType.CUSTOMER, values[0]);
        assertEquals(SenderType.ADMIN, values[1]);
    }

    @Test
    void valueOf_ReturnsCorrectEnum() {
        assertEquals(SenderType.CUSTOMER, SenderType.valueOf("CUSTOMER"));
        assertEquals(SenderType.ADMIN, SenderType.valueOf("ADMIN"));
    }

    @Test
    void valueOf_InvalidValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> SenderType.valueOf("INVALID"));
    }

    @Test
    void name_ReturnsCorrectName() {
        assertEquals("CUSTOMER", SenderType.CUSTOMER.name());
        assertEquals("ADMIN", SenderType.ADMIN.name());
    }

    @Test
    void ordinal_ReturnsCorrectOrdinal() {
        assertEquals(0, SenderType.CUSTOMER.ordinal());
        assertEquals(1, SenderType.ADMIN.ordinal());
    }
}
