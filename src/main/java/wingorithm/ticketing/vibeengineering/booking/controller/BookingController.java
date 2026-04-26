// src/main/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingController.java
package wingorithm.ticketing.vibeengineering.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import wingorithm.ticketing.vibeengineering.booking.model.dto.BookingResponse;
import wingorithm.ticketing.vibeengineering.booking.model.dto.ReserveTicketRequest;
import wingorithm.ticketing.vibeengineering.booking.service.BookingService;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BaseResponse<BookingResponse> reserveTicket(
            @RequestBody ReserveTicketRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        BookingResponse response = bookingService.reserveTicket(request, idempotencyKey);
        return BaseResponse.success(response);
    }
}
