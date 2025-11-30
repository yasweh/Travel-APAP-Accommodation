package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.dto.support.*;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import apap.ti._5.accommodation_2306212083_be.service.SupportTicketService;
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
class SupportTicketControllerTest {

    @Mock
    private SupportTicketService supportTicketService;

    @InjectMocks
    private SupportTicketController supportTicketController;

    private UserPrincipal customerUser;
    private UserPrincipal ownerUser;
    private UserPrincipal superadminUser;
    private TicketResponseDTO testTicketResponse;
    private TicketDetailResponseDTO testTicketDetail;

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

        testTicketResponse = TicketResponseDTO.builder()
                .id(UUID.randomUUID())
                .subject("Test Ticket")
                .status(TicketStatus.OPEN)
                .build();

        testTicketDetail = TicketDetailResponseDTO.builder()
                .id(UUID.randomUUID())
                .subject("Test Ticket")
                .status(TicketStatus.OPEN)
                .build();
    }

    @Test
    void getAllTickets_AsCustomer_ReturnsOwnTickets() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(true);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<TicketResponseDTO> tickets = Arrays.asList(testTicketResponse);
            when(supportTicketService.getAllTickets(any(UUID.class), isNull(), isNull()))
                    .thenReturn(tickets);

            ResponseEntity<List<TicketResponseDTO>> response = supportTicketController.getAllTickets(null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
        }
    }

    @Test
    void getAllTickets_AsOwner_ReturnsOwnerPropertyTickets() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(true);

            List<TicketResponseDTO> tickets = Arrays.asList(testTicketResponse);
            when(supportTicketService.getAllTicketsForOwner(any(UUID.class), isNull(), isNull()))
                    .thenReturn(tickets);

            ResponseEntity<List<TicketResponseDTO>> response = supportTicketController.getAllTickets(null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    void getAllTickets_AsSuperadmin_ReturnsAllTickets() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<TicketResponseDTO> tickets = Arrays.asList(testTicketResponse);
            when(supportTicketService.getAllTickets(isNull(), isNull(), isNull()))
                    .thenReturn(tickets);

            ResponseEntity<List<TicketResponseDTO>> response = supportTicketController.getAllTickets(null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    void getAllTickets_WithFilters_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);
            securityUtil.when(SecurityUtil::isCustomer).thenReturn(false);
            securityUtil.when(SecurityUtil::isAccommodationOwner).thenReturn(false);

            List<TicketResponseDTO> tickets = Arrays.asList(testTicketResponse);
            when(supportTicketService.getAllTickets(isNull(), eq(TicketStatus.OPEN), eq(ServiceSource.ACCOMMODATION)))
                    .thenReturn(tickets);

            ResponseEntity<List<TicketResponseDTO>> response = supportTicketController.getAllTickets(
                    TicketStatus.OPEN, ServiceSource.ACCOMMODATION);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    void getTicketDetail_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);

            UUID ticketId = UUID.randomUUID();
            when(supportTicketService.getTicketDetailWithAccess(any(UUID.class), any(UUID.class), anyString()))
                    .thenReturn(testTicketDetail);

            ResponseEntity<TicketDetailResponseDTO> response = supportTicketController.getTicketDetail(ticketId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Test
    void createTicket_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);

            CreateTicketRequestDTO request = new CreateTicketRequestDTO();
            request.setSubject("Test Ticket");
            request.setInitialMessage("Test Description");
            request.setServiceSource(ServiceSource.ACCOMMODATION);
            request.setExternalBookingId("booking-001");
            request.setUserId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

            when(supportTicketService.createTicket(any(CreateTicketRequestDTO.class)))
                    .thenReturn(testTicketResponse);

            ResponseEntity<TicketResponseDTO> response = supportTicketController.createTicket(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Test
    void updateTicketStatus_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);

            UUID ticketId = UUID.randomUUID();
            UpdateStatusRequestDTO request = new UpdateStatusRequestDTO();
            request.setStatus(TicketStatus.IN_PROGRESS);

            when(supportTicketService.updateTicketStatusWithPermissionCheck(
                    any(UUID.class), any(UpdateStatusRequestDTO.class), any(UUID.class), anyString()))
                    .thenReturn(testTicketResponse);

            ResponseEntity<TicketResponseDTO> response = supportTicketController.updateTicketStatus(ticketId, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    void deleteTicket_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(customerUser);

            UUID ticketId = UUID.randomUUID();
            doNothing().when(supportTicketService).deleteTicket(any(UUID.class), any(UUID.class));

            ResponseEntity<Map<String, String>> response = supportTicketController.deleteTicket(ticketId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Ticket deleted successfully", response.getBody().get("message"));
        }
    }

    @Test
    void addProgress_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(superadminUser);

            UUID ticketId = UUID.randomUUID();
            AddProgressRequestDTO request = new AddProgressRequestDTO();
            request.setDescription("Progress added");

            ProgressResponseDTO progressResponse = ProgressResponseDTO.builder()
                    .id(UUID.randomUUID())
                    .description("Progress added")
                    .build();

            when(supportTicketService.addProgressWithPermissionCheck(
                    any(UUID.class), any(AddProgressRequestDTO.class), any(UUID.class), anyString()))
                    .thenReturn(progressResponse);

            ResponseEntity<ProgressResponseDTO> response = supportTicketController.addProgress(ticketId, request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }
    }

    @Test
    void deleteProgress_Success() {
        UUID ticketId = UUID.randomUUID();
        UUID progressId = UUID.randomUUID();
        doNothing().when(supportTicketService).deleteProgress(any(UUID.class), any(UUID.class));

        ResponseEntity<Map<String, String>> response = supportTicketController.deleteProgress(ticketId, progressId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Progress entry deleted successfully", response.getBody().get("message"));
    }

    @Test
    void addMessage_Success() {
        UUID ticketId = UUID.randomUUID();
        AddMessageRequestDTO request = new AddMessageRequestDTO();
        request.setMessage("Test message");

        MessageResponseDTO messageResponse = MessageResponseDTO.builder()
                .id(UUID.randomUUID())
                .message("Test message")
                .build();

        when(supportTicketService.addMessage(any(UUID.class), any(AddMessageRequestDTO.class)))
                .thenReturn(messageResponse);

        ResponseEntity<MessageResponseDTO> response = supportTicketController.addMessage(ticketId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void getTicketMessages_Success() {
        UUID ticketId = UUID.randomUUID();
        List<MessageResponseDTO> messages = Arrays.asList(
                MessageResponseDTO.builder().id(UUID.randomUUID()).message("Test").build()
        );

        when(supportTicketService.getTicketMessages(any(UUID.class))).thenReturn(messages);

        ResponseEntity<List<MessageResponseDTO>> response = supportTicketController.getTicketMessages(ticketId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void markMessagesAsRead_Success() {
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        doNothing().when(supportTicketService).markMessagesAsRead(any(UUID.class), any(UUID.class));

        ResponseEntity<Map<String, String>> response = supportTicketController.markMessagesAsRead(ticketId, userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Messages marked as read", response.getBody().get("message"));
    }

    @Test
    void getAvailableBookings_Success() {
        UUID userId = UUID.randomUUID();
        List<?> bookings = Arrays.asList("booking-1");

        doReturn(bookings).when(supportTicketService).getAvailableBookings(any(ServiceSource.class), any(UUID.class));

        ResponseEntity<List<?>> response = supportTicketController.getAvailableBookings(
                ServiceSource.ACCOMMODATION, userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getDashboard_Success() {
        UUID userId = UUID.randomUUID();
        SupportDashboardResponseDTO dashboard = new SupportDashboardResponseDTO();

        when(supportTicketService.getDashboardData(any(UUID.class))).thenReturn(dashboard);

        ResponseEntity<SupportDashboardResponseDTO> response = supportTicketController.getDashboard(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleException_ReturnsBadRequest() {
        Exception ex = new RuntimeException("Test error");

        ResponseEntity<Map<String, String>> response = supportTicketController.handleException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Test error", response.getBody().get("error"));
    }
}
