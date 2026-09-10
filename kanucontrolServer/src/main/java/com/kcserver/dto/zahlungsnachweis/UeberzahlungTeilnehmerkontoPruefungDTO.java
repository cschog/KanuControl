package com.kcserver.dto.zahlungsnachweis;

import java.util.List;
import java.math.BigDecimal;

public record UeberzahlungTeilnehmerkontoPruefungDTO(

        boolean ueberzahlung,
        BigDecimal ueberzahlungsbetrag,
        List<TeilnehmerOhneKontoDTO> teilnehmerOhneKonto,
        boolean gemeinsameFinanzGruppe,
        String finanzGruppeKuerzel
) {
}
