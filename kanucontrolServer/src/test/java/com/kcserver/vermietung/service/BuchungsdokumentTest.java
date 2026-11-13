package com.kcserver.vermietung.service;

import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.*;
import org.springframework.transaction.support.SimpleTransactionStatus;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungsdokumentTest {
 BuchungsnachrichtWorkflowTest f;
 BuchungsdokumentService service;
 void login(String user,String role){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,"n/a",List.of(new SimpleGrantedAuthority(role))));}
 @BeforeEach void setup()throws Exception{
  f=new BuchungsnachrichtWorkflowTest();f.setup();
  when(f.vs.vorlage(anyLong(),any())).thenReturn(new VorlagenModell.Vorlage(1L,"Betreff","Text",List.of(),"Hallo {{vorname}},"));
  when(f.vs.erstellenVorgang(eq(f.b),any(),isNull(),any(),anyMap(),anySet(),isNull(),eq(false))).thenReturn(new VorlagenService.Ergebnis(null,"Betreff","Text","x","x"));
  login("alice","ROLE_VERMIETUNG");
  when(f.er.dokument(anyString(),anyString(),anyString(),anyLong())).thenAnswer(i->{var e=f.drafts.get(i.getArgument(0));return e!=null&&e.isUnabhaengig()&&e.getTenant().equals(i.getArgument(2))&&e.getBuchungId().equals(i.getArgument(3))&&(e.isAktuelleFassung()||e.getBenutzer().equals(i.getArgument(1)))?Optional.of(e):Optional.empty();});
  when(f.er.findByBuchungIdAndTenantAndUnabhaengigTrueAndAktuelleFassungTrue(anyLong(),anyString())).thenAnswer(i->f.drafts.values().stream().filter(e->e.isUnabhaengig()&&e.isAktuelleFassung()&&e.getBuchungId().equals(i.getArgument(0))&&e.getTenant().equals(i.getArgument(1))).toList());
  when(f.ar.findByVersandSchluessel(anyString())).thenAnswer(i->f.archives.values().stream().filter(a->Objects.equals(a.getVersandSchluessel(),i.getArgument(0))).findFirst());
  var status=mock(VorlagenArchivService.class);doAnswer(i->{f.archives.get(i.getArgument(0)).setErgebnis(Boolean.TRUE.equals(i.getArgument(1))?"SMTP_BESTAETIGT":"FEHLER_ODER_UNKLAR");return null;}).when(status).ergebnis(anyString(),anyBoolean(),nullable(String.class));
  var tm=mock(PlatformTransactionManager.class);when(tm.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
  service=new BuchungsdokumentService(f.er,f.br,f.service,f.mails,f.ar,status,f.mapper,tm);
 }
 @AfterEach void cleanup(){f.cleanup();}
 BuchungsdokumentService.Senden request(BuchungsdokumentService.Fassung d){var m=service.vorbereiten(1L,d.id());return new BuchungsdokumentService.Senden(UUID.randomUUID().toString(),m.pruefsumme(),m.routingHash(),"  Individueller Betreff  ","Text\nmit Zeilen\n");}
 @Test void alleDokumentartenUnabhaengigVonHistorischemStatusUndOhneAdresse(){
  f.b.setStatus(Buchungsstatus.ABGESCHLOSSEN);f.b.getMieter().setEmail(null);
  for(var art:List.of(Buchungsstatus.RESERVIERT,Buchungsstatus.BESTAETIGT,Buchungsstatus.ABGELEHNT,Buchungsstatus.STORNIERT)){
   var d=service.vorschau(1L,art);assertFalse(d.gespeichert());service.speichern(1L,d.id(),null);
  }
  assertEquals(4,service.liste(1L).dokumente().size());assertEquals(Buchungsstatus.ABGESCHLOSSEN,f.b.getStatus());verifyNoInteractions(f.bs,f.mails);assertTrue(f.archives.isEmpty());assertTrue(f.documents.isEmpty());
 }
 @Test void vorschauErsetztNieUndSpeichernVerlangtBewussteErsetzung(){
  var d=service.vorschau(1L,Buchungsstatus.RESERVIERT);service.speichern(1L,d.id(),null);var neu=service.vorschau(1L,Buchungsstatus.RESERVIERT);
  assertEquals(d.id(),service.liste(1L).dokumente().getFirst().id());assertThrows(Exception.class,()->service.speichern(1L,neu.id(),null));
  service.speichern(1L,neu.id(),d.id());assertEquals(neu.id(),service.liste(1L).dokumente().getFirst().id());assertArrayEquals(d.pdf(),service.lesen(1L,d.id()).pdf());
 }
 @Test void aenderungWirdErkanntOhneAlteFassungZuVeraendern(){
  var d=service.vorschau(1L,Buchungsstatus.RESERVIERT);service.speichern(1L,d.id(),null);assertFalse(service.lesen(1L,d.id()).datenGeaendert());f.b.getMieter().setStrasse("Neue Straße");
  var alt=service.lesen(1L,d.id());assertTrue(alt.datenGeaendert());assertArrayEquals(d.pdf(),alt.pdf());assertFalse(service.vorschau(1L,Buchungsstatus.RESERVIERT).datenGeaendert());
 }
 @Test void importExportUndManuellerVersandVerwendenExaktDieselbenBytesUndTexte()throws Exception{
  var edited=BriefpapierPdf.render(VorlagensystemTest.geruest(),Map.of(),List.of("Extern bearbeitet"),"Gruß",null,List.of(),"x");
  var d=service.importieren(1L,Buchungsstatus.RESERVIERT,null,edited,"Extern.pdf");assertEquals("Manuell hochgeladen",d.herkunft());assertArrayEquals(edited,service.lesen(1L,d.id()).pdf());
  var r=request(d);var result=service.senden(1L,d.id(),r);assertEquals("SMTP_BESTAETIGT",result.ergebnis());assertEquals(Buchungsstatus.ANFRAGE,f.b.getStatus());assertArrayEquals(edited,f.archives.get(result.id()).getPdf());
  var route=org.mockito.ArgumentCaptor.forClass(HomepageStatusMailService.StatusVorschau.class);verify(f.mails).nachrichtVorbereiten(route.capture(),eq(edited),eq("Extern.pdf"),anyString());assertEquals(r.betreff(),route.getValue().betreff());assertEquals(r.mailtext(),route.getValue().text());assertTrue(f.archives.get(result.id()).getMetadaten().contains(d.pruefsumme()));
  var node=f.mapper.readTree(f.archives.get(result.id()).getMetadaten());assertEquals(r.mailtext(),node.get("mailtext").asText());assertEquals(r.betreff(),node.get("betreff").asText());
  service.senden(1L,d.id(),r);verify(f.mails,times(1)).reservierungSenden(any());verifyNoInteractions(f.bs);verify(f.fs,never()).add(anyLong(),any());
 }
 @Test void ersetzungNachMailvorbereitungKannNiemalsAnhangUnbemerktAustauschen(){
  var d=service.vorschau(1L,Buchungsstatus.RESERVIERT);service.speichern(1L,d.id(),null);var r=request(d);
  var neu=service.vorschau(1L,Buchungsstatus.RESERVIERT);service.speichern(1L,neu.id(),d.id());var sent=service.senden(1L,d.id(),r);assertArrayEquals(d.pdf(),f.archives.get(sent.id()).getPdf());
  assertThrows(Exception.class,()->service.senden(1L,neu.id(),r));assertEquals(1,f.archives.size());
 }
 @Test void ungueltigerImportUndPruefsummenKonfliktBewahrenVorhandeneFassung(){
  var d=service.vorschau(1L,Buchungsstatus.RESERVIERT);service.speichern(1L,d.id(),null);
  assertThrows(Exception.class,()->service.importieren(1L,Buchungsstatus.RESERVIERT,d.id(),"%PDF-broken".getBytes(),"bad.pdf"));assertEquals(d.id(),service.liste(1L).dokumente().getFirst().id());
  var r=request(d);assertThrows(Exception.class,()->service.senden(1L,d.id(),new BuchungsdokumentService.Senden(r.versuch(),"wrong",r.routingHash(),r.betreff(),r.mailtext())));verify(f.mails,never()).reservierungSenden(any());
 }
 @Test void smtpFehlerWirdNichtAutomatischWiederholtUndArchivBleibtUnveraendert(){
  var d=service.vorschau(1L,Buchungsstatus.RESERVIERT);var r=request(d);doThrow(new RuntimeException("SMTP unklar")).when(f.mails).reservierungSenden(any());
  var result=service.senden(1L,d.id(),r);assertEquals("FEHLER_ODER_UNKLAR",result.ergebnis());service.senden(1L,d.id(),r);verify(f.mails,times(1)).reservierungSenden(any());assertArrayEquals(d.pdf(),f.archives.get(result.id()).getPdf());
 }
 @Test void berechtigungenTenantBenutzerUndStatusaktionenSindGetrennt(){
  var d=service.vorschau(1L,Buchungsstatus.RESERVIERT);login("bob","ROLE_VERMIETUNG");assertThrows(Exception.class,()->service.lesen(1L,d.id()));
  login("alice","ROLE_VERMIETUNG");service.speichern(1L,d.id(),null);login("bob","ROLE_VERMIETUNG");assertEquals(d.id(),service.lesen(1L,d.id()).id());
  login("bob","ROLE_USER");assertThrows(Exception.class,()->service.liste(1L));assertThrows(Exception.class,()->service.vorschau(1L,Buchungsstatus.RESERVIERT));
  login("alice","ROLE_ADMIN");TenantContext.setCurrentTenant("anderer");assertThrows(Exception.class,()->service.lesen(1L,d.id()));TenantContext.setCurrentTenant("ekc_test");
  assertThrows(Exception.class,()->f.service.lesen(1L,d.id()));assertThrows(Exception.class,()->f.service.ausfuehren(1L,d.id(),new BuchungsnachrichtService.Freigabe("x",false)));verifyNoInteractions(f.bs);
 }
 @Test void simulationIstImPdfMarkiertUndImportBleibtSimulation()throws Exception{
  f.b.setStatus(Buchungsstatus.BESTAETIGT);when(f.verbrauch.berechneVorschau(f.b)).thenReturn(List.of());when(f.verbrauch.berechneSimulationsSnapshots(eq(f.b),anyList())).thenReturn(List.of());
  var d=service.vorschau(1L,Buchungsstatus.ABGESCHLOSSEN);assertTrue(d.simulation());
  try(var pdf=org.apache.pdfbox.Loader.loadPDF(d.pdf())){assertTrue(new org.apache.pdfbox.text.PDFTextStripper().getText(pdf).contains("ENTWURF / SIMULATION"));}
  var imported=service.importieren(1L,Buchungsstatus.ABGESCHLOSSEN,null,f.original,"Extern.pdf");assertTrue(imported.simulation());assertArrayEquals(f.original,imported.pdf());assertTrue(f.documents.isEmpty());verifyNoInteractions(f.bs);verify(f.verbrauch,never()).berechneUndSpeichere(any(),anyList());
 }
 @Test void abgelaufeneVorschauKannNichtGespeichertOderVersandtWerden(){
  var d=service.vorschau(1L,Buchungsstatus.RESERVIERT);var r=request(d);f.drafts.get(d.id()).setAblauf(Instant.now().minusSeconds(1));assertThrows(Exception.class,()->service.speichern(1L,d.id(),null));assertThrows(Exception.class,()->service.senden(1L,d.id(),r));
 }
 @Test void buchungsOderRoutingAenderungNachVorbereitungVerlangtNeuePruefung(){
  var d=service.vorschau(1L,Buchungsstatus.RESERVIERT);var r=request(d);f.b.getMieter().setEmail("neu@example.org");
  assertThrows(Exception.class,()->service.senden(1L,d.id(),r));verify(f.mails,never()).reservierungSenden(any());
 }
 @Test void migrationsVorschlagMitUndOhneAlteNummerNutztKonfigurierteAnredeUndGruss(){
  var d=service.vorschau(1L,Buchungsstatus.RESERVIERT);var m=service.vorbereiten(1L,d.id());
  assertFalse(m.migrationsText().contains("bisherige Buchungsnummer"));assertTrue(m.migrationsText().startsWith("Hallo "));assertTrue(m.migrationsText().endsWith("Gruß"));
  f.b.setFruehereBuchungsnummer("ALT-4711");m=service.vorbereiten(1L,d.id());assertTrue(m.migrationsText().contains("ALT-4711"));assertTrue(m.migrationsText().contains(f.b.getBuchungsnummer()));
  var data=Map.of("buchungsnummer",f.b.getBuchungsnummer());assertEquals("Neue Nummer: "+f.b.getBuchungsnummer()+".",VorlagenModell.ersetzen("Frühere Buchungsnummer: {{fruehere_buchungsnummer}}. Neue Nummer: {{buchungsnummer}}.",data));
 }
 @Test void verschluesseltePdfWirdVorErsetzungAbgewiesen()throws Exception{
  var d=service.vorschau(1L,Buchungsstatus.RESERVIERT);service.speichern(1L,d.id(),null);byte[] locked;
  try(var pdf=new org.apache.pdfbox.pdmodel.PDDocument();var out=new java.io.ByteArrayOutputStream()){
   pdf.addPage(new org.apache.pdfbox.pdmodel.PDPage());pdf.protect(new org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy("owner","password",new org.apache.pdfbox.pdmodel.encryption.AccessPermission()));pdf.save(out);locked=out.toByteArray();
  }
  assertThrows(Exception.class,()->service.importieren(1L,Buchungsstatus.RESERVIERT,d.id(),locked,"Locked.pdf"));assertArrayEquals(d.pdf(),service.lesen(1L,d.id()).pdf());
 }

}
