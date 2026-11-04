package kc.isolated;

import com.kcserver.core.audit.*;
import com.kcserver.core.audit.repository.AuditSessionRepository;
import com.kcserver.core.audit.service.AuditSessionService;
import com.kcserver.core.tenancy.TenantContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import jakarta.servlet.FilterChain;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditPersonalAccountsTest {
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); TenantContext.clear(); }

    @ParameterizedTest
    @ValueSource(strings = {"service-account-ekc-homepage", "service-account-other", "SERVICE-ACCOUNT-EKC-HOMEPAGE"})
    void technischeAnfragenPassierenDenFilterOhneAuditEintrag(String username) throws Exception {
        var service = mock(AuditSessionService.class);
        var jwt = Jwt.withTokenValue("test").header("alg", "none")
                .claim("preferred_username", username).claim("sid", "technical-session").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, java.util.List.of()));
        var request = new MockHttpServletRequest(); var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        new AuditFilter(service).doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verifyNoInteractions(service);
    }

    @Test void persoenlicheAnfrageWirdWeiterhinErfasst() throws Exception {
        var service = mock(AuditSessionService.class);
        var jwt = Jwt.withTokenValue("test").header("alg", "none")
                .claim("preferred_username", "admin").claim("name", "Admin Name")
                .claim("email", "admin@example.invalid").claim("sid", "personal-session").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, java.util.List.of()));
        TenantContext.setCurrentTenant("club_a");
        var request = new MockHttpServletRequest(); request.addHeader("User-Agent", "Browser");
        var chain = mock(FilterChain.class); var response = new MockHttpServletResponse();
        new AuditFilter(service).doFilter(request, response, chain);
        verify(service).registerOrUpdate("admin", "Admin Name", "admin@example.invalid", "club_a",
                "personal-session", "127.0.0.1", "Browser");
        verify(chain).doFilter(request, response);
    }

    @Test void auchDirekteRegistrierungIgnoriertTechnischeKonten() {
        var repository = mock(AuditSessionRepository.class);
        var service = new AuditSessionService(repository);
        service.registerOrUpdate("service-account-ekc-homepage", null, null, "club_a", null, "127.0.0.1", "client");
        verifyNoInteractions(repository);
        service.registerOrUpdate("chris", "Chris", null, "club_a", "sid", "127.0.0.1", "Browser");
        verify(repository).upsertSession(eq("chris"), eq("Chris"), isNull(), eq("club_a"), eq("sid"),
                any(), eq("127.0.0.1"), eq("Browser"));
        assertTrue(AuditAccountPolicy.isPersonalAccount("user-service-account-helper"));
    }
}
