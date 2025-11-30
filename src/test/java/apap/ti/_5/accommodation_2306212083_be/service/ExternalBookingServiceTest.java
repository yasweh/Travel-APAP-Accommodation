package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.external.*;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.exception.ExternalServiceException;
import apap.ti._5.accommodation_2306212083_be.model.AccommodationBooking;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalBookingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AccommodationBookingRepository accommodationBookingRepository;

    @InjectMocks
    private ExternalBookingService externalBookingService;

    private UUID userId;
    private AccommodationBooking testBooking;
    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        testProperty = new Property();
        testProperty.setPropertyId("prop-001");
        testProperty.setPropertyName("Test Hotel");
        
        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("rt-001");
        testRoomType.setName("Deluxe Room");
        testRoomType.setProperty(testProperty);
        
        testRoom = new Room();
        testRoom.setRoomId("room-001");
        testRoom.setName("Room 101");
        testRoom.setRoomType(testRoomType);
        
        testBooking = new AccommodationBooking();
        testBooking.setBookingId("booking-001");
        testBooking.setCustomerId(userId);
        testBooking.setRoom(testRoom);
        testBooking.setActiveStatus(1);
        testBooking.setStatus(0);
        testBooking.setCheckInDate(LocalDateTime.now().plusDays(1));
        testBooking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        testBooking.setTotalDays(2);
        testBooking.setTotalPrice(1000000);
    }

    @Test
    void fetchBookingsByService_Accommodation_ReturnsBookings() {
        List<AccommodationBooking> bookings = Arrays.asList(testBooking);
        when(accommodationBookingRepository.findAll()).thenReturn(bookings);

        List<?> result = externalBookingService.fetchBookingsByService(ServiceSource.ACCOMMODATION, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void fetchBookingsByService_Accommodation_WithInactiveBooking_FiltersOut() {
        testBooking.setActiveStatus(0);
        List<AccommodationBooking> bookings = Arrays.asList(testBooking);
        when(accommodationBookingRepository.findAll()).thenReturn(bookings);

        List<?> result = externalBookingService.fetchBookingsByService(ServiceSource.ACCOMMODATION, userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void fetchBookingsByService_Insurance_ReturnsBookings() {
        List<InsurancePolicyDTO> policies = new ArrayList<>();
        InsurancePolicyDTO policy = new InsurancePolicyDTO();
        policy.setId("ins-001");
        policy.setUserId(userId.toString());
        policies.add(policy);

        ResponseEntity<List<InsurancePolicyDTO>> responseEntity = ResponseEntity.ok(policies);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        List<?> result = externalBookingService.fetchBookingsByService(ServiceSource.INSURANCE, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void fetchBookingsByService_Flight_ReturnsBookings() {
        List<FlightBookingDTO> bookings = new ArrayList<>();
        FlightBookingDTO booking = new FlightBookingDTO();
        booking.setId(UUID.randomUUID());
        booking.setUserId(userId);
        bookings.add(booking);

        ResponseEntity<List<FlightBookingDTO>> responseEntity = ResponseEntity.ok(bookings);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        List<?> result = externalBookingService.fetchBookingsByService(ServiceSource.FLIGHT, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void fetchBookingsByService_Rental_ReturnsBookings() {
        List<RentalVehicleDTO> vehicles = new ArrayList<>();
        RentalVehicleDTO vehicle = new RentalVehicleDTO();
        vehicle.setId("rental-001");
        vehicle.setUserId(userId.toString());
        vehicles.add(vehicle);

        ResponseEntity<List<RentalVehicleDTO>> responseEntity = ResponseEntity.ok(vehicles);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        List<?> result = externalBookingService.fetchBookingsByService(ServiceSource.RENTAL, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void fetchBookingsByService_Tour_ReturnsBookings() {
        List<TourPackageDTO> packages = new ArrayList<>();
        TourPackageDTO tourPackage = new TourPackageDTO();
        tourPackage.setId("tour-001");
        tourPackage.setUserId(userId.toString());
        packages.add(tourPackage);

        ResponseEntity<List<TourPackageDTO>> responseEntity = ResponseEntity.ok(packages);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        List<?> result = externalBookingService.fetchBookingsByService(ServiceSource.TOUR, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void fetchBookingById_Accommodation_ReturnsBooking() {
        when(accommodationBookingRepository.findById("booking-001")).thenReturn(Optional.of(testBooking));

        Object result = externalBookingService.fetchBookingById(ServiceSource.ACCOMMODATION, "booking-001");

        assertNotNull(result);
        assertTrue(result instanceof AccommodationBookingDTO);
    }

    @Test
    void fetchBookingById_Accommodation_NotFound_ReturnsNull() {
        when(accommodationBookingRepository.findById("invalid")).thenReturn(Optional.empty());

        Object result = externalBookingService.fetchBookingById(ServiceSource.ACCOMMODATION, "invalid");

        assertNull(result);
    }

    @Test
    void fetchBookingById_Insurance_ReturnsBooking() {
        List<InsurancePolicyDTO> policies = new ArrayList<>();
        InsurancePolicyDTO policy = new InsurancePolicyDTO();
        policy.setId("ins-001");
        policy.setBookingId("ins-001");
        policies.add(policy);

        ResponseEntity<List<InsurancePolicyDTO>> responseEntity = ResponseEntity.ok(policies);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Object result = externalBookingService.fetchBookingById(ServiceSource.INSURANCE, "ins-001");

        assertNotNull(result);
    }

    @Test
    void fetchBookingById_Insurance_NotFound_ReturnsNull() {
        List<InsurancePolicyDTO> policies = new ArrayList<>();

        ResponseEntity<List<InsurancePolicyDTO>> responseEntity = ResponseEntity.ok(policies);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Object result = externalBookingService.fetchBookingById(ServiceSource.INSURANCE, "not-exists");

        assertNull(result);
    }

    @Test
    void validateBookingExists_Accommodation_Exists() {
        when(accommodationBookingRepository.findById("booking-001")).thenReturn(Optional.of(testBooking));

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.ACCOMMODATION, "booking-001", userId);

        assertTrue(result);
    }

    @Test
    void validateBookingExists_Accommodation_WrongUser() {
        when(accommodationBookingRepository.findById("booking-001")).thenReturn(Optional.of(testBooking));

        UUID otherUserId = UUID.randomUUID();
        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.ACCOMMODATION, "booking-001", otherUserId);

        assertFalse(result);
    }

    @Test
    void validateBookingExists_Accommodation_NotFound() {
        when(accommodationBookingRepository.findById("invalid")).thenReturn(Optional.empty());

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.ACCOMMODATION, "invalid", userId);

        assertFalse(result);
    }

    @Test
    void validateBookingExists_NullUserId_ReturnsFalse() {
        when(accommodationBookingRepository.findById("booking-001")).thenReturn(Optional.of(testBooking));

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.ACCOMMODATION, "booking-001", null);

        assertFalse(result);
    }

    @Test
    void fetchBookingsByService_ExternalApiError_ThrowsException() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("API Error"));

        assertThrows(ExternalServiceException.class, () ->
                externalBookingService.fetchBookingsByService(ServiceSource.INSURANCE, userId));
    }

    @Test
    void fetchBookingById_ExternalApiError_ReturnsNull() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("API Error"));

        Object result = externalBookingService.fetchBookingById(ServiceSource.INSURANCE, "ins-001");

        assertNull(result);
    }

    @Test
    void fetchBookingById_Flight_ReturnsBooking() {
        List<FlightBookingDTO> bookings = new ArrayList<>();
        FlightBookingDTO booking = new FlightBookingDTO();
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);
        booking.setUserId(userId);
        bookings.add(booking);

        ResponseEntity<List<FlightBookingDTO>> responseEntity = ResponseEntity.ok(bookings);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Object result = externalBookingService.fetchBookingById(ServiceSource.FLIGHT, bookingId.toString());

        assertNotNull(result);
    }

    @Test
    void fetchBookingById_Flight_NotFound_ReturnsNull() {
        List<FlightBookingDTO> bookings = new ArrayList<>();
        FlightBookingDTO booking = new FlightBookingDTO();
        booking.setId(UUID.randomUUID());
        bookings.add(booking);

        ResponseEntity<List<FlightBookingDTO>> responseEntity = ResponseEntity.ok(bookings);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Object result = externalBookingService.fetchBookingById(ServiceSource.FLIGHT, "non-existent");

        assertNull(result);
    }

    @Test
    void fetchBookingById_Rental_ReturnsBooking() {
        List<RentalVehicleDTO> vehicles = new ArrayList<>();
        RentalVehicleDTO vehicle = new RentalVehicleDTO();
        vehicle.setId("rental-001");
        vehicle.setUserId(userId.toString());
        vehicles.add(vehicle);

        ResponseEntity<List<RentalVehicleDTO>> responseEntity = ResponseEntity.ok(vehicles);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Object result = externalBookingService.fetchBookingById(ServiceSource.RENTAL, "rental-001");

        assertNotNull(result);
    }

    @Test
    void fetchBookingById_Rental_NotFound_ReturnsNull() {
        List<RentalVehicleDTO> vehicles = new ArrayList<>();
        RentalVehicleDTO vehicle = new RentalVehicleDTO();
        vehicle.setId("other-id");
        vehicles.add(vehicle);

        ResponseEntity<List<RentalVehicleDTO>> responseEntity = ResponseEntity.ok(vehicles);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Object result = externalBookingService.fetchBookingById(ServiceSource.RENTAL, "rental-001");

        assertNull(result);
    }

    @Test
    void fetchBookingById_Tour_ReturnsBooking() {
        List<TourPackageDTO> packages = new ArrayList<>();
        TourPackageDTO tourPackage = new TourPackageDTO();
        tourPackage.setId("tour-001");
        tourPackage.setUserId(userId.toString());
        packages.add(tourPackage);

        ResponseEntity<List<TourPackageDTO>> responseEntity = ResponseEntity.ok(packages);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Object result = externalBookingService.fetchBookingById(ServiceSource.TOUR, "tour-001");

        assertNotNull(result);
    }

    @Test
    void fetchBookingById_Tour_NotFound_ReturnsNull() {
        List<TourPackageDTO> packages = new ArrayList<>();
        TourPackageDTO tourPackage = new TourPackageDTO();
        tourPackage.setId("other-id");
        packages.add(tourPackage);

        ResponseEntity<List<TourPackageDTO>> responseEntity = ResponseEntity.ok(packages);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Object result = externalBookingService.fetchBookingById(ServiceSource.TOUR, "tour-001");

        assertNull(result);
    }

    @Test
    void fetchBookingById_NullResponse_ReturnsNull() {
        ResponseEntity<List<InsurancePolicyDTO>> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Object result = externalBookingService.fetchBookingById(ServiceSource.INSURANCE, "ins-001");

        assertNull(result);
    }

    @Test
    void validateBookingExists_Insurance_Valid() {
        List<InsurancePolicyDTO> policies = new ArrayList<>();
        InsurancePolicyDTO policy = new InsurancePolicyDTO();
        policy.setId("ins-001");
        policy.setBookingId("ins-001");
        policy.setUserId(userId.toString());
        policies.add(policy);

        ResponseEntity<List<InsurancePolicyDTO>> responseEntity = ResponseEntity.ok(policies);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.INSURANCE, "ins-001", userId);

        assertTrue(result);
    }

    @Test
    void validateBookingExists_Insurance_WrongUser() {
        List<InsurancePolicyDTO> policies = new ArrayList<>();
        InsurancePolicyDTO policy = new InsurancePolicyDTO();
        policy.setId("ins-001");
        policy.setBookingId("ins-001");
        policy.setUserId("different-user-id");
        policies.add(policy);

        ResponseEntity<List<InsurancePolicyDTO>> responseEntity = ResponseEntity.ok(policies);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.INSURANCE, "ins-001", userId);

        assertFalse(result);
    }

    @Test
    void validateBookingExists_Flight_Valid() {
        List<FlightBookingDTO> bookings = new ArrayList<>();
        FlightBookingDTO booking = new FlightBookingDTO();
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);
        booking.setUserId(userId);
        bookings.add(booking);

        ResponseEntity<List<FlightBookingDTO>> responseEntity = ResponseEntity.ok(bookings);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.FLIGHT, bookingId.toString(), userId);

        assertTrue(result);
    }

    @Test
    void validateBookingExists_Flight_WrongUser() {
        List<FlightBookingDTO> bookings = new ArrayList<>();
        FlightBookingDTO booking = new FlightBookingDTO();
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);
        booking.setUserId(UUID.randomUUID()); // different user
        bookings.add(booking);

        ResponseEntity<List<FlightBookingDTO>> responseEntity = ResponseEntity.ok(bookings);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.FLIGHT, bookingId.toString(), userId);

        assertFalse(result);
    }

    @Test
    void validateBookingExists_Rental_Valid() {
        List<RentalVehicleDTO> vehicles = new ArrayList<>();
        RentalVehicleDTO vehicle = new RentalVehicleDTO();
        vehicle.setId("rental-001");
        vehicle.setUserId(userId.toString());
        vehicles.add(vehicle);

        ResponseEntity<List<RentalVehicleDTO>> responseEntity = ResponseEntity.ok(vehicles);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.RENTAL, "rental-001", userId);

        assertTrue(result);
    }

    @Test
    void validateBookingExists_Rental_WrongUser() {
        List<RentalVehicleDTO> vehicles = new ArrayList<>();
        RentalVehicleDTO vehicle = new RentalVehicleDTO();
        vehicle.setId("rental-001");
        vehicle.setUserId("different-user");
        vehicles.add(vehicle);

        ResponseEntity<List<RentalVehicleDTO>> responseEntity = ResponseEntity.ok(vehicles);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.RENTAL, "rental-001", userId);

        assertFalse(result);
    }

    @Test
    void validateBookingExists_Tour_Valid() {
        List<TourPackageDTO> packages = new ArrayList<>();
        TourPackageDTO tourPackage = new TourPackageDTO();
        tourPackage.setId("tour-001");
        tourPackage.setUserId(userId.toString());
        packages.add(tourPackage);

        ResponseEntity<List<TourPackageDTO>> responseEntity = ResponseEntity.ok(packages);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.TOUR, "tour-001", userId);

        assertTrue(result);
    }

    @Test
    void validateBookingExists_Tour_WrongUser() {
        List<TourPackageDTO> packages = new ArrayList<>();
        TourPackageDTO tourPackage = new TourPackageDTO();
        tourPackage.setId("tour-001");
        tourPackage.setUserId("different-user");
        packages.add(tourPackage);

        ResponseEntity<List<TourPackageDTO>> responseEntity = ResponseEntity.ok(packages);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.TOUR, "tour-001", userId);

        assertFalse(result);
    }

    @Test
    void validateBookingExists_Exception_ReturnsFalse() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("API Error"));

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.INSURANCE, "ins-001", userId);

        assertFalse(result);
    }

    @Test
    void validateBookingExists_NullBooking_ReturnsFalse() {
        ResponseEntity<List<InsurancePolicyDTO>> responseEntity = ResponseEntity.ok(new ArrayList<>());
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.INSURANCE, "not-exists", userId);

        assertFalse(result);
    }

    @Test
    void validateBookingOwnership_NullUserId_ReturnsFalse() {
        when(accommodationBookingRepository.findById("booking-001")).thenReturn(Optional.of(testBooking));

        Object booking = externalBookingService.fetchBookingById(ServiceSource.ACCOMMODATION, "booking-001");
        
        // Validate with null userId should fail
        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.ACCOMMODATION, "booking-001", null);

        assertFalse(result);
    }

    @Test
    void validateBookingOwnership_InsuranceNullUserId_ReturnsFalse() {
        List<InsurancePolicyDTO> policies = new ArrayList<>();
        InsurancePolicyDTO policy = new InsurancePolicyDTO();
        policy.setId("ins-001");
        policy.setBookingId("ins-001");
        policy.setUserId(null); // null userId in policy
        policies.add(policy);

        ResponseEntity<List<InsurancePolicyDTO>> responseEntity = ResponseEntity.ok(policies);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.INSURANCE, "ins-001", userId);

        assertFalse(result);
    }

    @Test
    void validateBookingOwnership_FlightNullUserId_ReturnsFalse() {
        List<FlightBookingDTO> bookings = new ArrayList<>();
        FlightBookingDTO booking = new FlightBookingDTO();
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);
        booking.setUserId(null); // null userId
        bookings.add(booking);

        ResponseEntity<List<FlightBookingDTO>> responseEntity = ResponseEntity.ok(bookings);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.FLIGHT, bookingId.toString(), userId);

        assertFalse(result);
    }

    @Test
    void validateBookingOwnership_RentalNullUserId_ReturnsFalse() {
        List<RentalVehicleDTO> vehicles = new ArrayList<>();
        RentalVehicleDTO vehicle = new RentalVehicleDTO();
        vehicle.setId("rental-001");
        vehicle.setUserId(null); // null userId
        vehicles.add(vehicle);

        ResponseEntity<List<RentalVehicleDTO>> responseEntity = ResponseEntity.ok(vehicles);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.RENTAL, "rental-001", userId);

        assertFalse(result);
    }

    @Test
    void validateBookingOwnership_TourNullUserId_ReturnsFalse() {
        List<TourPackageDTO> packages = new ArrayList<>();
        TourPackageDTO tourPackage = new TourPackageDTO();
        tourPackage.setId("tour-001");
        tourPackage.setUserId(null); // null userId
        packages.add(tourPackage);

        ResponseEntity<List<TourPackageDTO>> responseEntity = ResponseEntity.ok(packages);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.TOUR, "tour-001", userId);

        assertFalse(result);
    }

    @Test
    void fetchBookingById_Flight_WithException_ReturnsNull() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("API Error"));

        Object result = externalBookingService.fetchBookingById(ServiceSource.FLIGHT, "flight-001");

        assertNull(result);
    }

    @Test
    void fetchBookingById_Rental_WithException_ReturnsNull() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("API Error"));

        Object result = externalBookingService.fetchBookingById(ServiceSource.RENTAL, "rental-001");

        assertNull(result);
    }

    @Test
    void fetchBookingById_Tour_WithException_ReturnsNull() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("API Error"));

        Object result = externalBookingService.fetchBookingById(ServiceSource.TOUR, "tour-001");

        assertNull(result);
    }

    @Test
    void convertToAccommodationDTO_WithNullRoom_HandlesGracefully() {
        AccommodationBooking bookingWithoutRoom = new AccommodationBooking();
        bookingWithoutRoom.setBookingId("booking-no-room");
        bookingWithoutRoom.setCustomerId(userId);
        bookingWithoutRoom.setActiveStatus(1);
        bookingWithoutRoom.setRoom(null);

        when(accommodationBookingRepository.findById("booking-no-room")).thenReturn(Optional.of(bookingWithoutRoom));

        Object result = externalBookingService.fetchBookingById(ServiceSource.ACCOMMODATION, "booking-no-room");

        assertNotNull(result);
        assertTrue(result instanceof AccommodationBookingDTO);
    }

    @Test
    void convertToAccommodationDTO_WithNullRoomType_HandlesGracefully() {
        Room roomWithoutType = new Room();
        roomWithoutType.setRoomId("room-no-type");
        roomWithoutType.setName("Room Without Type");
        roomWithoutType.setRoomType(null);

        AccommodationBooking bookingWithRoomNoType = new AccommodationBooking();
        bookingWithRoomNoType.setBookingId("booking-room-no-type");
        bookingWithRoomNoType.setCustomerId(userId);
        bookingWithRoomNoType.setActiveStatus(1);
        bookingWithRoomNoType.setRoom(roomWithoutType);

        when(accommodationBookingRepository.findById("booking-room-no-type")).thenReturn(Optional.of(bookingWithRoomNoType));

        Object result = externalBookingService.fetchBookingById(ServiceSource.ACCOMMODATION, "booking-room-no-type");

        assertNotNull(result);
        assertTrue(result instanceof AccommodationBookingDTO);
    }

    @Test
    void convertToAccommodationDTO_WithNullProperty_HandlesGracefully() {
        RoomType roomTypeWithoutProperty = new RoomType();
        roomTypeWithoutProperty.setRoomTypeId("rt-no-prop");
        roomTypeWithoutProperty.setName("Room Type No Property");
        roomTypeWithoutProperty.setProperty(null);

        Room roomWithTypeNoProperty = new Room();
        roomWithTypeNoProperty.setRoomId("room-no-prop");
        roomWithTypeNoProperty.setName("Room No Property");
        roomWithTypeNoProperty.setRoomType(roomTypeWithoutProperty);

        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("booking-no-prop");
        booking.setCustomerId(userId);
        booking.setActiveStatus(1);
        booking.setRoom(roomWithTypeNoProperty);

        when(accommodationBookingRepository.findById("booking-no-prop")).thenReturn(Optional.of(booking));

        Object result = externalBookingService.fetchBookingById(ServiceSource.ACCOMMODATION, "booking-no-prop");

        assertNotNull(result);
        assertTrue(result instanceof AccommodationBookingDTO);
    }

    @Test
    void fetchBookingById_Accommodation_WithInactiveBooking_ReturnsNull() {
        AccommodationBooking inactiveBooking = new AccommodationBooking();
        inactiveBooking.setBookingId("inactive-booking");
        inactiveBooking.setActiveStatus(0); // inactive

        when(accommodationBookingRepository.findById("inactive-booking")).thenReturn(Optional.of(inactiveBooking));

        Object result = externalBookingService.fetchBookingById(ServiceSource.ACCOMMODATION, "inactive-booking");

        assertNull(result);
    }

    @Test
    void fetchBookingById_Accommodation_WithException_ReturnsNull() {
        when(accommodationBookingRepository.findById(anyString())).thenThrow(new RuntimeException("DB Error"));

        Object result = externalBookingService.fetchBookingById(ServiceSource.ACCOMMODATION, "booking-001");

        assertNull(result);
    }

    @Test
    void validateBookingExists_AccommodationNullCustomerId_ReturnsFalse() {
        AccommodationBooking bookingNullCustomer = new AccommodationBooking();
        bookingNullCustomer.setBookingId("booking-null-customer");
        bookingNullCustomer.setCustomerId(null);
        bookingNullCustomer.setActiveStatus(1);
        bookingNullCustomer.setRoom(testRoom);

        when(accommodationBookingRepository.findById("booking-null-customer")).thenReturn(Optional.of(bookingNullCustomer));

        boolean result = externalBookingService.validateBookingExists(
                ServiceSource.ACCOMMODATION, "booking-null-customer", userId);

        assertFalse(result);
    }

    @Test
    void fetchBookingsByService_Accommodation_WithException_ThrowsExternalServiceException() {
        when(accommodationBookingRepository.findAll()).thenThrow(new RuntimeException("DB Error"));

        assertThrows(ExternalServiceException.class, () ->
                externalBookingService.fetchBookingsByService(ServiceSource.ACCOMMODATION, userId));
    }
}
