package com.kcserver.dto.zahlungsnachweis;

import com.kcserver.enumtype.Zahlungsweg;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanzGruppeZahlungDTO(
        Long zahlungsnachweisId,
        LocalDate datum,
        BigDecimal betrag,
        Zahlungsweg zahlungsweg,
        String bemerkung,
        Long anzahlDokumente
) {
}