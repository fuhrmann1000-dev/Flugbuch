package de.windenshelter.flugbuch.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import de.windenshelter.flugbuch.dto.FlugbuchEintragDto;
import de.windenshelter.flugbuch.model.StagingMainFlightLog;

@Mapper(componentModel = "spring")
public interface FlugbuchMapper {

    @Mapping(target = "date", source = "datum")
    @Mapping(target = "startTime", source = "startzeit")
    @Mapping(target = "landingTime", source = "landezeit")
    @Mapping(target = "aircraftType", source = "muster")
    @Mapping(target = "registration", source = "kennzeichen")
    @Mapping(target = "guests", source = "gaeste")
    @Mapping(target = "flightType", source = "flugart")
    @Mapping(target = "departureAirfield", source = "startPlatz")
    @Mapping(target = "destinationAirfield", source = "zielPlatz")
    @Mapping(target = "flightDirector", source = "flugLeiter")
    @Mapping(target = "towedAircraft", source = "geschleppter")
    @Mapping(target = "towHeight", source = "schleppHoehe")
    @Mapping(target = "amount", source = "betrag")
    @Mapping(target = "remarks", source = "bemerkung")
    @Mapping(target = "flightCount", source = "flugAnzahl")
    FlugbuchEintragDto toDto(StagingMainFlightLog stagingEintrag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "datum", source = "date")
    @Mapping(target = "startzeit", source = "startTime")
    @Mapping(target = "landezeit", source = "landingTime")
    @Mapping(target = "muster", source = "aircraftType")
    @Mapping(target = "kennzeichen", source = "registration")
    @Mapping(target = "gaeste", source = "guests")
    @Mapping(target = "flugart", source = "flightType")
    @Mapping(target = "startPlatz", source = "departureAirfield")
    @Mapping(target = "zielPlatz", source = "destinationAirfield")
    @Mapping(target = "flugLeiter", source = "flightDirector")
    @Mapping(target = "geschleppter", source = "towedAircraft")
    @Mapping(target = "schleppHoehe", source = "towHeight")
    @Mapping(target = "betrag", source = "amount")
    @Mapping(target = "bemerkung", source = "remarks")
    @Mapping(target = "flugAnzahl", source = "flightCount")
    StagingMainFlightLog toEntity(FlugbuchEintragDto dto);
}