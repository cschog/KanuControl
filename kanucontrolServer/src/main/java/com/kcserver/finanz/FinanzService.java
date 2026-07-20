package com.kcserver.finanz;

import com.kcserver.dto.finanzen.FinanzSummaryDTO;
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

    public void validatePlanung(List<? extends FinanzPosition> list) {

        BigDecimal eigenanteil = saldo(list);

        if (eigenanteil.compareTo(MINDEST_EIGENANTEIL) > 0) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Der Eigenanteil muss mindestens 250 € betragen."
            );
        }
    }

    public FinanzAmpel ermittleAmpel(List<? extends FinanzPosition> list) {

        BigDecimal eigenanteil = saldo(list);

        if (eigenanteil.compareTo(EMPFOHLENER_EIGENANTEIL) <= 0) {
            return FinanzAmpel.GRUEN;
        }

        if (eigenanteil.compareTo(MINDEST_EIGENANTEIL) <= 0) {
            return FinanzAmpel.GELB;
        }

        return FinanzAmpel.ROT;
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

        BigDecimal kjfpZuschuss = list.stream()
                .filter(p -> p.getKategorie() == FinanzKategorie.KJFP_ZUSCHUSS)
                .map(FinanzPosition::getBetrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ⭐ NEU: notwendiger Teilnehmerbeitrag
        BigDecimal notwendigerBeitrag =
                berechneNotwendigenTeilnehmerBeitrag(
                        list,
                        teilnehmerAnzahl
                );

        FinanzSummaryDTO dto = new FinanzSummaryDTO();
        dto.setKosten(kosten);
        dto.setEinnahmen(einnahmen);
        dto.setSaldo(saldo);
        dto.setDeckung(deckung);
        dto.setTeilnehmerKostenProPerson(proPerson);
        dto.setEmpfohlenerTeilnehmerBeitrag(notwendigerBeitrag);
        dto.setKjfpZuschuss(kjfpZuschuss);

        return dto;

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

    private static final BigDecimal EMPFOHLENER_EIGENANTEIL =
            BigDecimal.valueOf(-500);

    private static final BigDecimal MINDEST_EIGENANTEIL =
            BigDecimal.valueOf(-250);

    public enum FinanzAmpel {
        GRUEN,
        GELB,
        ROT
    }

}