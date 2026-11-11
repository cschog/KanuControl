package com.kcserver.vermietung.service;

import com.kcserver.core.entity.Person;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;

class AbrechnungVorlagenTest {
 static BigDecimal n(String s){return new BigDecimal(s);}
 @AfterEach void cleanup(){TenantContext.clear();}
 static Buchung buchung(){
  var b=new Buchung();b.setId(1L);b.setBuchungsnummer("2025-30");b.setBuchungsquelle(Buchungsquelle.DIREKT);b.setStatus(Buchungsstatus.ABGESCHLOSSEN);
  b.setAnreise(LocalDateTime.of(2025,10,3,16,0));b.setAbreise(LocalDateTime.of(2025,10,5,14,0));
  var o=new Mietobjekt();o.setId(7L);o.setBezeichnung("Bootshaus");b.setMietobjekt(o);
  var p=new Person();p.setName("Macherey");p.setVorname("Markus");p.setOrganisation("Pfadfinder Erkelenz e.V.");p.setStrasse("Am Lievendahl 12");p.setPlz("41812");p.setOrt("Erkelenz");p.setBankName("Kreissparkasse Heinsberg");p.setIban("DE96312512201402493231");p.setBic("WELADED1ERK");p.setKontoinhaber("Pfadfinder Erkelenz e.V.");b.setMieter(p);return b;
 }
 static BuchungFinanzenDTO finanzen(BigDecimal bezahlt){
  return new BuchungFinanzenDTO(1L,"2025-30","Markus Macherey","Bootshaus",Buchungsquelle.DIREKT,"ABGESCHLOSSEN",LocalDate.of(2025,10,3),LocalDate.of(2025,10,5),false,n("50"),BigDecimal.ZERO,
    List.of(new MietkostenPositionDTO("Bootshaus EG",1,n("250"),n("500"),2L,"TAG"),new MietkostenPositionDTO("Schlafräume OG",1,n("125"),n("250"),2L,"TAG"),new MietkostenPositionDTO("Grill",1,n("15"),n("15"),1L,"BUCHUNG")),BigDecimal.ZERO,n("765"),n("136"),true,
    List.of(new FinanzbewegungDTO(1L,Finanzkategorie.ANZAHLUNG,Finanzbewegungsart.EINGANG,n("50"),LocalDate.of(2025,2,24),""),new FinanzbewegungDTO(2L,Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,bezahlt.subtract(n("50")),LocalDate.of(2025,9,1),"")),List.of(),BigDecimal.ZERO);
 }
 static Vorlage vorlage(){return new Vorlage(null,"Abrechnung {{buchungsnummer}}","Anbei die Abrechnung.",List.of(
  new Block("Intro","Aufenthalt {{anreise}} bis {{abreise}}",Bedingung.IMMER,List.of()),
  new Block("Zahlung","ZAHLUNGSAUFFORDERUNG {{restzahlung}}",Bedingung.RESTZAHLUNG_POSITIV,List.of()),
  new Block("Erstattung","ERSTATTUNGSTEXT {{erstattung}}\n{{mieter_bank_name}}\n{{mieter_bank_iban}}\n{{mieter_bank_bic}}\n{{mieter_kontoinhaber}}",Bedingung.ERSTATTUNG_POSITIV,List.of()),
  new Block("Ausgeglichen","AUSGEGLICHENTEXT",Bedingung.AUSGEGLICHEN,List.of())));}
 @Test void rechnungMitZweiZahlungenUndErstattungOhneZahlungsaufforderung()throws Exception {renderFall(n("1000"),"ERSTATTUNGSTEXT",List.of("ZAHLUNGSAUFFORDERUNG","AUSGEGLICHENTEXT"));}
 @Test void restzahlungOhneErstattungstextOderErstattungskonto()throws Exception {renderFall(n("500"),"ZAHLUNGSAUFFORDERUNG",List.of("ERSTATTUNGSTEXT","Kreissparkasse"));}
 @Test void ausgeglichenOhneBeideZahlungstexte()throws Exception {renderFall(n("901"),"AUSGEGLICHENTEXT",List.of("ERSTATTUNGSTEXT","ZAHLUNGSAUFFORDERUNG"));}
 void renderFall(BigDecimal bezahlt,String erwartet,List<String> verboten)throws Exception {
  TenantContext.setCurrentTenant("ekc_test");var b=buchung();var vs=mock(VorlagenService.class);var pr=mock(MandantenBriefpapierRepository.class);var vr=mock(BuchungVerbrauchskostenRepository.class);var fs=mock(BuchungFinanzenService.class);
  var p=new MandantenBriefpapier();p.setPdf(getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes());p.setGruss("Mit freundlichen Grüßen\nHildegard Schog");
  when(pr.findById(1L)).thenReturn(Optional.of(p));when(vs.vorlage(7L,Vorgang.ABGERECHNET)).thenReturn(vorlage());when(fs.toDto(b)).thenReturn(finanzen(bezahlt));
  var strom=new BuchungVerbrauchskosten();strom.setBezeichnung("Strom");strom.setVerbrauch(n("27.0"));strom.setMengeneinheit("kWh");strom.setPreis(n("1"));strom.setKosten(n("27"));
  var gas=new BuchungVerbrauchskosten();gas.setBezeichnung("Gas");gas.setVerbrauch(n("35.6"));gas.setMengeneinheit("m³");gas.setPreis(n("2.5"));gas.setKosten(n("89"));
  var wasser=new BuchungVerbrauchskosten();wasser.setBezeichnung("Wasser");wasser.setAbrechnungstage(2);wasser.setPreis(n("10"));wasser.setKosten(n("20"));when(vr.findByBuchungIdOrderByIdAsc(1L)).thenReturn(List.of(strom,gas,wasser));
  for(var row:finanzen(bezahlt).mietkosten()){var position=new BuchungMietbereich();position.setBuchung(b);position.setAnzahl(row.anzahl());position.setVereinbarterPreis(row.einzelpreis());position.setBerechnungsart(Berechnungsart.valueOf(row.berechnungsart()));b.getMietbereiche().add(position);}
  var service=new AbrechnungVorlagenService(vs,pr,vr,fs);byte[] bytes=service.erstellen(b,2,null);
  try(var doc=Loader.loadPDF(bytes)){var text=new PDFTextStripper().getText(doc);assertTrue(text.contains(erwartet),text);for(var s:verboten)assertFalse(text.contains(s),text);assertTrue(text.contains("24.02.2025"));assertTrue(text.contains("01.09.2025"));assertTrue(text.contains("Strom"));assertTrue(text.contains("Gas"));assertTrue(text.contains("Revision 2"));assertTrue(text.contains("Pfadfinder Erkelenz"));}
  if(bezahlt.compareTo(n("1000"))==0){assertEquals(n("-99.00"),finanzen(bezahlt).saldo());java.nio.file.Files.write(java.nio.file.Path.of("/tmp/kc-rechnung-erstattung.pdf"),bytes);b.getMieter().setIban(null);assertThrows(Exception.class,()->service.erstellen(b,3,null));}
 }
 @ParameterizedTest @EnumSource(Zahlungsvariante.class)
 void zahlungsvariantenSindExplizitUndKeineZahlungseingaenge(Zahlungsvariante mode){var b=buchung();b.setZahlungsvariante(mode);b.setAnzahlungBetrag(n("50"));assertEquals(mode==Zahlungsvariante.ANZAHLUNG?n("50"):BigDecimal.ZERO,Zahlungsplan.anzahlung(b));if(mode!=Zahlungsvariante.ANZAHLUNG)assertEquals(BigDecimal.ZERO,Zahlungsplan.restmiete(b));}
 @ParameterizedTest @EnumSource(Zahlungsvariante.class)
 void reservierungsPdfZeigtNurPassendenZahlungsplan(Zahlungsvariante mode)throws Exception {
  TenantContext.setCurrentTenant("ekc_test");var b=buchung();b.setZahlungsvariante(mode);b.setAnzahlungBetrag(n("50"));b.setKautionBetrag(BigDecimal.ZERO);
  var pos=new BuchungMietbereich();pos.setBuchung(b);pos.setAnzahl(1);pos.setVereinbarterPreis(n("500"));pos.setBerechnungsart(Berechnungsart.BUCHUNG);b.getMietbereiche().add(pos);
  var pr=mock(MandantenBriefpapierRepository.class);var p=new MandantenBriefpapier();p.setPdf(getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes());p.setGruss("Gruß");when(pr.findById(1L)).thenReturn(Optional.of(p));
  var service=new VorlagenService(pr,mock(MietobjektVorlageRepository.class),mock(MietobjektRepository.class),new com.fasterxml.jackson.databind.ObjectMapper());
  var v=new Vorlage(null,"Betreff","Mail",List.of(new Block("Plan","",Bedingung.IMMER,List.of(
   new Variante(Bedingung.ANZAHLUNG_POSITIV,"ANZAHLUNGSTEXT {{anzahlung}}"),new Variante(Bedingung.RESTMIETE_POSITIV,"RESTMIETTEXT {{restmiete}}"),new Variante(Bedingung.ZAHLUNG_GESAMTMIETE_VORAUS,"VORAUSTEXT {{mietbetrag}}"),new Variante(Bedingung.ZAHLUNG_BEI_ABRECHNUNG,"ABRECHNUNGSTEXT"),new Variante(Bedingung.KAUTION_POSITIV,"KAUTIONSTEXT {{kaution}}")))));
  var basis=new ReservierungsbestaetigungPdf.Inhalt(Map.of("briefdatum","11.11.2026","bank_name","Bank: Clubbank","bank_iban","IBAN: CLUBIBAN","bank_bic","BIC: CLUBBIC"),false,"https://example.org");
  var result=service.erstellen(b,basis,v);
  try(var doc=Loader.loadPDF(result.pdf())){var text=new PDFTextStripper().getText(doc);assertEquals(mode==Zahlungsvariante.ANZAHLUNG,text.contains("ANZAHLUNGSTEXT"));assertEquals(mode==Zahlungsvariante.ANZAHLUNG,text.contains("RESTMIETTEXT"));assertEquals(mode==Zahlungsvariante.GESAMTMIETE_VORAUS,text.contains("VORAUSTEXT"));assertEquals(mode==Zahlungsvariante.BEI_ABRECHNUNG,text.contains("ABRECHNUNGSTEXT"));assertFalse(text.contains("KAUTIONSTEXT"));}
 }
 @Test void kurzfristgrenzeUndFristen(){var heute=LocalDate.of(2026,11,11);assertEquals(Zahlungsvariante.GESAMTMIETE_VORAUS,Zahlungsplan.vorschlag(heute.plusDays(41).atStartOfDay(),42,heute));assertEquals(Zahlungsvariante.ANZAHLUNG,Zahlungsplan.vorschlag(heute.plusDays(42).atStartOfDay(),42,heute));assertEquals(Zahlungsvariante.ANZAHLUNG,Zahlungsplan.vorschlag(heute.atStartOfDay(),0,heute));assertEquals(heute,Zahlungsplan.frist(heute.plusDays(2),heute));}
 @Test void langeTabelleWiederholtKopfUndVerliertKeineZeile()throws Exception {
  byte[] papier=getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes();var rows=new ArrayList<Abrechnungsbereich.Zeile>();for(int i=0;i<120;i++)rows.add(new Abrechnungsbereich.Zeile("Position "+i+" mit mehrzeiliger Beschreibung zur Prüfung des Seitenumbruchs","2 Tage","10,00 €","20,00 €"));
  byte[] bytes=AbrechnungBriefLayout.render(papier,Map.of(),List.of("Einleitung","Abschließender Zahlungstext"),"Mit freundlichen Grüßen",null,"2025-30",new Abrechnungsbereich(rows));
  try(var doc=Loader.loadPDF(bytes)){assertTrue(doc.getNumberOfPages()>2);String text=new PDFTextStripper().getText(doc);assertTrue(text.contains("Position 119"));assertTrue(text.contains("Abschließender Zahlungstext"));assertEquals(doc.getNumberOfPages(),text.split("Position / Zahlung",-1).length-1);assertTrue(text.contains("Seite 2"));}java.nio.file.Files.write(java.nio.file.Path.of("/tmp/kc-rechnung-lang.pdf"),bytes);
 }
}
