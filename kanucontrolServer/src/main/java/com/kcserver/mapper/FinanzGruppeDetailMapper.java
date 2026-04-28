package com.kcserver.mapper;

import com.kcserver.dto.finanzen.FinanzGruppeDetailDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;
import com.kcserver.entity.FinanzGruppe;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FinanzGruppeDetailMapper {

    public FinanzGruppeDetailDTO toDetailDTO(FinanzGruppe g) {

        List<TeilnehmerKurzDTO> teilnehmer =
                g.getTeilnehmer().stream()
                        .map(t -> new TeilnehmerKurzDTO(
                                t.getId(),
                                t.getPerson().getId(),
                                t.getPerson().getVorname(),
                                t.getPerson().getName()
                        ))
                        .toList();

        return new FinanzGruppeDetailDTO(
                g.getId(),
                g.getKuerzel(),
                teilnehmer
        );
    }
}