package com.kcserver.service;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.kcserver.enumtype.TeilnehmerRolle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class BeitragsService {

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

        if (struktur == null) {
            return BigDecimal.ZERO;
        }

        // =========================================
        // Alter
        // =========================================

        Integer alter = null;

        if (teilnehmer.getPerson().getGeburtsdatum() != null) {

            alter = Period.between(
                    teilnehmer.getPerson().getGeburtsdatum(),
                    LocalDate.now()
            ).getYears();
        }

        TeilnehmerRolle rolle =
                teilnehmer.getRolle();

        // =========================================
        // Regeln prüfen
        // =========================================

        for (Beitragsregel regel : struktur.getRegeln()) {

            // Rolle prüfen
            if (regel.getRolle() != null &&
                    regel.getRolle() != rolle) {

                continue;
            }

            // Alter von
            if (regel.getAlterVon() != null &&
                    alter != null &&
                    alter < regel.getAlterVon()) {

                continue;
            }

            // Alter bis
            if (regel.getAlterBis() != null &&
                    alter != null &&
                    alter > regel.getAlterBis()) {

                continue;
            }

            return regel.getBeitrag();
        }

        return BigDecimal.ZERO;
    }
}