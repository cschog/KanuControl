package com.kcserver.service.zahlungsnachweis;

import com.kcserver.entity.Zahlungsnachweis;
import com.kcserver.entity.ZahlungsPosition;
import com.kcserver.repository.abrechnung.AbrechnungBuchungRepository;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRueckzahlungSumme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ZahlungsnachweisSaldoService {

    private final AbrechnungBuchungRepository
            abrechnungBuchungRepository;


    /**
     * Betrag des Zahlungsnachweises, der Teilnehmern
     * über ZahlungsPositionen zugeordnet wurde.
     */
    public BigDecimal getZugeordnetenBetrag(
            Zahlungsnachweis zahlungsnachweis
    ) {

        if (zahlungsnachweis == null
                || zahlungsnachweis.getPositionen() == null) {

            return BigDecimal.ZERO;
        }

        return zahlungsnachweis.getPositionen()
                .stream()
                .map(ZahlungsPosition::getBetrag)
                .filter(Objects::nonNull)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    /**
     * Ursprüngliche Überzahlung.
     *
     * Beispiel:
     *
     * Zahlungsnachweis:       400 €
     * Zugeordnet:             300 €
     * ----------------------------
     * Überzahlung:            100 €
     */
    public BigDecimal getUeberzahlung(
            Zahlungsnachweis zahlungsnachweis
    ) {

        if (zahlungsnachweis == null
                || zahlungsnachweis.getBetrag() == null) {

            return BigDecimal.ZERO;
        }

        BigDecimal ueberzahlung =
                zahlungsnachweis.getBetrag()
                        .subtract(
                                getZugeordnetenBetrag(
                                        zahlungsnachweis
                                )
                        );

        /*
         * Negative Überzahlungen sollen hier nicht entstehen.
         *
         * Falls aus irgendeinem Grund mehr zugeordnet
         * wurde als tatsächlich eingegangen ist,
         * handelt es sich nicht um eine Überzahlung.
         */
        return ueberzahlung.max(
                BigDecimal.ZERO
        );
    }


    /**
     * Betrag, der bereits aus der Überzahlung
     * zurückgezahlt wurde.
     */
    public BigDecimal getBereitsZurueckgezahlt(
            Zahlungsnachweis zahlungsnachweis
    ) {

        if (zahlungsnachweis == null
                || zahlungsnachweis.getId() == null) {

            return BigDecimal.ZERO;
        }

        BigDecimal betrag =
                abrechnungBuchungRepository
                        .sumZurueckgezahltByUrspruenglichemZahlungsnachweisId(
                                zahlungsnachweis.getId()
                        );

        return betrag != null
                ? betrag
                : BigDecimal.ZERO;
    }


    /**
     * Noch offene Überzahlung.
     *
     * Beispiel:
     *
     * Überzahlung:            100 €
     * Bereits zurückgezahlt:   40 €
     * ----------------------------
     * Offen:                   60 €
     */
    public BigDecimal getOffeneUeberzahlung(
            Zahlungsnachweis zahlungsnachweis
    ) {

        BigDecimal offen =
                getUeberzahlung(zahlungsnachweis)
                        .subtract(
                                getBereitsZurueckgezahlt(
                                        zahlungsnachweis
                                )
                        );

        /*
         * Sicherheitsnetz:
         * Auch bei fehlerhaften Altdaten soll kein
         * negativer offener Betrag zurückgegeben werden.
         */
        return offen.max(
                BigDecimal.ZERO
        );
    }
    public Map<Long, BigDecimal> getZurueckgezahltByZahlungsnachweis(
            Long veranstaltungId
    ) {

        return abrechnungBuchungRepository
                .sumZurueckgezahltByVeranstaltungGrouped(
                        veranstaltungId
                )
                .stream()
                .collect(
                        Collectors.toMap(
                                ZahlungsnachweisRueckzahlungSumme::getZahlungsnachweisId,
                                ZahlungsnachweisRueckzahlungSumme::getZurueckgezahlt
                        )
                );
    }
}