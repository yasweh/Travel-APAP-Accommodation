package apap.ti._5.accommodation_2306212083_be.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketStatusTest {

    @Test
    void values_ReturnsAllEnumValues() {
        TicketStatus[] values = TicketStatus.values();
        
        assertEquals(3, values.length);
        assertEquals(TicketStatus.OPEN, values[0]);
        assertEquals(TicketStatus.IN_PROGRESS, values[1]);
        assertEquals(TicketStatus.CLOSED, values[2]);
    }

    @Test
    void valueOf_ReturnsCorrectEnum() {
        assertEquals(TicketStatus.OPEN, TicketStatus.valueOf("OPEN"));
        assertEquals(TicketStatus.IN_PROGRESS, TicketStatus.valueOf("IN_PROGRESS"));
        assertEquals(TicketStatus.CLOSED, TicketStatus.valueOf("CLOSED"));
    }

    @Test
    void valueOf_InvalidValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> TicketStatus.valueOf("INVALID"));
    }

    @Test
    void name_ReturnsCorrectName() {
        assertEquals("OPEN", TicketStatus.OPEN.name());
        assertEquals("IN_PROGRESS", TicketStatus.IN_PROGRESS.name());
        assertEquals("CLOSED", TicketStatus.CLOSED.name());
    }

    @Test
    void ordinal_ReturnsCorrectOrdinal() {
        assertEquals(0, TicketStatus.OPEN.ordinal());
        assertEquals(1, TicketStatus.IN_PROGRESS.ordinal());
        assertEquals(2, TicketStatus.CLOSED.ordinal());
    }
}
