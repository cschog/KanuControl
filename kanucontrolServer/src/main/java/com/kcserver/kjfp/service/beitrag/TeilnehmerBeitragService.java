package com.kcserver.kjfp.service.beitrag;

import com.kcserver.kjfp.entity.beitraege.Beitragsregel;
import com.kcserver.kjfp.entity.beitraege.Beitragsstruktur;
import com.kcserver.kjfp.entity.Teilnehmer;
import com.kcserver.kjfp.entity.Veranstaltung;
import com.kcserver.kjfp.service.AltersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeilnehmerBeitragService {

    private final BeitragsregelService beitragsregelService;
    private final AltersService altersService;


    public BigDecimal getSollBeitrag(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        if (veranstaltung == null || teilnehmer == null) {
            return BigDecimal.ZERO;
        }

        Beitragsstruktur struktur = veranstaltung.getBeitragsstruktur();

        if (struktur == null
                || struktur.getRegeln() == null
                || struktur.getRegeln().isEmpty()) {
            return BigDecimal.ZERO;
        }

        Integer alter =
                altersService.berechneAlterBeiBeginn(
                        teilnehmer.getPerson().getGeburtsdatum(),
                        veranstaltung.getBeginnDatum()
                );

        if (alter == null) {
            return BigDecimal.ZERO;
        }

        return beitragsregelService
                .findPassendeRegel(
                        struktur,
                        alter,
                        teilnehmer.getRolle()
                )
                .map(Beitragsregel::getBeitrag)
                .orElse(BigDecimal.ZERO);
    }
}