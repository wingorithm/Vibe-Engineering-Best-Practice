// src/main/java/wingorithm/ticketing/vibeengineering/booking/service/BookingService.java
package wingorithm.ticketing.vibeengineering.booking.service;

import wingorithm.ticketing.vibeengineering.booking.model.dto.BookingResponse;
import wingorithm.ticketing.vibeengineering.booking.model.dto.ReserveTicketRequest;

public interface BookingService {
    BookingResponse reserveTicket(ReserveTicketRequest request, String idempotencyKey);
}
