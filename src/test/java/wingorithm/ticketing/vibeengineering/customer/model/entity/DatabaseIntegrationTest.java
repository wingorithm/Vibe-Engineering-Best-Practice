package wingorithm.ticketing.vibeengineering.customer.model.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;

@SpringBootTest
class DatabaseIntegrationTest {
    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void testFindCustomer() {
        UUID id = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        CustomerEntity customer = entityManager.find(CustomerEntity.class, id);
        assertThat(customer).isNotNull();
        assertThat(customer.getName()).isEqualTo("Alice Lover");
        assertThat(customer.getTier().getName()).isEqualTo("Lovers");
        assertThat(customer.getCreatedAt()).isNotNull();
    }
}
