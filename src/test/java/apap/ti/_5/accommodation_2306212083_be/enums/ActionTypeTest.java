package apap.ti._5.accommodation_2306212083_be.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionTypeTest {

    @Test
    void values_ReturnsAllEnumValues() {
        ActionType[] values = ActionType.values();
        
        assertEquals(6, values.length);
        assertEquals(ActionType.CREATED, values[0]);
        assertEquals(ActionType.STATUS_CHANGED, values[1]);
        assertEquals(ActionType.ASSIGNED, values[2]);
        assertEquals(ActionType.PROGRESS_ADDED, values[3]);
        assertEquals(ActionType.MESSAGE_ADDED, values[4]);
        assertEquals(ActionType.CLOSED, values[5]);
    }

    @Test
    void valueOf_ReturnsCorrectEnum() {
        assertEquals(ActionType.CREATED, ActionType.valueOf("CREATED"));
        assertEquals(ActionType.STATUS_CHANGED, ActionType.valueOf("STATUS_CHANGED"));
        assertEquals(ActionType.ASSIGNED, ActionType.valueOf("ASSIGNED"));
        assertEquals(ActionType.PROGRESS_ADDED, ActionType.valueOf("PROGRESS_ADDED"));
        assertEquals(ActionType.MESSAGE_ADDED, ActionType.valueOf("MESSAGE_ADDED"));
        assertEquals(ActionType.CLOSED, ActionType.valueOf("CLOSED"));
    }

    @Test
    void valueOf_InvalidValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ActionType.valueOf("INVALID"));
    }

    @Test
    void name_ReturnsCorrectName() {
        assertEquals("CREATED", ActionType.CREATED.name());
        assertEquals("STATUS_CHANGED", ActionType.STATUS_CHANGED.name());
        assertEquals("ASSIGNED", ActionType.ASSIGNED.name());
        assertEquals("PROGRESS_ADDED", ActionType.PROGRESS_ADDED.name());
        assertEquals("MESSAGE_ADDED", ActionType.MESSAGE_ADDED.name());
        assertEquals("CLOSED", ActionType.CLOSED.name());
    }

    @Test
    void ordinal_ReturnsCorrectOrdinal() {
        assertEquals(0, ActionType.CREATED.ordinal());
        assertEquals(1, ActionType.STATUS_CHANGED.ordinal());
        assertEquals(2, ActionType.ASSIGNED.ordinal());
        assertEquals(3, ActionType.PROGRESS_ADDED.ordinal());
        assertEquals(4, ActionType.MESSAGE_ADDED.ordinal());
        assertEquals(5, ActionType.CLOSED.ordinal());
    }
}
