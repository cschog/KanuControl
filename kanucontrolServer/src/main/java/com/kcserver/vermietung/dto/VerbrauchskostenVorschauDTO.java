package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Verbrauchsabrechnungsart;
import com.kcserver.vermietung.enumtype.Verbrauchsart;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VerbrauchskostenVorschauDTO(
        Long zaehlerId,
        String zaehlerBezeichnung,
        String bezeichnung,
        Verbrauchsart verbrauchsart,
        Verbrauchsabrechnungsart abrechnungsart,
        String mengeneinheit,
        BigDecimal preis,
        BigDecimal anfangsstand,
        BigDecimal endstand,
        BigDecimal verbrauch,
        LocalDateTime anfangsablesung,
        LocalDateTime endablesung,
        Integer abrechnungstage,
        BigDecimal kosten,
        boolean erfolgreich,
        String hinweis
) {
}