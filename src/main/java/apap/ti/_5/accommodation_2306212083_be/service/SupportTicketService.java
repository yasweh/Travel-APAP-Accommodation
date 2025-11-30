package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.support.*;
import apap.ti._5.accommodation_2306212083_be.enums.ActionType;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import apap.ti._5.accommodation_2306212083_be.exception.InvalidBookingException;
import apap.ti._5.accommodation_2306212083_be.exception.TicketNotFoundException;
import apap.ti._5.accommodation_2306212083_be.model.SupportProgress;
import apap.ti._5.accommodation_2306212083_be.model.SupportTicket;
import apap.ti._5.accommodation_2306212083_be.model.TicketMessage;
import apap.ti._5.accommodation_2306212083_be.repository.SupportProgressRepository;
import apap.ti._5.accommodation_2306212083_be.repository.SupportTicketRepository;
import apap.ti._5.accommodation_2306212083_be.repository.TicketMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main service for Support Ticket operations
 */
@Service
@Slf4j
public class SupportTicketService {
    
    private final SupportTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final SupportProgressRepository progressRepository;
    private final ExternalBookingService externalBookingService;
    private final PropertyService propertyService;
    
    public SupportTicketService(
            SupportTicketRepository ticketRepository,
            TicketMessageRepository messageRepository,
            SupportProgressRepository progressRepository,
            ExternalBookingService externalBookingService,
            PropertyService propertyService) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.progressRepository = progressRepository;
        this.externalBookingService = externalBookingService;
        this.propertyService = propertyService;
    }
    
    /**
     * GET /api/support-tickets
     * Get all tickets with optional filters
     */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getAllTickets(UUID userId, TicketStatus status, ServiceSource serviceSource) {
        log.info("Fetching tickets with filters: userId={}, status={}, serviceSource={}", 
                userId, status, serviceSource);
        
        List<SupportTicket> tickets = ticketRepository.findByFilters(userId, status, serviceSource);
        
        return tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tickets for Accommodation Owner (filtered by their properties)
     */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getAllTicketsForOwner(UUID ownerId, TicketStatus status, ServiceSource serviceSource) {
        log.info("Fetching tickets for owner: ownerId={}, status={}, serviceSource={}", 
                ownerId, status, serviceSource);
        
        // Get all properties owned by this owner
        List<String> ownedPropertyIds = propertyService.getPropertiesByOwner(ownerId)
                .stream()
                .map(property -> property.getPropertyId())
                .collect(Collectors.toList());
        
        if (ownedPropertyIds.isEmpty()) {
            log.info("Owner {} has no properties, returning empty ticket list", ownerId);
            return List.of();
        }
        
        // Get all tickets for ACCOMMODATION service that belong to owner's properties
        List<SupportTicket> tickets = ticketRepository.findByFilters(null, status, ServiceSource.ACCOMMODATION);
        
        // Filter by properties owned by this owner
        List<SupportTicket> filteredTickets = tickets.stream()
                .filter(ticket -> ticket.getPropertyId() != null)
                .filter(ticket -> ownedPropertyIds.contains(ticket.getPropertyId()))
                .collect(Collectors.toList());
        
        log.info("Found {} tickets for owner's {} properties", filteredTickets.size(), ownedPropertyIds.size());
        
        return filteredTickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * GET /api/support-tickets/{id} with access control
     */
    @Transactional(readOnly = true)
    public TicketDetailResponseDTO getTicketDetailWithAccess(UUID ticketId, UUID requestingUserId, String role) {
        log.info("Fetching ticket detail for ticketId={}, requestingUserId={}, role={}", 
                ticketId, requestingUserId, role);
        
        SupportTicket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + ticketId));
        
        // Access control based on role
        if ("Customer".equals(role)) {
            // Customer can only see their own tickets
            if (!ticket.getUserId().equals(requestingUserId)) {
                throw new IllegalArgumentException("Access denied: You can only view your own tickets");
            }
        } else if ("Accommodation Owner".equals(role)) {
            // Owner can see tickets for their properties
            if (ticket.getPropertyId() == null) {
                throw new IllegalArgumentException("Access denied: This ticket is not for your property");
            }
            // Check if owner actually owns this property
            var propertyOpt = propertyService.getPropertyById(ticket.getPropertyId());
            if (propertyOpt.isEmpty()) {
                throw new IllegalArgumentException("Access denied: Property not found");
            }
            if (!propertyOpt.get().getOwnerId().equals(requestingUserId)) {
                throw new IllegalArgumentException("Access denied: This ticket is not for your property");
            }
        }
        // Superadmin can see all tickets
        
        // Fetch messages
        List<MessageResponseDTO> messages = messageRepository
                .findByTicketIdAndDeletedFalseOrderBySentAtAsc(ticketId)
                .stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
        
        // Fetch progress entries
        List<ProgressResponseDTO> progressEntries = progressRepository
                .findByTicketIdAndDeletedFalseOrderByPerformedAtAsc(ticketId)
                .stream()
                .map(this::mapToProgressResponse)
                .collect(Collectors.toList());
        
        // Fetch external booking data
        Object externalBookingData = externalBookingService.fetchBookingById(
                ticket.getServiceSource(),
                ticket.getExternalBookingId()
        );
        
        TicketDetailResponseDTO response = mapToTicketDetailResponse(ticket);
        response.setMessages(messages);
        response.setProgressEntries(progressEntries);
        response.setExternalBookingData(externalBookingData);
        response.setExternalBookingDataAvailable(externalBookingData != null);
        
        return response;
    }
    
    /**
     * POST /api/support-tickets
     * Create a new support ticket
     */
    @Transactional
    public TicketResponseDTO createTicket(CreateTicketRequestDTO request) {
        log.info("Creating new ticket for userId={}, serviceSource={}, bookingId={}", 
                request.getUserId(), request.getServiceSource(), request.getExternalBookingId());
        
        // Step 1: Validate that external booking exists and belongs to user
        boolean bookingExists = externalBookingService.validateBookingExists(
                request.getServiceSource(),
                request.getExternalBookingId(),
                request.getUserId()
        );
        
        if (!bookingExists) {
            throw new InvalidBookingException(
                    "Booking not found or does not belong to user: " + request.getExternalBookingId());
        }
        
        // Step 2: Check if ticket already exists for this booking
        boolean ticketExists = ticketRepository.existsByServiceSourceAndExternalBookingIdAndDeletedFalse(
                request.getServiceSource(),
                request.getExternalBookingId()
        );
        
        if (ticketExists) {
            throw new InvalidBookingException(
                    "A ticket already exists for this booking: " + request.getExternalBookingId());
        }
        
        // Step 3: Create the ticket
        SupportTicket ticket = new SupportTicket();
        ticket.setUserId(request.getUserId());
        ticket.setSubject(request.getSubject());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setServiceSource(request.getServiceSource());
        ticket.setExternalBookingId(request.getExternalBookingId());
        ticket.setInitialMessage(request.getInitialMessage());
        ticket.setDeleted(false);
        
        // Auto-set propertyId if service source is ACCOMMODATION
        if (request.getServiceSource() == ServiceSource.ACCOMMODATION) {
            try {
                // Fetch booking to get propertyId
                Object bookingData = externalBookingService.fetchBookingById(
                        ServiceSource.ACCOMMODATION, request.getExternalBookingId());
                if (bookingData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bookingMap = (Map<String, Object>) bookingData;
                    String propertyId = (String) bookingMap.get("propertyId");
                    ticket.setPropertyId(propertyId);
                }
            } catch (Exception e) {
                log.warn("Could not set propertyId for ticket: {}", e.getMessage());
                // Continue without propertyId - not critical
            }
        }
        
        ticket = ticketRepository.save(ticket);
        
        // Step 4: Add initial progress entry
        addProgressEntry(ticket, ActionType.CREATED, 
                "Ticket created by user", request.getUserId());
        
        // Step 5: Add initial message
        TicketMessage initialMsg = new TicketMessage();
        initialMsg.setTicket(ticket);
        initialMsg.setSenderType(apap.ti._5.accommodation_2306212083_be.enums.SenderType.CUSTOMER);
        initialMsg.setSenderId(request.getUserId());
        initialMsg.setMessage(request.getInitialMessage());
        initialMsg.setReadByRecipient(false);
        initialMsg.setDeleted(false);
        messageRepository.save(initialMsg);
        
        log.info("Ticket created successfully with id={}", ticket.getId());
        
        return mapToTicketResponse(ticket);
    }
    
    /**
     * PATCH /api/support-tickets/{id}/status with permission check
     * Update ticket status (customers can only close, admin/owner can change to any status)
     */
    @Transactional
    public TicketResponseDTO updateTicketStatusWithPermissionCheck(UUID ticketId, UpdateStatusRequestDTO request, 
                                                                    UUID userId, String role) {
        log.info("Updating ticket status for ticketId={} to {} by userId={}, role={}", 
                ticketId, request.getStatus(), userId, role);
        
        SupportTicket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + ticketId));
        
        // Permission check for Customer
        if ("Customer".equals(role)) {
            // Customer can only close their own tickets
            if (!ticket.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Access denied: You can only update your own tickets");
            }
            
            if (!TicketStatus.CLOSED.equals(request.getStatus())) {
                throw new IllegalArgumentException("Access denied: Customers can only close tickets");
            }
        } else if ("Accommodation Owner".equals(role)) {
            // Owner can change status for tickets related to their properties
            if (ServiceSource.ACCOMMODATION.equals(ticket.getServiceSource())) {
                if (ticket.getPropertyId() != null) {
                    var property = propertyService.getPropertyById(ticket.getPropertyId());
                    if (property.isEmpty() || !property.get().getOwnerId().equals(userId)) {
                        throw new IllegalArgumentException("Access denied: You do not own the property associated with this ticket");
                    }
                }
            } else {
                throw new IllegalArgumentException("Access denied: You can only update accommodation tickets");
            }
        }
        // Superadmin can update any ticket
        
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(request.getStatus());
        ticket = ticketRepository.save(ticket);
        
        // Add progress entry for status change
        String description = String.format("Status changed from %s to %s", oldStatus, request.getStatus());
        if (request.getReason() != null && !request.getReason().isEmpty()) {
            description += ". Reason: " + request.getReason();
        }
        
        addProgressEntry(ticket, ActionType.STATUS_CHANGED, description, request.getUpdatedBy());
        
        log.info("Ticket status updated successfully");
        
        return mapToTicketResponse(ticket);
    }
    
    /**
     * PATCH /api/support-tickets/{id}/status
     * Update ticket status
     */
    @Transactional
    public TicketResponseDTO updateTicketStatus(UUID ticketId, UpdateStatusRequestDTO request) {
        log.info("Updating ticket status for ticketId={} to {}", ticketId, request.getStatus());
        
        SupportTicket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + ticketId));
        
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(request.getStatus());
        ticket = ticketRepository.save(ticket);
        
        // Add progress entry for status change
        String description = String.format("Status changed from %s to %s", oldStatus, request.getStatus());
        if (request.getReason() != null && !request.getReason().isEmpty()) {
            description += ". Reason: " + request.getReason();
        }
        
        addProgressEntry(ticket, ActionType.STATUS_CHANGED, description, request.getUpdatedBy());
        
        log.info("Ticket status updated successfully");
        
        return mapToTicketResponse(ticket);
    }
    
    /**
     * DELETE /api/support-tickets/{id}
     * Soft delete a ticket (only allowed if status is OPEN)
     */
    @Transactional
    public void deleteTicket(UUID ticketId, UUID userId) {
        log.info("Deleting ticket with id={}", ticketId);
        
        SupportTicket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + ticketId));
        
        // Only allow deletion if ticket is OPEN
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new IllegalStateException("Cannot delete ticket with status: " + ticket.getStatus());
        }
        
        // Verify user owns the ticket - DISABLED for debug
        // if (!ticket.getUserId().equals(userId)) {
        //     throw new IllegalArgumentException("User does not have permission to delete this ticket");
        // }
        
        ticket.setDeleted(true);
        ticketRepository.save(ticket);
        
        log.info("Ticket deleted successfully");
    }
    
    /**
     * POST /api/support-tickets/{id}/progress with permission check
     * Add progress entry to ticket (only admin or property owner)
     */
    @Transactional
    public ProgressResponseDTO addProgressWithPermissionCheck(UUID ticketId, AddProgressRequestDTO request, 
                                                               UUID userId, String role) {
        log.info("Adding progress to ticketId={} by userId={}, role={}", ticketId, userId, role);
        
        SupportTicket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + ticketId));
        
        // Permission check for Accommodation Owner
        if ("Accommodation Owner".equals(role)) {
            // Owner can only add progress if ticket is for accommodation service and they own the property
            if (!ServiceSource.ACCOMMODATION.equals(ticket.getServiceSource())) {
                throw new IllegalArgumentException("Access denied: You can only add progress to accommodation tickets");
            }
            
            if (ticket.getPropertyId() == null) {
                throw new IllegalArgumentException("Access denied: This ticket is not associated with a property");
            }
            
            // Verify that the owner owns this property
            var property = propertyService.getPropertyById(ticket.getPropertyId());
            if (property.isEmpty() || !property.get().getOwnerId().equals(userId)) {
                throw new IllegalArgumentException("Access denied: You do not own the property associated with this ticket");
            }
        }
        // Superadmin can add progress to any ticket
        
        // If ticket was OPEN, change to IN_PROGRESS
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticketRepository.save(ticket);
        }
        
        SupportProgress progress = addProgressEntry(
                ticket,
                ActionType.PROGRESS_ADDED,
                request.getDescription(),
                request.getPerformedBy()
        );
        
        return mapToProgressResponse(progress);
    }
    
    /**
     * POST /api/support-tickets/{id}/progress
     * Add progress entry to ticket
     */
    @Transactional
    public ProgressResponseDTO addProgress(UUID ticketId, AddProgressRequestDTO request) {
        log.info("Adding progress to ticketId={}", ticketId);
        
        SupportTicket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + ticketId));
        
        // If ticket was OPEN, change to IN_PROGRESS
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticketRepository.save(ticket);
        }
        
        SupportProgress progress = addProgressEntry(
                ticket,
                ActionType.PROGRESS_ADDED,
                request.getDescription(),
                request.getPerformedBy()
        );
        
        return mapToProgressResponse(progress);
    }
    
    /**
     * DELETE /api/support-tickets/{ticketId}/progress/{progressId}
     * Soft delete progress entry
     */
    @Transactional
    public void deleteProgress(UUID ticketId, UUID progressId) {
        log.info("Deleting progress with id={} from ticketId={}", progressId, ticketId);
        
        SupportProgress progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("Progress entry not found with id: " + progressId));
        
        if (!progress.getTicket().getId().equals(ticketId)) {
            throw new IllegalArgumentException("Progress entry does not belong to this ticket");
        }
        
        progress.setDeleted(true);
        progressRepository.save(progress);
        
        log.info("Progress deleted successfully");
    }
    
    /**
     * POST /api/support-tickets/{id}/messages
     * Add message to ticket
     */
    @Transactional
    public MessageResponseDTO addMessage(UUID ticketId, AddMessageRequestDTO request) {
        log.info("Adding message to ticketId={}", ticketId);
        
        SupportTicket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + ticketId));
        
        TicketMessage message = new TicketMessage();
        message.setTicket(ticket);
        message.setSenderType(request.getSenderType());
        message.setSenderId(request.getSenderId());
        message.setMessage(request.getMessage());
        message.setReadByRecipient(false);
        message.setDeleted(false);
        
        message = messageRepository.save(message);
        
        // Add progress entry
        addProgressEntry(ticket, ActionType.MESSAGE_ADDED,
                request.getSenderType() + " added a message",
                request.getSenderId());
        
        log.info("Message added successfully");
        
        return mapToMessageResponse(message);
    }
    
    /**
     * GET /api/support-tickets/{id}/messages
     * Get all messages for a ticket
     */
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getTicketMessages(UUID ticketId) {
        log.info("Fetching messages for ticketId={}", ticketId);
        
        // Verify ticket exists
        ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + ticketId));
        
        return messageRepository.findByTicketIdAndDeletedFalseOrderBySentAtAsc(ticketId)
                .stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * PUT /api/support-tickets/{ticketId}/messages/mark-read
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(UUID ticketId, UUID userId) {
        log.info("Marking messages as read for ticketId={}", ticketId);
        
        List<TicketMessage> unreadMessages = messageRepository
                .findByTicketIdAndReadByRecipientFalseAndDeletedFalse(ticketId);
        
        // Only mark messages sent by others as read
        unreadMessages.stream()
                .filter(msg -> !msg.getSenderId().equals(userId))
                .forEach(msg -> {
                    msg.setReadByRecipient(true);
                    messageRepository.save(msg);
                });
        
        log.info("Marked {} messages as read", unreadMessages.size());
    }
    
    /**
     * GET /api/support-tickets/bookings
     * Fetch available bookings for a user from all services
     */
    @Transactional(readOnly = true)
    public List<?> getAvailableBookings(ServiceSource serviceSource, UUID userId) {
        log.info("Fetching available bookings for serviceSource={}, userId={}", serviceSource, userId);
        
        return externalBookingService.fetchBookingsByService(serviceSource, userId);
    }
    
    /**
     * GET /api/support-tickets/dashboard
     * Get comprehensive dashboard data: all bookings from 5 external services + user's support tickets
     */
    @Transactional(readOnly = true)
    public SupportDashboardResponseDTO getDashboardData(UUID userId) {
        log.info("Fetching dashboard data for userId={}", userId);
        
        SupportDashboardResponseDTO dashboard = new SupportDashboardResponseDTO();
        
        // Fetch bookings from all 5 external services
        dashboard.setAccommodationBookings(
            externalBookingService.fetchBookingsByService(ServiceSource.ACCOMMODATION, userId));
        dashboard.setFlightBookings(
            externalBookingService.fetchBookingsByService(ServiceSource.FLIGHT, userId));
        dashboard.setRentalBookings(
            externalBookingService.fetchBookingsByService(ServiceSource.RENTAL, userId));
        dashboard.setTourBookings(
            externalBookingService.fetchBookingsByService(ServiceSource.TOUR, userId));
        dashboard.setInsuranceBookings(
            externalBookingService.fetchBookingsByService(ServiceSource.INSURANCE, userId));
        
        // Fetch user's existing support tickets
        List<TicketResponseDTO> tickets = getAllTickets(userId, null, null);
        dashboard.setSupportTickets(tickets);
        
        log.info("Dashboard data fetched successfully: {} accommodation, {} flight, {} rental, {} tour, {} insurance bookings, {} tickets",
                dashboard.getAccommodationBookings().size(),
                dashboard.getFlightBookings().size(),
                dashboard.getRentalBookings().size(),
                dashboard.getTourBookings().size(),
                dashboard.getInsuranceBookings().size(),
                dashboard.getSupportTickets().size());
        
        return dashboard;
    }
    
    // ========== Private Helper Methods ==========
    
    private SupportProgress addProgressEntry(SupportTicket ticket, ActionType actionType, 
                                            String description, UUID performedBy) {
        SupportProgress progress = new SupportProgress();
        progress.setTicket(ticket);
        progress.setActionType(actionType);
        progress.setDescription(description);
        progress.setPerformedBy(performedBy);
        progress.setDeleted(false);
        
        return progressRepository.save(progress);
    }
    
    private TicketResponseDTO mapToTicketResponse(SupportTicket ticket) {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setId(ticket.getId());
        dto.setUserId(ticket.getUserId());
        dto.setSubject(ticket.getSubject());
        dto.setStatus(ticket.getStatus());
        dto.setServiceSource(ticket.getServiceSource());
        dto.setExternalBookingId(ticket.getExternalBookingId());
        dto.setPropertyId(ticket.getPropertyId());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        
        // Add property name if available
        if (ticket.getPropertyId() != null) {
            propertyService.getPropertyById(ticket.getPropertyId())
                .ifPresent(property -> dto.setPropertyName(property.getPropertyName()));
        }
        
        // Add customer name from external booking service
        try {
            Object bookingData = externalBookingService.fetchBookingById(
                ticket.getServiceSource(), 
                ticket.getExternalBookingId()
            );
            if (bookingData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> bookingMap = (Map<String, Object>) bookingData;
                if (bookingMap.containsKey("customerName")) {
                    dto.setCustomerName((String) bookingMap.get("customerName"));
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch customer name for ticket {}: {}", ticket.getId(), e.getMessage());
        }
        
        // Count unread messages
        long unreadCount = messageRepository.countByTicketIdAndReadByRecipientFalseAndDeletedFalse(ticket.getId());
        dto.setUnreadMessagesCount((int) unreadCount);
        
        return dto;
    }
    
    private TicketDetailResponseDTO mapToTicketDetailResponse(SupportTicket ticket) {
        TicketDetailResponseDTO dto = new TicketDetailResponseDTO();
        dto.setId(ticket.getId());
        dto.setUserId(ticket.getUserId());
        dto.setSubject(ticket.getSubject());
        dto.setStatus(ticket.getStatus());
        dto.setServiceSource(ticket.getServiceSource());
        dto.setExternalBookingId(ticket.getExternalBookingId());
        dto.setInitialMessage(ticket.getInitialMessage());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        
        return dto;
    }
    
    private MessageResponseDTO mapToMessageResponse(TicketMessage message) {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setId(message.getId());
        dto.setTicketId(message.getTicket().getId());
        dto.setSenderType(message.getSenderType());
        dto.setSenderId(message.getSenderId());
        dto.setMessage(message.getMessage());
        dto.setSentAt(message.getSentAt());
        dto.setReadByRecipient(message.getReadByRecipient());
        
        return dto;
    }
    
    private ProgressResponseDTO mapToProgressResponse(SupportProgress progress) {
        ProgressResponseDTO dto = new ProgressResponseDTO();
        dto.setId(progress.getId());
        dto.setTicketId(progress.getTicket().getId());
        dto.setActionType(progress.getActionType());
        dto.setDescription(progress.getDescription());
        dto.setPerformedBy(progress.getPerformedBy());
        dto.setPerformedAt(progress.getPerformedAt());
        
        return dto;
    }
}
