package com.kcserver.kjfp.dto.reisekosten;

import java.math.BigDecimal;
import java.util.List;

public record ReisekostenabrechnungListResponse(

        Long id,
        Long finanzGruppeId,
        String belegnummer,
        Long fahrerId,
        String fahrerName,
        Integer gesamtKilometer,
        BigDecimal gesamtBetrag,

        boolean druckbar,
        List<String> fehler

) {}