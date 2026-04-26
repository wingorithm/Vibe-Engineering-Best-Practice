package wingorithm.ticketing.vibeengineering.customer.model.entity;

import jakarta.persistence.*;
import lombok.*;
import wingorithm.ticketing.vibeengineering.common.model.entity.BaseEntity;
import java.util.UUID;

@Entity
@Table(name = "customer", schema = "vibeengineer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity extends BaseEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id")
    private CustomerTierEntity tier;
}
