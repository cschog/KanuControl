package com.kcserver.integration.support;

import com.kcserver.tenancy.TenantContext;
import com.kcserver.tenancy.TenantSchemaProvisioner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static com.kcserver.integration.support.TestJwtUtils.jwtWithTenant;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTenantIntegrationTest {

    /**
     * 🔑 Standard-Test-Tenant
     * (kann pro Testklasse überschrieben werden)
     */
    protected String tenant() {
        return "ekc_test";
    }

    @Autowired
    protected TenantSchemaProvisioner tenantSchemaProvisioner;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected MockMvc mockMvc;

    /* =========================================================
       TENANT BOOTSTRAP
       ========================================================= */

    @BeforeEach
    void setupTenant() {
        String tenant = tenant();

        // ⭐ Schema sicherstellen
        tenantSchemaProvisioner.createFromBaseline(tenant);

        // ⭐ ThreadLocal für Hibernate Resolver
        TenantContext.setCurrentTenant(tenant);

        // ⭐ DB Schema für direkte Repository Calls im Test
        jdbcTemplate.execute("SET search_path TO " + tenant);

        // ⭐ saubere DB pro Test
        jdbcTemplate.execute("TRUNCATE TABLE teilnehmer CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE mitglied CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE person CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE verein CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE veranstaltung CASCADE");

        // ⭐ optional: IDs resetten (Postgres)
        jdbcTemplate.execute("ALTER SEQUENCE person_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE verein_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE veranstaltung_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE teilnehmer_id_seq RESTART WITH 1");
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    /* =========================================================
       JWT + TENANT HEADER + THREADLOCAL (entscheidend!)
       ========================================================= */

    protected RequestPostProcessor tenantAuth() {
        String tenant = tenant();

        return request -> {
            // ⭐ WICHTIG: Hibernate Resolver braucht ThreadLocal
            TenantContext.setCurrentTenant(tenant);

            // ⭐ Tenant Header für TenantFilter
            request.addHeader("X-Tenant", tenant);

            // ⭐ JWT mit Tenant Claim
            return jwtWithTenant(tenant).postProcessRequest(request);
        };
    }

    protected MockHttpServletRequestBuilder tenantRequest(
            MockHttpServletRequestBuilder builder
    ) {
        return builder.with(tenantAuth());
    }

    /* =========================================================
       Für direkte Repository-Zugriffe im Test
       ========================================================= */

    protected void ensureTenantSchema() {
        String tenant = tenant();

        TenantContext.setCurrentTenant(tenant);
        jdbcTemplate.execute("SET search_path TO " + tenant);
    }
}