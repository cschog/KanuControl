package com.kcserver.kjfp.dto.reisekosten;

import java.time.LocalDate;
import java.util.List;

public record ReisekostenabrechnungUpdateRequest(

        LocalDate abrechnungsdatum,

        Long finanzGruppeId,
        String bemerkung,
        List<Long> mitfahrerIds,
        List<FahrtabschnittRequest> fahrtabschnitte

) {}
