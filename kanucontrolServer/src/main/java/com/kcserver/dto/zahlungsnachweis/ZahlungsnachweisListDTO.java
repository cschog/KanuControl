package com.kcserver.dto.zahlungsnachweis;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ZahlungsnachweisListDTO {

    private Long id;
    private LocalDate datum;
    private BigDecimal betrag;
    private String bemerkung;
    private Long anzahlTeilnehmer;
    private Long anzahlDokumente;

    public ZahlungsnachweisListDTO(
            Long id,
            LocalDate datum,
            BigDecimal betrag,
            String bemerkung,
            Long anzahlTeilnehmer,
            Long anzahlDokumente
    ) {
        this.id = id;
        this.datum = datum;
        this.betrag = betrag;
        this.bemerkung = bemerkung;
        this.anzahlTeilnehmer = anzahlTeilnehmer;
        this.anzahlDokumente = anzahlDokumente;
    }
}