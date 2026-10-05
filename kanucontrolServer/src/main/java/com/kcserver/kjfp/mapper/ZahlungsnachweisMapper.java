package com.kcserver.kjfp.mapper;

import com.kcserver.kjfp.dto.abrechnung.DokumentDTO;
import com.kcserver.kjfp.entity.Dokument;
import com.kcserver.kjfp.entity.beitraege.ZahlungsPosition;
import com.kcserver.kjfp.entity.beitraege.Zahlungsnachweis;
import com.kcserver.kjfp.dto.zahlungsnachweis.ZahlungsPositionDTO;
import com.kcserver.kjfp.dto.zahlungsnachweis.ZahlungsnachweisDetailDTO;
import com.kcserver.kjfp.dto.zahlungsnachweis.ZahlungsnachweisListDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ZahlungsnachweisMapper {

    /* =========================================================
       LIST
       ========================================================= */

    @Mapping(
            target = "finanzGruppeId",
            source = "finanzGruppe.id"
    )
    @Mapping(
            target = "teilnehmer",
            ignore = true
    )
    @Mapping(
            target = "anzahlTeilnehmer",
            expression = """
        java(entity.getPositionen() == null
            ? 0L
            : (long) entity.getPositionen().size())
        """
    )
    @Mapping(
            target = "anzahlDokumente",
            expression = """
            java(entity.getDokumente() == null
                ? 0L
                : (long) entity.getDokumente().size())
            """
    )
    @Mapping(
            target = "rueckzahlung",
            expression = "java(entity.getUrspruenglicherZahlungsnachweis() != null)"
    )
    ZahlungsnachweisListDTO toListDTO(
            Zahlungsnachweis entity
    );

    /* =========================================================
       DETAIL
       ========================================================= */

    @Mapping(
            target = "finanzGruppeId",
            source = "finanzGruppe.id"
    )
    ZahlungsnachweisDetailDTO toDetailDTO(
            Zahlungsnachweis entity
    );

    /* =========================================================
       POSITION
       ========================================================= */

    @Mapping(
            target = "teilnehmerId",
            source = "teilnehmer.id"
    )
    @Mapping(
            target = "vorname",
            source = "teilnehmer.person.vorname"
    )
    @Mapping(
            target = "nachname",
            source = "teilnehmer.person.name"
    )
    ZahlungsPositionDTO toPositionDTO(
            ZahlungsPosition entity
    );

    /* =========================================================
       DOKUMENT
       ========================================================= */

    DokumentDTO toDokumentDTO(
            Dokument entity
    );
}