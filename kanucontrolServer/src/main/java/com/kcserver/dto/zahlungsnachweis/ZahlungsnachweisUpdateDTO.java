package com.kcserver.dto.zahlungsnachweis;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ZahlungsnachweisUpdateDTO {

    private LocalDate datum;

    private BigDecimal betrag;

    private String bemerkung;

    private List<ZahlungsPositionDTO> positionen =
            new ArrayList<>();
}