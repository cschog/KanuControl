package com.kcserver.dto.finanz;

public record FinanzGruppeOverviewDTO(
        Long id,
        String kuerzel,
        Long teilnehmerCount,
        Long belegCount
) {}