package wingorithm.ticketing.vibeengineering.event.model.mapper;

import org.mapstruct.Mapper;
import wingorithm.ticketing.vibeengineering.event.model.dto.EventResponse;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventResponse toResponse(EventEntity entity);
}
