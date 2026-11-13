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
    private static com.kcserver.vermietung.config.HausmeisterBerichtFreigabe freigabe() {
        var env = new org.springframework.mock.env.MockEnvironment()
                .withProperty("kc.runtime-environment", "PROD")
                .withProperty("app.environment", "PROD")
                .withProperty("kc.hausmeisterbericht.enabled", "true");
        env.setActiveProfiles("prod");
        return new com.kcserver.vermietung.config.HausmeisterBerichtFreigabe(env);
    }
    @Test void unvollstaendigeKontaktdatenUndHtmlZeichenWerdenSicherDargestellt() {
        Buchung b = new Buchung(); b.setId(1L); b.setBuchungsjahr(2026); b.setLaufendeNummer(1);
        b.setAnreise(LocalDateTime.of(2026,11,9,12,0)); b.setAbreise(b.getAnreise().plusDays(2));
        b.setStatus(Buchungsstatus.STORNIERT);
        var person = new com.kcserver.core.entity.Person(); person.setName("<script>"); b.setMieter(person);
        var body = HausmeisterBerichtService.render("Bootshaus", LocalDate.of(2026,11,8), LocalDate.of(2026,12,6), List.of(b));
        assertTrue(body[0].contains("STORNIERT"));
        assertFalse(body[0].contains("Telefon:"));
        assertFalse(body[1].contains("Telefon:"));
        assertFalse(body[1].contains("tel:"));
        assertFalse(body[1].contains("<script>"));
        assertTrue(body[1].contains("&lt;script&gt;"));
        assertTrue(body[1].contains("09.11.2026 12:00"));
    }

    @Test void airbnbWirdMarkiertUndFehlendeTelefonnummernWerdenWeggelassen() {
        var b = new Buchung(); b.setId(1L); b.setBuchungsjahr(2026); b.setLaufendeNummer(1);
        b.setAnreise(LocalDateTime.of(2026,11,9,12,0)); b.setAbreise(b.getAnreise().plusDays(2));
        b.setStatus(Buchungsstatus.BESTAETIGT);
        var person = new com.kcserver.core.entity.Person(); person.setVorname("Erika"); person.setName("Mustermann");
        b.setMieter(person);
        for (var quelle : com.kcserver.vermietung.enumtype.Buchungsquelle.values()) {
            b.setBuchungsquelle(quelle);
            for (var telefon : new String[]{"", "   ", "+49 123 456"}) {
                person.setTelefon(telefon); person.setTelefonFestnetz(null);
                var body = HausmeisterBerichtService.render("Bootshaus", LocalDate.of(2026,11,8), LocalDate.of(2026,12,6), List.of(b));
                for (var text : body) {
                    assertEquals(quelle == com.kcserver.vermietung.enumtype.Buchungsquelle.AIRBNB, text.contains("Airbnb"));
                    assertEquals(!telefon.isBlank(), text.contains("Telefon:"));
                    assertFalse(text.contains("Nicht hinterlegt")); assertFalse(text.contains("DIREKT")); assertFalse(text.contains("Direkt"));
                }
            }
        }
        person.setTelefon(null); person.setTelefonFestnetz("02403 12345");
        var body = HausmeisterBerichtService.render("Bootshaus", LocalDate.of(2026,11,8), LocalDate.of(2026,12,6), List.of(b));
        assertTrue(body[0].contains("Telefon: 02403 12345"));
        assertTrue(body[1].contains("href=\"tel:0240312345\""));
        b.setMieter(null);
        body = HausmeisterBerichtService.render("Bootshaus", LocalDate.of(2026,11,8), LocalDate.of(2026,12,6), List.of(b));
        assertFalse(body[0].contains("Telefon:")); assertFalse(body[1].contains("Telefon:"));
    }

    @SuppressWarnings("unchecked")
    @Test void deaktivierterVersandOderBereitsGesendeteWocheErzeugenKeineMail() {
        var configs = mock(HomepageAnbindungRepository.class); var bookings = mock(BuchungRepository.class);
        ObjectProvider<JavaMailSender> sender = mock(ObjectProvider.class); var smtp = new SmtpMailProperties();
        var service = new HausmeisterBerichtService(configs, bookings, sender, smtp, freigabe());
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
        var config = new HomepageAnbindung(); config.setMietobjektId(7L); config.setHausmeisterEmail("real@example.org"); config.setHausmeisterCcEmail("cc@example.org");
        var objekt = new Mietobjekt(); objekt.setBezeichnung("Bootshaus"); config.setMietobjekt(objekt);
        when(configs.lockById(7L)).thenReturn(java.util.Optional.of(config));
        when(bookings.findByMietobjektIdAndStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(), any(), any(), any()))
            .thenReturn(new java.util.ArrayList<>());
        var service = new HausmeisterBerichtService(configs, bookings, provider, smtp, freigabe());
        service.manuellSenden(7L);
        assertEquals("test@example.org", message.getAllRecipients()[0].toString());
        assertTrue(message.getSubject().startsWith("[TEST]"));
        assertEquals(1, message.getAllRecipients().length);
        assertNull(message.getRecipients(jakarta.mail.Message.RecipientType.CC));
        assertNull(config.getHausmeisterLetzteWoche());
        verify(configs, never()).saveAndFlush(any());
        verify(sender).send(message);
        var week = LocalDate.of(2026,11,8);
        service.wocheSenden(7L, week);
        assertEquals(week, config.getHausmeisterLetzteWoche());
        verify(configs).saveAndFlush(config);
        smtp.setTestMode(false);
        config.setHausmeisterAutomatisch(false);
        service.manuellSenden(7L);
        assertEquals("real@example.org", message.getRecipients(jakarta.mail.Message.RecipientType.TO)[0].toString());
        assertEquals("cc@example.org", message.getRecipients(jakarta.mail.Message.RecipientType.CC)[0].toString());
        clearInvocations(sender);
        service.wocheSenden(7L, week.plusWeeks(1));
        verifyNoInteractions(sender);
    }
    @Test @SuppressWarnings("unchecked")
    void objektSchalterUeberschreibtServervorgabeNurInProd() {
        var configs = mock(HomepageAnbindungRepository.class);
        var config = new HomepageAnbindung(); config.setMietobjektId(7L); config.setHausmeisterEmail("real@example.org");
        config.setHausmeisterAutomatisch(true);
        when(configs.findAll()).thenReturn(java.util.List.of(config));
        var env = new org.springframework.mock.env.MockEnvironment()
                .withProperty("kc.runtime-environment", "PROD").withProperty("app.environment", "PROD");
        env.setActiveProfiles("prod");
        var service = new HausmeisterBerichtService(configs, mock(BuchungRepository.class), mock(ObjectProvider.class),
                new SmtpMailProperties(), new com.kcserver.vermietung.config.HausmeisterBerichtFreigabe(env));
        assertEquals(java.util.List.of(7L), service.objektIds());
        config.setHausmeisterAutomatisch(false); assertTrue(service.objektIds().isEmpty());
        config.setHausmeisterAutomatisch(null); assertTrue(service.objektIds().isEmpty());
        env.setProperty("kc.hausmeisterbericht.enabled", "true");
        service = new HausmeisterBerichtService(configs, mock(BuchungRepository.class), mock(ObjectProvider.class),
                new SmtpMailProperties(), new com.kcserver.vermietung.config.HausmeisterBerichtFreigabe(env));
        assertEquals(java.util.List.of(7L), service.objektIds());
        config.setHausmeisterAutomatisch(false); assertTrue(service.objektIds().isEmpty());
        config.setHausmeisterAutomatisch(true); env.setActiveProfiles("test");
        service = new HausmeisterBerichtService(configs, mock(BuchungRepository.class), mock(ObjectProvider.class),
                new SmtpMailProperties(), new com.kcserver.vermietung.config.HausmeisterBerichtFreigabe(env));
        assertTrue(service.objektIds().isEmpty());
    }

}
