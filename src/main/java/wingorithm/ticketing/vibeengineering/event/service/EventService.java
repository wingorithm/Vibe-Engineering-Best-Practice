package wingorithm.ticketing.vibeengineering.event.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import wingorithm.ticketing.vibeengineering.event.model.dto.EventResponse;

import java.time.LocalDate;

public interface EventService {
    Page<EventResponse> searchEvents(String location, String artist, LocalDate date, Pageable pageable);
}
