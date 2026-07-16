package com.kcserver.dto.finanzen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanzSummaryDTO {

    private BigDecimal kosten;
    private BigDecimal einnahmen;
    private BigDecimal saldo;
    private BigDecimal deckung;
    private BigDecimal teilnehmerKostenProPerson;
    private BigDecimal empfohlenerTeilnehmerBeitrag;
    private BigDecimal kjfpZuschuss;
}