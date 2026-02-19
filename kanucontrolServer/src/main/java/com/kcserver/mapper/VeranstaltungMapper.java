package com.kcserver.mapper;

import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungListDTO;
import com.kcserver.entity.Veranstaltung;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface VeranstaltungMapper {

    /* =========================
       LIST
       ========================= */

    @Mapping(source = "verein.name", target = "vereinName")
    @Mapping(source = "leiter.name", target = "leiterName")
    @Mapping(source = "leiter.vorname", target = "leiterVorname")
    VeranstaltungListDTO toListDTO(Veranstaltung veranstaltung);

    /* =========================
       DETAIL
       ========================= */

    @Mapping(source = "verein.id", target = "vereinId")
    @Mapping(source = "leiter.id", target = "leiterId")
    @Mapping(source = "individuelleGebuehren", target = "individuelleGebuehren")
    @Mapping(source = "standardGebuehr", target = "standardGebuehr")
    VeranstaltungDetailDTO toDetailDTO(Veranstaltung veranstaltung);
}