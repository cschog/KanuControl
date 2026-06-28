package com.kcserver.mapper;

import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungListDTO;
import com.kcserver.entity.Veranstaltung;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = { PersonMapper.class, VereinMapper.class }
)
public interface VeranstaltungMapper {

    /* =========================
       LIST
       ========================= */

    @Mapping(source = "verein.name", target = "vereinName")
    @Mapping(source = "verein.abk", target = "vereinAbk")
    @Mapping(source = "leiter.name", target = "leiterName")
    @Mapping(source = "leiter.vorname", target = "leiterVorname")
    VeranstaltungListDTO toListDTO(Veranstaltung veranstaltung);

    /* =========================
       DETAIL
       ========================= */

    @Mapping(source = "verein.id", target = "vereinId")
    @Mapping(source = "leiter.id", target = "leiterId")

    @Mapping(source = "unterkunftsart.id", target = "unterkunftsartId")
    @Mapping(source = "unterkunftsart.bezeichnung", target = "unterkunftsartBezeichnung")

    @Mapping(source = "verpflegungsmodell.id", target = "verpflegungsmodellId")
    @Mapping(source = "verpflegungsmodell.bezeichnung", target = "verpflegungsmodellBezeichnung")

    @Mapping(source = "beitragsstruktur.id", target = "beitragsstrukturId")
    @Mapping(source = "beitragsstruktur.name", target = "beitragsstrukturName")

    @Mapping(source = "individuelleGebuehren", target = "individuelleGebuehren")
    @Mapping(source = "standardGebuehr", target = "standardGebuehr")
    @Mapping(source = "scope", target = "scope")
    VeranstaltungDetailDTO toDetailDTO(Veranstaltung veranstaltung);
}