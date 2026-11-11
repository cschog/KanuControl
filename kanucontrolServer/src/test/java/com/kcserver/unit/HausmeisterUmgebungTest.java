package com.kcserver.unit;

import com.kcserver.vermietung.config.*;
import com.kcserver.vermietung.entity.HomepageAnbindung;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.env.MockEnvironment;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HausmeisterUmgebungTest {
    private static HausmeisterBerichtFreigabe policy(String runtime, String app, boolean enabled, String... profiles) {
        var env = new MockEnvironment().withProperty("kc.runtime-environment", runtime)
                .withProperty("app.environment", app).withProperty("kc.hausmeisterbericht.enabled", "" + enabled);
        env.setActiveProfiles(profiles);
        return new HausmeisterBerichtFreigabe(env);
    }

    @ParameterizedTest
    @CsvSource({"DEV,DEV,dev", "TEST,TEST,test", "DEV,PROD,prod", "TEST,PROD,prod", "UNKNOWN,PROD,prod", "PROD,TEST,prod", "PROD,DEV,prod"})
    @SuppressWarnings("unchecked")
    void automatikSperreGreiftVorJederAbfrageAuchMitEmpfaengerUndSmtpFreigabe(String runtime, String app, String profile) {
        var gate = policy(runtime, app, true, profile);
        var configs = mock(HomepageAnbindungRepository.class);
        var bookings = mock(BuchungRepository.class);
        ObjectProvider<JavaMailSender> mail = mock(ObjectProvider.class);
        var jdbc = mock(JdbcTemplate.class);
        var smtp = new SmtpMailProperties(); smtp.setEnabled(true);
        var saved = new HomepageAnbindung(); saved.setMietobjektId(7L); saved.setHausmeisterEmail("saved@example.org");
        when(configs.lockById(7L)).thenReturn(Optional.of(saved));
        var service = new HausmeisterBerichtService(configs, bookings, mail, smtp, gate);
        new HausmeisterBerichtScheduler(jdbc, service, gate).senden();
        assertTrue(service.objektIds().isEmpty());
        service.wocheSenden(7L, LocalDate.of(2026, 11, 8));
        assertEquals(false, service.status().get("enabled"));
        verifyNoInteractions(jdbc, configs, bookings, mail);
    }

    @Test void nurExplizitesProdOhneWiderspruchIstFreigegeben() {
        assertFalse(new HausmeisterBerichtFreigabe(new MockEnvironment()).isEnabled());
        assertFalse(policy("PROD", "PROD", false, "prod").isEnabled());
        assertFalse(policy("PROD", "PROD", true, "test").isEnabled());
        assertFalse(policy("PROD", "PROD", true, "prod", "test").isEnabled());
        assertFalse(policy("PROD", "PROD", true, "prod", "dev").isEnabled());
        assertTrue(policy("PROD", "PROD", true, "prod").isEnabled());
    }

    @Test void zeitplanBleibtSonntag15UhrBerlin() throws Exception {
        var scheduled = HausmeisterBerichtScheduler.class.getMethod("senden")
                .getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);
        assertEquals("Europe/Berlin", scheduled.zone());
        var cron = org.springframework.scheduling.support.CronExpression.parse(scheduled.cron());
        assertEquals(java.time.LocalDateTime.of(2026,11,8,15,0),
                cron.next(java.time.LocalDateTime.of(2026,11,7,23,59)));
    }
    @ParameterizedTest
    @CsvSource({"DEV,dev,true", "DEV,dev,false", "TEST,test,true", "TEST,test,false", "PROD,prod,false"})
    void manuellerHttpVersandIstUnabhaengigVonAutomatikFreigabe(String umgebung,String profil,boolean testmodus) throws Exception {
        var configs=mock(HomepageAnbindungRepository.class);var bookings=mock(BuchungRepository.class);
        @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> mail=mock(ObjectProvider.class);
        var sender=mock(JavaMailSender.class);var message=new jakarta.mail.internet.MimeMessage((jakarta.mail.Session)null);
        when(mail.getIfAvailable()).thenReturn(sender);when(sender.createMimeMessage()).thenReturn(message);
        var saved=new HomepageAnbindung();saved.setMietobjektId(7L);saved.setHausmeisterEmail("saved@example.org");
        var objekt=new com.kcserver.vermietung.entity.Mietobjekt();objekt.setBezeichnung("Bootshaus");saved.setMietobjekt(objekt);
        var letzteWoche=LocalDate.of(2026,11,1);saved.setHausmeisterLetzteWoche(letzteWoche);
        when(configs.lockById(7L)).thenReturn(Optional.of(saved));
        when(bookings.findByMietobjektIdAndStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any(),any())).thenReturn(new java.util.ArrayList<>());
        var smtp=new SmtpMailProperties();smtp.setEnabled(true);smtp.setFrom("kc@example.org");smtp.setTestMode(testmodus);smtp.setTestRecipient("test@example.org");
        var service=new HausmeisterBerichtService(configs,bookings,mail,smtp,policy(umgebung,umgebung,false,profil));
        var controller=new com.kcserver.vermietung.controller.HomepageAnbindungController(mock(HomepageAnbindungService.class),service);
        var mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/vermietung/objekte/7/homepage-anbindung/hausmeister-senden"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        assertEquals(testmodus?"test@example.org":"saved@example.org",message.getAllRecipients()[0].toString());
        assertTrue(message.getSubject().contains("[MANUELL]"));assertEquals(testmodus,message.getSubject().contains("[TEST]"));verify(sender).send(message);
        var heute=LocalDate.now(java.time.ZoneId.of("Europe/Berlin"));
        verify(bookings).findByMietobjektIdAndStatusInAndAnreiseLessThanAndAbreiseGreaterThan(eq(7L),any(),eq(heute.plusWeeks(4).atStartOfDay()),eq(heute.atStartOfDay()));
        assertEquals(letzteWoche,saved.getHausmeisterLetzteWoche());verify(configs,never()).saveAndFlush(any());
        clearInvocations(configs,bookings,mail);service.wocheSenden(7L,heute);verifyNoInteractions(configs,bookings,mail);
    }

    @Test void echteProfilkonfigurationenBleibenOhneSeparateFreigabeGesperrt() {
        for (String profile : new String[]{"dev", "test", "prod"}) {
            new org.springframework.boot.test.context.runner.ApplicationContextRunner()
                .withInitializer(new org.springframework.boot.test.context.ConfigDataApplicationContextInitializer())
                .withPropertyValues("spring.profiles.active=" + profile, "KC_ENVIRONMENT=PROD")
                .run(context -> {
                    var env = context.getEnvironment();
                    assertFalse(new HausmeisterBerichtFreigabe(env).isEnabled());
                    if (profile.equals("prod")) {
                        // Fehlende Produktions-URL muss fehlschlagen, keine lokale Ausweich-URL.
                        assertThrows(IllegalArgumentException.class, () -> env.getRequiredProperty("spring.datasource.url"));
                    }
                });
        }
    }

    @Test void echteTestkonfigurationIgnoriertBerichtsfreigabeUndProdVerwendetExaktDbUrl() {
        for (String profile : new String[]{"test", "prod"}) {
            new org.springframework.boot.test.context.runner.ApplicationContextRunner()
                .withInitializer(new org.springframework.boot.test.context.ConfigDataApplicationContextInitializer())
                .withPropertyValues("spring.profiles.active=" + profile, "KC_ENVIRONMENT=PROD",
                        "KC_HAUSMEISTERBERICHT_ENABLED=true", "DB_URL=jdbc:postgresql://production-only/kc")
                .run(context -> {
                    var env = context.getEnvironment();
                    assertEquals(profile.equals("prod"), new HausmeisterBerichtFreigabe(env).isEnabled());
                    assertEquals("jdbc:postgresql://production-only/kc", env.getRequiredProperty("spring.datasource.url"));
                });
        }
    }

}
