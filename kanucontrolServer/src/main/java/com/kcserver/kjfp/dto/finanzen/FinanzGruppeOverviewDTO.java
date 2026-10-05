package com.kcserver.kjfp.dto.finanzen;

import com.kcserver.kjfp.dto.teilnehmer.TeilnehmerKurzDTO;
import com.kcserver.kjfp.enumtype.FinanzgruppeTyp;

import java.util.List;
import java.math.BigDecimal;

public record FinanzGruppeOverviewDTO(
        Long id,
        String kuerzel,
        FinanzgruppeTyp typ,
        boolean system,
        List<TeilnehmerKurzDTO> teilnehmer,
        long belegCount,
        BigDecimal einnahmen,
        BigDecimal ausgaben,
        BigDecimal saldo
) {}