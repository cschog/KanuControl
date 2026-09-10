package com.kcserver.dto.zahlungsnachweis;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OffeneUeberzahlungDTO(

        Long zahlungsnachweisId,
        LocalDate datum,
        BigDecimal urspruenglicherBetrag,
        BigDecimal zugeordnet,
        BigDecimal bereitsZurueckgezahlt,
        BigDecimal offeneUeberzahlung,
        String bemerkung,

        Long finanzGruppeId,
        String finanzGruppeKuerzel
) {
}