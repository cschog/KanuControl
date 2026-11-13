package com.kcserver.vermietung.service;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.*;
import org.springframework.transaction.support.SimpleTransactionStatus;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungsnachrichtWorkflowTest {
 BuchungsnachrichtService service;Buchung b;BuchungRepository br;BuchungService bs;BuchungFinanzenService fs;VorlagenService vs;HomepageStatusMailService mails;
 BuchungsnachrichtEntwurfRepository er;VorlagenVersandRepository ar;VermietungAbrechnungRepository rr;VerbrauchsabrechnungService verbrauch;
 Map<String,BuchungsnachrichtEntwurf> drafts;Map<String,VorlagenVersand> archives;List<VermietungAbrechnung> documents;
 byte[] original;ObjectMapper mapper;
 @BeforeEach void setup()throws Exception{
  TenantContext.setCurrentTenant("ekc_test");SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("alice","n/a",List.of()));
  b=AbrechnungVorlagenTest.buchung();b.setStatus(Buchungsstatus.ANFRAGE);b.setZahlungsvariante(Zahlungsvariante.BEI_ABRECHNUNG);b.getMieter().setEmail("tenant@example.org");b.setAnreise(LocalDate.now(ZoneId.of("Europe/Berlin")).plusDays(56).atTime(16,0));
  drafts=new HashMap<>();archives=new HashMap<>();documents=new ArrayList<>();mapper=new ObjectMapper().findAndRegisterModules();
  br=mock(BuchungRepository.class);bs=mock(BuchungService.class);fs=mock(BuchungFinanzenService.class);vs=mock(VorlagenService.class);mails=mock(HomepageStatusMailService.class);
  er=mock(BuchungsnachrichtEntwurfRepository.class);ar=mock(VorlagenVersandRepository.class);rr=mock(VermietungAbrechnungRepository.class);verbrauch=mock(VerbrauchsabrechnungService.class);
  var papier=mock(MandantenBriefpapierRepository.class);var p=new MandantenBriefpapier();original=BriefpapierPdf.render(VorlagensystemTest.geruest(),Map.of(),List.of("Original"),"Gruß",null,List.of(),"2026-1");p.setPdf(VorlagensystemTest.geruest());p.setVersion(1L);p.setGruss("Gruß");when(papier.findById(1L)).thenReturn(Optional.of(p));
  when(br.findLockedById(1L)).thenReturn(Optional.of(b));
  when(fs.toDto(b)).thenAnswer(i->finanzen());when(fs.toDto(eq(b),anyList())).thenAnswer(i->finanzen());
  when(er.saveAndFlush(any())).thenAnswer(i->{BuchungsnachrichtEntwurf e=i.getArgument(0);e.setVersion(e.getVersion()==null?0L:e.getVersion()+1);drafts.put(e.getId(),e);return e;});
  when(er.gesperrt(anyString(),anyString(),anyString(),anyLong())).thenAnswer(i->{var e=drafts.get(i.getArgument(0));return e!=null&&e.getBenutzer().equals(i.getArgument(1))&&e.getTenant().equals(i.getArgument(2))&&e.getBuchungId().equals(i.getArgument(3))?Optional.of(e):Optional.empty();});
  when(ar.saveAndFlush(any())).thenAnswer(i->{VorlagenVersand a=i.getArgument(0);archives.put(a.getId(),a);return a;});when(ar.findById(anyString())).thenAnswer(i->Optional.ofNullable(archives.get(i.getArgument(0))));
  when(rr.findByBuchungIdOrderByRevisionDesc(1L)).thenAnswer(i->List.copyOf(documents));when(rr.saveAndFlush(any())).thenAnswer(i->{VermietungAbrechnung d=i.getArgument(0);d.setId(20L+documents.size());documents.addFirst(d);return d;});
  when(vs.erstellenVorgang(eq(b),any(),isNull(),any(),anyMap(),anySet(),nullable(Abrechnungsbereich.class))).thenReturn(new VorlagenService.Ergebnis(original,"Betreff","Mailtext","x","x"));
  when(mails.nachrichtVorschau(eq(b),anyString(),anyString())).thenAnswer(i->new HomepageStatusMailService.StatusVorschau("club@example.org",b.getMieter().getEmail(),null,null,i.getArgument(1),i.getArgument(2),false,null));
  when(mails.nachrichtVorbereiten(any(),any(),anyString(),anyString())).thenAnswer(i->new HomepageStatusMailService.ReservierungMail(mock(org.springframework.mail.javamail.JavaMailSender.class),mock(jakarta.mail.internet.MimeMessage.class),i.getArgument(1),i.getArgument(3)));
  var status=mock(VorlagenArchivService.class);doAnswer(i->{var a=archives.get(i.getArgument(0));a.setErgebnis(Boolean.TRUE.equals(i.getArgument(1))?"SMTP_BESTAETIGT":"FEHLER_ODER_UNKLAR");a.setFehler(i.getArgument(2));return null;}).when(status).ergebnis(anyString(),anyBoolean(),nullable(String.class));
  when(bs.anwendenGeprueft(eq(1L),any(),anyList())).thenAnswer(i->{b.setStatus(i.getArgument(1));if(b.getStatus()==Buchungsstatus.ABGESCHLOSSEN)b.setAbschlussRevision(b.getAbrechnungRevision());return new BuchungDTO();});
  var invoice=mock(AbrechnungVorlagenService.class);when(invoice.entwurf(eq(b),anyInt(),isNull(),any(),anyList(),any())).thenReturn(new VorlagenService.Ergebnis(original,"Rechnung","Rechnungsmail","x","x"));when(invoice.entwurf(eq(b),anyInt(),isNull(),any(),anyList(),any(),eq(false))).thenReturn(new VorlagenService.Ergebnis(original,"Rechnung","Rechnungsmail","x","x"));
  var tm=mock(PlatformTransactionManager.class);when(tm.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
  service=new BuchungsnachrichtService(er,br,bs,fs,verbrauch,vs,papier,invoice,rr,mails,ar,status,new BuchungsnachrichtDaten(mock(HomepageAnbindungRepository.class)),mapper,tm);
 }
 BuchungFinanzenDTO finanzen(){var f=AbrechnungVorlagenTest.finanzen(new BigDecimal("1000"));return new BuchungFinanzenDTO(f.buchungId(),f.buchungsnummer(),f.mieter(),f.mietobjekt(),f.buchungsquelle(),b.getStatus().name(),f.anreise(),f.abreise(),false,f.anzahlungSoll(),f.kautionSoll(),f.mietkosten(),f.mietrabatt(),f.mieteSoll(),f.verbrauchSoll(),true,f.bewegungen(),f.entschaedigungen(),f.entschaedigungSoll(),b.getStornoEinbehalt());}
 @AfterEach void cleanup(){TenantContext.clear();SecurityContextHolder.clearContext();}
 BuchungsnachrichtService.Ansicht start(Buchungsstatus ziel){return service.erstellen(1L,new BuchungsnachrichtService.Start(ziel,List.of(),""));}
 @Test void bearbeitetesMehrseitigesPdfUndTexteWerdenExaktVersandtUndArchiviert()throws Exception{
  var d=start(Buchungsstatus.BESTAETIGT);assertEquals(Buchungsstatus.ANFRAGE,b.getStatus());
  var edited=service.aendern(1L,d.id(),new BuchungsnachrichtService.Aenderung(d.version(),"Individueller Betreff","Individueller Text",false));
  byte[] pdf;try(var doc=new org.apache.pdfbox.pdmodel.PDDocument();var out=new java.io.ByteArrayOutputStream()){doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());doc.save(out);pdf=out.toByteArray();}
  var upload=service.hochladen(1L,d.id(),edited.version(),pdf,"Extern.pdf");assertNotEquals(edited.fingerprint(),upload.fingerprint());
  var result=service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(upload.fingerprint(),false));assertTrue(result.ausgefuehrt());assertEquals("SMTP_BESTAETIGT",result.ergebnis());assertEquals(Buchungsstatus.BESTAETIGT,b.getStatus());assertArrayEquals(pdf,archives.get(result.versuchId()).getPdf());assertArrayEquals(pdf,result.pdf());assertEquals("Individueller Text",result.mailtext());
  var arg=org.mockito.ArgumentCaptor.forClass(HomepageStatusMailService.StatusVorschau.class);verify(mails).nachrichtVorbereiten(arg.capture(),eq(pdf),eq("Extern.pdf"),anyString());assertEquals("Individueller Betreff",arg.getValue().betreff());
  service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(result.fingerprint(),false));verify(bs,times(1)).anwendenGeprueft(anyLong(),any(),anyList());verify(mails,times(1)).reservierungSenden(any());verify(vs,never()).speichern(anyLong(),any(),any());
 }
 @Test void stilleBestaetigungBenoetigtKeineMailadresseUndSendetNichts(){b.setStatus(Buchungsstatus.RESERVIERT);b.setZahlungsvariante(Zahlungsvariante.ANZAHLUNG);b.setAnzahlungBetrag(new BigDecimal("50"));b.getMieter().setEmail(null);var d=start(Buchungsstatus.BESTAETIGT);assertTrue(d.ohneMail());assertNull(d.pdf());var result=service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(d.fingerprint(),false));assertTrue(result.ausgefuehrt());verifyNoInteractions(mails);assertTrue(archives.isEmpty());}
 @Test void geaenderteBuchungEntwertetEntwurfUndUpload(){var d=start(Buchungsstatus.BESTAETIGT);b.getMieter().setStrasse("Andere Straße");assertThrows(Exception.class,()->service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(d.fingerprint(),false)));assertThrows(Exception.class,()->service.hochladen(1L,d.id(),d.version(),original,"x.pdf"));verify(bs,never()).anwendenGeprueft(anyLong(),any(),anyList());verify(mails,never()).reservierungSenden(any());}
 @Test void andererBenutzerOderMandantHatKeinenZugriff(){var d=start(Buchungsstatus.BESTAETIGT);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("bob","n/a",List.of()));assertThrows(Exception.class,()->service.lesen(1L,d.id()));SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("alice","n/a",List.of()));TenantContext.setCurrentTenant("anderer");assertThrows(Exception.class,()->service.lesen(1L,d.id()));}
 @Test void stornoFriertErstattungEinOhneRueckzahlungZuBuchen(){b.setStatus(Buchungsstatus.BESTAETIGT);var d=start(Buchungsstatus.STORNIERT);assertEquals(new BigDecimal("1000.00"),d.storno().erstattung());assertNull(b.getStornoDatum());service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(d.fingerprint(),false));assertEquals(d.storno().datum(),b.getStornoDatum());assertEquals(d.storno().erstattung(),b.getStornoErstattung());verify(fs,never()).add(anyLong(),any());}
 @Test void tageswechselEntwertetStorno(){b.setStatus(Buchungsstatus.BESTAETIGT);var d=start(Buchungsstatus.STORNIERT);drafts.get(d.id()).setDatum(LocalDate.now(ZoneId.of("Europe/Berlin")).minusDays(1));assertThrows(Exception.class,()->service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(d.fingerprint(),false)));verify(bs,never()).anwendenGeprueft(anyLong(),any(),anyList());}
 @Test void fehlgeschlagenerVersandKannOhneZweitenStatuswechselWiederholtWerden(){doThrow(new RuntimeException("SMTP" )).doNothing().when(mails).reservierungSenden(any());var d=start(Buchungsstatus.BESTAETIGT);var failed=service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(d.fingerprint(),false));assertTrue(failed.ausgefuehrt());assertEquals("FEHLER_ODER_UNKLAR",failed.ergebnis());var result=service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(failed.fingerprint(),true));assertEquals("SMTP_BESTAETIGT",result.ergebnis());assertEquals(2,archives.size());verify(bs,times(1)).anwendenGeprueft(anyLong(),any(),anyList());}
 @Test void abschlusspreviewSchreibtKeineVerbrauchswerteUndAusfuehrungArchiviertExakteRechnung(){b.setStatus(Buchungsstatus.ABRECHENBAR);when(verbrauch.berechneSnapshots(eq(b),anyList())).thenReturn(List.of());var d=start(Buchungsstatus.ABGESCHLOSSEN);assertEquals(Buchungsstatus.ABRECHENBAR,b.getStatus());assertTrue(documents.isEmpty());verify(verbrauch,never()).berechneUndSpeichere(any(),anyList());service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(d.fingerprint(),false));assertEquals(1,documents.size());assertArrayEquals(original,documents.getFirst().getPdf());assertEquals(1,documents.getFirst().getRevision());}
 @Test void originalWiederherstellenEntwertetDiePruefung(){var d=start(Buchungsstatus.BESTAETIGT);var upload=service.hochladen(1L,d.id(),d.version(),original,"extern.pdf");var restored=service.aendern(1L,d.id(),new BuchungsnachrichtService.Aenderung(upload.version(),upload.betreff(),upload.mailtext(),true));assertNull(restored.externAm());assertNotEquals(upload.fingerprint(),restored.fingerprint());assertArrayEquals(original,restored.pdf());}
 @Test void aktivePdfInhalteUndHeaderInjectionWerdenAbgewiesen()throws Exception{var d=start(Buchungsstatus.BESTAETIGT);assertThrows(Exception.class,()->service.aendern(1L,d.id(),new BuchungsnachrichtService.Aenderung(d.version(),"Betreff\nBcc: jemand", "Text",false)));byte[] unsafe;try(var doc=new org.apache.pdfbox.pdmodel.PDDocument();var out=new java.io.ByteArrayOutputStream()){doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());doc.getDocumentCatalog().setOpenAction(new org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript("alert(1)"));doc.save(out);unsafe=out.toByteArray();}assertThrows(Exception.class,()->service.hochladen(1L,d.id(),d.version(),unsafe,"bad.pdf"));}
 @Test void unabhaengigeVorschauOhneMieterEmailOderVersandkonfiguration(){
  b.getMieter().setEmail(null);
  var v=service.vorschau(1L,new BuchungsnachrichtService.Start(Buchungsstatus.RESERVIERT,List.of(),null));
  assertNull(v.empfaenger());assertArrayEquals(original,v.pdf());assertEquals("Betreff",v.betreff());
  assertEquals(Buchungsstatus.ANFRAGE,b.getStatus());assertTrue(drafts.isEmpty());assertTrue(documents.isEmpty());assertTrue(archives.isEmpty());
  verifyNoInteractions(mails,bs);verify(fs,never()).add(anyLong(),any());
 }
 @Test void unabhaengigeRechnungVorAbschlussUndKautionsabwicklung(){
  b.setStatus(Buchungsstatus.BESTAETIGT);b.setKautionBetrag(new BigDecimal("250"));b.getMieter().setEmail(null);
  when(verbrauch.berechneVorschau(b)).thenReturn(List.of());when(verbrauch.berechneSimulationsSnapshots(b,List.of())).thenReturn(List.of());
  var v=service.vorschau(1L,new BuchungsnachrichtService.Start(Buchungsstatus.ABGESCHLOSSEN,List.of(),null));
  assertTrue(pdfText(v.pdf()).contains("ENTWURF / SIMULATION"));assertEquals("Rechnungsmail",v.mailtext());assertEquals(Buchungsstatus.BESTAETIGT,b.getStatus());
  verifyNoInteractions(bs,mails);assertTrue(drafts.isEmpty());assertTrue(documents.isEmpty());assertTrue(archives.isEmpty());
  verify(verbrauch,never()).berechneUndSpeichere(any(),anyList());verify(fs,never()).add(anyLong(),any());
 }
 @Test void entfernteAblesungenWerdenNurFuerVorschauVerwendetUndAlsHinweisMarkiert(){
  b.setStatus(Buchungsstatus.ABRECHENBAR);b.getMieter().setEmail(null);
  var start=b.getAnreise().minusDays(3);var end=b.getAbreise().plusDays(3);
  var row=new VerbrauchskostenVorschauDTO(7L,"Zähler","Strom",null,null,"kWh",BigDecimal.ONE,BigDecimal.TEN,new BigDecimal("20"),BigDecimal.TEN,start,end,null,BigDecimal.TEN,false,"Abstand",true);
  when(verbrauch.berechneVorschau(b)).thenReturn(List.of(row));when(verbrauch.berechneSnapshots(eq(b),anyList())).thenReturn(List.of());
  var v=service.vorschau(1L,new BuchungsnachrichtService.Start(Buchungsstatus.ABGESCHLOSSEN,List.of(),null));
  assertTrue(v.hinweise().stream().anyMatch(h->h.contains("24 Stunden")));
  verify(verbrauch).berechneSimulationsSnapshots(b,List.of(new AblesungBestaetigungDTO(7L,b.getAnreise(),b.getAbreise(),start,end,BigDecimal.TEN,new BigDecimal("20"))));
  verifyNoInteractions(bs,mails);assertTrue(drafts.isEmpty());
 }
 @Test void abschlussOhneMailBenoetigtKeineAdresseUndArchiviertBearbeiteteRechnung()throws Exception{
  b.setStatus(Buchungsstatus.ABRECHENBAR);b.getMieter().setEmail(null);when(verbrauch.berechneSnapshots(eq(b),anyList())).thenReturn(List.of());
  var d=service.erstellen(1L,new BuchungsnachrichtService.Start(Buchungsstatus.ABGESCHLOSSEN,List.of(),"",false));
  assertTrue(d.ohneMail());assertNull(d.routing());assertArrayEquals(original,d.pdf());assertTrue(documents.isEmpty());
  var uploaded=service.hochladen(1L,d.id(),d.version(),original,"Manuelle-Rechnung.pdf");
  var done=service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(uploaded.fingerprint(),false));
  assertTrue(done.ausgefuehrt());assertTrue(done.ohneMail());assertEquals(Buchungsstatus.ABGESCHLOSSEN,b.getStatus());assertEquals(1,documents.size());assertArrayEquals(uploaded.pdf(),documents.getFirst().getPdf());assertTrue(archives.isEmpty());verifyNoInteractions(mails);
  service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(done.fingerprint(),false));assertEquals(1,documents.size());
 }
 @Test void abschlussMitAusdruecklichemMailversandBleibtMoeglich(){
  b.setStatus(Buchungsstatus.ABRECHENBAR);when(verbrauch.berechneSnapshots(eq(b),anyList())).thenReturn(List.of());
  var d=service.erstellen(1L,new BuchungsnachrichtService.Start(Buchungsstatus.ABGESCHLOSSEN,List.of(),"",true));assertFalse(d.ohneMail());
  var done=service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(d.fingerprint(),false));assertEquals("SMTP_BESTAETIGT",done.ergebnis());assertEquals(1,documents.size());verify(mails).reservierungSenden(any());
 }
 @Test void abschlussOhneMailUmgehtKeineKautionspruefung(){
  b.setStatus(Buchungsstatus.ABRECHENBAR);when(verbrauch.berechneSnapshots(eq(b),anyList())).thenReturn(List.of());doThrow(new IllegalArgumentException("Kaution offen")).when(bs).pruefeKautionsabschluss(b);
  assertThrows(IllegalArgumentException.class,()->service.erstellen(1L,new BuchungsnachrichtService.Start(Buchungsstatus.ABGESCHLOSSEN,List.of(),"",false)));
  assertTrue(drafts.isEmpty());assertTrue(documents.isEmpty());verifyNoInteractions(mails);
 }
 @Test void geaenderteRechnungsdatenEntwertenAuchAbschlussOhneMail(){
  b.setStatus(Buchungsstatus.ABRECHENBAR);when(verbrauch.berechneSnapshots(eq(b),anyList())).thenReturn(List.of());var d=service.erstellen(1L,new BuchungsnachrichtService.Start(Buchungsstatus.ABGESCHLOSSEN,List.of(),"",false));
  b.getMieter().setStrasse("Neue Adresse");assertThrows(Exception.class,()->service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe(d.fingerprint(),false)));assertTrue(documents.isEmpty());assertEquals(Buchungsstatus.ABRECHENBAR,b.getStatus());verifyNoInteractions(mails);
 }
 @Test void veranstalterWechselUndStammdatenAenderungEntwertenDenEntwurf(){
  var v=new com.kcserver.core.entity.Verein();v.setId(42L);v.setName("Schule");v.setStrasse("Schulstraße 1");v.setPlz("12345");v.setOrt("Stadt");b.setVeranstalter(v);
  var d=start(Buchungsstatus.BESTAETIGT);v.setStrasse("Neue Straße 2");
  assertThrows(Exception.class,()->service.lesen(1L,d.id()));
  v.setStrasse("Schulstraße 1");assertDoesNotThrow(()->service.lesen(1L,d.id()));
  b.setVeranstalter(null);assertThrows(Exception.class,()->service.lesen(1L,d.id()));
 }

 private static String pdfText(byte[] bytes){try(var d=org.apache.pdfbox.Loader.loadPDF(bytes)){return new org.apache.pdfbox.text.PDFTextStripper().getText(d);}catch(Exception ex){throw new RuntimeException(ex);}}
 @Test void zukunftssimulationVerbrauchtKeineRechnungsnummerUndFehlendeVerbraeucheWerdenNichtNull(){
  b.setAbreise(b.getAnreise().plusDays(4));
  when(verbrauch.berechneVorschau(b)).thenReturn(List.of());
  when(verbrauch.berechneSimulationsSnapshots(b,List.of())).thenReturn(List.of());
  var v=service.vorschau(1L,new BuchungsnachrichtService.Start(Buchungsstatus.ABGESCHLOSSEN,List.of(),null));
  assertTrue(pdfText(v.pdf()).contains("ENTWURF / SIMULATION"));
  verifyNoInteractions(bs,mails);verify(rr,never()).saveAndFlush(any());assertTrue(documents.isEmpty());
  var missing=new VerbrauchskostenVorschauDTO(7L,"Zähler","Strom",null,null,"kWh",BigDecimal.ONE,null,null,null,null,null,null,null,false,"Endablesung fehlt",false);
  when(verbrauch.berechneVorschau(b)).thenReturn(List.of(missing));
  var error=assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.vorschau(1L,new BuchungsnachrichtService.Start(Buchungsstatus.ABGESCHLOSSEN,List.of(),null)));
  assertTrue(error.getReason().contains("Endablesung fehlt"));
 }

}
