package com.kcserver.dto.reisekosten;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReisekostenKonfigurationResponse(

        Long id,

        BigDecimal pkwSatz,

        BigDecimal mitfahrerSatz,

        BigDecimal anhaengerSatz,

        LocalDate gueltigVon,

        LocalDate gueltigBis

) {}