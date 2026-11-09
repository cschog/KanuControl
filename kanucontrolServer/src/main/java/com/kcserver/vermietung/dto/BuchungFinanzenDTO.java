package com.kcserver.vermietung.dto;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record BuchungFinanzenDTO(Long buchungId, String buchungsnummer, String mieter, String mietobjekt, Buchungsquelle buchungsquelle, String buchungsstatus, LocalDate anreise, LocalDate abreise, boolean unbefristet, BigDecimal anzahlungSoll, BigDecimal kautionSoll, List<MietkostenPositionDTO> mietkosten, BigDecimal mietrabatt, BigDecimal mieteSoll, BigDecimal verbrauchSoll, boolean verbrauchVorschauVollstaendig, List<FinanzbewegungDTO> bewegungen,List<EntschaedigungPositionDTO> entschaedigungen,BigDecimal entschaedigungSoll) {
 private BigDecimal sum(java.util.Set<com.kcserver.vermietung.enumtype.Finanzkategorie> cats,com.kcserver.vermietung.enumtype.Finanzbewegungsart art){return bewegungen.stream().filter(m->cats.contains(m.kategorie())&&m.art()==art).map(FinanzbewegungDTO::betrag).reduce(BigDecimal.ZERO,BigDecimal::add);}
 @com.fasterxml.jackson.annotation.JsonProperty public BigDecimal saldo(){
  var rent=java.util.Set.of(com.kcserver.vermietung.enumtype.Finanzkategorie.MIETE,com.kcserver.vermietung.enumtype.Finanzkategorie.ANZAHLUNG);
  var caution=java.util.Set.of(com.kcserver.vermietung.enumtype.Finanzkategorie.KAUTION);var utility=java.util.Set.of(com.kcserver.vermietung.enumtype.Finanzkategorie.VERBRAUCHSKOSTEN);
  var incoming=com.kcserver.vermietung.enumtype.Finanzbewegungsart.EINGANG;var refund=com.kcserver.vermietung.enumtype.Finanzbewegungsart.RUECKZAHLUNG;var offset=com.kcserver.vermietung.enumtype.Finanzbewegungsart.VERRECHNUNG;
  boolean due=!java.util.Set.of("ANFRAGE","STORNIERT","ABGELEHNT").contains(buchungsstatus);
  var r=due?mieteSoll:BigDecimal.ZERO;var u=due?nebenkostenSoll():BigDecimal.ZERO;var c=due?kautionSoll:BigDecimal.ZERO;
  var rentIn=sum(rent,incoming);var rentRefund=sum(rent,refund);var over=rentIn.subtract(r).max(BigDecimal.ZERO);
  var rentPaid=rentIn.subtract(rentRefund.min(over)).max(BigDecimal.ZERO);
  var cautionIn=sum(caution,incoming);var cautionBack=cautionIn.subtract(sum(caution,refund)).subtract(sum(caution,offset)).subtract(rentRefund.subtract(over).max(BigDecimal.ZERO)).max(BigDecimal.ZERO);
  var utilityPaid=sum(utility,incoming).subtract(sum(utility,refund)).add(sum(utility,offset));
  return r.subtract(rentPaid).max(BigDecimal.ZERO).add(u.subtract(utilityPaid).max(BigDecimal.ZERO)).add(c.subtract(cautionIn).max(BigDecimal.ZERO)).subtract(cautionBack).subtract(rentPaid.subtract(r).max(BigDecimal.ZERO)).subtract(utilityPaid.subtract(u).max(BigDecimal.ZERO)).setScale(2);
 }
 @com.fasterxml.jackson.annotation.JsonProperty public BigDecimal nebenkostenSoll(){return verbrauchSoll.add(entschaedigungSoll);}
 @com.fasterxml.jackson.annotation.JsonProperty public BigDecimal abrechnungGesamt(){return mieteSoll.add(nebenkostenSoll());}
}
