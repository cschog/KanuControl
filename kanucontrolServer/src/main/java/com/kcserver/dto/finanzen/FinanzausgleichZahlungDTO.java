package com.kcserver.dto.finanzen;

import com.kcserver.dto.abrechnung.DokumentDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class FinanzausgleichZahlungDTO {

    private Long id;

    private Long finanzGruppeId;

    private BigDecimal betrag;

    private LocalDate datum;

    private String bemerkung;

    private List<DokumentDTO> dokumente =
            new ArrayList<>();
}