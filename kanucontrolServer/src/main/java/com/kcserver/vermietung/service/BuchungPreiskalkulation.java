package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.entity.BuchungMietbereich;
import com.kcserver.vermietung.enumtype.*;
import java.math.*;
import java.time.temporal.ChronoUnit;

/** Einheitliche Preisberechnung für Buchung, Rabatt und Finanzübersicht. */
public final class BuchungPreiskalkulation {
    private BuchungPreiskalkulation() {}
    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    public static long einheiten(BuchungMietbereich position) {
        Buchung b = position.getBuchung();
        if (position.getBerechnungsart() != Berechnungsart.TAG || b == null
                || b.getAnreise() == null || b.getAbreise() == null || b.isUnbefristet()) return 1;
        return Math.max(1, ChronoUnit.DAYS.between(b.getAnreise().toLocalDate(), b.getAbreise().toLocalDate()));
    }
    public static BigDecimal preis(BuchungMietbereich position) {
        if (Zahlungsplan.ohneAbrechnung(position.getBuchung())) return BigDecimal.ZERO.setScale(2);
        return zero(position.getVereinbarterPreis())
                .multiply(BigDecimal.valueOf(position.getAnzahl() == null ? 0 : position.getAnzahl()))
                .multiply(BigDecimal.valueOf(einheiten(position))).setScale(2, RoundingMode.HALF_UP);
    }
    public static BigDecimal positionsRabatt(BuchungMietbereich position, RabattwertTyp typ, BigDecimal wert) {
        BigDecimal brutto = preis(position);
        if (typ == null || wert == null || wert.signum() <= 0) return BigDecimal.ZERO.setScale(2);
        BigDecimal rabatt = typ == RabattwertTyp.PROZENTUAL
                ? brutto.multiply(wert).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : wert.multiply(BigDecimal.valueOf(position.getAnzahl() == null ? 0 : position.getAnzahl()))
                        .multiply(BigDecimal.valueOf(einheiten(position)));
        return rabatt.min(brutto).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
    public static BigDecimal miete(Buchung buchung) {
        BigDecimal brutto = buchung.getMietbereiche().stream().map(BuchungPreiskalkulation::preis)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return brutto.subtract(rabatt(buchung).min(brutto)).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
    /** Net amounts in booking order; preserves position-specific discounts. */
    public static java.util.List<BigDecimal> nettoPositionen(Buchung b) {
        var brutto=b.getMietbereiche().stream().map(BuchungPreiskalkulation::preis).toList();
        boolean snapshots=b.getBuchungsquelle()!=Buchungsquelle.AIRBNB && b.getRabattart()!=Rabattart.KEIN_RABATT
            && !(b.getRabattart()==Rabattart.INDIVIDUELL && b.getRabattwertTyp()==RabattwertTyp.ABSOLUT)
            && b.getMietbereiche().stream().anyMatch(p->p.getRabattwertTyp()!=null);
        if(snapshots)return b.getMietbereiche().stream().map(p->preis(p).subtract(positionsRabatt(p,p.getRabattwertTyp(),p.getRabattwert()))).toList();
        return verteileRabatt(brutto,rabatt(b));
    }
    /** Allocate a global/legacy discount without losing cents or creating negative rows. */
    public static java.util.List<BigDecimal> verteileRabatt(java.util.List<BigDecimal> brutto,BigDecimal rabatt) {
        var restBrutto=brutto.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
        var restRabatt=rabatt.max(BigDecimal.ZERO).min(restBrutto);var result=new java.util.ArrayList<BigDecimal>();
        for(var betrag:brutto){
            var anteil=restBrutto.signum()==0?BigDecimal.ZERO:restRabatt.multiply(betrag).divide(restBrutto,2,RoundingMode.HALF_UP).min(betrag).min(restRabatt);
            result.add(betrag.subtract(anteil));restBrutto=restBrutto.subtract(betrag);restRabatt=restRabatt.subtract(anteil);
        }
        return java.util.List.copyOf(result);
    }
    public static BigDecimal rabatt(Buchung buchung) {
        BigDecimal brutto = buchung.getMietbereiche().stream().map(BuchungPreiskalkulation::preis).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (buchung.getBuchungsquelle() == Buchungsquelle.AIRBNB || buchung.getRabattart() == Rabattart.KEIN_RABATT) return BigDecimal.ZERO.setScale(2);
        if (buchung.getRabattart() == Rabattart.INDIVIDUELL && buchung.getRabattwertTyp() == RabattwertTyp.ABSOLUT)
            return zero(buchung.getRabattwert()).min(brutto).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        // Ältere Buchungen ohne Positions-Rabattsnapshots behalten ihren gespeicherten Rabatt.
        if (buchung.getMietbereiche().stream().noneMatch(p -> p.getRabattwertTyp() != null))
            return zero(buchung.getRabattbetrag()).min(brutto).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return buchung.getMietbereiche().stream()
                .map(p -> positionsRabatt(p, p.getRabattwertTyp(), p.getRabattwert()))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }
}
