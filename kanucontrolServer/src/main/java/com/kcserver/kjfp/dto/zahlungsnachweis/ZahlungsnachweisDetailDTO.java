package com.kcserver.kjfp.dto.zahlungsnachweis;

import com.kcserver.kjfp.dto.abrechnung.DokumentDTO;
import com.kcserver.kjfp.enumtype.Zahlungsweg;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ZahlungsnachweisDetailDTO {

    private Long id;

    private LocalDate datum;

    private BigDecimal betrag;

    private Zahlungsweg zahlungsweg;

    private String bemerkung;

    private Long finanzGruppeId;

    private List<ZahlungsPositionDTO> positionen =
            new ArrayList<>();

    private List<DokumentDTO> dokumente =
            new ArrayList<>();
}