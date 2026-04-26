package wingorithm.ticketing.vibeengineering.event.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import wingorithm.ticketing.vibeengineering.event.model.dto.EventResponse;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import wingorithm.ticketing.vibeengineering.event.model.mapper.EventMapper;
import wingorithm.ticketing.vibeengineering.event.repository.EventRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void searchEvents_returnsPagedEventResponses() {
        // Arrange
        String location = "Tokyo";
        String artist = "Vibe Band";
        LocalDate searchDate = LocalDate.of(2026, 4, 26);
        Pageable pageable = PageRequest.of(0, 10);

        LocalDateTime expectedStart = LocalDateTime.of(2026, 4, 26, 0, 0);
        LocalDateTime expectedEnd = LocalDateTime.of(2026, 4, 27, 0, 0);

        EventEntity entity = EventEntity.builder()
                .id(UUID.randomUUID())
                .name("Vibe Concert")
                .artist(artist)
                .location(location)
                .dateTime(LocalDateTime.of(2026, 4, 26, 19, 0))
                .totalTickets(1000)
                .basePrice(BigDecimal.valueOf(100.00))
                .build();

        EventResponse response = EventResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .artist(entity.getArtist())
                .location(entity.getLocation())
                .dateTime(entity.getDateTime())
                .totalTickets(entity.getTotalTickets())
                .basePrice(entity.getBasePrice())
                .build();

        Page<EventEntity> entityPage = new PageImpl<>(List.of(entity));
        
        when(eventRepository.searchEvents(eq(location), eq(artist), eq(expectedStart), eq(expectedEnd), eq(pageable)))
                .thenReturn(entityPage);
        when(eventMapper.toResponse(any(EventEntity.class))).thenReturn(response);

        // Act
        Page<EventResponse> resultPage = eventService.searchEvents(location, artist, searchDate, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(response, resultPage.getContent().get(0));

        verify(eventRepository).searchEvents(location, artist, expectedStart, expectedEnd, pageable);
        verify(eventMapper).toResponse(entity);
    }
}
