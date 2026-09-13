package com.kcserver.dto.finanzen;


import com.kcserver.enumtype.FinanzausgleichBeitragsstatus;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinanzausgleichDTO {

    private Long finanzGruppeId;
    private String finanzGruppeKuerzel;

    private BigDecimal teilnehmerBeitraegeSoll;

    private BigDecimal teilnehmerBeitraegeUeberweisung;
    private BigDecimal teilnehmerBeitraegeQuittung;

    private BigDecimal ausgaben;
    private BigDecimal fahrkosten;

    private BigDecimal erstattungVomVK;

    private FinanzausgleichBeitragsstatus beitragsstatus;
}
