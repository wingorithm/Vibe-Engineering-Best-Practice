package wingorithm.ticketing.vibeengineering.booking.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wingorithm.ticketing.vibeengineering.booking.model.dto.BookingResponse;
import wingorithm.ticketing.vibeengineering.booking.model.dto.ReserveTicketRequest;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingEntity;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingStatus;
import wingorithm.ticketing.vibeengineering.booking.model.entity.IdempotencyKeyEntity;
import wingorithm.ticketing.vibeengineering.booking.model.entity.IdempotencyKeyStatus;
import wingorithm.ticketing.vibeengineering.booking.repository.BookingRepository;
import wingorithm.ticketing.vibeengineering.booking.repository.IdempotencyKeyRepository;
import wingorithm.ticketing.vibeengineering.booking.service.BookingService;
import wingorithm.ticketing.vibeengineering.booking.service.PriceCalculatorService;
import wingorithm.ticketing.vibeengineering.customer.repository.CustomerRepository;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import wingorithm.ticketing.vibeengineering.event.repository.EventRepository;
import wingorithm.ticketing.vibeengineering.exception.IdempotencyException;
import wingorithm.ticketing.vibeengineering.exception.TicketUnavailableException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final CustomerRepository customerRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final PriceCalculatorService priceCalculatorService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    @SneakyThrows
    public BookingResponse reserveTicket(ReserveTicketRequest request, String idempotencyKey) {
        Optional<IdempotencyKeyEntity> idempotencyKeyOpt = idempotencyKeyRepository.findById(idempotencyKey);

        if (idempotencyKeyOpt.isPresent()) {
            IdempotencyKeyEntity key = idempotencyKeyOpt.get();
            if (key.getStatus() == IdempotencyKeyStatus.COMPLETED) {
                return objectMapper.readValue(key.getResponseBody(), BookingResponse.class);
            }
            if (key.getStatus() == IdempotencyKeyStatus.PENDING &&
                key.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
                throw new IdempotencyException("Request with key " + idempotencyKey + " is already processing.");
            }
        }

        IdempotencyKeyEntity idempotencyKeyEntity = idempotencyKeyOpt.orElseGet(() ->
            IdempotencyKeyEntity.builder()
                .key(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build()
        );
        idempotencyKeyEntity.setStatus(IdempotencyKeyStatus.PENDING);
        idempotencyKeyRepository.save(idempotencyKeyEntity);

        EventEntity event = eventRepository.findByIdWithPessimisticLock(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getAvailableTickets() < request.getQuantity()) {
            throw new TicketUnavailableException("Not enough tickets available.");
        }

        event.setAvailableTickets(event.getAvailableTickets() - request.getQuantity());
        eventRepository.save(event);

        var customer = customerRepository.findById(request.getCustomerId()).orElseThrow(()-> new RuntimeException("Customer not found"));

        BigDecimal finalPrice = priceCalculatorService.calculateFinalPrice(event, customer, request.getQuantity());

        BookingEntity booking = BookingEntity.builder()
                .id(UUID.randomUUID())
                .event(event)
                .customer(customer)
                .status(BookingStatus.RESERVED)
                .reservedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .totalPrice(event.getBasePrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                .tierDiscountAmount(event.getBasePrice().multiply(BigDecimal.valueOf(request.getQuantity())).subtract(finalPrice))
                .finalPrice(finalPrice)
                .idempotencyKey(idempotencyKey)
                .build();
        bookingRepository.save(booking);

        BookingResponse response = BookingResponse.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus().name())
                .expiresAt(booking.getExpiresAt())
                .finalPrice(booking.getFinalPrice())
                .build();
        
        idempotencyKeyEntity.setStatus(IdempotencyKeyStatus.COMPLETED);
        idempotencyKeyEntity.setResponseBody(objectMapper.writeValueAsString(response));
        idempotencyKeyRepository.save(idempotencyKeyEntity);

        return response;
    }
}
