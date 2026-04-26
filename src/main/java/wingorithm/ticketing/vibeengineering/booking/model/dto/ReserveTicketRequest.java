package wingorithm.ticketing.vibeengineering.booking.model.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ReserveTicketRequest {
    private UUID eventId;
    private UUID customerId;
    private Integer quantity;
}
