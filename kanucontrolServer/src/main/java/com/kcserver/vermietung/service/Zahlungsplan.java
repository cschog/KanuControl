package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.enumtype.Zahlungsvariante;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
/** Agreed schedule only: never derives payments from a requested amount. */
public final class Zahlungsplan {
 private Zahlungsplan() {}
 public static boolean ohneAbrechnung(Buchung b) {return b != null && b.getZahlungsvariante()==Zahlungsvariante.OHNE_ABRECHNUNG;}
 public static Zahlungsvariante vorschlag(LocalDateTime anreise,Integer grenze,LocalDate heute) {
  return (grenze==null || grenze>0) && anreise!=null && ChronoUnit.DAYS.between(heute,anreise.toLocalDate()) < (grenze==null?42:grenze)
   ? Zahlungsvariante.GESAMTMIETE_VORAUS : Zahlungsvariante.ANZAHLUNG;
 }
 public static LocalDate frist(LocalDate anreise,LocalDate heute) {return anreise.minusDays(7).isBefore(heute)?heute:anreise.minusDays(7);}
 public static BigDecimal anzahlung(Buchung b) {return b.getZahlungsvariante()==Zahlungsvariante.ANZAHLUNG?b.getAnzahlungBetrag():BigDecimal.ZERO;}
 public static BigDecimal restmiete(Buchung b) {return b.getZahlungsvariante()==Zahlungsvariante.ANZAHLUNG?BuchungPreiskalkulation.miete(b).subtract(anzahlung(b)):BigDecimal.ZERO;}
}
