package com.kcserver.service;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.TeilnehmerRolle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BeitragsService {

    private final AltersService altersService;

    public BigDecimal berechneBeitrag(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        // =========================================
        // Individuell überschrieben
        // =========================================

        if (teilnehmer.getIndividuellerBeitrag() != null) {
            return teilnehmer.getIndividuellerBeitrag();
        }

        // =========================================
        // Standardgebühr
        // =========================================

        if (!veranstaltung.isIndividuelleGebuehren()) {

            return veranstaltung.getStandardGebuehr();
        }

        // =========================================
        // Struktur
        // =========================================

        Beitragsstruktur struktur =
                veranstaltung.getBeitragsstruktur();

        if (struktur == null || struktur.getRegeln() == null) {
            return BigDecimal.ZERO;
        }

        // =========================================
        // Alter berechnen
        // =========================================

        Integer alter = altersService.berechneAlterBeiBeginn(
                teilnehmer.getPerson() != null
                        ? teilnehmer.getPerson().getGeburtsdatum()
                        : null,
                LocalDate.now()
        );

        if (alter == null) {
            alter = 0;
        }

        TeilnehmerRolle rolle =
                teilnehmer.getRolle();

        // =========================================
        // Regeln sortieren
        // =========================================

        List<Beitragsregel> regeln =
                struktur.getRegeln()
                        .stream()
                        .sorted(Comparator.comparing(Beitragsregel::getSortierung))
                        .toList();

        // =========================================
        // Rollen-spezifische Regeln zuerst
        // =========================================

        BigDecimal rollenTreffer =
                findePassendenBeitrag(regeln, alter, rolle, false);

        if (rollenTreffer != null) {
            return rollenTreffer;
        }

        // =========================================
        // Fallback Teilnehmer-Regeln
        // =========================================

        BigDecimal standardTreffer =
                findePassendenBeitrag(regeln, alter, rolle, true);

        if (standardTreffer != null) {
            return standardTreffer;
        }

        return BigDecimal.ZERO;
    }

    /* =========================================================
       MATCHING
       ========================================================= */

    private BigDecimal findePassendenBeitrag(
            List<Beitragsregel> regeln,
            int alter,
            TeilnehmerRolle rolle,
            boolean nurStandardRegeln
    ) {

        int alterVon = 0;

        for (Beitragsregel regel : regeln) {

            // =========================================
            // Rollenfilter
            // =========================================

            if (nurStandardRegeln) {

                if (regel.getRolle() != null) {
                    continue;
                }

            } else {

                if (regel.getRolle() != rolle) {
                    continue;
                }
            }

            Integer alterBis = regel.getAlterBis();

            boolean alterMatch =
                    alter >= alterVon &&
                            (alterBis == null || alter <= alterBis);

            if (alterMatch) {
                return regel.getBeitrag();
            }

            // =========================================
            // Nächster Bereich
            // =========================================

            if (alterBis != null) {
                alterVon = alterBis + 1;
            }
        }

        return null;
    }
}