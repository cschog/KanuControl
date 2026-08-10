package com.kcserver.dto.teilnehmer;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeilnehmerBeitragSummaryDTO {

    private Integer anzahlTeilnehmer;

    private Integer bezahlt;

    private Integer teilweise;

    private Integer offen;

    private BigDecimal sollSumme;

    private BigDecimal bezahltSumme;

    private BigDecimal offenSumme;
}