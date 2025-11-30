package apap.ti._5.accommodation_2306212083_be.enums;

/**
 * Enum representing the external service sources for bookings
 */
public enum ServiceSource {
    ACCOMMODATION("accommodation", "http://2306212083-be.hafizmuh.site/api/bookings"),
    INSURANCE("insurance", "http://2306240061-be.hafizmuh.site/api/policy"),
    FLIGHT("flight", "http://2306211660-be.hafizmuh.site/api/booking"),
    RENTAL("rental", "http://2306203236-be.hafizmuh.site/api/bookings"),
    TOUR("tour", "http://2306219575-be.hafizmuh.site/api/package/public/all"),
    BILL("bill", "http://2306211660-be.hafizmuh.site/api/bill");

    private final String code;
    private final String apiUrl;

    ServiceSource(String code, String apiUrl) {
        this.code = code;
        this.apiUrl = apiUrl;
    }

    public String getCode() {
        return code;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}
