package wingorithm.ticketing.vibeengineering.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingEntity;
import wingorithm.ticketing.vibeengineering.exception.PaymentFailedException;
import wingorithm.ticketing.vibeengineering.payment.integration.PaymentGatewayClient;
import wingorithm.ticketing.vibeengineering.payment.integration.dto.PaymentRequest;
import wingorithm.ticketing.vibeengineering.payment.integration.dto.PaymentResponse;
import wingorithm.ticketing.vibeengineering.payment.service.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentGatewayClient paymentGatewayClient;

    @Override
    public PaymentResponse processPayment(BookingEntity booking) {
        log.info("Sending payment request for booking ID: {}", booking.getId());

        PaymentRequest request = PaymentRequest.builder()
                .eventName(booking.getEvent().getName())
                .customerName(booking.getCustomer().getName())
                .transactionId(booking.getId())
                .amount(booking.getFinalPrice())
                .currency("IDR") // Assuming fixed currency for now
                .build();

        try {
            PaymentResponse response = paymentGatewayClient.processPayment(request);
            if ("COMPLETED".equals(response.getStatus())) {
                log.info("Payment successful for booking ID: {}", booking.getId());
                return response;
            } else {
                log.warn("Payment failed for booking ID: {}. Reason: {}", booking.getId(), response.getError() != null ? response.getError().getMessage() : "Unknown reason");
                throw new PaymentFailedException(response.getError() != null ? response.getError().getMessage() : "Payment failed");
            }
        } catch (Exception e) {
            log.error("Error during payment processing for booking ID: {}", booking.getId(), e);
            throw new PaymentFailedException("Payment gateway error: " + e.getMessage());
        }
    }
}
