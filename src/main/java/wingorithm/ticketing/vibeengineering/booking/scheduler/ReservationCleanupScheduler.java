package wingorithm.ticketing.vibeengineering.booking.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingEntity;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingStatus;
import wingorithm.ticketing.vibeengineering.booking.repository.BookingRepository;
import wingorithm.ticketing.vibeengineering.event.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupScheduler {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    @Scheduled(fixedRate = 60000) // Runs every minute
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Running reservation cleanup job...");
        List<BookingEntity> expiredBookings = bookingRepository.findAllByStatusAndExpiresAtBefore(BookingStatus.RESERVED, LocalDateTime.now());

        for (BookingEntity booking : expiredBookings) {
            log.info("Expiring booking ID: {}", booking.getId());
            var event = booking.getEvent();
            event.setAvailableTickets(event.getAvailableTickets() + 1); // Assuming 1 ticket per booking for now
            eventRepository.save(event);

            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }
        log.info("Finished reservation cleanup job. Expired {} reservations.", expiredBookings.size());
    }
}
