package com.kcserver.dto.foerder;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FoerdersatzCreateUpdateDTO {

    private LocalDate gueltigVon;
    private LocalDate gueltigBis;
    private BigDecimal betragProTeilnehmer;
    private String beschluss;
}