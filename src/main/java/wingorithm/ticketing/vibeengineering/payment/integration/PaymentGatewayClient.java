package wingorithm.ticketing.vibeengineering.payment.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import wingorithm.ticketing.vibeengineering.payment.integration.dto.PaymentRequest;
import wingorithm.ticketing.vibeengineering.payment.integration.dto.PaymentResponse;

@FeignClient(name = "payment-gateway", url = "http://localhost:9001/api/v1/payment")
public interface PaymentGatewayClient {

    @PostMapping
    PaymentResponse processPayment(@RequestBody PaymentRequest request);
}
