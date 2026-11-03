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
