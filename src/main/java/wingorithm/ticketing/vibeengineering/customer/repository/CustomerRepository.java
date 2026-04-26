package wingorithm.ticketing.vibeengineering.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerEntity;

import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
}
