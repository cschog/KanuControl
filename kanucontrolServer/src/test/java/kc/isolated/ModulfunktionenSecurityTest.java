package kc.isolated;

import com.kcserver.vermietung.controller.VorlagenController;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import com.kcserver.vermietung.service.VorlagenModell.*;
import com.kcserver.kjfp.controller.fahrkosten.ReisekostenKonfigurationController;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenKonfigurationSaveRequest;
import com.kcserver.kjfp.service.reisekosten.ReisekostenKonfigurationService;
import com.kcserver.fahrten.controller.FahrtenController;
import com.kcserver.fahrten.service.FahrtenService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModulfunktionenSecurityTest {
    @Configuration @EnableMethodSecurity(proxyTargetClass=true)
    static class Config {
        @Bean VorlagenService vorlagenService() { return mock(VorlagenService.class); }
        @Bean VorlagenController vorlagen(VorlagenService service) {
            return new VorlagenController(service,mock(BuchungRepository.class),mock(ReservierungsbestaetigungPdf.class),mock(VorlagenVersandRepository.class),mock(AbrechnungVorlagenService.class));
        }
        @Bean ReisekostenKonfigurationService reisekostenService() { return mock(ReisekostenKonfigurationService.class); }
        @Bean ReisekostenKonfigurationController reisekosten(ReisekostenKonfigurationService service) { return new ReisekostenKonfigurationController(service); }
        @Bean FahrtenService fahrtenService() { return mock(FahrtenService.class); }
        @Bean FahrtenController fahrten(FahrtenService service) { return new FahrtenController(service); }
    }
    private AnnotationConfigApplicationContext context;
    @BeforeEach void setup() { context=new AnnotationConfigApplicationContext(Config.class); }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext();context.close(); }
    private void rolle(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test","unused",List.of(new SimpleGrantedAuthority("ROLE_"+role))));
    }
    @ParameterizedTest @ValueSource(strings={"VERMIETUNG","ADMIN"})
    void dokumentvorlagenUndBriefpapierSindMitModulzugriffSchreibbar(String role) throws Exception {
        rolle(role);
        var controller=context.getBean(VorlagenController.class);
        var vorlage=new Vorlage(null,"Betreff","Mail",List.of(new Block("Titel","Text",Bedingung.IMMER,List.of())),"Hallo {{vorname}} {{nachname}},");
        assertDoesNotThrow(()->controller.speichern(7L,Vorgang.RESERVIERUNG,vorlage));
        assertDoesNotThrow(()->controller.speichern(null,"Gruß",null,null,false,"geprüft"));
        verify(context.getBean(VorlagenService.class)).speichern(7L,Vorgang.RESERVIERUNG,vorlage);
        verify(context.getBean(VorlagenService.class)).speichern(null,"Gruß",null,null,false,"geprüft");
    }
    @ParameterizedTest @ValueSource(strings={"KJFP","FAHRTEN","USER","HOMEPAGE_INTEGRATION"})
    void dokumentvorlagenBleibenOhneVermietungszugriffGesperrt(String role) {
        rolle(role);var controller=context.getBean(VorlagenController.class);
        assertThrows(AccessDeniedException.class,()->controller.speichern(7L,Vorgang.RESERVIERUNG,null));
        assertThrows(AccessDeniedException.class,()->controller.speichern(null,"Gruß",null,null,false,"geprüft"));
        verifyNoInteractions(context.getBean(VorlagenService.class));
    }
    @ParameterizedTest @ValueSource(strings={"KJFP","ADMIN"})
    void fahrkostenKonfigurationIstMitKjfpZugriffSchreibbar(String role) {
        rolle(role);var controller=context.getBean(ReisekostenKonfigurationController.class);
        var input=new ReisekostenKonfigurationSaveRequest(new BigDecimal("0.30"),new BigDecimal("0.05"),new BigDecimal("0.10"),LocalDate.of(2026,11,9));
        assertDoesNotThrow(controller::list);assertDoesNotThrow(controller::aktuell);
        assertDoesNotThrow(()->controller.create(input));
        verify(context.getBean(ReisekostenKonfigurationService.class)).create(input);
    }
    @ParameterizedTest @ValueSource(strings={"VERMIETUNG","FAHRTEN","USER","HOMEPAGE_INTEGRATION"})
    void fahrkostenKonfigurationBleibtOhneKjfpZugriffGesperrt(String role) {
        rolle(role);var controller=context.getBean(ReisekostenKonfigurationController.class);
        assertThrows(AccessDeniedException.class,controller::list);
        assertThrows(AccessDeniedException.class,()->controller.create(null));
        verifyNoInteractions(context.getBean(ReisekostenKonfigurationService.class));
    }
    @ParameterizedTest @ValueSource(strings={"FAHRTEN","KJFP","ADMIN"})
    void fahrtenSindOhneZusaetzlicheAdminRolleSchreibbar(String role) {
        rolle(role);var controller=context.getBean(FahrtenController.class);
        assertDoesNotThrow(()->controller.removeBoot(7L,8L,9L));
        verify(context.getBean(FahrtenService.class)).removeBoot(7L,8L,9L);
    }
}
