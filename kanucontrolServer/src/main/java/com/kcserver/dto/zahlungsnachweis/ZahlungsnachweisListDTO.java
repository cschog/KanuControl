package com.kcserver.dto.zahlungsnachweis;

import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;
import com.kcserver.enumtype.Zahlungsweg;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ZahlungsnachweisListDTO {

    private Long id;
    private LocalDate datum;
    private BigDecimal betrag;
    private String bemerkung;
    private Zahlungsweg zahlungsweg;
    private Long finanzGruppeId;
    private Long anzahlTeilnehmer;
    private Long anzahlDokumente;
    private boolean rueckzahlung;

    private List<TeilnehmerKurzDTO> teilnehmer;

    public ZahlungsnachweisListDTO(
            Long id,
            LocalDate datum,
            BigDecimal betrag,
            String bemerkung,
            Zahlungsweg zahlungsweg,
            Long finanzGruppeId,
            Long anzahlTeilnehmer,
            Long anzahlDokumente,
            boolean rueckzahlung
    ) {
        this.id = id;
        this.datum = datum;
        this.betrag = betrag;
        this.bemerkung = bemerkung;
        this.zahlungsweg = zahlungsweg;
        this.finanzGruppeId = finanzGruppeId;
        this.anzahlTeilnehmer = anzahlTeilnehmer;
        this.anzahlDokumente = anzahlDokumente;
        this.rueckzahlung = rueckzahlung;
        this.teilnehmer = new ArrayList<>();
    }
}