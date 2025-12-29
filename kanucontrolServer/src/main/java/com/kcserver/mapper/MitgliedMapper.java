package com.kcserver.mapper;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.entity.Mitglied;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MitgliedMapper {

    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "verein.id", target = "vereinId")
    @Mapping(source = "verein.name", target = "vereinName")
    @Mapping(source = "verein.abk", target = "vereinAbk")
    MitgliedDTO toDTO(Mitglied mitglied);
}