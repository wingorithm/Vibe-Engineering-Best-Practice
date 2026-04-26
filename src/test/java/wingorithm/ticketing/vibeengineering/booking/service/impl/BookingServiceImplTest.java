package wingorithm.ticketing.vibeengineering.booking.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import wingorithm.ticketing.vibeengineering.booking.model.dto.BookingResponse;
import wingorithm.ticketing.vibeengineering.booking.model.dto.ReserveTicketRequest;
import wingorithm.ticketing.vibeengineering.booking.model.entity.IdempotencyKeyEntity;
import wingorithm.ticketing.vibeengineering.booking.model.entity.IdempotencyKeyStatus;
import wingorithm.ticketing.vibeengineering.booking.repository.BookingRepository;
import wingorithm.ticketing.vibeengineering.booking.repository.IdempotencyKeyRepository;
import wingorithm.ticketing.vibeengineering.booking.service.PriceCalculatorService;
import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerEntity;
import wingorithm.ticketing.vibeengineering.customer.repository.CustomerRepository;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import wingorithm.ticketing.vibeengineering.event.repository.EventRepository;
import wingorithm.ticketing.vibeengineering.exception.TicketUnavailableException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock
    private PriceCalculatorService priceCalculatorService;
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void reserveTicket_Success() {
        ReserveTicketRequest request = new ReserveTicketRequest();
        request.setEventId(UUID.randomUUID());
        request.setCustomerId(UUID.randomUUID());
        request.setQuantity(1);
        String idempotencyKey = UUID.randomUUID().toString();

        EventEntity event = EventEntity.builder().id(request.getEventId()).availableTickets(10).basePrice(new BigDecimal("100.00")).build();
        CustomerEntity customer = CustomerEntity.builder().id(request.getCustomerId()).build();

        when(idempotencyKeyRepository.findById(idempotencyKey)).thenReturn(Optional.empty());
        when(idempotencyKeyRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(eventRepository.findByIdWithPessimisticLock(request.getEventId())).thenReturn(Optional.of(event));
        when(customerRepository.findById(request.getCustomerId())).thenReturn(Optional.of(customer));
        when(priceCalculatorService.calculateFinalPrice(any(), any(), anyInt())).thenReturn(new BigDecimal("100.00"));
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        BookingResponse response = bookingService.reserveTicket(request, idempotencyKey);

        assertNotNull(response);
    }

    @Test
    void reserveTicket_NoTicketsAvailable_ShouldThrowException() {
        ReserveTicketRequest request = new ReserveTicketRequest();
        request.setEventId(UUID.randomUUID());
        request.setCustomerId(UUID.randomUUID());
        request.setQuantity(1);
        String idempotencyKey = UUID.randomUUID().toString();

        EventEntity event = EventEntity.builder().id(request.getEventId()).availableTickets(0).build();

        when(idempotencyKeyRepository.findById(idempotencyKey)).thenReturn(Optional.empty());
        when(idempotencyKeyRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(eventRepository.findByIdWithPessimisticLock(request.getEventId())).thenReturn(Optional.of(event));

        assertThrows(TicketUnavailableException.class, () -> {
            bookingService.reserveTicket(request, idempotencyKey);
        });
    }

    @Test
    void reserveTicket_IdempotencyKeyExists_ShouldReturnCachedResponse() throws Exception {
        ReserveTicketRequest request = new ReserveTicketRequest();
        request.setEventId(UUID.randomUUID());
        String idempotencyKey = UUID.randomUUID().toString();

        BookingResponse cachedResponse = BookingResponse.builder().bookingId(UUID.randomUUID()).build();
        IdempotencyKeyEntity idempotencyKeyEntity = IdempotencyKeyEntity.builder()
                .key(idempotencyKey)
                .status(IdempotencyKeyStatus.COMPLETED)
                .responseBody(objectMapper.writeValueAsString(cachedResponse))
                .build();

        when(idempotencyKeyRepository.findById(idempotencyKey)).thenReturn(Optional.of(idempotencyKeyEntity));

        BookingResponse response = bookingService.reserveTicket(request, idempotencyKey);

        assertNotNull(response);
        assertEquals(cachedResponse.getBookingId(), response.getBookingId());
    }
}
