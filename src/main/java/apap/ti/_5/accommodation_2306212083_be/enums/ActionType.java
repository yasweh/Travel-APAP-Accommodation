package apap.ti._5.accommodation_2306212083_be.enums;

/**
 * Enum representing types of progress actions taken on a support ticket.
 */
public enum ActionType {
    /**
     * Investigation into the issue has begun
     */
    INVESTIGATION_STARTED,

    /**
     * Vendor has been contacted regarding the issue
     */
    CONTACTED_VENDOR,

    /**
     * Awaiting response from vendor
     */
    AWAITING_VENDOR_RESPONSE,

    /**
     * Awaiting response from customer
     */
    AWAITING_CUSTOMER_RESPONSE,

    /**
     * Issue has been identified
     */
    ISSUE_IDENTIFIED,

    /**
     * Solution is being implemented
     */
    SOLUTION_IN_PROGRESS,

    /**
     * Issue has been resolved
     */
    ISSUE_RESOLVED,

    /**
     * Refund has been processed
     */
    REFUND_PROCESSED,

    /**
     * Booking has been cancelled
     */
    BOOKING_CANCELLED,

    /**
     * Escalated to higher support tier
     */
    ESCALATED,

    /**
     * Other progress action
     */
    OTHER
}
