package wingorithm.ticketing.vibeengineering.payment.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    @JsonProperty("transaction_id")
    private UUID transactionId;
    private String status;
    private BigDecimal amount;
    private String currency;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("receipt_url")
    private String receiptUrl;
    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;
    private Error error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethod {
        private String type;
        @JsonProperty("card_brand")
        private String cardBrand;
        @JsonProperty("last_four")
        private String lastFour;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Error {
        private String code;
        private String message;
        @JsonProperty("decline_code")
        private String declineCode;
    }
}
