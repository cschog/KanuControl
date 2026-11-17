package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.enumtype.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AbrechnungPositionenTest {
 static BigDecimal n(String x){return new BigDecimal(x);}
 static Buchung buchung(String erster,String zweiter){
  var b=AbrechnungVorlagenTest.buchung();b.setRabattart(Rabattart.VEREIN);int i=0;
  for(var row:AbrechnungVorlagenTest.finanzen(n("1000")).mietkosten()){
   var p=new BuchungMietbereich();p.setBuchung(b);p.setAnzahl(row.anzahl());p.setVereinbarterPreis(row.einzelpreis());p.setBerechnungsart(Berechnungsart.valueOf(row.berechnungsart()));p.setRabattwertTyp(RabattwertTyp.PROZENTUAL);p.setRabattwert(n(i==0?erster:i==1?zweiter:"0"));b.getMietbereiche().add(p);i++;
  }return b;
 }
 static BuchungFinanzenDTO finanzen(Buchung b){var f=AbrechnungVorlagenTest.finanzen(n("1000"));return new BuchungFinanzenDTO(f.buchungId(),f.buchungsnummer(),f.mieter(),f.mietobjekt(),f.buchungsquelle(),f.buchungsstatus(),f.anreise(),f.abreise(),f.unbefristet(),f.anzahlungSoll(),f.kautionSoll(),f.mietkosten(),BuchungPreiskalkulation.rabatt(b),BuchungPreiskalkulation.miete(b),f.verbrauchSoll(),f.verbrauchVorschauVollstaendig(),f.bewegungen(),f.entschaedigungen(),f.entschaedigungSoll());}
 @Test void gleicherProzentsatzWirdMitBeidenPositionsnummernZusammengefasst()throws Exception{
  var b=buchung("10","10");var verbrauch=new ArrayList<BuchungVerbrauchskosten>();
  for(var name:List.of("Strom","Gas","Wasser")){var v=new BuchungVerbrauchskosten();v.setBezeichnung(name);v.setKosten(n(name.equals("Strom")?"27":name.equals("Gas")?"89":"20"));v.setPreis(v.getKosten());verbrauch.add(v);}
  var t=Abrechnungsbereich.aus(finanzen(b),verbrauch,1,b);
  assertTrue(t.zeilen().stream().anyMatch(z->z.beschreibung().equals("1. Bootshaus EG")&&z.betrag().startsWith("500,00")));
  assertTrue(t.zeilen().stream().anyMatch(z->z.beschreibung().equals("Rabatt von 10 % auf Positionen 1 und 2")&&z.betrag().startsWith("-75,00")));
  assertTrue(t.zeilen().stream().anyMatch(z->z.beschreibung().equals("3. Grill")));
  var papier=getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes();
  byte[] pdf=AbrechnungBriefLayout.render(papier,Map.of(),List.of("Abrechnung mit Positionsrabatt"),"Mit freundlichen Grüßen",null,"2025-30",t);
  try(var doc=org.apache.pdfbox.Loader.loadPDF(pdf)){assertTrue(new org.apache.pdfbox.text.PDFTextStripper().getText(doc).contains("Rabatt von 10 % auf Positionen 1 und 2"));}
  java.nio.file.Files.write(java.nio.file.Path.of("/tmp/kc-rechnung-positionsrabatt.pdf"),pdf);
 }
 @Test void unterschiedlicheProzentsaetzeBleibenGetrenntUndVollrabattEntferntKeinePosition(){
  var b=buchung("100","5");var t=Abrechnungsbereich.aus(finanzen(b),List.of(),1,b);
  assertTrue(t.zeilen().stream().anyMatch(z->z.beschreibung().equals("1. Bootshaus EG")));
  assertTrue(t.zeilen().stream().anyMatch(z->z.beschreibung().equals("Rabatt von 100 % auf Position 1")&&z.betrag().startsWith("-500,00")));
  assertTrue(t.zeilen().stream().anyMatch(z->z.beschreibung().equals("Rabatt von 5 % auf Position 2")));
 }
 @Test void pauschalrabattErfindetKeinenProzentsatz(){
  var b=buchung("0","0");b.setRabattart(Rabattart.INDIVIDUELL);b.setRabattwertTyp(RabattwertTyp.ABSOLUT);b.setRabattwert(n("50"));var t=Abrechnungsbereich.aus(finanzen(b),List.of(),1,b);
  assertTrue(t.zeilen().stream().anyMatch(z->z.beschreibung().equals("Rabatt auf Positionen 1, 2 und 3")&&z.betrag().startsWith("-50,00")));
  assertFalse(t.zeilen().stream().anyMatch(z->z.beschreibung().contains("%")));
 }
 @Test void leereVerbrauchspositionenEntfallenUndZaehlernamenWerdenAusgeblendet(){
  var b=buchung("0","0");var strom=new BuchungVerbrauchskosten();strom.setBezeichnung("Strom");strom.setZaehlerBezeichnung("Campingplatz");strom.setVerbrauch(n("1.9"));strom.setMengeneinheit("kWh");strom.setPreis(n("2"));strom.setKosten(n("3.8"));var leer=new BuchungVerbrauchskosten();leer.setBezeichnung("Unbenutzt");leer.setKosten(BigDecimal.ZERO);
  var t=Abrechnungsbereich.aus(finanzen(b),List.of(strom,leer),1,b);
  assertTrue(t.zeilen().stream().anyMatch(z->z.beschreibung().equals("4. Strom")&&z.menge().equals("1,9 kWh")));
  assertFalse(t.zeilen().stream().anyMatch(z->z.beschreibung().contains("Unbenutzt")||z.beschreibung().startsWith("Rabatt")));
 }
 @Test void mietpositionenZeigenTagOderUebernachtungenUndPauschalenBleibenUnveraendert(){
  var f=AbrechnungVorlagenTest.finanzen(n("1000"));
  for(int naechte:new int[]{0,1,2}){
   long einheiten=Math.max(1,naechte);
   var pos=new MietkostenPositionDTO("Bootshaus",1,n("100"),n("100").multiply(BigDecimal.valueOf(einheiten)),einheiten,"TAG");
   var pauschal=new MietkostenPositionDTO("Erdgeschoss mit Wiese",1,n("15"),n("15"),1L,"BUCHUNG");
   var dto=new BuchungFinanzenDTO(f.buchungId(),f.buchungsnummer(),f.mieter(),f.mietobjekt(),f.buchungsquelle(),f.buchungsstatus(),f.anreise(),f.anreise().plusDays(naechte),false,f.anzahlungSoll(),f.kautionSoll(),List.of(pos,pauschal),f.mietrabatt(),f.mieteSoll(),f.verbrauchSoll(),f.verbrauchVorschauVollstaendig(),f.bewegungen(),f.entschaedigungen(),f.entschaedigungSoll());
   var wasser=new BuchungVerbrauchskosten();wasser.setBezeichnung("Wasserpreis");wasser.setAbrechnungstage((int)einheiten);wasser.setPreis(n("10"));wasser.setKosten(n("10").multiply(BigDecimal.valueOf(einheiten)));
   var strom=new BuchungVerbrauchskosten();strom.setBezeichnung("Strompreis");strom.setVerbrauch(n("25"));strom.setMengeneinheit("kWh");strom.setPreis(n("2"));strom.setKosten(n("50"));
   var rows=Abrechnungsbereich.aus(dto,List.of(wasser,strom),1).zeilen();
   assertEquals(naechte==0?"1 Tag":naechte==1?"1 Übernachtung":"2 Übernachtungen",rows.stream().filter(z->z.beschreibung().endsWith("Wasser")).findFirst().orElseThrow().menge());
   assertEquals("25 kWh",rows.stream().filter(z->z.beschreibung().endsWith("Strom")).findFirst().orElseThrow().menge());
   assertEquals(naechte==0?"1 Tag":naechte==1?"1 Übernachtung":"2 Übernachtungen",rows.get(1).menge());
   assertEquals(naechte==0?"1":naechte==1?"1 Übernachtung":"2 Übernachtungen (pauschal)",rows.get(2).menge());assertEquals(Abrechnungsbereich.geld(pos.gesamtbetrag()),rows.get(1).betrag());
  }
 }

 @Test void alleStromzaehlerErgebenEinePositionMitSummenUndGasWasserHabenKurzeNamen(){
  var eg=new BuchungVerbrauchskosten();eg.setBezeichnung("Strompreis");eg.setVerbrauchsart(Verbrauchsart.STROM);eg.setZaehlerBezeichnung("Strom-Zähler Bootshaus");eg.setVerbrauch(n("25"));eg.setMengeneinheit("kWh");eg.setPreis(n("2"));eg.setKosten(n("50"));
  var camping=new BuchungVerbrauchskosten();camping.setBezeichnung("Campingplatz");camping.setVerbrauchsart(Verbrauchsart.STROM);camping.setZaehlerBezeichnung("Camping-Zähler");camping.setVerbrauch(n("5.5"));camping.setMengeneinheit("kWh");camping.setPreis(n("2"));camping.setKosten(n("11"));
  var gas=new BuchungVerbrauchskosten();gas.setBezeichnung("Gaspreis");gas.setVerbrauchsart(Verbrauchsart.GAS);gas.setZaehlerBezeichnung("Gaszähler");gas.setVerbrauch(n("0.1"));gas.setMengeneinheit("m3");gas.setPreis(n("3"));gas.setKosten(n("0.30"));
  var wasser=new BuchungVerbrauchskosten();wasser.setBezeichnung("Wasserpreis");wasser.setVerbrauchsart(Verbrauchsart.WASSER);wasser.setAbrechnungstage(2);wasser.setPreis(n("10"));wasser.setKosten(n("20"));
  var rows=Abrechnungsbereich.aus(AbrechnungVorlagenTest.finanzen(n("1000")),List.of(eg,gas,camping,wasser),1).zeilen();
  var strom=rows.stream().filter(z->z.beschreibung().matches("[0-9]+\\. Strom")).toList();assertEquals(1,strom.size());
  assertEquals("30,5 kWh",strom.getFirst().menge());assertEquals(Abrechnungsbereich.geld(n("61")),strom.getFirst().betrag());assertEquals(Abrechnungsbereich.geld(n("2")),strom.getFirst().preis());
  assertTrue(rows.stream().anyMatch(z->z.beschreibung().equals("5. Gas")));assertTrue(rows.stream().anyMatch(z->z.beschreibung().equals("6. Wasser")&&z.menge().equals("2 Übernachtungen")));
  assertFalse(rows.stream().anyMatch(z->z.beschreibung().contains("Zähler")||z.beschreibung().contains("preis")));assertEquals(n("25"),eg.getVerbrauch());
  camping.setPreis(n("3"));camping.setKosten(n("16.50"));
  var mixed=Abrechnungsbereich.aus(AbrechnungVorlagenTest.finanzen(n("1000")),List.of(eg,camping),1).zeilen().stream().filter(z->z.beschreibung().endsWith(". Strom")).findFirst().orElseThrow();
  assertEquals("",mixed.preis());assertEquals(Abrechnungsbereich.geld(n("66.50")),mixed.betrag());
 }

}
