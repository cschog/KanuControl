package com.kcserver.mapper;

import com.kcserver.dto.finanzen.FinanzGruppeOverviewDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;
import com.kcserver.entity.FinanzGruppe;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FinanzGruppeOverviewMapper {

    public FinanzGruppeOverviewDTO toDTO(
            FinanzGruppe g,
            long belegCount,
            BigDecimal einnahmen,
            BigDecimal ausgaben
    ) {

        List<TeilnehmerKurzDTO> teilnehmer =
                g.getTeilnehmer().stream()
                        .map(t -> new TeilnehmerKurzDTO(
                                t.getId(),
                                t.getPerson().getId(),
                                t.getPerson().getVorname(),
                                t.getPerson().getName()
                        ))
                        .toList();

        BigDecimal saldo = einnahmen.subtract(ausgaben);

        return new FinanzGruppeOverviewDTO(
                g.getId(),
                g.getKuerzel(),
                g.getTyp(),
                g.isSystem(),
                teilnehmer,
                belegCount,
                einnahmen,
                ausgaben,
                saldo
        );
    }
}