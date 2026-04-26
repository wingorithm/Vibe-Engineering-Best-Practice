package wingorithm.ticketing.vibeengineering.event.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    @Query("SELECT e FROM EventEntity e " +
           "WHERE (:location IS NULL OR e.location = :location) " +
           "AND (:artist IS NULL OR e.artist = :artist) " +
           "AND (CAST(:dateStart AS timestamp) IS NULL OR (e.dateTime >= :dateStart AND e.dateTime < :dateEnd))")
    Page<EventEntity> searchEvents(
            @Param("location") String location,
            @Param("artist") String artist,
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EventEntity e WHERE e.id = :id")
    Optional<EventEntity> findByIdWithPessimisticLock(@Param("id") UUID id);
}