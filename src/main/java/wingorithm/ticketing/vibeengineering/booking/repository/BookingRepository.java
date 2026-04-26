package wingorithm.ticketing.vibeengineering.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingEntity;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {}
