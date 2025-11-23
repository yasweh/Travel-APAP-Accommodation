package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.support.AddProgressRequestDTO;
import apap.ti._5.accommodation_2306212083_be.dto.support.ProgressResponseDTO;
import apap.ti._5.accommodation_2306212083_be.exception.TicketNotFoundException;
import apap.ti._5.accommodation_2306212083_be.exception.UnauthorizedTicketAccessException;
import apap.ti._5.accommodation_2306212083_be.model.SupportProgress;
import apap.ti._5.accommodation_2306212083_be.model.SupportTicket;
import apap.ti._5.accommodation_2306212083_be.repository.SupportProgressRepository;
import apap.ti._5.accommodation_2306212083_be.repository.SupportTicketRepository;
import apap.ti._5.accommodation_2306212083_be.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing support ticket progress updates.
 * Only admins and vendors can add progress.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SupportProgressService {

    private final SupportProgressRepository supportProgressRepository;
    private final SupportTicketRepository supportTicketRepository;

    /**
     * Add progress update to a ticket (Admin/Vendor only)
     * @param ticketId The ticket ID
     * @param request The progress request
     * @return Created progress response
     */
    public ProgressResponseDTO addProgress(String ticketId, AddProgressRequestDTO request) {
        // Only admins and vendors can add progress
        if (!CurrentUser.isSuperadmin() && !CurrentUser.isOwner()) {
            throw new UnauthorizedTicketAccessException("Only admins and vendors can add progress updates");
        }

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        Long userId = CurrentUser.getUserId();
        String userName = CurrentUser.getName();
        String role = CurrentUser.getRole();

        // Create progress
        SupportProgress progress = SupportProgress.builder()
                .supportTicket(ticket)
                .userId(userId)
                .userName(userName)
                .role(role)
                .text(request.getText())
                .actionType(request.getActionType())
                .build();

        progress = supportProgressRepository.save(progress);
        
        log.info("Progress added to ticket {} by {} ({}): {}", 
                ticketId, userName, role, request.getActionType());

        return mapToProgressDTO(progress);
    }

    /**
     * Get all progress updates for a ticket
     * @param ticketId The ticket ID
     * @return List of progress updates
     */
    @Transactional(readOnly = true)
    public List<ProgressResponseDTO> getProgress(String ticketId) {
        // Verify ticket exists
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        // Verify user has access
        Long userId = CurrentUser.getUserId();
        if (!CurrentUser.isSuperadmin() && !ticket.getUserId().equals(userId)) {
            throw new UnauthorizedTicketAccessException(ticketId);
        }

        List<SupportProgress> progressList = supportProgressRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        
        return progressList.stream()
                .map(this::mapToProgressDTO)
                .collect(Collectors.toList());
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
}
