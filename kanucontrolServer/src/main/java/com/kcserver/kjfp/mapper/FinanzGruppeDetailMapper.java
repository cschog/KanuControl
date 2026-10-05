package com.kcserver.kjfp.mapper;

import com.kcserver.kjfp.dto.finanzen.FinanzGruppeDetailDTO;
import com.kcserver.kjfp.dto.teilnehmer.TeilnehmerKurzDTO;
import com.kcserver.kjfp.entity.FinanzGruppe;
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
                g.getTyp(),
                g.isSystem(),
                teilnehmer
        );
    }
}