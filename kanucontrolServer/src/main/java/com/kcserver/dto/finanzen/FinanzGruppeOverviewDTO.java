package com.kcserver.dto.finanzen;

import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;

import java.util.List;

public record FinanzGruppeOverviewDTO(
        Long id,
        String kuerzel,
        boolean system,
        List<TeilnehmerKurzDTO> teilnehmer,
        long belegCount
) {}