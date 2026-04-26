// src/test/java/wingorithm/ticketing/vibeengineering/booking/BookingFlowIntegrationTest.java
package wingorithm.ticketing.vibeengineering.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import wingorithm.ticketing.vibeengineering.booking.controller.BookingController;
import wingorithm.ticketing.vibeengineering.booking.model.dto.BookingResponse;
import wingorithm.ticketing.vibeengineering.booking.model.dto.ReserveTicketRequest;
import wingorithm.ticketing.vibeengineering.booking.service.BookingService;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingFlowIntegrationTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @Test
    void reserveTicket_Success_ReturnsReservedStatus() {
        ReserveTicketRequest request = new ReserveTicketRequest();
        String idempotencyKey = UUID.randomUUID().toString();

        BookingResponse mockBookingResponse = BookingResponse.builder()
                .bookingId(UUID.randomUUID())
                .status("RESERVED")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .finalPrice(new BigDecimal("100.00"))
                .build();

        when(bookingService.reserveTicket(any(ReserveTicketRequest.class), eq(idempotencyKey)))
                .thenReturn(mockBookingResponse);

        BaseResponse<BookingResponse> response = bookingController.reserveTicket(request, idempotencyKey);

        assertNotNull(response);
        assertEquals("0000", response.getErrorSchema().getErrorCode());
        assertEquals("Success", response.getErrorSchema().getMessage());
        assertNotNull(response.getOutputSchema());
        assertEquals("RESERVED", response.getOutputSchema().getStatus());
    }
}
