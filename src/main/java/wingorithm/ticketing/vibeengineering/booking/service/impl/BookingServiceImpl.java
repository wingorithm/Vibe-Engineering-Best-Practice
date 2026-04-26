// src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImpl.java
package wingorithm.ticketing.vibeengineering.booking.service.impl;

import org.springframework.stereotype.Service;
import wingorithm.ticketing.vibeengineering.booking.model.dto.BookingResponse;
import wingorithm.ticketing.vibeengineering.booking.model.dto.ReserveTicketRequest;
import wingorithm.ticketing.vibeengineering.booking.service.BookingService;

@Service
public class BookingServiceImpl implements BookingService {

    @Override
    public BookingResponse reserveTicket(ReserveTicketRequest request, String idempotencyKey) {
        // TODO: Implementation to follow in the next task
        return null;
    }
}
