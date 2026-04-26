package wingorithm.ticketing.vibeengineering.booking.model.dto;

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
public class BookingResponse {
    private UUID bookingId;
    private String status;
    private LocalDateTime expiresAt;
    private BigDecimal finalPrice;
}
