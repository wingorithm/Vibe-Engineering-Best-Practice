package wingorithm.ticketing.vibeengineering.payment.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingEntity;
import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerEntity;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import wingorithm.ticketing.vibeengineering.exception.PaymentFailedException;
import wingorithm.ticketing.vibeengineering.payment.integration.PaymentGatewayClient;
import wingorithm.ticketing.vibeengineering.payment.integration.dto.PaymentResponse;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentGatewayClient paymentGatewayClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processPayment_Success() {
        EventEntity event = EventEntity.builder().name("Test Event").build();
        CustomerEntity customer = CustomerEntity.builder().name("Test Customer").build();
        BookingEntity booking = BookingEntity.builder()
                .id(UUID.randomUUID())
                .event(event)
                .customer(customer)
                .finalPrice(new BigDecimal("100.00"))
                .build();

        PaymentResponse successResponse = PaymentResponse.builder()
                .status("COMPLETED")
                .transactionId(UUID.randomUUID())
                .receiptUrl("http://receipt.url/")
                .build();

        when(paymentGatewayClient.processPayment(any())).thenReturn(successResponse);

        PaymentResponse result = paymentService.processPayment(booking);

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void processPayment_Failure_ShouldThrowPaymentFailedException() {
        EventEntity event = EventEntity.builder().name("Test Event").build();
        CustomerEntity customer = CustomerEntity.builder().name("Test Customer").build();
        BookingEntity booking = BookingEntity.builder()
                .id(UUID.randomUUID())
                .event(event)
                .customer(customer)
                .finalPrice(new BigDecimal("100.00"))
                .build();

        PaymentResponse failedResponse = PaymentResponse.builder()
                .status("FAILED")
                .error(PaymentResponse.Error.builder().message("Insufficient funds").build())
                .build();

        when(paymentGatewayClient.processPayment(any())).thenReturn(failedResponse);

        assertThrows(PaymentFailedException.class, () -> {
            paymentService.processPayment(booking);
        });
    }

    @Test
    void processPayment_GatewayError_ShouldThrowPaymentFailedException() {
        EventEntity event = EventEntity.builder().name("Test Event").build();
        CustomerEntity customer = CustomerEntity.builder().name("Test Customer").build();
        BookingEntity booking = BookingEntity.builder()
                .id(UUID.randomUUID())
                .event(event)
                .customer(customer)
                .finalPrice(new BigDecimal("100.00"))
                .build();

        when(paymentGatewayClient.processPayment(any())).thenThrow(new RuntimeException("Connection refused"));

        assertThrows(PaymentFailedException.class, () -> {
            paymentService.processPayment(booking);
        });
    }
}
