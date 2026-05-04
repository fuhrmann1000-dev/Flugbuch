package de.windenshelter.flugbuch.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import de.windenshelter.flugbuch.dto.FlugbuchEintragDto;
import de.windenshelter.flugbuch.model.StagingSchleppkladdeEintrag;

@Mapper(componentModel = "spring")
public interface FlugbuchMapper {

    FlugbuchEintragDto toDto(StagingSchleppkladdeEintrag stagingEintrag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    StagingSchleppkladdeEintrag toEntity(FlugbuchEintragDto dto);
}