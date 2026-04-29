package wingorithm.ticketing.vibeengineering.customer.model.entity;

import jakarta.persistence.*;
import lombok.*;
import wingorithm.ticketing.vibeengineering.common.model.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_tier", schema = "vibeengineer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerTierEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;
}
