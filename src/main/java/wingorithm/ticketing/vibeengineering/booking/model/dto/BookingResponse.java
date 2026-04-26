package wingorithm.ticketing.vibeengineering.booking.model.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID bookingId;
    private String status;
    private LocalDateTime expiresAt;
    private BigDecimal finalPrice;
}
