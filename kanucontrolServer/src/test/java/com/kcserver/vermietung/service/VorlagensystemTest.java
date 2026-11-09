package com.kcserver.vermietung.service;

import org.junit.jupiter.api.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import org.apache.pdfbox.text.PDFTextStripper;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.core.tenancy.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;

class VorlagensystemTest {
    static byte[] geruest() throws Exception {
        try(var d=new PDDocument();var out=new ByteArrayOutputStream()){
            var p=new PDPage(PDRectangle.A4);d.addPage(p);var f=new PDAcroForm(d);d.getDocumentCatalog().setAcroForm(f);
            var resources=new PDResources();resources.put(COSName.getPDFName("Helv"),new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA));
            f.setDefaultResources(resources);f.setDefaultAppearance("/Helv 10 Tf 0 g");
            int i=0;for(String name:new TreeSet<>(BriefpapierPdf.PFLICHT)){
                var field=new PDTextField(f);field.setPartialName(name);field.getWidgets().getFirst().setRectangle(name.equals("textbereich")?new PDRectangle(44,160,507,430):new PDRectangle(44,610+i++*20,300,18));
                field.getWidgets().getFirst().setPage(p);f.getFields().add(field);p.getAnnotations().add(field.getWidgets().getFirst());
            }d.save(out);return out.toByteArray();
        }
    }
    @AfterEach void cleanup(){TenantContext.clear();}
    @Test void normaleZeilenumbruecheSindErlaubtAberAndereSteuerzeichenNicht(){
        for(String umbruch:List.of("\n","\r\n","\r")){
            assertDoesNotThrow(()->pruefeText("Mit freundlichen Grüßen"+umbruch+"Hildegard Schog",false));
            assertEquals("Hallo Erika\nGruß",ersetzen("Hallo {{vorname}}"+umbruch+"Gruß",Map.of("vorname","Erika")));
        }
        for(String verboten:List.of("\u0000","\u000b","\u007f"))assertThrows(Exception.class,()->pruefeText("Text"+verboten,false));
    }
    @Test void mehrzeiligeVorlagenNormalisierenOhneBetreffOderTitelUmbruecheZuErlauben(){
        var v=new Vorlage(null,"Betreff","Hallo\r\n{{vorname}}",List.of(new Block("Titel","Zeile 1\rZeile 2",Bedingung.IMMER,List.of(new Variante(Bedingung.OG_GEBUCHT,"OG\r\ngebucht")))));
        var n=normalisieren(v);pruefe(n);assertEquals("Hallo\n{{vorname}}",n.mailtext());assertEquals("Zeile 1\nZeile 2",n.bloecke().getFirst().text());assertEquals("OG\ngebucht",n.bloecke().getFirst().varianten().getFirst().text());
        for(String umbruch:List.of("\n","\r\n","\r")){
            assertThrows(Exception.class,()->pruefe(normalisieren(new Vorlage(null,"Betreff"+umbruch+"Zusatz","Mail",v.bloecke()))));
            assertThrows(Exception.class,()->pruefe(normalisieren(new Vorlage(null,"Betreff","Mail",List.of(new Block("Titel"+umbruch+"Zusatz","Text",Bedingung.IMMER,List.of()))))));
        }
    }
    @Test void briefpapierPruefungUndSpeichernAkzeptierenMultipartZeilenumbrueche()throws Exception{
        TenantContext.setCurrentTenant("ekc_test");var repo=mock(MandantenBriefpapierRepository.class);when(repo.findById(1L)).thenReturn(Optional.empty());
        when(repo.saveAndFlush(any())).thenAnswer(i->{MandantenBriefpapier b=i.getArgument(0);b.setVersion(0L);return b;});
        var s=new VorlagenService(repo,mock(MietobjektVorlageRepository.class),mock(MietobjektRepository.class),new ObjectMapper());
        byte[] asset=getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes();
        String lf="Mit freundlichen Grüßen\nHildegard Schog";String multipart=lf.replace("\n","\r\n");
        var probe=s.probe(null,multipart,asset,null,false);
        assertEquals(s.probe(null,lf,asset,null,false).fingerprint(),probe.fingerprint());
        try(var d=Loader.loadPDF(probe.pdf())){String text=new PDFTextStripper().getText(d);assertTrue(text.contains("Mit freundlichen Grüßen"));assertTrue(text.contains("Hildegard Schog"));}
        s.speichern(null,multipart,asset,null,false,probe.fingerprint());
        var saved=org.mockito.ArgumentCaptor.forClass(MandantenBriefpapier.class);verify(repo).saveAndFlush(saved.capture());assertEquals(lf,saved.getValue().getGruss());
    }
    @Test void platzhalterSindTypisiertUndKeineAusdruckssprache(){
        assertEquals("Rest 350,00 €",ersetzen("Rest {{restmiete}} €",Map.of("restmiete","350,00")));
        assertThrows(Exception.class,()->ersetzen("{{miete-anzahlung}}",Map.of()));
        assertThrows(Exception.class,()->ersetzen("{{sql}}",Map.of()));
        assertThrows(Exception.class,()->ersetzen("{{vorname}}",Map.of()));
    }
    @Test void blockUndTextbedingungenEntfernenTitelUndLeereFlaechen(){
        var v=new Vorlage(null,"Betreff","Mail",List.of(new Block("Kaution","{{kaution}}",Bedingung.KAUTION_POSITIV,List.of()),new Block("OG","",Bedingung.IMMER,List.of(new Variante(Bedingung.OG_GEBUCHT,"gebucht"),new Variante(Bedingung.OG_NICHT_GEBUCHT,"nicht gebucht")))));
        assertEquals(List.of("gebucht"),texte(v,Map.of(),EnumSet.of(Bedingung.IMMER,Bedingung.OG_GEBUCHT)));
        assertThrows(Exception.class,()->pruefe(new Vorlage(null,"x","y",Collections.nCopies(6,v.bloecke().getFirst()))));
        assertThrows(Exception.class,()->pruefe(new Vorlage(null,"x\nBcc: test","y",v.bloecke())));
    }
    @Test void uploadPrueftFelderGeometrieUndAktiveInhalte()throws Exception{
        byte[] valid=geruest();assertDoesNotThrow(()->BriefpapierPdf.pruefen(valid));
        try(var d=Loader.loadPDF(valid);var out=new ByteArrayOutputStream()){
            d.getDocumentCatalog().getAcroForm().getFields().removeFirst();d.save(out);assertThrows(Exception.class,()->BriefpapierPdf.pruefen(out.toByteArray()));
        }
        try(var d=Loader.loadPDF(valid);var out=new ByteArrayOutputStream()){
            d.getDocumentCatalog().getCOSObject().setString("JS","alert(1)");d.save(out);assertThrows(Exception.class,()->BriefpapierPdf.pruefen(out.toByteArray()));
        }
        try(var d=Loader.loadPDF(valid);var out=new ByteArrayOutputStream()){
            d.getDocumentCatalog().getAcroForm().getField("textbereich").getWidgets().getFirst().setRectangle(new PDRectangle(44,610,507,220));d.save(out);assertThrows(Exception.class,()->BriefpapierPdf.pruefen(out.toByteArray()));
        }
    }
    @Test void hauptbriefBleibtEinseitigUndAbschlussWirdGeprueft()throws Exception{
        byte[] valid=geruest();
        byte[] pdf=BriefpapierPdf.render(valid,Map.of(),List.of("Titel","Miete 500,00 €"),"Mit freundlichen Grüßen\nHildegard Schog",null,List.of(),"2027-015");
        try(var d=Loader.loadPDF(pdf)){assertEquals(1,d.getNumberOfPages());assertNull(d.getDocumentCatalog().getAcroForm());assertTrue(new PDFTextStripper().getText(d).contains("Hildegard Schog"));assertTrue(d.getPage(0).getAnnotations().stream().noneMatch(a->"Widget".equals(a.getSubtype())));}
        assertThrows(Exception.class,()->BriefpapierPdf.render(valid,Map.of(),Collections.nCopies(100,"Langer Absatz"),"Gruß",null,List.of(),"1"));
    }
    @Test void entschaedigungBeginntAufSeiteZweiUndWirdVollstaendigFortgesetzt()throws Exception{
        var rows=new ArrayList<String>();for(int i=0;i<180;i++)rows.add("1 · Gegenstand "+i+" · VERLUST · 10,00 € · 10,00 €");
        var bytes=BriefpapierPdf.render(geruest(),Map.of(),List.of("Hauptbrief"),"Gruß",null,rows,"2027-015");
        try(var d=Loader.loadPDF(bytes)){
            assertTrue(d.getNumberOfPages()>2);var strip=new PDFTextStripper();strip.setStartPage(1);strip.setEndPage(1);assertFalse(strip.getText(d).contains("Gegenstand"));
            strip.setStartPage(2);strip.setEndPage(d.getNumberOfPages());String rest=strip.getText(d);assertTrue(rest.contains("Gegenstand 179"));assertTrue(rest.contains("Buchung 2027-015"));
            java.nio.file.Files.write(java.nio.file.Path.of("/tmp/kanucontrol-vorlagen-test.pdf"),bytes);
        }
    }
    @Test void tenantFehltUndFremdeRevisionWerdenAbgewiesen()throws Exception{
        var paper=mock(MandantenBriefpapierRepository.class);var templates=mock(MietobjektVorlageRepository.class);var objects=mock(MietobjektRepository.class);
        var s=new VorlagenService(paper,templates,objects,new ObjectMapper());
        assertThrows(Exception.class,s::briefpapier);verifyNoInteractions(paper);
        TenantContext.setCurrentTenant("ekc_test");when(objects.existsById(7L)).thenReturn(true);
        var entity=new MietobjektVorlage();entity.setVersion(2L);when(templates.findByMietobjektIdAndVorgang(7L,Vorgang.RESERVIERUNG)).thenReturn(Optional.of(entity));
        var v=new Vorlage(1L,"B","M",List.of(new Block("Titel","Text",Bedingung.IMMER,List.of())));
        assertThrows(Exception.class,()->s.speichern(7L,Vorgang.RESERVIERUNG,v));verify(templates,never()).saveAndFlush(any());
    }
    @Test void fingerprintEnthaeltDatenUndKonfigurationsrevisionUndTenant()throws Exception{
        TenantContext.setCurrentTenant("ekc_test");var fixture=new ReservierungsbestaetigungTest();fixture.setup();
        var paperRepo=mock(MandantenBriefpapierRepository.class);var templates=mock(MietobjektVorlageRepository.class);var mapper=new ObjectMapper();
        var s=new VorlagenService(paperRepo,templates,mock(MietobjektRepository.class),mapper);
        var paper=new MandantenBriefpapier();paper.setPdf(geruest());paper.setVersion(1L);paper.setGruss("Mit freundlichen Grüßen\nHildegard Schog");when(paperRepo.findById(1L)).thenReturn(Optional.of(paper));
        var v=new Vorlage(1L,"{{buchungsnummer}}","Hallo {{vorname}}",List.of(new Block("Zahlung","Restmiete {{restmiete}} €",Bedingung.IMMER,List.of())));
        var entity=new MietobjektVorlage();entity.setVersion(1L);entity.setInhalt(mapper.writeValueAsString(v));when(templates.findByMietobjektIdAndVorgang(7L,Vorgang.RESERVIERUNG)).thenReturn(Optional.of(entity));
        var basis=fixture.pdf.inhalt(fixture.b,java.time.LocalDate.of(2026,11,9));
        var anredeVorlage=new Vorlage(v.version(),v.betreff(),v.mailtext(),v.bloecke(),"Guten Tag {{vorname}} {{nachname}},");
        var anredeErgebnis=s.erstellen(fixture.b,basis,anredeVorlage);
        try(var doc=Loader.loadPDF(anredeErgebnis.pdf())) {
            assertTrue(new PDFTextStripper().getText(doc).contains("Guten Tag "+fixture.b.getMieter().getVorname()+" "+fixture.b.getMieter().getName()+","));
        }
        assertNotEquals(anredeErgebnis.fingerprint(),s.erstellen(fixture.b,basis,null).fingerprint());
        var a=s.erstellen(fixture.b,basis,null);assertEquals(a.fingerprint(),s.erstellen(fixture.b,basis,null).fingerprint());
        try(var d=Loader.loadPDF(a.pdf())){assertTrue(new PDFTextStripper().getText(d).contains("350,00"));}
        paper.setVersion(2L);assertNotEquals(a.fingerprint(),s.erstellen(fixture.b,basis,null).fingerprint());
        paper.setVersion(1L);TenantContext.setCurrentTenant("anderer_test");assertNotEquals(a.fingerprint(),s.erstellen(fixture.b,basis,null).fingerprint());
        when(templates.findByMietobjektIdAndVorgang(7L,Vorgang.RESERVIERUNG)).thenReturn(Optional.empty());assertThrows(Exception.class,()->s.erstellen(fixture.b,basis,null));
    }
    @Test void briefpapierErfordertGepruefteVorschauUndBildWirdValidiert()throws Exception{
        TenantContext.setCurrentTenant("ekc_test");var repo=mock(MandantenBriefpapierRepository.class);when(repo.findById(1L)).thenReturn(Optional.empty());
        var s=new VorlagenService(repo,mock(MietobjektVorlageRepository.class),mock(MietobjektRepository.class),new ObjectMapper());var asset=geruest();
        var p=s.probe(null,"Gruß",asset,null,false);assertNotNull(p.pdf());
        assertThrows(Exception.class,()->s.speichern(null,"Anderer Gruß",asset,null,false,p.fingerprint()));verify(repo,never()).saveAndFlush(any());
        assertThrows(Exception.class,()->BriefpapierPdf.pruefeBild(new byte[]{1,2,3}));
    }
    @Test void vorbereitetesBootshausGeruestErfuelltDenVertrag()throws Exception{
        byte[] asset=getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes();
        assertDoesNotThrow(()->BriefpapierPdf.pruefen(asset));
        var result=BriefpapierPdf.render(asset,Map.of("empfaenger_name","Erika Mustermann","empfaenger_strasse","Musterstraße 1","empfaenger_ort","52076 Aachen","briefdatum","09.11.2026","buchungsnummer","2026-001","anrede","Hallo Erika,","bank_name","Bank: Musterbank","bank_iban","IBAN: DE00000000000000000000","bank_bic","BIC: MUSTER00"),
            List.of("Reservierung","Vielen Dank für die Anmeldung vom 20.06.2027 bis 22.06.2027.","Mietpreis","Die Miete beträgt 500,00 €.","Zahlung","Die Anzahlung beträgt 150,00 €. Die Restmiete beträgt 350,00 €.","Stornierung","Abmeldungen sind mindestens acht Wochen vor dem Aufenthalt erforderlich.","Nutzungshinweise","Bitte beachtet die Hausregeln und vereinbart die Schlüsselübergabe rechtzeitig."),"Mit freundlichen Grüßen\nHildegard Schog",null,List.of(),"2026-001");
        java.nio.file.Files.write(java.nio.file.Path.of("/tmp/kanucontrol-briefpapier-probe.pdf"),result);
    }
    @Test void veraenderteEmpfaengerSperrenDenVersandVorMimeVorbereitung()throws Exception{
        var configs=mock(HomepageAnbindungRepository.class);var sender=mock(org.springframework.mail.javamail.JavaMailSender.class);
        var pdf=mock(ReservierungsbestaetigungPdf.class);var b=ReservierungsbestaetigungTest.beispiel();
        var smtp=new com.kcserver.vermietung.config.SmtpMailProperties();smtp.setEnabled(true);smtp.setFrom("from@example.org");
        @SuppressWarnings("unchecked") org.springframework.beans.factory.ObjectProvider<org.springframework.mail.javamail.JavaMailSender> provider=mock(org.springframework.beans.factory.ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);when(sender.createMimeMessage()).thenAnswer(i->new jakarta.mail.internet.MimeMessage(jakarta.mail.Session.getInstance(new Properties())));
        when(pdf.entwurf(b,null)).thenReturn(new VorlagenService.Ergebnis(geruest(),"Betreff","Text","daten-hash","revision=1"));
        var config=new HomepageAnbindung();config.setAntwortTestEmail("test@example.org");when(configs.findById(7L)).thenReturn(Optional.of(config));
        try(var vf=jakarta.validation.Validation.buildDefaultValidatorFactory()){
            var s=new HomepageStatusMailService(mock(HomepageStatusMailRepository.class),configs,vf.getValidator(),provider,smtp);
            org.springframework.test.util.ReflectionTestUtils.setField(s,"reservierungsPdf",pdf);org.springframework.test.util.ReflectionTestUtils.setField(s,"vorlagen",mock(VorlagenService.class));
            String fingerprint=s.reservierungVorschau(b).fingerprint();
            assertThrows(Exception.class,()->s.reservierungVorbereiten(b));
            config.setAntwortCcEmail("changed@example.org");assertThrows(Exception.class,()->s.reservierungVorbereiten(b,fingerprint));verify(sender,never()).createMimeMessage();
            String fresh=s.reservierungVorschau(b).fingerprint();var mail=s.reservierungVorbereiten(b,fresh);
            assertArrayEquals(geruestBytes(mail),mail.pdf());assertTrue(mail.metadaten().contains("revision=1"));verify(sender,never()).send(any(jakarta.mail.internet.MimeMessage.class));
        }
    }
    private byte[] geruestBytes(HomepageStatusMailService.ReservierungMail m)throws Exception{
        m.message().saveChanges();var multi=(jakarta.mail.Multipart)m.message().getContent();
        for(int i=0;i<multi.getCount();i++){var part=multi.getBodyPart(i);if(part.getFileName()!=null)return part.getInputStream().readAllBytes();}
        throw new AssertionError("PDF-Anhang fehlt");
    }
    @Test void archivSpeichertGenauDenAnhangUndTrenntVorbereitungVomErfolg(){
        TenantContext.setCurrentTenant("ekc_test");var repo=mock(VorlagenVersandRepository.class);
        when(repo.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));var service=new VorlagenArchivService(repo);var b=ReservierungsbestaetigungTest.beispiel();
        byte[] bytes={1,2,3};var mail=new HomepageStatusMailService.ReservierungMail(null,null,bytes,"revision=4; testmodus=true");
        String id=service.beginnen(b,mail);var captor=org.mockito.ArgumentCaptor.forClass(VorlagenVersand.class);verify(repo).saveAndFlush(captor.capture());var archived=captor.getValue();
        assertEquals("VORBEREITET",archived.getErgebnis());assertArrayEquals(bytes,archived.getPdf());assertNotSame(bytes,archived.getPdf());
        when(repo.findById(id)).thenReturn(Optional.of(archived));service.ergebnis(id,true,null);assertEquals("SMTP_BESTAETIGT",archived.getErgebnis());assertNotNull(archived.getSmtpAm());
    }
}
