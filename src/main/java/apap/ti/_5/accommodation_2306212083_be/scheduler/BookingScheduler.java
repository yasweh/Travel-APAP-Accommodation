package apap.ti._5.accommodation_2306212083_be.scheduler;

import apap.ti._5.accommodation_2306212083_be.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingService bookingService;

    /**
     * Auto check-in scheduler  
     * Runs every day at 1 AM (01:00:00)
     * Updates booking status from 1 (Confirmed) to 2 (Done) when checkInDate <= today
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void autoCheckIn() {
        System.out.println("Running auto check-in scheduler...");
        bookingService.autoCheckInBookings();
        System.out.println("Auto check-in completed.");
    }
}
