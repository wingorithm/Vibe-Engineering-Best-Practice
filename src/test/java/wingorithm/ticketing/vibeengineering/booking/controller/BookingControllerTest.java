// src/test/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingControllerTest.java
package wingorithm.ticketing.vibeengineering.booking.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wingorithm.ticketing.vibeengineering.booking.model.dto.BookingResponse;
import wingorithm.ticketing.vibeengineering.booking.model.dto.ReserveTicketRequest;
import wingorithm.ticketing.vibeengineering.booking.service.BookingService;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @Test
    void reserveTicket_Success() {
        ReserveTicketRequest request = new ReserveTicketRequest();
        String idempotencyKey = UUID.randomUUID().toString();
        BookingResponse bookingResponse = BookingResponse.builder().build();

        when(bookingService.reserveTicket(request, idempotencyKey)).thenReturn(bookingResponse);

        BaseResponse<BookingResponse> response = bookingController.reserveTicket(request, idempotencyKey);

        assertNotNull(response);
        assertEquals("0000", response.getErrorSchema().getErrorCode());
        assertEquals(bookingResponse, response.getOutputSchema());
    }
}
