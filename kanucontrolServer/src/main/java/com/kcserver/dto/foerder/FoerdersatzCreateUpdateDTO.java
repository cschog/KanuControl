package com.kcserver.dto.foerder;

import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FoerdersatzCreateUpdateDTO {

    private LocalDate gueltigVon;
    private LocalDate gueltigBis;
    private VeranstaltungTyp typ;
    private BigDecimal foerdersatz;
    private BigDecimal foerderdeckel;
    private String beschluss;
}