package com.kcserver.dto.reisekosten;

import java.math.BigDecimal;

public record ReisekostenabrechnungListResponse(

        Long id,

        Long fahrerId,

        String fahrerName,

        Integer gesamtKilometer,

        BigDecimal gesamtBetrag

) {}