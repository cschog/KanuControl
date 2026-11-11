package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.BuchungFinanzenDTO;
import com.kcserver.vermietung.entity.BuchungVerbrauchskosten;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

/** A fixed, typed table; no calculations or scripts in user templates. */
public record Abrechnungsbereich(List<Zeile> zeilen) {
 public record Zeile(String beschreibung, String menge, String preis, String betrag) {}
 static String kategorie(com.kcserver.vermietung.enumtype.Finanzkategorie k){return switch(k){case MIETE -> "Miete";case ANZAHLUNG -> "Anzahlung";case KAUTION -> "Kaution";case VERBRAUCHSKOSTEN -> "Nebenkosten";};}
 static String geld(BigDecimal n) {return NumberFormat.getCurrencyInstance(Locale.GERMANY).format(n);}
 public record Rabattzeile(String text, BigDecimal betrag) {}
 private record Rabattdarstellung(List<Rabattzeile> zeilen) {}
 private static String bezug(List<Integer> nummern) {
  if(nummern.size()==1)return "Position "+nummern.getFirst();
  return "Positionen "+String.join(", ",nummern.subList(0,nummern.size()-1).stream().map(Object::toString).toList())+" und "+nummern.getLast();
 }
 private static String prozent(BigDecimal wert){return wert.stripTrailingZeros().toPlainString().replace('.',',')+" %";}
 public static Abrechnungsbereich aus(BuchungFinanzenDTO f,List<BuchungVerbrauchskosten> verbrauch,int revision) {
  return aus(f,verbrauch,revision,new Rabattdarstellung(generischerRabatt(f)));
 }
 private static List<Rabattzeile> generischerRabatt(BuchungFinanzenDTO f){
  var nummern=new ArrayList<Integer>();int nr=0;
  for(var p:f.mietkosten())if(p.anzahl()!=null && p.anzahl()>0 && p.gesamtbetrag().signum()!=0)nummern.add(++nr);
  return f.mietrabatt().signum()>0?List.of(new Rabattzeile("Rabatt auf "+bezug(nummern),f.mietrabatt())):List.of();
 }
 public static Abrechnungsbereich aus(BuchungFinanzenDTO f,List<BuchungVerbrauchskosten> verbrauch,int revision,com.kcserver.vermietung.entity.Buchung b){
  var netto=BuchungPreiskalkulation.nettoPositionen(b);
  if(netto.size()!=f.mietkosten().size())throw new IllegalArgumentException("Mietpositionen stimmen nicht überein.");
  var gruppen=new LinkedHashMap<String,List<Integer>>();var betraege=new LinkedHashMap<String,BigDecimal>();int nr=0;
  for(int i=0;i<netto.size();i++){
   var row=f.mietkosten().get(i);if(row.anzahl()==null || row.anzahl()<=0 || row.gesamtbetrag().signum()==0)continue;
   nr++;var rabatt=row.gesamtbetrag().subtract(netto.get(i));if(rabatt.signum()<=0)continue;
   var pos=b.getMietbereiche().get(i);
   boolean global=b.getRabattart()==com.kcserver.vermietung.enumtype.Rabattart.INDIVIDUELL && b.getRabattwertTyp()==com.kcserver.vermietung.enumtype.RabattwertTyp.ABSOLUT;
   var typ=global?b.getRabattwertTyp():pos.getRabattwertTyp()!=null?pos.getRabattwertTyp():b.getRabattwertTyp();
   var wert=global?b.getRabattwert():pos.getRabattwertTyp()!=null?pos.getRabattwert():b.getRabattwert();
   String label=typ==com.kcserver.vermietung.enumtype.RabattwertTyp.PROZENTUAL && wert!=null?"Rabatt von "+prozent(wert):"Rabatt";
   gruppen.computeIfAbsent(label,k->new ArrayList<>()).add(nr);betraege.merge(label,rabatt,BigDecimal::add);
  }
  var rows=new ArrayList<Rabattzeile>();gruppen.forEach((label,nummern)->rows.add(new Rabattzeile(label+" auf "+bezug(nummern),betraege.get(label))));
  return aus(f,verbrauch,revision,new Rabattdarstellung(rows));
 }
 private static Abrechnungsbereich aus(BuchungFinanzenDTO f,List<BuchungVerbrauchskosten> verbrauch,int revision,Rabattdarstellung rabatt) {
  var z=new ArrayList<Zeile>();int nr=0;
  z.add(new Zeile("Abrechnung - Revision "+revision,"","",""));
  for(var p:f.mietkosten()){
   if(p.anzahl()==null || p.anzahl()<=0 || p.gesamtbetrag().signum()==0)continue;
   z.add(new Zeile((++nr)+". "+p.bezeichnung(),p.anzahl()+" × "+p.abrechnungseinheiten()+("TAG".equals(p.berechnungsart())?" Tage":""),geld(p.einzelpreis()),geld(p.gesamtbetrag())));
  }
  for(var row:rabatt.zeilen())z.add(new Zeile(row.text(),"","",geld(row.betrag().negate())));
  for(var p:verbrauch){
   if(p.getKosten()==null || p.getKosten().signum()==0)continue;
   String name=p.getBezeichnung();String zaehler=p.getZaehlerBezeichnung();
   if(zaehler!=null && !zaehler.isBlank() && !name.toLowerCase(Locale.ROOT).contains(zaehler.toLowerCase(Locale.ROOT)))name+=" - "+zaehler;
   z.add(new Zeile((++nr)+". "+name,
    p.getVerbrauch()!=null?p.getVerbrauch().stripTrailingZeros().toPlainString().replace('.',',')+" "+Objects.toString(p.getMengeneinheit(),""):
     p.getAbrechnungstage()!=null?p.getAbrechnungstage()+" Tage":"1 pauschal",geld(p.getPreis()),geld(p.getKosten())));
  }
  for(var p:f.entschaedigungen())if(p.gesamtbetrag().signum()!=0)z.add(new Zeile((++nr)+". Entschädigung: "+p.bezeichnung()+" ("+(p.anlass().name().equals("VERLUST")?"Verlust":"Beschädigung")+")",p.anzahl().stripTrailingZeros().toPlainString().replace('.',','),geld(p.einzelpreis()),geld(p.gesamtbetrag())));
  z.add(new Zeile("Abrechnungssumme","","",geld(f.abrechnungGesamt())));
  var netto=BigDecimal.ZERO;
  for(var p:f.bewegungen()) {
   if(p.betrag().signum()==0)continue;
   var wert=switch(p.art()) {
    case EINGANG -> p.betrag().negate();
    case RUECKZAHLUNG -> p.betrag();
    case VERRECHNUNG -> p.kategorie()==com.kcserver.vermietung.enumtype.Finanzkategorie.KAUTION?p.betrag():p.betrag().negate();
   };
   netto=netto.add(wert);
   var art=switch(p.art()){case EINGANG -> "Bereits gezahlt";case RUECKZAHLUNG -> "Bereits erstattet";case VERRECHNUNG -> "Verrechnung";};
   z.add(new Zeile(art+" am "+p.datum().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))+" ("+kategorie(p.kategorie())+")","","",geld(wert)));
  }
  // Keep the existing financial balance authoritative, including requested deposits/category rules.
  var ausgleich=f.saldo().subtract(f.abrechnungGesamt().add(netto));
  if(ausgleich.signum()!=0)z.add(new Zeile("Kautionsanforderung / Kategorieausgleich","","",geld(ausgleich)));
  z.add(new Zeile(f.saldo().signum()<0?"Rückerstattung":f.saldo().signum()>0?"Restzahlung":"Ausgeglichen","","",geld(f.saldo().abs())));
  return new Abrechnungsbereich(List.copyOf(z));
 }
}
