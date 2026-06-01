package com.kcserver.dto.reisekosten;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReisekostenKonfigurationSaveRequest(

        BigDecimal pkwSatz,

        BigDecimal mitfahrerSatz,

        BigDecimal anhaengerSatz,

        LocalDate gueltigVon

) {}