package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.support.*;
import apap.ti._5.accommodation_2306212083_be.enums.ActionType;
import apap.ti._5.accommodation_2306212083_be.enums.SenderType;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import apap.ti._5.accommodation_2306212083_be.exception.InvalidBookingException;
import apap.ti._5.accommodation_2306212083_be.exception.TicketNotFoundException;
import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.SupportProgress;
import apap.ti._5.accommodation_2306212083_be.model.SupportTicket;
import apap.ti._5.accommodation_2306212083_be.model.TicketMessage;
import apap.ti._5.accommodation_2306212083_be.repository.SupportProgressRepository;
import apap.ti._5.accommodation_2306212083_be.repository.SupportTicketRepository;
import apap.ti._5.accommodation_2306212083_be.repository.TicketMessageRepository;
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
class SupportTicketServiceTest {

    @Mock
    private SupportTicketRepository ticketRepository;

    @Mock
    private TicketMessageRepository messageRepository;

    @Mock
    private SupportProgressRepository progressRepository;

    @Mock
    private ExternalBookingService externalBookingService;

    @Mock
    private PropertyService propertyService;

    @InjectMocks
    private SupportTicketService supportTicketService;

    private UUID userId;
    private UUID ticketId;
    private SupportTicket testTicket;
    private TicketMessage testMessage;
    private SupportProgress testProgress;
    private Property testProperty;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        ticketId = UUID.randomUUID();

        testProperty = new Property();
        testProperty.setPropertyId("prop-001");
        testProperty.setPropertyName("Test Hotel");
        testProperty.setOwnerId(userId);

        testTicket = new SupportTicket();
        testTicket.setId(ticketId);
        testTicket.setUserId(userId);
        testTicket.setSubject("Test Ticket");
        testTicket.setStatus(TicketStatus.OPEN);
        testTicket.setServiceSource(ServiceSource.ACCOMMODATION);
        testTicket.setExternalBookingId("booking-001");
        testTicket.setPropertyId("prop-001");
        testTicket.setInitialMessage("Test message");
        testTicket.setDeleted(false);
        testTicket.setCreatedAt(LocalDateTime.now());
        testTicket.setUpdatedAt(LocalDateTime.now());

        testMessage = new TicketMessage();
        testMessage.setId(UUID.randomUUID());
        testMessage.setTicket(testTicket);
        testMessage.setSenderType(SenderType.CUSTOMER);
        testMessage.setSenderId(userId);
        testMessage.setMessage("Test message");
        testMessage.setReadByRecipient(false);
        testMessage.setDeleted(false);
        testMessage.setSentAt(LocalDateTime.now());

