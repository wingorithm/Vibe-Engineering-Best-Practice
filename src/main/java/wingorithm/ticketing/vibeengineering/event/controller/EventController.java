package wingorithm.ticketing.vibeengineering.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;
import wingorithm.ticketing.vibeengineering.event.model.dto.EventResponse;
import wingorithm.ticketing.vibeengineering.event.service.EventService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Search", description = "API for finding concert events")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Search events", description = "Find events by location, artist, or date with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public BaseResponse<Page<EventResponse>> searchEvents(
            @Parameter(description = "Filter by location") @RequestParam(required = false) String location,
            @Parameter(description = "Filter by artist name") @RequestParam(required = false) String artist,
            @Parameter(description = "Filter by date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        
        Page<EventResponse> result = eventService.searchEvents(location, artist, date, pageable);
        return BaseResponse.success(result);
    }
}
