package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.model.Maintenance;
import apap.ti._5.accommodation_2306212083_be.repository.MaintenanceRepository;
import apap.ti._5.accommodation_2306212083_be.repository.AccommodationBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final AccommodationBookingRepository bookingRepository;

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
        
        return maintenanceRepository.save(maintenance);
    }

    @Override
    public List<Maintenance> getMaintenanceByRoomId(String roomId) {
        return maintenanceRepository.findByRoom_RoomId(roomId);
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
}
