package apap.ti._5.accommodation_2306212083_be.enums;

/**
 * Enum representing the external service sources that can generate bookings.
 * Each service has a code used in ticket ID generation.
 */
public enum ServiceSource {
    ACCOMMODATION("ACC"),
    FLIGHT("FLT"),
    VEHICLE_RENTAL("VHC"),
    TOUR_PACKAGE("TPK"),
    INSURANCE("INS");

    private final String code;

    ServiceSource(String code) {
        this.code = code;
    }

    /**
     * Get the service code for ticket ID generation
     * @return Service code (e.g., "ACC", "FLT")
     */
    public String getCode() {
        return code;
    }

    /**
     * Get ServiceSource from code
     * @param code The service code
     * @return ServiceSource enum value
     */
    public static ServiceSource fromCode(String code) {
        for (ServiceSource service : values()) {
            if (service.code.equals(code)) {
                return service;
            }
        }
        throw new IllegalArgumentException("Invalid service code: " + code);
    }
}
