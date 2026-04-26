package wingorithm.ticketing.vibeengineering.payment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingEntity;
import wingorithm.ticketing.vibeengineering.payment.integration.PaymentGatewayClient;
import wingorithm.ticketing.vibeengineering.payment.integration.dto.PaymentRequest;
import wingorithm.ticketing.vibeengineering.payment.integration.dto.PaymentResponse;
import wingorithm.ticketing.vibeengineering.payment.service.PaymentService;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentGatewayClient paymentGatewayClient;

    @Override
    public PaymentResponse processPayment(BookingEntity booking) {
        // Implementation to follow in a later task
        return PaymentResponse.builder().status("PENDING").build();
    }
}
