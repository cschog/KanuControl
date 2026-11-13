package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.controller.ReservierungsbestaetigungController;
import com.kcserver.vermietung.config.SmtpMailProperties;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Validation;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservierungMailTest {
    @Test void mimeAnhangUndTextNutzenTestRoutingOhneCc() throws Exception {
        try(var factory=Validation.buildDefaultValidatorFactory()) {
            var configs=mock(HomepageAnbindungRepository.class);var sender=mock(JavaMailSender.class);
            var pdf=mock(ReservierungsbestaetigungPdf.class);var b=ReservierungsbestaetigungTest.beispiel();
            var smtp=new SmtpMailProperties();smtp.setEnabled(true);smtp.setFrom("absender@example.org");
            @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> provider=mock(ObjectProvider.class);
            when(provider.getIfAvailable()).thenReturn(sender);
            when(sender.createMimeMessage()).thenAnswer(i->new MimeMessage(Session.getInstance(new Properties())));
            var service=new HomepageStatusMailService(mock(HomepageStatusMailRepository.class),configs,factory.getValidator(),provider,smtp);
            ReflectionTestUtils.setField(service,"reservierungsPdf",pdf);
            byte[] actualPdf=java.nio.file.Files.readAllBytes(java.nio.file.Path.of("src/main/resources/pdf/Reservierungsbestaetigung.pdf"));
            when(pdf.erstellen(b)).thenReturn(new ReservierungsbestaetigungPdf.Dokument(actualPdf,"Reservierungsbestaetigung_2027-015.pdf"));
            var config=new HomepageAnbindung();config.setAntwortTestEmail("objekttest@example.org");config.setAntwortCcEmail("kopie@example.org");
            config.setEmpfaengerEmail("antwort@example.org");when(configs.findById(7L)).thenReturn(Optional.of(config));
            var vorbereitet=service.reservierungVorbereiten(b);var message=vorbereitet.message();message.saveChanges();
            assertEquals("[TEST] 2027-015 Reservierungsbestätigung",message.getSubject());
            assertEquals("objekttest@example.org",message.getRecipients(Message.RecipientType.TO)[0].toString());
            assertNull(message.getRecipients(Message.RecipientType.CC));
            assertEquals("antwort@example.org",message.getReplyTo()[0].toString());
            var parts=new ArrayList<Part>();collect(message,parts);
            var attachments=parts.stream().filter(p->{try{return p.getFileName()!=null;}catch(Exception e){throw new RuntimeException(e);}}).toList();
            assertEquals(1,attachments.size());assertEquals("Reservierungsbestaetigung_2027-015.pdf",attachments.getFirst().getFileName());
            assertArrayEquals(actualPdf,attachments.getFirst().getInputStream().readAllBytes());
            assertTrue(parts.stream().anyMatch(p->{try{return p.isMimeType("text/plain")&&HomepageStatusMailService.RESERVIERUNG_TEXT.equals(p.getContent());}catch(Exception e){throw new RuntimeException(e);}}));
            verify(sender,never()).send(any(MimeMessage.class));
            service.reservierungSenden(vorbereitet);verify(sender).send(message);
            smtp.setTestMode(true);smtp.setTestRecipient("globaltest@example.org");
            var global=service.reservierungVorbereiten(b).message();
            assertEquals("globaltest@example.org",global.getRecipients(Message.RecipientType.TO)[0].toString());assertNull(global.getRecipients(Message.RecipientType.CC));
        }
    }
    private static void collect(Part part,List<Part> result)throws Exception{
        if(part.isMimeType("multipart/*")){var multi=(Multipart)part.getContent();for(int i=0;i<multi.getCount();i++)collect(multi.getBodyPart(i),result);}
        else result.add(part);
    }
    @Test void versandfehlerBleibtSichtbarWiederholungBrauchtKeinenStatuswechselUndErfolgSperrtDoppelversand() {
        var repo=mock(BuchungRepository.class);var mails=mock(HomepageStatusMailService.class);
        var service=new ReservierungsversandService(repo,mails);var b=ReservierungsbestaetigungTest.beispiel();b.setStatus(Buchungsstatus.RESERVIERT);
        when(repo.findLockedById(11L)).thenReturn(Optional.of(b));var prepared=mock(HomepageStatusMailService.ReservierungMail.class);
        when(mails.reservierungVorbereiten(b)).thenReturn(prepared);
        doThrow(new MailSendException("offline")).doNothing().when(mails).reservierungSenden(prepared);
        assertNotNull(service.versenden(11L,null));assertNotNull(b.getReservierungMailFehler());assertNull(b.getReservierungMailGesendetAm());
        assertEquals(Buchungsstatus.RESERVIERT,b.getStatus());assertNull(service.versenden(11L,null));
        assertNull(b.getReservierungMailFehler());assertNotNull(b.getReservierungMailGesendetAm());
        assertThrows(ResponseStatusException.class,()->service.versenden(11L,null));verify(mails,times(2)).reservierungSenden(prepared);
    }
    @Test void vorschauIstReadOnlyUndSendetNicht() {
        var repo=mock(BuchungRepository.class);var mails=mock(HomepageStatusMailService.class);var versand=mock(ReservierungsversandService.class);
        var b=ReservierungsbestaetigungTest.beispiel();when(repo.findById(11L)).thenReturn(Optional.of(b));
        var controller=new ReservierungsbestaetigungController(repo,mails,versand);
        assertEquals("no-store",controller.vorschau(11L).getHeaders().getCacheControl());
        assertEquals(Buchungsstatus.ANFRAGE,b.getStatus());verify(mails).reservierungVorschau(b);
        verifyNoInteractions(versand);verify(repo,never()).save(any());verify(repo,never()).findLockedById(any());
    }
}
