// src/main/java/wingorithm/ticketing/vibeengineering/booking/service/template/BookingFlowTemplate.java
package wingorithm.ticketing.vibeengineering.booking.service.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Transactional;
import wingorithm.ticketing.vibeengineering.booking.model.dto.BookingResponse;
import wingorithm.ticketing.vibeengineering.booking.model.dto.ReserveTicketRequest;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingEntity;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingStatus;
import wingorithm.ticketing.vibeengineering.booking.model.entity.IdempotencyKeyEntity;
import wingorithm.ticketing.vibeengineering.booking.model.entity.IdempotencyKeyStatus;
import wingorithm.ticketing.vibeengineering.booking.repository.BookingRepository;
import wingorithm.ticketing.vibeengineering.booking.repository.IdempotencyKeyRepository;
import wingorithm.ticketing.vibeengineering.booking.service.PriceCalculatorService;
import wingorithm.ticketing.vibeengineering.customer.repository.CustomerRepository;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import wingorithm.ticketing.vibeengineering.event.repository.EventRepository;
import wingorithm.ticketing.vibeengineering.exception.IdempotencyException;
import wingorithm.ticketing.vibeengineering.exception.PaymentFailedException;
import wingorithm.ticketing.vibeengineering.exception.TicketUnavailableException;
import wingorithm.ticketing.vibeengineering.payment.integration.dto.PaymentResponse;
import wingorithm.ticketing.vibeengineering.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BookingFlowTemplate {

    protected final BookingRepository bookingRepository;
    protected final EventRepository eventRepository;
    protected final CustomerRepository customerRepository;
    protected final IdempotencyKeyRepository idempotencyKeyRepository;
    protected final PriceCalculatorService priceCalculatorService;
    protected final PaymentService paymentService;
    protected final ObjectMapper objectMapper;

    @Transactional
    @SneakyThrows
    public final BookingResponse processBooking(ReserveTicketRequest request, String idempotencyKey) {
        // Idempotency check before starting the transaction
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

        BookingEntity booking = null;
        try {
            // Phase 1: Reserve tickets (first transaction)
            booking = reserve(request, idempotencyKeyEntity);

            // Phase 2: Process payment
            PaymentResponse paymentResponse = processPayment(booking);

            // Phase 3: Confirm booking (second transaction)
            return confirmBooking(booking, paymentResponse);
        } catch (PaymentFailedException e) {
            // Compensation for failed payment
            if (booking != null) {
                compensate(booking, e.getMessage());
            }
            throw e; // Re-throw to inform the caller
        } catch (Exception e) {
            // Handle other unexpected exceptions during the flow
            if (booking != null) {
                compensate(booking, e.getMessage());
            }
            throw e;
        }
    }

    protected abstract BookingEntity reserve(ReserveTicketRequest request, IdempotencyKeyEntity idempotencyKeyEntity);
    protected abstract PaymentResponse processPayment(BookingEntity booking);
    protected abstract BookingResponse confirmBooking(BookingEntity booking, PaymentResponse paymentResponse);
    protected abstract void compensate(BookingEntity booking, String failureReason);
}
