package com.kcserver.dto.reisekosten;

import java.time.LocalDate;
import java.util.List;

public record ReisekostenabrechnungUpdateRequest(

        LocalDate abrechnungsdatum,

        String bemerkung,
        List<Long> mitfahrerIds,
        List<FahrtabschnittRequest> fahrtabschnitte

) {}
