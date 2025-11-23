package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.support.*;
import apap.ti._5.accommodation_2306212083_be.enums.*;
import apap.ti._5.accommodation_2306212083_be.exception.*;
import apap.ti._5.accommodation_2306212083_be.model.SupportTicket;
import apap.ti._5.accommodation_2306212083_be.model.TicketMessage;
import apap.ti._5.accommodation_2306212083_be.model.SupportProgress;
import apap.ti._5.accommodation_2306212083_be.repository.SupportTicketRepository;
import apap.ti._5.accommodation_2306212083_be.repository.TicketMessageRepository;
import apap.ti._5.accommodation_2306212083_be.repository.SupportProgressRepository;
import apap.ti._5.accommodation_2306212083_be.security.CurrentUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing support tickets.
 * Handles ticket creation, retrieval, status updates, and external booking validation.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final SupportProgressRepository supportProgressRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Map of service sources to their API base URLs
     * In production, these should be configured in application.properties
     */
    private static final Map<ServiceSource, String> SERVICE_BASE_URLS = Map.of(
        ServiceSource.ACCOMMODATION, "http://localhost:8080/api",
        ServiceSource.FLIGHT, "https://flight-service-url/api",
        ServiceSource.VEHICLE_RENTAL, "https://vehicle-rental-url/api",
        ServiceSource.TOUR_PACKAGE, "https://tour-package-url/api",
        ServiceSource.INSURANCE, "https://insurance-service-url/api"
    );

    /**
     * Create a new support ticket
     * @param request The ticket creation request
     * @return Created ticket response
     */
    public TicketResponseDTO createTicket(CreateTicketRequestDTO request) {
        Long userId = CurrentUser.getUserId();
        
        // Validate booking exists and user owns it
        BookingInfoDTO bookingInfo = validateAndFetchBooking(
            request.getExternalServiceSource(),
            request.getExternalBookingId(),
            userId
        );

        // Generate unique ticket ID
        String ticketId = generateTicketId(request.getExternalServiceSource());

        // Create ticket entity
        SupportTicket ticket = SupportTicket.builder()
                .ticketId(ticketId)
                .userId(userId)
                .externalServiceSource(request.getExternalServiceSource())
                .externalBookingId(request.getExternalBookingId())
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(request.getPriority())
                .category(request.getCategory())
                .status(TicketStatus.OPEN)
                .build();

        ticket = supportTicketRepository.save(ticket);
        
        log.info("Created support ticket {} for user {} (Booking: {}-{})", 
                ticketId, userId, request.getExternalServiceSource(), request.getExternalBookingId());

        return mapToResponseDTO(ticket, bookingInfo, 0);
    }

    /**
     * Get all tickets for the current user
     * @return List of tickets
     */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getMyTickets() {
        Long userId = CurrentUser.getUserId();
        List<SupportTicket> tickets = supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return tickets.stream()
                .map(ticket -> {
                    BookingInfoDTO bookingInfo = fetchBookingInfoSafely(
                        ticket.getExternalServiceSource(), 
                        ticket.getExternalBookingId()
                    );
                    int unreadCount = ticketMessageRepository
                        .countUnreadMessagesForTicket(ticket.getTicketId(), userId)
                        .intValue();
                    return mapToResponseDTO(ticket, bookingInfo, unreadCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all tickets (admin only)
     * @return List of all tickets
     */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getAllTickets() {
        if (!CurrentUser.isSuperadmin()) {
            throw new UnauthorizedTicketAccessException("Only admins can view all tickets");
        }

        List<SupportTicket> tickets = supportTicketRepository.findAllByOrderByCreatedAtDesc();
        
        return tickets.stream()
                .map(ticket -> {
                    BookingInfoDTO bookingInfo = fetchBookingInfoSafely(
                        ticket.getExternalServiceSource(), 
                        ticket.getExternalBookingId()
                    );
                    return mapToResponseDTO(ticket, bookingInfo, 0);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get detailed ticket information including messages and progress
     * @param ticketId The ticket ID
     * @return Detailed ticket response
     */
    @Transactional(readOnly = true)
    public TicketDetailResponseDTO getTicketDetail(String ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        // Verify user has access to this ticket
        Long userId = CurrentUser.getUserId();
        if (!CurrentUser.isSuperadmin() && !ticket.getUserId().equals(userId)) {
            throw new UnauthorizedTicketAccessException(ticketId);
        }

        // Fetch booking info
        BookingInfoDTO bookingInfo = fetchBookingInfoSafely(
            ticket.getExternalServiceSource(),
            ticket.getExternalBookingId()
        );

        // Fetch messages
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        List<MessageResponseDTO> messageDTOs = messages.stream()
                .map(this::mapToMessageDTO)
                .collect(Collectors.toList());

        // Fetch progress
        List<SupportProgress> progress = supportProgressRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        List<ProgressResponseDTO> progressDTOs = progress.stream()
                .map(this::mapToProgressDTO)
                .collect(Collectors.toList());

        int unreadCount = ticketMessageRepository
                .countUnreadMessagesForTicket(ticketId, userId)
                .intValue();

        return TicketDetailResponseDTO.detailBuilder()
                .ticketId(ticket.getTicketId())
                .userId(ticket.getUserId())
                .assignedAdminId(ticket.getAssignedAdminId())
                .externalServiceSource(ticket.getExternalServiceSource())
                .externalBookingId(ticket.getExternalBookingId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .closedAt(ticket.getClosedAt())
                .bookingInfo(bookingInfo)
                .unreadMessageCount(unreadCount)
                .messages(messageDTOs)
                .progress(progressDTOs)
                .build();
    }

    /**
     * Close a ticket (user can close their own tickets)
     * @param ticketId The ticket ID
     */
    public void closeTicket(String ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        Long userId = CurrentUser.getUserId();
        if (!CurrentUser.isSuperadmin() && !ticket.getUserId().equals(userId)) {
            throw new UnauthorizedTicketAccessException(ticketId);
        }

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        supportTicketRepository.save(ticket);
        
        log.info("Ticket {} closed by user {}", ticketId, userId);
    }

    /**
     * Assign ticket to an admin (admin only)
     * @param ticketId The ticket ID
     * @param adminId The admin ID to assign
     */
    public void assignTicket(String ticketId, Long adminId) {
        if (!CurrentUser.isSuperadmin()) {
            throw new UnauthorizedTicketAccessException("Only admins can assign tickets");
        }

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        ticket.setAssignedAdminId(adminId);
        supportTicketRepository.save(ticket);
        
        log.info("Ticket {} assigned to admin {}", ticketId, adminId);
    }

    /**
     * Update ticket status (admin only)
     * @param ticketId The ticket ID
     * @param request The status update request
     */
    public void updateTicketStatus(String ticketId, UpdateStatusRequestDTO request) {
        if (!CurrentUser.isSuperadmin()) {
            throw new UnauthorizedTicketAccessException("Only admins can update ticket status");
        }

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        ticket.setStatus(request.getStatus());
        if (request.getStatus() == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        supportTicketRepository.save(ticket);
        
        log.info("Ticket {} status updated to {} by admin {}", ticketId, request.getStatus(), CurrentUser.getUserId());
    }

    /**
     * Generate unique ticket ID with format: ST-{SERVICE_CODE}-{YEAR}-{SEQUENCE}
     * @param serviceSource The service source
     * @return Generated ticket ID
     */
    private String generateTicketId(ServiceSource serviceSource) {
        String serviceCode = serviceSource.getCode();
        String year = String.valueOf(Year.now().getValue());
        
        Integer maxSequence = supportTicketRepository
                .findMaxSequenceForServiceAndYear(serviceCode, year)
                .orElse(0);
        
        int newSequence = maxSequence + 1;
        String sequenceStr = String.format("%04d", newSequence);
        
        return String.format("ST-%s-%s-%s", serviceCode, year, sequenceStr);
    }

    /**
     * Validate booking exists and user is the owner
     * @param serviceSource The service source
     * @param bookingId The booking ID
     * @param userId The user ID
     * @return Booking information
     */
    private BookingInfoDTO validateAndFetchBooking(ServiceSource serviceSource, String bookingId, Long userId) {
        try {
            String baseUrl = SERVICE_BASE_URLS.get(serviceSource);
            String endpoint = determineBookingEndpoint(serviceSource, bookingId);
            String url = baseUrl + endpoint;

            log.info("Validating booking {} from service {}", bookingId, serviceSource);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> bookingData = objectMapper.readValue(
                    response.getBody(), 
                    new TypeReference<Map<String, Object>>() {}
                );
                
                // Verify user owns this booking
                Object ownerIdObj = bookingData.get("userId");
                Long ownerId = ownerIdObj instanceof Number ? 
                    ((Number) ownerIdObj).longValue() : 
                    Long.parseLong(ownerIdObj.toString());
                
                if (!ownerId.equals(userId)) {
                    throw BookingValidationException.notBookingOwner(bookingId);
                }

                return parseBookingInfo(bookingData, bookingId);
            }
            
            throw BookingValidationException.bookingNotFound(serviceSource.name(), bookingId);
            
        } catch (BookingValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate booking: {} - {}", bookingId, e.getMessage());
            throw BookingValidationException.serviceUnavailable(serviceSource.name());
        }
    }

    /**
     * Fetch booking info safely (returns null if fails)
     * @param serviceSource The service source
     * @param bookingId The booking ID
     * @return Booking information or null
     */
    private BookingInfoDTO fetchBookingInfoSafely(ServiceSource serviceSource, String bookingId) {
        try {
            String baseUrl = SERVICE_BASE_URLS.get(serviceSource);
            String endpoint = determineBookingEndpoint(serviceSource, bookingId);
            String url = baseUrl + endpoint;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> bookingData = objectMapper.readValue(
                    response.getBody(), 
                    new TypeReference<Map<String, Object>>() {}
                );
                return parseBookingInfo(bookingData, bookingId);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch booking info for {} - {}", bookingId, e.getMessage());
        }
        
        // Return minimal info if fetch fails
        return BookingInfoDTO.builder()
                .bookingId(bookingId)
                .status("UNKNOWN")
                .details(Map.of("error", "Could not fetch booking details"))
                .build();
    }

    /**
     * Determine the correct endpoint for each service type
     * @param serviceSource The service source
     * @param bookingId The booking ID
     * @return API endpoint path
     */
    private String determineBookingEndpoint(ServiceSource serviceSource, String bookingId) {
        return switch (serviceSource) {
            case ACCOMMODATION -> "/booking/" + bookingId;
            case FLIGHT -> "/flight-bookings/" + bookingId;
            case VEHICLE_RENTAL -> "/rentals/" + bookingId;
            case TOUR_PACKAGE -> "/packages/" + bookingId;
            case INSURANCE -> "/policies/" + bookingId;
        };
    }

    /**
     * Parse booking data into BookingInfoDTO
     * @param data The raw booking data
     * @param bookingId The booking ID
     * @return BookingInfoDTO
     */
    private BookingInfoDTO parseBookingInfo(Map<String, Object> data, String bookingId) {
        return BookingInfoDTO.builder()
                .bookingId(bookingId)
                .userId(getLongValue(data, "userId"))
                .status(getStringValue(data, "status"))
                .createdAt(getStringValue(data, "createdAt"))
                .totalPrice(getIntegerValue(data, "totalPrice"))
                .details(data)
                .build();
    }

    /**
     * Map SupportTicket entity to TicketResponseDTO
     */
    private TicketResponseDTO mapToResponseDTO(SupportTicket ticket, BookingInfoDTO bookingInfo, int unreadCount) {
        return TicketResponseDTO.builder()
                .ticketId(ticket.getTicketId())
                .userId(ticket.getUserId())
                .assignedAdminId(ticket.getAssignedAdminId())
                .externalServiceSource(ticket.getExternalServiceSource())
                .externalBookingId(ticket.getExternalBookingId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .closedAt(ticket.getClosedAt())
                .bookingInfo(bookingInfo)
                .unreadMessageCount(unreadCount)
                .build();
    }

    /**
     * Map TicketMessage entity to MessageResponseDTO
     */
    private MessageResponseDTO mapToMessageDTO(TicketMessage message) {
        List<String> attachments = new ArrayList<>();
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            try {
                attachments = objectMapper.readValue(message.getAttachments(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse attachments for message {}", message.getMessageId());
            }
        }

        return MessageResponseDTO.builder()
                .messageId(message.getMessageId())
                .ticketId(message.getTicketId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderType(message.getSenderType())
                .messageBody(message.getMessageBody())
                .isRead(message.getIsRead())
                .attachments(attachments)
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * Map SupportProgress entity to ProgressResponseDTO
     */
    private ProgressResponseDTO mapToProgressDTO(SupportProgress progress) {
        return ProgressResponseDTO.builder()
                .progressId(progress.getProgressId())
                .ticketId(progress.getTicketId())
                .userId(progress.getUserId())
                .userName(progress.getUserName())
                .text(progress.getText())
                .role(progress.getRole())
                .actionType(progress.getActionType())
                .createdAt(progress.getCreatedAt())
                .build();
    }

    // Helper methods for safe data extraction
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value != null) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
