package wingorithm.ticketing.vibeengineering.event.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wingorithm.ticketing.vibeengineering.event.model.dto.EventResponse;
import wingorithm.ticketing.vibeengineering.event.model.mapper.EventMapper;
import wingorithm.ticketing.vibeengineering.event.repository.EventRepository;
import wingorithm.ticketing.vibeengineering.event.service.EventService;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> searchEvents(String location, String artist, LocalDate date, Pageable pageable) {
        java.time.LocalDateTime dateStart = null;
        java.time.LocalDateTime dateEnd = null;
        if (date != null) {
            dateStart = date.atStartOfDay();
            dateEnd = date.plusDays(1).atStartOfDay();
        }
        return eventRepository.searchEvents(location, artist, dateStart, dateEnd, pageable)
                .map(eventMapper::toResponse);
    }
}
