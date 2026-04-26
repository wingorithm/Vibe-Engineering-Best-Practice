package wingorithm.ticketing.vibeengineering.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    @Query("SELECT e FROM EventEntity e " +
           "WHERE (:location IS NULL OR e.location = :location) " +
           "AND (:artist IS NULL OR e.artist = :artist) " +
           "AND (CAST(:date AS date) IS NULL OR CAST(e.dateTime AS date) = :date)")
    Page<EventEntity> searchEvents(
            @Param("location") String location,
            @Param("artist") String artist,
            @Param("date") LocalDate date,
            Pageable pageable);
}