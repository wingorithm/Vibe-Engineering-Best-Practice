package wingorithm.ticketing.vibeengineering.event.model.dto;

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
public class EventResponse {
    private UUID id;
    private String name;
    private String artist;
    private String location;
    private LocalDateTime dateTime;
    private Integer totalTickets;
    private BigDecimal basePrice;
}
