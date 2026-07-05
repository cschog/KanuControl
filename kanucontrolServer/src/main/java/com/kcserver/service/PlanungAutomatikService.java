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

    private final PlanungBerechnungService planungBerechnungService;

    @Transactional
    public boolean aktualisiereAutomatischePositionen(
            Planung planung
    ) {

        if (planung == null
                || planung.getVeranstaltung() == null) {
            return false;
        }

        boolean changed = false;

        changed |= aktualisiere(
                planung,
                FinanzKategorie.UNTERKUNFT,
                planungBerechnungService.berechneUnterkunft(
                        planung.getVeranstaltung()
                )
        );

        changed |= aktualisiere(
                planung,
                FinanzKategorie.VERPFLEGUNG,
                planungBerechnungService.berechneVerpflegung(
                        planung.getVeranstaltung()
                )
        );

        changed |= aktualisiere(
                planung,
                FinanzKategorie.TEILNEHMERBEITRAG,
                planungBerechnungService.berechneTeilnehmerbeitraege(
                        planung.getVeranstaltung()
                )
        );

        changed |= aktualisiere(
                planung,
                FinanzKategorie.KJFP_ZUSCHUSS,
                planungBerechnungService.berechneKjfpZuschuss(
                        planung.getVeranstaltung()
                )
        );

        return changed;
    }

    private boolean aktualisiere(
            Planung planung,
            FinanzKategorie kategorie,
            BigDecimal betrag
    ) {

        boolean automatisch = istAutomatischeKategorie(kategorie);

        PlanungPosition position =
                findeOderErzeuge(planung, kategorie);

        boolean changed = false;

        if (position.isAutomatischBerechnet() != automatisch) {
            position.setAutomatischBerechnet(automatisch);
            changed = true;
        }

        if (position.isEditierbar() == automatisch) {
            position.setEditierbar(!automatisch);
            changed = true;
        }

        BigDecimal neu =
                betrag == null
                        ? BigDecimal.ZERO
                        : betrag;

        if (neu.compareTo(position.getBetrag()) != 0) {
            position.setBetrag(neu);
            changed = true;
        }

        return changed;
    }

    private boolean istAutomatischeKategorie(FinanzKategorie kategorie) {
        return switch (kategorie) {
            case UNTERKUNFT,
                 VERPFLEGUNG,
                 TEILNEHMERBEITRAG,
                 KJFP_ZUSCHUSS -> true;
            default -> false;
        };
    }

    private PlanungPosition findeOderErzeuge(
            Planung planung,
            FinanzKategorie kategorie
    ) {

        return planung.getPositionen()
                .stream()
                .filter(position -> position.getKategorie() == kategorie)
                .findFirst()
                .orElseGet(() -> {

                    PlanungPosition position = new PlanungPosition();

                    position.setKategorie(kategorie);
                    position.setAutomatischBerechnet(true);
                    position.setEditierbar(false);

                    planung.addPosition(position);

                    return position;
                });
    }
}