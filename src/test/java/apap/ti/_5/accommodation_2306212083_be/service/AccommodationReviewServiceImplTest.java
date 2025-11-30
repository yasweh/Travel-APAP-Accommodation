package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.CreateReviewRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.ReviewResponseDTO;
import apap.ti._5.accommodation_2306212083_be.model.*;
import apap.ti._5.accommodation_2306212083_be.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationReviewServiceImplTest {

    @Mock
    private AccommodationReviewRepository reviewRepository;

    @Mock
    private AccommodationBookingRepository bookingRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private AccommodationReviewServiceImpl reviewService;

    private Property testProperty;
    private RoomType testRoomType;
    private Room testRoom;
    private AccommodationBooking testBooking;
    private AccommodationReview testReview;
    private UUID customerId;
    private UUID reviewId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        testProperty = new Property();
        testProperty.setPropertyId("PROP001");
        testProperty.setPropertyName("Test Hotel");
        testProperty.setOwnerId(UUID.randomUUID());
        testProperty.setActiveStatus(1);

        testRoomType = new RoomType();
        testRoomType.setRoomTypeId("RT001");
        testRoomType.setName("Suite");
        testRoomType.setProperty(testProperty);

        testRoom = new Room();
        testRoom.setRoomId("ROOM001");
        testRoom.setName("Room 101");
        testRoom.setRoomType(testRoomType);

        testBooking = AccommodationBooking.builder()
                .bookingId("BOOK001")
                .room(testRoom)
                .checkInDate(LocalDateTime.now().minusDays(5))
                .checkOutDate(LocalDateTime.now().minusDays(2))
                .customerId(customerId)
                .customerName("John Doe")
                .status(1) // Confirmed
                .build();

        testReview = AccommodationReview.builder()
                .reviewId(reviewId)
                .bookingId("BOOK001")
                .propertyId("PROP001")
                .customerId(customerId)
                .cleanlinessRating(5)
                .facilityRating(4)
                .serviceRating(5)
                .valueRating(4)
                .comment("Great hotel!")
                .activeStatus(1)
                .createdDate(LocalDateTime.now())
                .property(testProperty)
                .booking(testBooking)
                .build();
    }

    // ========== GET ALL REVIEWS TESTS ==========

    @Test
    void getAllReviews_Success() {
        when(reviewRepository.findAllActiveReviews()).thenReturn(Arrays.asList(testReview));

        List<ReviewResponseDTO> result = reviewService.getAllReviews();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("PROP001", result.get(0).getPropertyId());
    }

    @Test
    void getAllReviews_Empty() {
        when(reviewRepository.findAllActiveReviews()).thenReturn(Collections.emptyList());

        List<ReviewResponseDTO> result = reviewService.getAllReviews();

        assertTrue(result.isEmpty());
    }

    // ========== GET REVIEWS BY PROPERTY TESTS ==========

    @Test
    void getReviewsByProperty_Success() {
        when(reviewRepository.findByPropertyId("PROP001")).thenReturn(Arrays.asList(testReview));

        List<ReviewResponseDTO> result = reviewService.getReviewsByProperty("PROP001");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ========== GET REVIEWS BY CUSTOMER TESTS ==========

    @Test
    void getReviewsByCustomer_Success() {
        when(reviewRepository.findByCustomerId(customerId)).thenReturn(Arrays.asList(testReview));

        List<ReviewResponseDTO> result = reviewService.getReviewsByCustomer(customerId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ========== GET REVIEWS FOR OWNER PROPERTIES TESTS ==========

    @Test
    void getReviewsForOwnerProperties_Success() {
        UUID ownerId = testProperty.getOwnerId();
        when(propertyRepository.findByOwnerIdAndActiveStatus(ownerId, 1))
                .thenReturn(Arrays.asList(testProperty));
        when(reviewRepository.findAllActiveReviews()).thenReturn(Arrays.asList(testReview));

        List<ReviewResponseDTO> result = reviewService.getReviewsForOwnerProperties(ownerId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getReviewsForOwnerProperties_NoProperties() {
        UUID ownerId = UUID.randomUUID();
        when(propertyRepository.findByOwnerIdAndActiveStatus(ownerId, 1))
                .thenReturn(Collections.emptyList());
        when(reviewRepository.findAllActiveReviews()).thenReturn(Arrays.asList(testReview));

        List<ReviewResponseDTO> result = reviewService.getReviewsForOwnerProperties(ownerId);

        assertTrue(result.isEmpty());
    }

    // ========== GET REVIEW DETAIL TESTS ==========

    @Test
    void getReviewDetail_Success() {
        when(reviewRepository.findByIdAndActiveStatus(reviewId)).thenReturn(Optional.of(testReview));

        ReviewResponseDTO result = reviewService.getReviewDetail(reviewId);

        assertNotNull(result);
        assertEquals(reviewId, result.getReviewId());
    }

    @Test
    void getReviewDetail_NotFound() {
        when(reviewRepository.findByIdAndActiveStatus(reviewId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
                reviewService.getReviewDetail(reviewId));
    }

    // ========== CREATE REVIEW TESTS ==========

    @Test
    void createReview_Success() {
        CreateReviewRequestDTO request = new CreateReviewRequestDTO();
        request.setBookingId("BOOK001");
        request.setCleanlinessRating(5);
        request.setFacilityRating(4);
        request.setServiceRating(5);
        request.setValueRating(4);
        request.setComment("Great hotel!");

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(reviewRepository.existsByBookingId("BOOK001")).thenReturn(false);
        when(reviewRepository.save(any(AccommodationReview.class))).thenReturn(testReview);

        ReviewResponseDTO result = reviewService.createReview(request, customerId);

        assertNotNull(result);
        verify(reviewRepository).save(any(AccommodationReview.class));
    }

    @Test
    void createReview_BookingNotFound() {
        CreateReviewRequestDTO request = new CreateReviewRequestDTO();
        request.setBookingId("INVALID");

        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
                reviewService.createReview(request, customerId));
    }

    @Test
    void createReview_BookingNotConfirmed() {
        testBooking.setStatus(0); // Not confirmed
        CreateReviewRequestDTO request = new CreateReviewRequestDTO();
        request.setBookingId("BOOK001");

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(IllegalArgumentException.class, () -> 
                reviewService.createReview(request, customerId));
    }

    @Test
    void createReview_BeforeCheckout() {
        testBooking.setCheckOutDate(LocalDateTime.now().plusDays(2)); // Future checkout
        CreateReviewRequestDTO request = new CreateReviewRequestDTO();
        request.setBookingId("BOOK001");

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));

        assertThrows(IllegalArgumentException.class, () -> 
                reviewService.createReview(request, customerId));
    }

    @Test
    void createReview_AlreadyExists() {
        CreateReviewRequestDTO request = new CreateReviewRequestDTO();
        request.setBookingId("BOOK001");

        when(bookingRepository.findById("BOOK001")).thenReturn(Optional.of(testBooking));
        when(reviewRepository.existsByBookingId("BOOK001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
                reviewService.createReview(request, customerId));
    }

    // ========== DELETE REVIEW TESTS ==========

    @Test
    void deleteReview_Success() {
        when(reviewRepository.findByIdAndActiveStatus(reviewId)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(AccommodationReview.class))).thenReturn(testReview);

        reviewService.deleteReview(reviewId, customerId);

        verify(reviewRepository).save(any(AccommodationReview.class));
    }

    @Test
    void deleteReview_NotFound() {
        when(reviewRepository.findByIdAndActiveStatus(reviewId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
                reviewService.deleteReview(reviewId, customerId));
    }

    // ========== MAP TO DTO TESTS ==========

    @Test
    void mapToResponseDTO_WithNullProperty() {
        testReview.setProperty(null);
        when(reviewRepository.findAllActiveReviews()).thenReturn(Arrays.asList(testReview));

        List<ReviewResponseDTO> result = reviewService.getAllReviews();

        assertNull(result.get(0).getPropertyName());
    }

    @Test
    void mapToResponseDTO_WithNullBooking() {
        testReview.setBooking(null);
        when(reviewRepository.findAllActiveReviews()).thenReturn(Arrays.asList(testReview));

        List<ReviewResponseDTO> result = reviewService.getAllReviews();

        assertNull(result.get(0).getCustomerName());
    }
}
