package com.kcserver.service.beitrag;

import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.AltersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeilnehmerBeitragService {

    private final BeitragsregelService beitragsregelService;
    private final AltersService altersService;


    /**
     * Liefert den fachlich gültigen Beitrag
     * eines Teilnehmers für eine Veranstaltung.
     *
     * Regeln:
     *
     * 1. Individuelle Gebühren aktiv:
     *    -> individuellerBeitrag verwenden
     *    -> sonst Beitragsstruktur verwenden
     *
     * 2. Individuelle Gebühren deaktiviert:
     *    -> Standardgebühr der Veranstaltung
     */
    public BigDecimal getEffektiverBeitrag(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        if (veranstaltung == null || teilnehmer == null) {
            return BigDecimal.ZERO;
        }

        /* =========================================
           MANUELL ÜBERSCHRIEBEN
           ========================================= */

        if (teilnehmer.getIndividuellerBeitrag() != null) {

            return teilnehmer.getIndividuellerBeitrag();
        }

        /* =========================================
           STANDARDGEBÜHR
           ========================================= */

        if (!Boolean.TRUE.equals(
                veranstaltung.isIndividuelleGebuehren()
        )) {

            return veranstaltung.getStandardGebuehr() != null
                    ? veranstaltung.getStandardGebuehr()
                    : BigDecimal.ZERO;
        }

        /* =========================================
           BEITRAGSSTRUKTUR
           ========================================= */

        Beitragsstruktur struktur =
                veranstaltung.getBeitragsstruktur();

        if (struktur == null) {
            return BigDecimal.ZERO;
        }

        /* =========================================
           ALTER
           ========================================= */

        Integer alter =
                altersService.berechneAlterBeiBeginn(
                        teilnehmer.getPerson().getGeburtsdatum(),
                        veranstaltung.getBeginnDatum()
                );

        if (alter == null) {
            return BigDecimal.ZERO;
        }

        /* =========================================
           REGEL SUCHEN
           ========================================= */

        return beitragsregelService
                .findPassendeRegel(
                        struktur,
                        alter,
                        teilnehmer.getRolle()
                )
                .map(regel -> {

                    log.debug(
                            "Teilnehmer {} Alter {} Rolle {} → Beitrag {}",
                            teilnehmer.getId(),
                            alter,
                            teilnehmer.getRolle(),
                            regel.getBeitrag()
                    );

                    return regel.getBeitrag();
                })
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Alias für Alt-Code.
     * Kann später entfernt werden.
     */
    public BigDecimal berechneBeitrag(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        return getEffektiverBeitrag(
                veranstaltung,
                teilnehmer
        );
    }

    public boolean isBezahlt(Teilnehmer teilnehmer) {

        if (teilnehmer == null) {
            return false;
        }

        return Boolean.TRUE.equals(
                teilnehmer.getBezahlt()
        );
    }

    public BigDecimal getOffenerBetrag(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        if (isBezahlt(teilnehmer)) {
            return BigDecimal.ZERO;
        }

        return getEffektiverBeitrag(
                veranstaltung,
                teilnehmer
        );
    }

    public BigDecimal getBezahlteSumme(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {

        return teilnehmer.stream()

                .filter(this::isBezahlt)

                .map(t -> getEffektiverBeitrag(
                        veranstaltung,
                        t
                ))

                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}