        testProgress = new SupportProgress();
        testProgress.setId(UUID.randomUUID());
        testProgress.setTicket(testTicket);
        testProgress.setActionType(ActionType.CREATED);
        testProgress.setDescription("Test progress");
        testProgress.setPerformedBy(userId);
        testProgress.setDeleted(false);
        testProgress.setPerformedAt(LocalDateTime.now());
    }

    @Test
    void getAllTickets_ReturnsTickets() {
        List<SupportTicket> tickets = Arrays.asList(testTicket);
        when(ticketRepository.findByFilters(eq(userId), isNull(), isNull())).thenReturn(tickets);
        when(messageRepository.countByTicketIdAndReadByRecipientFalseAndDeletedFalse(any(UUID.class)))
                .thenReturn(0L);

        List<TicketResponseDTO> result = supportTicketService.getAllTickets(userId, null, null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Ticket", result.get(0).getSubject());
    }

    @Test
    void getAllTickets_WithFilters_ReturnsFilteredTickets() {
        List<SupportTicket> tickets = Arrays.asList(testTicket);
        when(ticketRepository.findByFilters(eq(userId), eq(TicketStatus.OPEN), eq(ServiceSource.ACCOMMODATION)))
                .thenReturn(tickets);
        when(messageRepository.countByTicketIdAndReadByRecipientFalseAndDeletedFalse(any(UUID.class)))
                .thenReturn(0L);

        List<TicketResponseDTO> result = supportTicketService.getAllTickets(
                userId, TicketStatus.OPEN, ServiceSource.ACCOMMODATION);

        assertFalse(result.isEmpty());
        assertEquals(TicketStatus.OPEN, result.get(0).getStatus());
    }

    @Test
    void getAllTicketsForOwner_ReturnsFilteredTickets() {
        List<Property> properties = Arrays.asList(testProperty);
        when(propertyService.getPropertiesByOwner(userId)).thenReturn(properties);
        when(ticketRepository.findByFilters(isNull(), isNull(), eq(ServiceSource.ACCOMMODATION)))
                .thenReturn(Arrays.asList(testTicket));
        when(messageRepository.countByTicketIdAndReadByRecipientFalseAndDeletedFalse(any(UUID.class)))
                .thenReturn(0L);

        List<TicketResponseDTO> result = supportTicketService.getAllTicketsForOwner(userId, null, null);

        assertFalse(result.isEmpty());
    }

    @Test
    void getAllTicketsForOwner_NoProperties_ReturnsEmptyList() {
        when(propertyService.getPropertiesByOwner(userId)).thenReturn(Collections.emptyList());

        List<TicketResponseDTO> result = supportTicketService.getAllTicketsForOwner(userId, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getTicketDetailWithAccess_AsTicketOwner_Success() {
        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));
        when(messageRepository.findByTicketIdAndDeletedFalseOrderBySentAtAsc(ticketId))
                .thenReturn(Arrays.asList(testMessage));
        when(progressRepository.findByTicketIdAndDeletedFalseOrderByPerformedAtAsc(ticketId))
                .thenReturn(Arrays.asList(testProgress));
        when(externalBookingService.fetchBookingById(any(ServiceSource.class), anyString()))
                .thenReturn(Map.of("bookingId", "booking-001"));

        TicketDetailResponseDTO result = supportTicketService.getTicketDetailWithAccess(
                ticketId, userId, "Customer");

        assertNotNull(result);
        assertEquals(ticketId, result.getId());
        assertNotNull(result.getMessages());
        assertNotNull(result.getProgressEntries());
    }

    @Test
    void getTicketDetailWithAccess_AsCustomer_AccessDenied() {
        UUID otherUserId = UUID.randomUUID();
        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));

        assertThrows(IllegalArgumentException.class, () ->
                supportTicketService.getTicketDetailWithAccess(ticketId, otherUserId, "Customer"));
    }

    @Test
    void getTicketDetailWithAccess_TicketNotFound() {
        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () ->
                supportTicketService.getTicketDetailWithAccess(ticketId, userId, "Customer"));
    }

    @Test
    void getTicketDetailWithAccess_AsAccommodationOwner_Success() {
        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));
        when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
        when(messageRepository.findByTicketIdAndDeletedFalseOrderBySentAtAsc(ticketId))
                .thenReturn(Collections.emptyList());
        when(progressRepository.findByTicketIdAndDeletedFalseOrderByPerformedAtAsc(ticketId))
                .thenReturn(Collections.emptyList());
        when(externalBookingService.fetchBookingById(any(ServiceSource.class), anyString()))
                .thenReturn(null);

        TicketDetailResponseDTO result = supportTicketService.getTicketDetailWithAccess(
                ticketId, userId, "Accommodation Owner");

        assertNotNull(result);
    }

    @Test
    void getTicketDetailWithAccess_AsAccommodationOwner_NoPropertyId_Denied() {
        testTicket.setPropertyId(null);
        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));

        assertThrows(IllegalArgumentException.class, () ->
                supportTicketService.getTicketDetailWithAccess(ticketId, userId, "Accommodation Owner"));
    }

    @Test
    void createTicket_Success() {
        CreateTicketRequestDTO request = new CreateTicketRequestDTO();
        request.setUserId(userId);
        request.setSubject("New Ticket");
        request.setServiceSource(ServiceSource.ACCOMMODATION);
        request.setExternalBookingId("booking-002");
        request.setInitialMessage("Initial message");

        when(externalBookingService.validateBookingExists(any(), anyString(), any(UUID.class)))
                .thenReturn(true);
        when(ticketRepository.existsByServiceSourceAndExternalBookingIdAndDeletedFalse(any(), anyString()))
                .thenReturn(false);
        when(externalBookingService.fetchBookingById(any(ServiceSource.class), anyString()))
                .thenReturn(Map.of("propertyId", "prop-001"));
        when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(i -> {
            SupportTicket t = i.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            return t;
        });
        when(progressRepository.save(any(SupportProgress.class))).thenReturn(testProgress);
        when(messageRepository.save(any(TicketMessage.class))).thenReturn(testMessage);
        when(messageRepository.countByTicketIdAndReadByRecipientFalseAndDeletedFalse(any(UUID.class)))
                .thenReturn(0L);

        TicketResponseDTO result = supportTicketService.createTicket(request);

        assertNotNull(result);
        verify(ticketRepository).save(any(SupportTicket.class));
        verify(progressRepository).save(any(SupportProgress.class));
        verify(messageRepository).save(any(TicketMessage.class));
    }

    @Test
    void createTicket_BookingNotFound_ThrowsException() {
        CreateTicketRequestDTO request = new CreateTicketRequestDTO();
        request.setUserId(userId);
        request.setSubject("New Ticket");
        request.setServiceSource(ServiceSource.ACCOMMODATION);
        request.setExternalBookingId("invalid-booking");
        request.setInitialMessage("Message");

        when(externalBookingService.validateBookingExists(any(), anyString(), any(UUID.class)))
                .thenReturn(false);

        assertThrows(InvalidBookingException.class, () ->
                supportTicketService.createTicket(request));
    }

    @Test
    void createTicket_TicketAlreadyExists_ThrowsException() {
        CreateTicketRequestDTO request = new CreateTicketRequestDTO();
        request.setUserId(userId);
        request.setSubject("New Ticket");
        request.setServiceSource(ServiceSource.ACCOMMODATION);
        request.setExternalBookingId("booking-001");
        request.setInitialMessage("Message");

        when(externalBookingService.validateBookingExists(any(), anyString(), any(UUID.class)))
                .thenReturn(true);
        when(ticketRepository.existsByServiceSourceAndExternalBookingIdAndDeletedFalse(any(), anyString()))
                .thenReturn(true);

        assertThrows(InvalidBookingException.class, () ->
                supportTicketService.createTicket(request));
    }

    @Test
    void updateTicketStatus_Success() {
        UpdateStatusRequestDTO request = new UpdateStatusRequestDTO();
        request.setStatus(TicketStatus.IN_PROGRESS);
        request.setUpdatedBy(userId);

        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);
        when(progressRepository.save(any(SupportProgress.class))).thenReturn(testProgress);
        when(messageRepository.countByTicketIdAndReadByRecipientFalseAndDeletedFalse(any(UUID.class)))
                .thenReturn(0L);

        TicketResponseDTO result = supportTicketService.updateTicketStatus(ticketId, request);

        assertNotNull(result);
        verify(ticketRepository).save(any(SupportTicket.class));
    }

    @Test
    void updateTicketStatusWithPermissionCheck_AsCustomer_OnlyClose() {
        UpdateStatusRequestDTO request = new UpdateStatusRequestDTO();
        request.setStatus(TicketStatus.CLOSED);
        request.setUpdatedBy(userId);

        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);
        when(progressRepository.save(any(SupportProgress.class))).thenReturn(testProgress);
        when(messageRepository.countByTicketIdAndReadByRecipientFalseAndDeletedFalse(any(UUID.class)))
                .thenReturn(0L);

        TicketResponseDTO result = supportTicketService.updateTicketStatusWithPermissionCheck(
                ticketId, request, userId, "Customer");

        assertNotNull(result);
    }

    @Test
    void updateTicketStatusWithPermissionCheck_AsCustomer_NotClose_Denied() {
        UpdateStatusRequestDTO request = new UpdateStatusRequestDTO();
        request.setStatus(TicketStatus.IN_PROGRESS);
        request.setUpdatedBy(userId);

        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));

        assertThrows(IllegalArgumentException.class, () ->
                supportTicketService.updateTicketStatusWithPermissionCheck(ticketId, request, userId, "Customer"));
    }

    @Test
    void updateTicketStatusWithPermissionCheck_AsOwner_Success() {
        UpdateStatusRequestDTO request = new UpdateStatusRequestDTO();
        request.setStatus(TicketStatus.IN_PROGRESS);
        request.setUpdatedBy(userId);

        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));
        when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
        when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);
        when(progressRepository.save(any(SupportProgress.class))).thenReturn(testProgress);
        when(messageRepository.countByTicketIdAndReadByRecipientFalseAndDeletedFalse(any(UUID.class)))
                .thenReturn(0L);

        TicketResponseDTO result = supportTicketService.updateTicketStatusWithPermissionCheck(
                ticketId, request, userId, "Accommodation Owner");

        assertNotNull(result);
    }

    @Test
    void deleteTicket_Success() {
        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);

        assertDoesNotThrow(() -> supportTicketService.deleteTicket(ticketId, userId));
        verify(ticketRepository).save(any(SupportTicket.class));
    }

    @Test
    void deleteTicket_NotOpen_ThrowsException() {
        testTicket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));

        assertThrows(IllegalStateException.class, () ->
                supportTicketService.deleteTicket(ticketId, userId));
    }

    @Test
    void deleteTicket_TicketNotFound_ThrowsException() {
        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () ->
                supportTicketService.deleteTicket(ticketId, userId));
    }

    @Test
    void addProgress_Success() {
        AddProgressRequestDTO request = new AddProgressRequestDTO();
        request.setDescription("Progress description");
        request.setPerformedBy(userId);

        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);
        when(progressRepository.save(any(SupportProgress.class))).thenReturn(testProgress);

        ProgressResponseDTO result = supportTicketService.addProgress(ticketId, request);

        assertNotNull(result);
        verify(ticketRepository).save(any(SupportTicket.class)); // Status changed to IN_PROGRESS
    }

    @Test
    void addProgressWithPermissionCheck_AsOwner_Success() {
        AddProgressRequestDTO request = new AddProgressRequestDTO();
        request.setDescription("Progress description");
        request.setPerformedBy(userId);

        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));
        when(propertyService.getPropertyById("prop-001")).thenReturn(Optional.of(testProperty));
        when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);
        when(progressRepository.save(any(SupportProgress.class))).thenReturn(testProgress);

        ProgressResponseDTO result = supportTicketService.addProgressWithPermissionCheck(
                ticketId, request, userId, "Accommodation Owner");

        assertNotNull(result);
    }

    @Test
    void addProgressWithPermissionCheck_AsOwner_WrongService_Denied() {
        testTicket.setServiceSource(ServiceSource.FLIGHT);
        AddProgressRequestDTO request = new AddProgressRequestDTO();
        request.setDescription("Progress");
        request.setPerformedBy(userId);

        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));

        assertThrows(IllegalArgumentException.class, () ->
                supportTicketService.addProgressWithPermissionCheck(ticketId, request, userId, "Accommodation Owner"));
    }

    @Test
    void deleteProgress_Success() {
        when(progressRepository.findById(any(UUID.class))).thenReturn(Optional.of(testProgress));
        when(progressRepository.save(any(SupportProgress.class))).thenReturn(testProgress);

        assertDoesNotThrow(() -> supportTicketService.deleteProgress(ticketId, testProgress.getId()));
        verify(progressRepository).save(any(SupportProgress.class));
    }

    @Test
    void deleteProgress_NotBelongToTicket_ThrowsException() {
        UUID otherTicketId = UUID.randomUUID();
        SupportTicket otherTicket = new SupportTicket();
        otherTicket.setId(otherTicketId);
        testProgress.setTicket(otherTicket);

        when(progressRepository.findById(any(UUID.class))).thenReturn(Optional.of(testProgress));

        assertThrows(IllegalArgumentException.class, () ->
                supportTicketService.deleteProgress(ticketId, testProgress.getId()));
    }

    @Test
    void addMessage_Success() {
        AddMessageRequestDTO request = new AddMessageRequestDTO();
        request.setSenderType(SenderType.CUSTOMER);
        request.setSenderId(userId);
        request.setMessage("Test message");

        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));
        when(messageRepository.save(any(TicketMessage.class))).thenReturn(testMessage);
        when(progressRepository.save(any(SupportProgress.class))).thenReturn(testProgress);

        MessageResponseDTO result = supportTicketService.addMessage(ticketId, request);

        assertNotNull(result);
        verify(messageRepository).save(any(TicketMessage.class));
    }

    @Test
    void getTicketMessages_Success() {
        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(testTicket));
        when(messageRepository.findByTicketIdAndDeletedFalseOrderBySentAtAsc(ticketId))
                .thenReturn(Arrays.asList(testMessage));

        List<MessageResponseDTO> result = supportTicketService.getTicketMessages(ticketId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getTicketMessages_TicketNotFound_ThrowsException() {
        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () ->
                supportTicketService.getTicketMessages(ticketId));
    }

    @Test
    void markMessagesAsRead_Success() {
        when(messageRepository.findByTicketIdAndReadByRecipientFalseAndDeletedFalse(ticketId))
                .thenReturn(Arrays.asList(testMessage));
        when(messageRepository.save(any(TicketMessage.class))).thenReturn(testMessage);

        // Message is from another user, so it should be marked as read
        UUID otherUserId = UUID.randomUUID();
        assertDoesNotThrow(() -> supportTicketService.markMessagesAsRead(ticketId, otherUserId));
        verify(messageRepository, times(1)).save(any(TicketMessage.class));
    }

    @Test
    void getAvailableBookings_Success() {
        List<String> mockBookings = Arrays.asList("booking-001");
        doReturn(mockBookings).when(externalBookingService).fetchBookingsByService(
                eq(ServiceSource.ACCOMMODATION), any(UUID.class));

        List<?> result = supportTicketService.getAvailableBookings(ServiceSource.ACCOMMODATION, userId);

        assertFalse(result.isEmpty());
    }

    @Test
    void getDashboardData_Success() {
        List<String> mockBookings = Arrays.asList("booking-001");
        doReturn(mockBookings).when(externalBookingService).fetchBookingsByService(
                any(ServiceSource.class), any(UUID.class));
        when(ticketRepository.findByFilters(eq(userId), isNull(), isNull()))
                .thenReturn(Arrays.asList(testTicket));
        when(messageRepository.countByTicketIdAndReadByRecipientFalseAndDeletedFalse(any(UUID.class)))
                .thenReturn(0L);

        SupportDashboardResponseDTO result = supportTicketService.getDashboardData(userId);

        assertNotNull(result);
        assertNotNull(result.getAccommodationBookings());
        assertNotNull(result.getFlightBookings());
        assertNotNull(result.getSupportTickets());
    }
}
