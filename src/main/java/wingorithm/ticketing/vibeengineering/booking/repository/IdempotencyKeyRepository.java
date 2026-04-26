package wingorithm.ticketing.vibeengineering.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wingorithm.ticketing.vibeengineering.booking.model.entity.IdempotencyKeyEntity;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {}
