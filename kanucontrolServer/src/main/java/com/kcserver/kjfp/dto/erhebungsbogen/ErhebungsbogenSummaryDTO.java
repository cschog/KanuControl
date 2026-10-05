package com.kcserver.kjfp.dto.erhebungsbogen;

import java.time.LocalDate;

// Nur Anzeige im UI
public record ErhebungsbogenSummaryDTO(
        Long id,
        Long veranstaltungId,
        boolean abgeschlossen,
        LocalDate stichtag
) {}
