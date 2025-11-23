package apap.ti._5.accommodation_2306212083_be.enums;

/**
 * Enum representing the current status of a support ticket.
 */
public enum TicketStatus {
    /**
     * Ticket has been created but not yet assigned or worked on
     */
    OPEN,

    /**
     * Ticket is actively being investigated or resolved
     */
    IN_PROGRESS,

    /**
     * Issue has been resolved, awaiting closure
     */
    RESOLVED,

    /**
     * Ticket is closed and no further action is required
     */
    CLOSED
}
