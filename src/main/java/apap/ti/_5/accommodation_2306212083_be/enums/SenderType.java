package apap.ti._5.accommodation_2306212083_be.enums;

/**
 * Enum representing the type of message sender in ticket communication.
 */
public enum SenderType {
    /**
     * Message sent by the customer who created the ticket
     */
    USER,

    /**
     * Message sent by an admin handling the ticket
     */
    ADMIN,

    /**
     * Message sent by a vendor (accommodation owner, etc.)
     */
    VENDOR
}
