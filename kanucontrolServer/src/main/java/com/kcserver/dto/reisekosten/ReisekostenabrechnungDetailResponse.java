package com.kcserver.dto.reisekosten;

import com.kcserver.dto.person.PersonRefDTO;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

public record ReisekostenabrechnungDetailResponse(

        Long id,
        Long veranstaltungId,
        String veranstaltungName,
        Long fahrerId,
        String fahrerName,
        LocalDate abrechnungsdatum,
        Integer gesamtKilometer,
        BigDecimal gesamtBetrag,
        String bemerkung,
        List<PersonRefDTO> mitfahrer,
        List<FahrtabschnittResponse> fahrtabschnitte

) {}
