package apap.ti._5.accommodation_2306212083_be.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceSourceTest {

    @Test
    void values_ReturnsAllEnumValues() {
        ServiceSource[] values = ServiceSource.values();
        
        assertEquals(5, values.length);
        assertEquals(ServiceSource.ACCOMMODATION, values[0]);
        assertEquals(ServiceSource.INSURANCE, values[1]);
        assertEquals(ServiceSource.FLIGHT, values[2]);
        assertEquals(ServiceSource.RENTAL, values[3]);
        assertEquals(ServiceSource.TOUR, values[4]);
    }

    @Test
    void valueOf_ReturnsCorrectEnum() {
        assertEquals(ServiceSource.ACCOMMODATION, ServiceSource.valueOf("ACCOMMODATION"));
        assertEquals(ServiceSource.INSURANCE, ServiceSource.valueOf("INSURANCE"));
        assertEquals(ServiceSource.FLIGHT, ServiceSource.valueOf("FLIGHT"));
        assertEquals(ServiceSource.RENTAL, ServiceSource.valueOf("RENTAL"));
        assertEquals(ServiceSource.TOUR, ServiceSource.valueOf("TOUR"));
    }

    @Test
    void valueOf_InvalidValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ServiceSource.valueOf("INVALID"));
    }

    @Test
    void getCode_ReturnsCorrectCode() {
        assertEquals("accommodation", ServiceSource.ACCOMMODATION.getCode());
        assertEquals("insurance", ServiceSource.INSURANCE.getCode());
        assertEquals("flight", ServiceSource.FLIGHT.getCode());
        assertEquals("rental", ServiceSource.RENTAL.getCode());
        assertEquals("tour", ServiceSource.TOUR.getCode());
    }

    @Test
    void getApiUrl_ReturnsCorrectUrl() {
        assertEquals("http://2306212083-be.hafizmuh.site/api/bookings", ServiceSource.ACCOMMODATION.getApiUrl());
        assertEquals("http://2306240061-be.hafizmuh.site/api/policy", ServiceSource.INSURANCE.getApiUrl());
        assertEquals("http://2306211660-be.hafizmuh.site/api/booking", ServiceSource.FLIGHT.getApiUrl());
        assertEquals("http://2306203236-be.hafizmuh.site/api/bookings", ServiceSource.RENTAL.getApiUrl());
        assertEquals("http://2306219575-be.hafizmuh.site/api/packages", ServiceSource.TOUR.getApiUrl());
    }

    @Test
    void name_ReturnsCorrectName() {
        assertEquals("ACCOMMODATION", ServiceSource.ACCOMMODATION.name());
        assertEquals("INSURANCE", ServiceSource.INSURANCE.name());
        assertEquals("FLIGHT", ServiceSource.FLIGHT.name());
        assertEquals("RENTAL", ServiceSource.RENTAL.name());
        assertEquals("TOUR", ServiceSource.TOUR.name());
    }

    @Test
    void ordinal_ReturnsCorrectOrdinal() {
        assertEquals(0, ServiceSource.ACCOMMODATION.ordinal());
        assertEquals(1, ServiceSource.INSURANCE.ordinal());
        assertEquals(2, ServiceSource.FLIGHT.ordinal());
        assertEquals(3, ServiceSource.RENTAL.ordinal());
        assertEquals(4, ServiceSource.TOUR.ordinal());
    }
}
