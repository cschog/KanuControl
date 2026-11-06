package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.config.SmtpMailProperties;
import com.kcserver.vermietung.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HausmeisterBerichtServiceTest {
    @Test void unvollstaendigeKontaktdatenUndHtmlZeichenWerdenSicherDargestellt() {
        Buchung b = new Buchung(); b.setId(1L); b.setBuchungsjahr(2026); b.setLaufendeNummer(1);
        b.setAnreise(LocalDateTime.of(2026,11,9,12,0)); b.setAbreise(b.getAnreise().plusDays(2));
        b.setStatus(Buchungsstatus.STORNIERT);
        var person = new com.kcserver.core.entity.Person(); person.setName("<script>"); b.setMieter(person);
        var body = HausmeisterBerichtService.render("Bootshaus", LocalDate.of(2026,11,8), LocalDate.of(2026,12,6), List.of(b));
        assertTrue(body[0].contains("STORNIERT"));
        assertTrue(body[0].contains("Telefon: Nicht hinterlegt"));
        assertFalse(body[1].contains("<script>"));
        assertTrue(body[1].contains("&lt;script&gt;"));
        assertTrue(body[1].contains("09.11.2026 12:00"));
    }

    @SuppressWarnings("unchecked")
    @Test void deaktivierterVersandOderBereitsGesendeteWocheErzeugenKeineMail() {
        var configs = mock(HomepageAnbindungRepository.class); var bookings = mock(BuchungRepository.class);
        ObjectProvider<JavaMailSender> sender = mock(ObjectProvider.class); var smtp = new SmtpMailProperties();
        var service = new HausmeisterBerichtService(configs, bookings, sender, smtp);
        var day = LocalDate.of(2026,11,8);
        service.wocheSenden(1L, day);
        verifyNoInteractions(configs, bookings, sender);
        smtp.setEnabled(true);
        var config = new HomepageAnbindung(); config.setHausmeisterEmail("test@example.org"); config.setHausmeisterLetzteWoche(day);
        when(configs.lockById(1L)).thenReturn(java.util.Optional.of(config));
        service.wocheSenden(1L, day);
        verifyNoInteractions(bookings, sender);
    }
    @SuppressWarnings("unchecked")
    @Test void testmodusVerwendetNurTestempfaengerUndTestversandMarkiertKeineWoche() throws Exception {
        var configs = mock(HomepageAnbindungRepository.class); var bookings = mock(BuchungRepository.class);
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        JavaMailSender sender = mock(JavaMailSender.class);
        var message = new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);
        when(provider.getIfAvailable()).thenReturn(sender); when(sender.createMimeMessage()).thenReturn(message);
        var smtp = new SmtpMailProperties(); smtp.setEnabled(true); smtp.setTestMode(true);
        smtp.setTestRecipient("test@example.org"); smtp.setFrom("kc@example.org");
        var config = new HomepageAnbindung(); config.setMietobjektId(7L); config.setHausmeisterEmail("real@example.org");
        var objekt = new Mietobjekt(); objekt.setBezeichnung("Bootshaus"); config.setMietobjekt(objekt);
        when(configs.lockById(7L)).thenReturn(java.util.Optional.of(config));
        when(bookings.findByMietobjektIdAndStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(), any(), any(), any()))
            .thenReturn(new java.util.ArrayList<>());
        var service = new HausmeisterBerichtService(configs, bookings, provider, smtp);
        service.testSenden(7L);
        assertEquals("test@example.org", message.getAllRecipients()[0].toString());
        assertTrue(message.getSubject().startsWith("[TEST]"));
        assertNull(config.getHausmeisterLetzteWoche());
        verify(configs, never()).saveAndFlush(any());
        verify(sender).send(message);
        var week = LocalDate.of(2026,11,8);
        service.wocheSenden(7L, week);
        assertEquals(week, config.getHausmeisterLetzteWoche());
        verify(configs).saveAndFlush(config);
    }
}
