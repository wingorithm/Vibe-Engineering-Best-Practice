package wingorithm.ticketing.vibeengineering.booking.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingEntity;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingStatus;
import wingorithm.ticketing.vibeengineering.booking.repository.BookingRepository;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import wingorithm.ticketing.vibeengineering.event.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationCleanupSchedulerTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ReservationCleanupScheduler scheduler;

    @Test
    void cleanupExpiredReservations() {
        EventEntity event = EventEntity.builder().id(UUID.randomUUID()).availableTickets(9).build();
        BookingEntity expiredBooking = BookingEntity.builder()
                .id(UUID.randomUUID())
                .status(BookingStatus.RESERVED)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .event(event)
                .build();

        when(bookingRepository.findAllByStatusAndExpiresAtBefore(eq(BookingStatus.RESERVED), any(LocalDateTime.class)))
                .thenReturn(List.of(expiredBooking));

        scheduler.cleanupExpiredReservations();

        verify(eventRepository, times(1)).save(event);
        verify(bookingRepository, times(1)).save(expiredBooking);
        assertEquals(10, event.getAvailableTickets());
        assertEquals(BookingStatus.CANCELLED, expiredBooking.getStatus());
    }
}
