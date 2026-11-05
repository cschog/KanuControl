package com.kcserver.core.mapper;

import com.kcserver.kjfp.mapper.UnterkunftsartMapper;
import com.kcserver.kjfp.mapper.VereinMapper;
import com.kcserver.kjfp.mapper.VerpflegungsmodellMapper;

import com.kcserver.core.mapper.PersonMapper;
import com.kcserver.core.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.core.dto.veranstaltung.VeranstaltungListDTO;
import com.kcserver.core.entity.Veranstaltung;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {
                PersonMapper.class,
                VereinMapper.class,
                UnterkunftsartMapper.class,
                VerpflegungsmodellMapper.class
        }
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

    @Mapping(
            source = "leiter",
            target = "leiter",
            qualifiedByName = "toDetailDTOWithoutMitgliedschaften"
    )

    @Mapping(
            source = "verein",
            target = "verein",
            qualifiedByName = "toRefDTO"
    )

    @Mapping(source = "beitragsstruktur.id", target = "beitragsstrukturId")
    @Mapping(source = "beitragsstruktur.name", target = "beitragsstrukturName")
    @Mapping(source = "individuelleGebuehren", target = "individuelleGebuehren")
    @Mapping(source = "standardGebuehr", target = "standardGebuehr")
    @Mapping(source = "scope", target = "scope")
    @Mapping(source = "kjfpAktiv", target = "kjfpAktiv")
    @Mapping(source = "fahrtenAktiv", target = "fahrtenAktiv")
    VeranstaltungDetailDTO toDetailDTO(Veranstaltung veranstaltung);
}
