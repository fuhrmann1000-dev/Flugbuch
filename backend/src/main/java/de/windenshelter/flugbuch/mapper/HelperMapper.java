package de.windenshelter.flugbuch.mapper;

import org.mapstruct.Mapper;

import de.windenshelter.flugbuch.dto.HelperAdminDto;
import de.windenshelter.flugbuch.dto.HelperPublicDto;
import de.windenshelter.flugbuch.model.Helper;

/** Field names already match between {@link Helper} and both DTOs, so no explicit @Mapping is needed. */
@Mapper(componentModel = "spring")
public interface HelperMapper {

    /** Reduced view: first name, competition, skills and availability only - see {@link HelperPublicDto}. */
    HelperPublicDto toPublicDto(Helper helper);

    /** Full record, contact details included - ADMIN-only, see {@link HelperAdminDto}. */
    HelperAdminDto toAdminDto(Helper helper);
}
