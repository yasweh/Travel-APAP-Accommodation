package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.MaintenanceDTO;
import apap.ti._5.accommodation_2306212083_be.model.Maintenance;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.MaintenanceRepository;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final AccommodationBookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    @Override
    public Maintenance addMaintenance(Maintenance maintenance) {
        // Validate schedule first
        validateMaintenanceSchedule(
            maintenance.getRoom().getRoomId(),
            maintenance.getStartDate(),
            maintenance.getStartTime(),
            maintenance.getEndDate(),
            maintenance.getEndTime()
        );
        
        // Set room availability to 0 (Not Available) during maintenance
        Room room = maintenance.getRoom();
        room.setAvailabilityStatus(0);
        room.setMaintenanceStart(LocalDateTime.of(maintenance.getStartDate(), maintenance.getStartTime()));
        room.setMaintenanceEnd(LocalDateTime.of(maintenance.getEndDate(), maintenance.getEndTime()));
        room.setUpdatedDate(LocalDateTime.now());
        roomRepository.save(room);
        
        return maintenanceRepository.save(maintenance);
    }

    @Override
    public List<MaintenanceDTO> getAllMaintenance() {
        return maintenanceRepository.findByActiveStatus(1).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceDTO> getMaintenanceByRoomId(String roomId) {
        return maintenanceRepository.findByRoom_RoomId(roomId).stream()
            .filter(m -> m.getActiveStatus() == 1)
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceDTO> getMaintenanceByRoomTypeId(String roomTypeId) {
        return maintenanceRepository.findByRoom_RoomType_RoomTypeId(roomTypeId).stream()
            .filter(m -> m.getActiveStatus() == 1)
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void validateMaintenanceSchedule(String roomId, LocalDate startDate, LocalTime startTime, 
                                            LocalDate endDate, LocalTime endTime) {
        // Validate date range
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }
        
        if (endDate.equals(startDate) && endTime.isBefore(startTime)) {
            throw new RuntimeException("End time cannot be before start time on the same day");
        }
        
        // Check for overlapping maintenance
        if (maintenanceRepository.existsOverlappingMaintenance(roomId, startDate, startTime, endDate, endTime)) {
            throw new RuntimeException("Maintenance schedule overlaps with existing maintenance");
        }
        
        // Check for booking conflicts
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
        
        if (bookingRepository.existsBookingConflict(roomId, startDateTime, endDateTime)) {
            throw new RuntimeException("Maintenance schedule conflicts with existing bookings");
        }
    }

    @Override
    public boolean hasMaintenanceConflict(String roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        // Convert LocalDateTime to LocalDate and LocalTime for maintenance check
        LocalDate checkInDate = checkIn.toLocalDate();
        LocalTime checkInTime = checkIn.toLocalTime();
        LocalDate checkOutDate = checkOut.toLocalDate();
        LocalTime checkOutTime = checkOut.toLocalTime();
        
        return maintenanceRepository.existsOverlappingMaintenance(
            roomId, checkInDate, checkInTime, checkOutDate, checkOutTime
        );
    }

    private MaintenanceDTO mapToDTO(Maintenance maintenance) {
        Room room = maintenance.getRoom();
        RoomType roomType = room.getRoomType();
        
        return MaintenanceDTO.builder()
            .maintenanceId(maintenance.getId())
            .roomId(room.getRoomId())
            .roomName(room.getName())
            .roomTypeId(roomType.getRoomTypeId())
            .roomTypeName(roomType.getName())
            .propertyId(roomType.getProperty().getPropertyId())
            .propertyName(roomType.getProperty().getPropertyName())
            .startDate(maintenance.getStartDate())
            .startTime(maintenance.getStartTime())
            .endDate(maintenance.getEndDate())
            .endTime(maintenance.getEndTime())
            .build();
    }
}
