package wingorithm.ticketing.vibeengineering.event.model.entity;

import jakarta.persistence.*;
import lombok.*;
import wingorithm.ticketing.vibeengineering.common.model.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event", schema = "vibeengineer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEntity extends BaseEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String artist;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "total_tickets", nullable = false)
    private Integer totalTickets;

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;
}