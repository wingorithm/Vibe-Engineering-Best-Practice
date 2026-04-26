package wingorithm.ticketing.vibeengineering.payment.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PaymentRequest {
    @JsonProperty("event_name")
    private String eventName;
    @JsonProperty("customer_name")
    private String customerName;
    @JsonProperty("trx_id")
    private UUID transactionId;
    private BigDecimal amount;
    private String currency;
}
