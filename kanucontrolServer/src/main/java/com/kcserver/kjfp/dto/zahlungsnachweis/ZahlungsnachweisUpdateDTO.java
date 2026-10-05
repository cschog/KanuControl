package com.kcserver.kjfp.dto.zahlungsnachweis;

import com.kcserver.kjfp.enumtype.Zahlungsweg;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ZahlungsnachweisUpdateDTO {

    private LocalDate datum;

    private BigDecimal betrag;

    private Zahlungsweg zahlungsweg;

    private Long finanzGruppeId;

    private String bemerkung;

    private List<ZahlungsPositionDTO> positionen =
            new ArrayList<>();
}