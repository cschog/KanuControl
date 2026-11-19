package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.core.tenancy.TenantContext;
import org.junit.jupiter.api.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class JahresabrechnungTest {
 Buchung b;BuchungRepository br;VermietungAbrechnungRepository dr;BuchungFinanzenService fs;AbrechnungVorlagenService renderer;JahresabrechnungService service;int jahr;
 BigDecimal n(String s){return new BigDecimal(s);}
 @BeforeEach void setup(){
  jahr=LocalDate.now(ZoneId.of("Europe/Berlin")).getYear();b=AbrechnungVorlagenTest.buchung();b.setStatus(Buchungsstatus.BESTAETIGT);b.setAnreise(LocalDateTime.of(jahr-1,5,1,12,0));b.setAbreise(LocalDateTime.of(3000,12,31,14,0));b.setKautionBetrag(n("200"));
  var mb=new Mietbereich();mb.setBezeichnung("Bootslager");var pos=new BuchungMietbereich();pos.setMietbereich(mb);pos.setBuchung(b);pos.setAnzahl(1);pos.setVereinbarterPreis(n("300"));pos.setBerechnungsart(Berechnungsart.GANZJAEHRIG);b.getMietbereiche().add(pos);
  br=mock(BuchungRepository.class);dr=mock(VermietungAbrechnungRepository.class);fs=mock(BuchungFinanzenService.class);renderer=mock(AbrechnungVorlagenService.class);
  when(br.findById(1L)).thenReturn(Optional.of(b));when(br.findLockedById(1L)).thenReturn(Optional.of(b));when(fs.toDto(b,List.of())).thenReturn(finanzen());
  when(renderer.jahresentwurf(eq(b),any(),eq(jahr),any())).thenReturn(new VorlagenService.Ergebnis(new byte[]{1,2},"Jahresabrechnung","","template-v1",""));
  when(dr.saveAndFlush(any())).thenAnswer(i->{var e=(VermietungAbrechnung)i.getArgument(0);if(e.getId()==null)e.setId(5L);when(dr.findByBuchungIdAndAbrechnungsJahr(1L,jahr)).thenReturn(Optional.of(e));return e;});
  service=new JahresabrechnungService(br,dr,fs,renderer);
 }
 FinanzbewegungDTO zahl(long id,Finanzkategorie k,Finanzbewegungsart a,String wert,int y){return new FinanzbewegungDTO(id,k,a,n(wert),LocalDate.of(y,1,1),"");}
 BuchungFinanzenDTO finanzen(){return new BuchungFinanzenDTO(1L,b.getBuchungsnummer(),"Markus","Bootshaus",Buchungsquelle.DIREKT,b.getStatus().name(),b.getAnreise().toLocalDate(),b.getAbreise().toLocalDate(),true,n("50"),n("200"),List.of(new MietkostenPositionDTO("Bootslager",1,n("300"),n("300"),1,"GANZJAEHRIG")),BigDecimal.ZERO,n("300"),n("88"),false,List.of(zahl(1,Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"900",jahr-1),zahl(2,Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"100",jahr),zahl(3,Finanzkategorie.ANZAHLUNG,Finanzbewegungsart.EINGANG,"50",jahr),zahl(4,Finanzkategorie.KAUTION,Finanzbewegungsart.EINGANG,"200",jahr),zahl(5,Finanzkategorie.VERBRAUCHSKOSTEN,Finanzbewegungsart.EINGANG,"88",jahr),zahl(6,Finanzkategorie.MIETE,Finanzbewegungsart.RUECKZAHLUNG,"20",jahr)),List.of(),BigDecimal.ZERO);}
 @AfterEach void cleanup(){TenantContext.clear();}
 @Test void jederzeitImJahrOhneEndeStatusOderKautionZuAendern(){
  for(int monat=1;monat<=12;monat++){var tag=LocalDate.of(jahr,monat,1);assertDoesNotThrow(()->JahresabrechnungService.pruefe(b,jahr,tag));}
  var v=service.vorschau(1L,jahr);assertEquals(n("170.00"),v.saldo());var d=service.abschliessen(1L,jahr,new JahresabrechnungService.Auftrag(v.fingerprint()));
  assertEquals(jahr,d.jahr());assertEquals(Buchungsstatus.BESTAETIGT,b.getStatus());assertEquals(LocalDateTime.of(3000,12,31,14,0),b.getAbreise());assertEquals(n("200"),b.getKautionBetrag());assertEquals(0,b.getAbschlussRevision());
  verify(br).findLockedById(1L);verify(fs,atLeastOnce()).toDto(b,List.of());verify(fs,never()).toDto(b);
 }
 @Test void jahresfilterSchliesstKautionUndVerbrauchAus(){var f=JahresabrechnungService.jahresfinanzen(finanzen(),jahr);assertEquals(3,f.bewegungen().size());assertEquals(BigDecimal.ZERO,f.kautionSoll());assertEquals(BigDecimal.ZERO,f.verbrauchSoll());assertEquals(n("170.00"),JahresabrechnungService.saldo(f));assertTrue(f.unbefristet());}
 @Test void erneutesErstellenErsetztDasselbeDokument(){var v=service.vorschau(1L,jahr);var d=service.abschliessen(1L,jahr,new JahresabrechnungService.Auftrag(v.fingerprint()));var neu=service.vorschau(1L,jahr);assertNotEquals(v.fingerprint(),neu.fingerprint());var ersetzt=service.abschliessen(1L,jahr,new JahresabrechnungService.Auftrag(neu.fingerprint()));assertEquals(d.id(),ersetzt.id());assertEquals(2,dr.findByBuchungIdAndAbrechnungsJahr(1L,jahr).orElseThrow().getRevision());}
 @Test void alterEntwurfNachZahlungsaenderungWirdAbgelehnt(){var v=service.vorschau(1L,jahr);b.setAbrechnungRevision(3);assertThrows(IllegalArgumentException.class,()->service.abschliessen(1L,jahr,new JahresabrechnungService.Auftrag(v.fingerprint())));verify(dr,never()).saveAndFlush(any());}
 @Test void auchGeaenderteVorlageMachtEntwurfUngueltig(){var v=service.vorschau(1L,jahr);when(renderer.jahresentwurf(eq(b),any(),eq(jahr),any())).thenReturn(new VorlagenService.Ergebnis(new byte[]{3},"Jahresabrechnung","","template-v2",""));assertThrows(IllegalArgumentException.class,()->service.abschliessen(1L,jahr,new JahresabrechnungService.Auftrag(v.fingerprint())));}
 @Test void nurLaufendesJahrUndAktiveUnbefristeteBuchung(){assertThrows(Exception.class,()->service.vorschau(1L,jahr-1));assertThrows(Exception.class,()->service.vorschau(1L,jahr+1));b.setStatus(Buchungsstatus.STORNIERT);assertThrows(Exception.class,()->service.vorschau(1L,jahr));b.setStatus(Buchungsstatus.BESTAETIGT);b.setAbreise(LocalDateTime.of(jahr,12,31,12,0));assertThrows(Exception.class,()->service.vorschau(1L,jahr));b.setAbreise(LocalDateTime.of(3000,12,31,14,0));b.setAnreise(LocalDateTime.of(jahr,12,1,12,0));assertDoesNotThrow(()->service.vorschau(1L,jahr));b.setAnreise(LocalDateTime.of(jahr+1,1,1,12,0));assertThrows(Exception.class,()->service.vorschau(1L,jahr));}
 @Test void ohneAbrechnungUndAirbnbBrauchenKeineVorlage(){for(var quelle:List.of(Buchungsquelle.DIREKT,Buchungsquelle.AIRBNB)){b.setBuchungsquelle(quelle);b.setZahlungsvariante(Zahlungsvariante.OHNE_ABRECHNUNG);var v=service.vorschau(1L,jahr);assertNull(v.pdfBase64());assertFalse(service.abschliessen(1L,jahr,new JahresabrechnungService.Auftrag(v.fingerprint())).pdfVorhanden());}verifyNoInteractions(renderer);}
 @Test void pdfHatJahrUndKeinFiktivesMietendeOderKautionsrueckzahlung()throws Exception {
  TenantContext.setCurrentTenant("ekc_test");var vs=mock(VorlagenService.class);var pr=mock(MandantenBriefpapierRepository.class);var papier=new MandantenBriefpapier();papier.setPdf(getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes());papier.setGruss("Mit freundlichen Grüßen");when(pr.findById(1L)).thenReturn(Optional.of(papier));when(vs.vorlage(7L,VorlagenModell.Vorgang.ABGERECHNET)).thenReturn(AbrechnungVorlagenTest.vorlage());
  var real=new AbrechnungVorlagenService(vs,pr,mock(BuchungVerbrauchskostenRepository.class),fs);var result=real.jahresentwurf(b,JahresabrechnungService.jahresfinanzen(finanzen(),jahr),jahr,LocalDate.of(jahr,2,1));
  try(var pdf=Loader.loadPDF(result.pdf())){var text=new PDFTextStripper().getText(pdf);assertTrue(text.contains("Jahresabrechnung "+jahr),text);assertTrue(text.contains("unbefristet weiter"));assertTrue(text.contains("170,00"));assertFalse(text.contains("3000"));assertFalse(text.contains("Kautionsanforderung / Kategorieausgleich"));assertFalse(text.contains("Bereits gezahlt am 01.01."+(jahr-1)));}
  java.nio.file.Files.write(java.nio.file.Path.of("/private/tmp/kc-jahresabschluss/jahresabrechnung.pdf"),result.pdf());
 }
}
