package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.support.AddMessageRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.support.MessageResponseDTO;
import apap.ti._5.accommodation_2306212083_be.enums.SenderType;
import apap.ti._5.accommodation_2306212083_be.exception.TicketNotFoundException;
import apap.ti._5.accommodation_2306212083_be.exception.UnauthorizedTicketAccessException;
import apap.ti._5.accommodation_2306212083_be.model.SupportTicket;
import apap.ti._5.accommodation_2306212083_be.model.TicketMessage;
import apap.ti._5.accommodation_2306212083_be.repository.SupportTicketRepository;
import apap.ti._5.accommodation_2306212083_be.repository.TicketMessageRepository;
import apap.ti._5.accommodation_2306212083_be.util.CurrentUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing ticket messages.
 * Handles message creation and retrieval.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ObjectMapper objectMapper;

    /**
     * Add a message to a ticket
     * @param ticketId The ticket ID
     * @param request The message request
     * @return Created message response
     */
    public MessageResponseDTO addMessage(String ticketId, AddMessageRequestDTO request) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        String userId = CurrentUser.getUserId();
        String role = CurrentUser.getRole();
        
        // Verify user has access to this ticket
        boolean isAdmin = CurrentUser.isSuperadmin();
        boolean isOwner = ticket.getUserId().equals(userId);
        boolean isVendor = CurrentUser.isOwner(); // Vendor can also participate

        if (!isAdmin && !isOwner && !isVendor) {
            throw new UnauthorizedTicketAccessException(ticketId);
        }

        // Determine sender type
        SenderType senderType;
        if (isAdmin) {
            senderType = SenderType.ADMIN;
        } else if (isVendor && !isOwner) {
            senderType = SenderType.VENDOR;
        } else {
            senderType = SenderType.USER;
        }

        // Convert attachments list to JSON string
        String attachmentsJson = null;
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            try {
                attachmentsJson = objectMapper.writeValueAsString(request.getAttachments());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize attachments: {}", e.getMessage());
            }
        }

        // Create message
        TicketMessage message = TicketMessage.builder()
                .supportTicket(ticket)
                .senderId(userId)
                .senderName(CurrentUser.getName())
                .senderType(senderType)
                .messageBody(request.getMessageBody())
                .isRead(false)
                .attachments(attachmentsJson)
                .build();

        message = ticketMessageRepository.save(message);
        
        log.info("Message added to ticket {} by user {} ({})", ticketId, userId, senderType);

        return mapToMessageDTO(message, request.getAttachments());
    }

    /**
     * Get all messages for a ticket
     * @param ticketId The ticket ID
     * @return List of messages
     */
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getMessages(String ticketId) {
        // Verify ticket exists
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        // Verify user has access
        String userId = CurrentUser.getUserId();
        if (!CurrentUser.isSuperadmin() && !ticket.getUserId().equals(userId)) {
            throw new UnauthorizedTicketAccessException(ticketId);
        }

        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        
        return messages.stream()
                .map(msg -> {
                    List<String> attachments = parseAttachments(msg.getAttachments());
                    return mapToMessageDTO(msg, attachments);
                })
                .collect(Collectors.toList());
    }

    /**
     * Mark messages as read
     * @param ticketId The ticket ID
     */
    public void markMessagesAsRead(String ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        String userId = CurrentUser.getUserId();
        if (!CurrentUser.isSuperadmin() && !ticket.getUserId().equals(userId)) {
            throw new UnauthorizedTicketAccessException(ticketId);
        }

        ticketMessageRepository.markAllMessagesAsRead(ticketId, userId);
        log.info("Messages in ticket {} marked as read by user {}", ticketId, userId);
    }

    /**
     * Parse attachments JSON string to list
     * @param attachmentsJson The JSON string
     * @return List of attachment URLs
     */
    private List<String> parseAttachments(String attachmentsJson) {
        if (attachmentsJson == null || attachmentsJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(attachmentsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse attachments JSON: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Map TicketMessage entity to MessageResponseDTO
     */
    private MessageResponseDTO mapToMessageDTO(TicketMessage message, List<String> attachments) {
        return MessageResponseDTO.builder()
                .messageId(message.getMessageId())
                .ticketId(message.getTicketId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderType(message.getSenderType())
                .messageBody(message.getMessageBody())
                .isRead(message.getIsRead())
                .attachments(attachments != null ? attachments : new ArrayList<>())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
