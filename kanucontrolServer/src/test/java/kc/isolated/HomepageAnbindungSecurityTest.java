package kc.isolated;

import com.kcserver.vermietung.controller.HomepageAnbindungController;
import com.kcserver.vermietung.dto.HomepageAnbindungDTO;
import com.kcserver.vermietung.service.HomepageAnbindungService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomepageAnbindungSecurityTest {
    @Configuration @EnableMethodSecurity(proxyTargetClass = true)
    static class Config {
        @Bean com.kcserver.vermietung.service.HomepageIntegrationService integrationService() {
            return mock(com.kcserver.vermietung.service.HomepageIntegrationService.class);
        }
        @Bean com.kcserver.vermietung.controller.HomepageIntegrationController integrationController(com.kcserver.vermietung.service.HomepageIntegrationService service) {
            return new com.kcserver.vermietung.controller.HomepageIntegrationController(service);
        }
        @Bean com.kcserver.vermietung.service.HomepageStatusMailService statusService() {
            return mock(com.kcserver.vermietung.service.HomepageStatusMailService.class);
        }
        @Bean com.kcserver.vermietung.controller.HomepageNachrichtenController statusController(com.kcserver.vermietung.service.HomepageStatusMailService service) {
            return new com.kcserver.vermietung.controller.HomepageNachrichtenController(service);
        }
        @Bean HomepageAnbindungService service() { return mock(HomepageAnbindungService.class); }
        @Bean HomepageAnbindungController controller(HomepageAnbindungService service) {
            return new HomepageAnbindungController(service);
        }
    }
    private AnnotationConfigApplicationContext context;
    private HomepageAnbindungController controller;
    @BeforeEach void setup() {
        context = new AnnotationConfigApplicationContext(Config.class);
        controller = context.getBean(HomepageAnbindungController.class);
    }
    @AfterEach void close() { SecurityContextHolder.clearContext(); context.close(); }
    private void role(String role) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("test", "unused", List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
    @Test void vermietungDarfLesenAberNichtSchreiben() {
        role("VERMIETUNG");
        assertDoesNotThrow(() -> controller.get(7L));
        assertThrows(AccessDeniedException.class, () -> controller.save(7L, new HomepageAnbindungDTO()));
        verify(context.getBean(HomepageAnbindungService.class), never()).save(any(), any());
    }
    @Test void adminDarfSpeichern() {
        role("ADMIN");
        assertDoesNotThrow(() -> controller.save(7L, new HomepageAnbindungDTO()));
        verify(context.getBean(HomepageAnbindungService.class)).save(eq(7L), any());
    }
    @Test void integrationHatKeineVerwaltungsrechteUndAdminKeineIntegrationsrechte() {
        role("HOMEPAGE_INTEGRATION");
        assertThrows(AccessDeniedException.class, () -> controller.get(7L));
        assertThrows(AccessDeniedException.class, () -> controller.save(7L, new HomepageAnbindungDTO()));
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("test").header("alg", "RS256")
            .claim("azp", "verein-homepage").build();
        var integration = context.getBean(com.kcserver.vermietung.controller.HomepageIntegrationController.class);
        assertDoesNotThrow(() -> integration.katalog(7L, jwt));
        role("ADMIN");
        assertThrows(AccessDeniedException.class, () -> integration.katalog(7L, jwt));
    }
    @Test void statusnachrichtenSindNurDemTechnischenZugangZugaenglich() {
        var controller=context.getBean(com.kcserver.vermietung.controller.HomepageNachrichtenController.class);
        var jwt=org.springframework.security.oauth2.jwt.Jwt.withTokenValue("test").header("alg","RS256").claim("azp","ekc-homepage").build();
        role("ADMIN");assertThrows(AccessDeniedException.class,()->controller.offen(7L,jwt));
        role("HOMEPAGE_INTEGRATION");assertDoesNotThrow(()->controller.offen(7L,jwt));
        var id=java.util.UUID.randomUUID();assertDoesNotThrow(()->controller.quittieren(7L,id,jwt));
        verify(context.getBean(com.kcserver.vermietung.service.HomepageStatusMailService.class)).quittieren(7L,"ekc-homepage",id.toString());
    }
    @Test void fremdeModulrolleHatKeinenZugriff() {
        role("KJFP");
        assertThrows(AccessDeniedException.class, () -> controller.get(7L));
        assertThrows(AccessDeniedException.class, () -> controller.save(7L, new HomepageAnbindungDTO()));
        verifyNoInteractions(context.getBean(HomepageAnbindungService.class));
    }
}
