package com.kcserver.finanz;

import com.kcserver.dto.finanz.FinanzSummaryDTO;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.enumtype.FinanzTyp;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class FinanzService {

    public BigDecimal sumKosten(List<? extends FinanzPosition> list) {
        return sumByTyp(list, FinanzTyp.KOSTEN);
    }

    public BigDecimal sumEinnahmen(List<? extends FinanzPosition> list) {
        return sumByTyp(list, FinanzTyp.EINNAHME);
    }

    public BigDecimal saldo(List<? extends FinanzPosition> list) {

        BigDecimal einnahmen = sumEinnahmen(list);
        BigDecimal kosten = sumKosten(list);

        return einnahmen.subtract(kosten);
    }

    public void validateAusgeglichen(List<? extends FinanzPosition> list) {

        if (saldo(list).compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Finanzierung nicht ausgeglichen"
            );
        }
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public BigDecimal sumByTyp(List<? extends FinanzPosition> list,
                               FinanzTyp typ) {

        BigDecimal sum = list.stream()
                .filter(p -> p.getKategorie().getTyp() == typ)
                .map(p -> safe(p.getBetrag()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    public FinanzSummaryDTO buildSummary(
            List<? extends FinanzPosition> list,
            long teilnehmerAnzahl
    ) {

        BigDecimal kosten = sumKosten(list);
        BigDecimal einnahmen = sumEinnahmen(list);

        BigDecimal saldo = einnahmen.subtract(kosten);

        BigDecimal deckung = kosten.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.valueOf(100)
                : einnahmen
                .divide(kosten, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);

        BigDecimal proPerson = teilnehmerAnzahl == 0
                ? BigDecimal.ZERO
                : kosten.divide(
                BigDecimal.valueOf(teilnehmerAnzahl),
                2,
                RoundingMode.HALF_UP
        );

        // ⭐ NEU: notwendiger Teilnehmerbeitrag
        BigDecimal notwendigerBeitrag =
                berechneNotwendigenTeilnehmerBeitrag(
                        list,
                        teilnehmerAnzahl
                );

        return new FinanzSummaryDTO(
                kosten,
                einnahmen,
                saldo,
                deckung,
                proPerson,
                notwendigerBeitrag
        );
    }
    public BigDecimal berechneNotwendigenTeilnehmerBeitrag(
            List<? extends FinanzPosition> positionen,
            long teilnehmerAnzahl
    ) {

        if (teilnehmerAnzahl == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal kosten = sumKosten(positionen);

        BigDecimal andereEinnahmen = positionen.stream()
                .filter(p -> p.getKategorie().getTyp() == FinanzTyp.EINNAHME)
                .filter(p -> p.getKategorie() != FinanzKategorie.TEILNEHMERBEITRAG)
                .map(FinanzPosition::getBetrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal rest = kosten.subtract(andereEinnahmen);

        if (rest.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return rest.divide(
                BigDecimal.valueOf(teilnehmerAnzahl),
                2,
                RoundingMode.HALF_UP
        );
    }

    public BigDecimal berechneTeilnehmerSumme(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {

        BigDecimal summe = BigDecimal.ZERO;

        for (Teilnehmer t : teilnehmer) {

            BigDecimal beitrag;

            if (!veranstaltung.isIndividuelleGebuehren()) {

                beitrag = safe(veranstaltung.getStandardGebuehr());

            } else {

                if (t.getIndividuellerBeitrag() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Individueller Beitrag fehlt für Teilnehmer ID="
                                    + t.getId()
                    );
                }

                beitrag = t.getIndividuellerBeitrag();
            }

            summe = summe.add(beitrag);
        }

        return summe.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal berechneGebuehrFuerTeilnehmer(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer) {

        if (!veranstaltung.isIndividuelleGebuehren()) {
            return safe(veranstaltung.getStandardGebuehr());
        }

        if (teilnehmer.getIndividuellerBeitrag() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Individueller Beitrag fehlt für Teilnehmer ID=" + teilnehmer.getId()
            );
        }

        return teilnehmer.getIndividuellerBeitrag();
    }

}