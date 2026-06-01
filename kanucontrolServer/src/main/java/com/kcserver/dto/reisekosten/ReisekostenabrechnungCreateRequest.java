package com.kcserver.dto.reisekosten;

import java.time.LocalDate;

public record ReisekostenabrechnungCreateRequest(

        Long veranstaltungId,
        Long fahrerId,
        LocalDate abrechnungsdatum,
        String bemerkung
) {}