package com.kcserver.dto.kik;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class KikZuschlagCreateUpdateDTO {

    private LocalDate gueltigVon;
    private LocalDate gueltigBis;

    private BigDecimal kikZuschlag;

    private String beschluss;
}