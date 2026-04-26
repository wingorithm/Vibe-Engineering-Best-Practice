package wingorithm.ticketing.vibeengineering.event.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;
import wingorithm.ticketing.vibeengineering.event.model.dto.EventResponse;
import wingorithm.ticketing.vibeengineering.event.service.EventService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @Test
    void searchEvents_ShouldReturnStandardizedResponse() {
        EventResponse response = EventResponse.builder()
                .location("Jakarta")
                .artist("Alice")
                .name("Test Event")
                .build();
                
        Page<EventResponse> pageResponse = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);
        
        when(eventService.searchEvents(eq("Jakarta"), any(), any(), any()))
                .thenReturn(pageResponse);

        BaseResponse<Page<EventResponse>> result = eventController.searchEvents("Jakarta", null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertNotNull(result.getErrorSchema());
        assertEquals("0000", result.getErrorSchema().getErrorCode());
        assertEquals("Success", result.getErrorSchema().getMessage());
        
        assertNotNull(result.getOutputSchema());
        assertEquals(1, result.getOutputSchema().getContent().size());
        assertEquals("Jakarta", result.getOutputSchema().getContent().get(0).getLocation());
    }

    @Test
    void searchEvents_ByArtist_ShouldReturnCorrectResult() {
        EventResponse response = EventResponse.builder()
                .location("Jakarta")
                .artist("Alice")
                .name("Test Event")
                .build();
                
        Page<EventResponse> pageResponse = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);
        
        when(eventService.searchEvents(any(), eq("Alice"), any(), any()))
                .thenReturn(pageResponse);

        BaseResponse<Page<EventResponse>> result = eventController.searchEvents(null, "Alice", null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertNotNull(result.getOutputSchema());
        assertEquals(1, result.getOutputSchema().getContent().size());
        assertEquals("Alice", result.getOutputSchema().getContent().get(0).getArtist());
    }
}
