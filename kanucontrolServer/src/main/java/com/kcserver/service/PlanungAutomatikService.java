package com.kcserver.service;

import com.kcserver.entity.Planung;
import com.kcserver.entity.PlanungPosition;
import com.kcserver.enumtype.FinanzKategorie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PlanungAutomatikService {

    private final PlanungBerechnungService berechnung;

    @Transactional
    public void aktualisiereAutomatischePositionen(
            Planung planung
    ) {

        if (planung == null) {
            return;
        }

        aktualisiere(
                planung,
                FinanzKategorie.UNTERKUNFT,
                berechnung.berechneUnterkunft(planung)
        );

        aktualisiere(
                planung,
                FinanzKategorie.VERPFLEGUNG,
                berechnung.berechneVerpflegung(planung)
        );

        aktualisiere(
                planung,
                FinanzKategorie.HONORARE,
                berechnung.berechneHonorare(planung)
        );

        aktualisiere(
                planung,
                FinanzKategorie.FAHRTKOSTEN,
                berechnung.berechneFahrtkosten(planung)
        );

        aktualisiere(
                planung,
                FinanzKategorie.VERBRAUCHSMATERIAL,
                berechnung.berechneVerbrauchsmaterial(planung)
        );

        aktualisiere(
                planung,
                FinanzKategorie.KULTUR,
                berechnung.berechneKultur(planung)
        );

        aktualisiere(
                planung,
                FinanzKategorie.MIETE,
                berechnung.berechneMiete(planung)
        );

        aktualisiere(
                planung,
                FinanzKategorie.SONSTIGE_KOSTEN,
                berechnung.berechneSonstigeKosten(planung)
        );

        aktualisiere(
                planung,
                FinanzKategorie.TEILNEHMERBEITRAG,
                berechnung.berechneTeilnehmerbeitraege(planung)
        );

        aktualisiere(
                planung,
                FinanzKategorie.KJFP_ZUSCHUSS,
                berechnung.berechneKjfpZuschuss(planung)
        );
    }

    private void aktualisiere(
            Planung planung,
            FinanzKategorie kategorie,
            BigDecimal betrag
    ) {

        PlanungPosition position =
                findeOderErzeuge(planung, kategorie);

        position.setAutomatischBerechnet(true);
        position.setEditierbar(false);
        position.setBetrag(
                betrag == null
                        ? BigDecimal.ZERO
                        : betrag
        );
    }

    private PlanungPosition findeOderErzeuge(
            Planung planung,
            FinanzKategorie kategorie
    ) {

        return planung.getPositionen()
                .stream()
                .filter(p -> p.getKategorie() == kategorie)
                .findFirst()
                .orElseGet(() -> {

                    PlanungPosition position = new PlanungPosition();

                    position.setKategorie(kategorie);

                    planung.addPosition(position);

                    return position;
                });
    }
}