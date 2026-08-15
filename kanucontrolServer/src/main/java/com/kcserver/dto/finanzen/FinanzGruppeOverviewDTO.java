package com.kcserver.dto.finanzen;

import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;
import com.kcserver.enumtype.FinanzgruppeTyp;

import java.util.List;

public record FinanzGruppeOverviewDTO(
        Long id,
        String kuerzel,
        FinanzgruppeTyp typ,
        boolean system,
        List<TeilnehmerKurzDTO> teilnehmer,
        long belegCount
) {}