package com.kcserver.vermietung.service;

import com.kcserver.vermietung.config.SmtpMailProperties;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatusMailVorschauTest {
    @ParameterizedTest @EnumSource(Buchungsstatus.class)
    @SuppressWarnings("unchecked")
    void jedeStatusmailZeigtUndVersendetExaktDenFreigegebenenInhalt(Buchungsstatus zielstatus) {
        try(var validation=Validation.buildDefaultValidatorFactory()) {
            var configRepo=mock(HomepageAnbindungRepository.class);var mailRepo=mock(HomepageStatusMailRepository.class);
            ObjectProvider<JavaMailSender> provider=mock(ObjectProvider.class);var sender=mock(JavaMailSender.class);
            when(provider.getIfAvailable()).thenReturn(sender);
            var smtp=new SmtpMailProperties();smtp.setEnabled(true);smtp.setFrom("kc@example.org");
            var config=new HomepageAnbindung();config.setAntwortTestEmail("test@example.org");config.setAntwortCcEmail("cc@example.org");config.setEmpfaengerEmail("reply@example.org");
            when(configRepo.findById(7L)).thenReturn(Optional.of(config));
            var service=new HomepageStatusMailService(mailRepo,configRepo,validation.getValidator(),provider,smtp);
            var b=ReservierungsbestaetigungTest.beispiel();var alt=b.getStatus();
            var v=service.statusVorschau(b,zielstatus,alt);
            assertEquals("test@example.org",v.empfaenger());assertEquals("cc@example.org",v.cc());assertTrue(v.testmodus());
            verifyNoInteractions(sender,provider,mailRepo);assertEquals(alt,b.getStatus());
            assertThrows(ResponseStatusException.class,()->service.statusVorbereiten(b,zielstatus,alt,null));
            var prepared=service.statusVorbereiten(b,zielstatus,alt,v.fingerprint());
            assertEquals(v.betreff(),prepared.message().getSubject());assertEquals(v.text(),prepared.message().getText());
            assertArrayEquals(new String[]{v.empfaenger()},prepared.message().getTo());
            assertArrayEquals(new String[]{v.cc()},prepared.message().getCc());assertEquals(v.replyTo(),prepared.message().getReplyTo());
            assertEquals(v.absender(),prepared.message().getFrom());verify(sender,never()).send(any(SimpleMailMessage.class));
            // Configuration changes after preparation must not alter the approved message.
            config.setAntwortTestEmail("changed@example.org");
            assertThrows(ResponseStatusException.class,()->service.statusVorbereiten(b,zielstatus,alt,v.fingerprint()));
            service.statusSenden(prepared);verify(sender).send(prepared.message());
            assertArrayEquals(new String[]{"test@example.org"},prepared.message().getTo());
            smtp.setTestMode(true);smtp.setTestRecipient("global@example.org");
            var global=service.statusVorschau(b,zielstatus,alt);assertEquals("global@example.org",global.empfaenger());assertNull(global.cc());
            assertNotEquals(v.fingerprint(),global.fingerprint());
            b.getMieter().setVorname("Geändert");assertNotEquals(global.fingerprint(),service.statusVorschau(b,zielstatus,alt).fingerprint());
        }
    }
}
