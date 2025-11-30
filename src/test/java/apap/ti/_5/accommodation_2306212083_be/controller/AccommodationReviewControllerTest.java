package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.CreateReviewRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.ReviewResponseDTO;
import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.service.AccommodationReviewService;
import apap.ti._5.accommodation_2306212083_be.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationReviewControllerTest {

    @Mock
    private AccommodationReviewService reviewService;

    @InjectMocks
    private AccommodationReviewController reviewController;

    private UserPrincipal customerUser;
    private UserPrincipal ownerUser;
    private UserPrincipal superadminUser;
    private ReviewResponseDTO testReview;

    @BeforeEach
    void setUp() {
        customerUser = UserPrincipal.builder()
                .userId("11111111-1111-1111-1111-111111111111")
                .username("customer")
                .role("Customer")
                .build();

        ownerUser = UserPrincipal.builder()
                .userId("22222222-2222-2222-2222-222222222222")
                .username("owner")
                .role("Accommodation Owner")
                .build();

        superadminUser = UserPrincipal.builder()
                .userId("33333333-3333-3333-3333-333333333333")
                .username("admin")
                .role("Superadmin")
                .build();

        testReview = ReviewResponseDTO.builder()
                .reviewId(UUID.randomUUID())
                .overallRating(5.0)
                .cleanlinessRating(5)
                .facilityRating(5)
                .serviceRating(5)
                .valueRating(5)
                .comment("Great stay!")
                .build();
    }

    @Test
    void getAllReviews_Success() {
        List<ReviewResponseDTO> reviews = Arrays.asList(testReview);
        when(reviewService.getAllReviews()).thenReturn(reviews);

        ResponseEntity<List<ReviewResponseDTO>> response = reviewController.getAllReviews();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getReviewsByProperty_Success() {
        List<ReviewResponseDTO> reviews = Arrays.asList(testReview);
        when(reviewService.getReviewsByProperty("prop-001")).thenReturn(reviews);

        ResponseEntity<List<ReviewResponseDTO>> response = reviewController.getReviewsByProperty("prop-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getMyReviews_AsCustomer_ReturnsOwnReviews() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(true);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<ReviewResponseDTO> reviews = Arrays.asList(testReview);
            when(reviewService.getReviewsByCustomer(any(UUID.class))).thenReturn(reviews);

            ResponseEntity<List<ReviewResponseDTO>> response = reviewController.getMyReviews();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(reviewService).getReviewsByCustomer(any(UUID.class));
        }
    }

    @Test
    void getMyReviews_AsOwner_ReturnsPropertyReviews() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(true);

            List<ReviewResponseDTO> reviews = Arrays.asList(testReview);
            when(reviewService.getReviewsForOwnerProperties(any(UUID.class))).thenReturn(reviews);

            ResponseEntity<List<ReviewResponseDTO>> response = reviewController.getMyReviews();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(reviewService).getReviewsForOwnerProperties(any(UUID.class));
        }
    }

    @Test
    void getMyReviews_AsSuperadmin_ReturnsAllReviews() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<ReviewResponseDTO> reviews = Arrays.asList(testReview);
            when(reviewService.getAllReviews()).thenReturn(reviews);

            ResponseEntity<List<ReviewResponseDTO>> response = reviewController.getMyReviews();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(reviewService).getAllReviews();
        }
    }

    @Test
    void getReviewsByCustomer_Success() {
        UUID customerId = UUID.randomUUID();
        List<ReviewResponseDTO> reviews = Arrays.asList(testReview);
        when(reviewService.getReviewsByCustomer(customerId)).thenReturn(reviews);

        ResponseEntity<List<ReviewResponseDTO>> response = reviewController.getReviewsByCustomer(customerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getReviewDetail_Success() {
        UUID reviewId = UUID.randomUUID();
        when(reviewService.getReviewDetail(reviewId)).thenReturn(testReview);

        ResponseEntity<ReviewResponseDTO> response = reviewController.getReviewDetail(reviewId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void createReview_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);

            CreateReviewRequestDTO request = new CreateReviewRequestDTO();
            request.setBookingId("booking-001");
            request.setCleanlinessRating(5);
            request.setFacilityRating(5);
            request.setServiceRating(5);
            request.setValueRating(5);
            request.setComment("Great stay!");

            when(reviewService.createReview(any(CreateReviewRequestDTO.class), any(UUID.class)))
                    .thenReturn(testReview);

            ResponseEntity<ReviewResponseDTO> response = reviewController.createReview(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Test
    void deleteReview_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);

            UUID reviewId = UUID.randomUUID();
            doNothing().when(reviewService).deleteReview(any(UUID.class), any(UUID.class));

            ResponseEntity<Map<String, String>> response = reviewController.deleteReview(reviewId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Review deleted successfully", response.getBody().get("message"));
        }
    }

    @Test
    void handleException_ReturnsBadRequest() {
        Exception ex = new RuntimeException("Test error");

        ResponseEntity<Map<String, String>> response = reviewController.handleException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Test error", response.getBody().get("error"));
    }
}
