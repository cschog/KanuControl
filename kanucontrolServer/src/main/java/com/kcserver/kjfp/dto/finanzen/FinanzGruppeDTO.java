package com.kcserver.kjfp.dto.finanzen;

import com.kcserver.kjfp.dto.teilnehmer.TeilnehmerKurzDTO;
import com.kcserver.kjfp.enumtype.FinanzgruppeTyp;

import java.util.List;

public record FinanzGruppeDTO(
        Long id,
        String kuerzel,
        FinanzgruppeTyp typ,
        boolean system,
        List<TeilnehmerKurzDTO> teilnehmer
) {}