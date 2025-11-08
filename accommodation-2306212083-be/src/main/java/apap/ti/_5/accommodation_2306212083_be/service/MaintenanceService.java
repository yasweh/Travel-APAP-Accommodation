package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.MaintenanceDTO;
import apap.ti._5.accommodation_2306212083_be.model.Maintenance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface MaintenanceService {
    Maintenance addMaintenance(Maintenance maintenance);
    List<MaintenanceDTO> getAllMaintenance();
    List<MaintenanceDTO> getMaintenanceByRoomId(String roomId);
    List<MaintenanceDTO> getMaintenanceByRoomTypeId(String roomTypeId);
    void validateMaintenanceSchedule(String roomId, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime);
    boolean hasMaintenanceConflict(String roomId, LocalDateTime checkIn, LocalDateTime checkOut);
}
