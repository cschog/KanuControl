package com.kcserver.dto.finanz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FinanzSummaryDTO {

    private BigDecimal kosten;
    private BigDecimal einnahmen;
    private BigDecimal saldo;
    private BigDecimal deckung;
    private BigDecimal teilnehmerKostenProPerson;
    private BigDecimal empfohlenerTeilnehmerBeitrag;
}