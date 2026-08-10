package com.kcserver.service.beitrag;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.AltersService;
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