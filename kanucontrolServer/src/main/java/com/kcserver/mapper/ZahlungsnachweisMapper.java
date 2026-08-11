package com.kcserver.mapper;

import com.kcserver.dto.zahlungsnachweis.*;
import com.kcserver.entity.ZahlungsPosition;
import com.kcserver.entity.Zahlungsnachweis;
import com.kcserver.entity.ZahlungsnachweisDokument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ZahlungsnachweisMapper {

    /* =========================================================
       LIST
       ========================================================= */

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
    ZahlungsnachweisListDTO toListDTO(
            Zahlungsnachweis entity
    );

    /* =========================================================
       DETAIL
       ========================================================= */

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

    ZahlungsnachweisDokumentDTO toDokumentDTO(
            ZahlungsnachweisDokument entity
    );
}