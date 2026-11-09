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
import org.springframework.web.server.ResponseStatusException;
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
    void sperreGreiftVorJederAbfrageAuchMitEmpfaengerUndSmtpFreigabe(String runtime, String app, String profile) {
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
        var error = assertThrows(ResponseStatusException.class, () -> service.testSenden(7L));
        assertEquals(403, error.getStatusCode().value());
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
    @Test void httpTestversandIstAuchMitGespeicherterAdresseInTestVerboten() throws Exception {
        var configs = mock(HomepageAnbindungRepository.class);
        var bookings = mock(BuchungRepository.class);
        @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> mail = mock(ObjectProvider.class);
        var saved = new HomepageAnbindung(); saved.setHausmeisterEmail("saved@example.org");
        when(configs.lockById(7L)).thenReturn(Optional.of(saved));
        var smtp = new SmtpMailProperties(); smtp.setEnabled(true);
        var service = new HausmeisterBerichtService(configs, bookings, mail, smtp, policy("TEST", "PROD", true, "prod"));
        var controller = new com.kcserver.vermietung.controller.HomepageAnbindungController(
                mock(HomepageAnbindungService.class), service);
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/vermietung/objekte/7/homepage-anbindung/hausmeister-test"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        verifyNoInteractions(configs, bookings, mail);
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
