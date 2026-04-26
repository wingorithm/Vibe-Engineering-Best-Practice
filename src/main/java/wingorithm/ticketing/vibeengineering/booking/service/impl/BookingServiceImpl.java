// src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImpl.java
package wingorithm.ticketing.vibeengineering.booking.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import wingorithm.ticketing.vibeengineering.booking.service.template.BookingFlowTemplate;
import wingorithm.ticketing.vibeengineering.customer.repository.CustomerRepository;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import wingorithm.ticketing.vibeengineering.event.repository.EventRepository;
import wingorithm.ticketing.vibeengineering.exception.PaymentFailedException;
import wingorithm.ticketing.vibeengineering.exception.TicketUnavailableException;
import wingorithm.ticketing.vibeengineering.payment.integration.dto.PaymentResponse;
import wingorithm.ticketing.vibeengineering.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class BookingServiceImpl extends BookingFlowTemplate implements BookingService {

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            CustomerRepository customerRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            PriceCalculatorService priceCalculatorService,
            PaymentService paymentService,
            ObjectMapper objectMapper) {
        super(bookingRepository, eventRepository, customerRepository, idempotencyKeyRepository, priceCalculatorService, paymentService, objectMapper);
    }

    @Override
    @Transactional // This transaction is for the reservation part
    protected BookingEntity reserve(ReserveTicketRequest request, IdempotencyKeyEntity idempotencyKeyEntity) {
        EventEntity event = eventRepository.findByIdWithPessimisticLock(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getAvailableTickets() < request.getQuantity()) {
            throw new TicketUnavailableException("Not enough tickets available.");
        }

        event.setAvailableTickets(event.getAvailableTickets() - request.getQuantity());
        eventRepository.save(event);

        var customer = customerRepository.findById(request.getCustomerId()).orElseThrow(() -> new RuntimeException("Customer not found"));

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
                .idempotencyKey(idempotencyKeyEntity.getKey())
                .build();
        return bookingRepository.save(booking);
    }

    @Override
    protected PaymentResponse processPayment(BookingEntity booking) {
        // This will be implemented in a later task after PaymentService is complete
        log.info("Processing payment for booking ID: {}", booking.getId());
        return paymentService.processPayment(booking);
    }

    @Override
    @Transactional // This transaction is for confirming the booking after payment
    protected BookingResponse confirmBooking(BookingEntity booking, PaymentResponse paymentResponse) {
        booking.setStatus(BookingStatus.SOLD);
        booking.setPaymentTransactionId(paymentResponse.getTransactionId());
        booking.setReceiptUrl(paymentResponse.getReceiptUrl());
        bookingRepository.save(booking);

        // Update idempotency key status
        idempotencyKeyRepository.findById(booking.getIdempotencyKey()).ifPresent(idempotencyKeyEntity -> {
            idempotencyKeyEntity.setStatus(IdempotencyKeyStatus.COMPLETED);
            try {
                idempotencyKeyEntity.setResponseBody(objectMapper.writeValueAsString(BookingResponse.builder()
                    .bookingId(booking.getId())
                    .status(booking.getStatus().name())
                    .expiresAt(booking.getExpiresAt())
                    .finalPrice(booking.getFinalPrice())
                    .build()));
            } catch (Exception e) {
                log.error("Error serializing response body for idempotency key", e);
            }
            idempotencyKeyRepository.save(idempotencyKeyEntity);
        });

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus().name())
                .expiresAt(booking.getExpiresAt())
                .finalPrice(booking.getFinalPrice())
                .build();
    }

    @Override
    @Transactional // This transaction is for compensating a failed payment
    protected void compensate(BookingEntity booking, String failureReason) {
        log.warn("Payment failed for booking ID: {}. Compensating...", booking.getId());

        // Revert ticket count
        var event = booking.getEvent();
        event.setAvailableTickets(event.getAvailableTickets() + booking.getTotalPrice().intValue()); // Assuming quantity is 1 for now
        eventRepository.save(event);

        // Mark booking as cancelled
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentFailureReason(failureReason);
        bookingRepository.save(booking);

        // Update idempotency key status to FAILED
        idempotencyKeyRepository.findById(booking.getIdempotencyKey()).ifPresent(idempotencyKeyEntity -> {
            idempotencyKeyEntity.setStatus(IdempotencyKeyStatus.FAILED);
            idempotencyKeyEntity.setResponseBody("Payment failed: " + failureReason);
            idempotencyKeyRepository.save(idempotencyKeyEntity);
        });
    }

    @Override
    public BookingResponse reserveTicket(ReserveTicketRequest request, String idempotencyKey) {
        return processBooking(request, idempotencyKey);
    }
}
