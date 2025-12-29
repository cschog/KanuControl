package com.kcserver.mapper;

import com.kcserver.dto.VeranstaltungDTO;
import com.kcserver.entity.Veranstaltung;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface VeranstaltungMapper {

    @Mapping(source = "verein.id", target = "vereinId")
    @Mapping(source = "leiter.id", target = "leiterId")
    VeranstaltungDTO toDTO(Veranstaltung veranstaltung);
}