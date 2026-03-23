package com.kcserver.support.tenant;

import com.kcserver.support.config.MockMvcTestConfig;
import com.kcserver.tenancy.TenantContext;
import com.kcserver.tenancy.TenantSchemaProvisioner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.kcserver.support.config.TestSecurityConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)   // 🔥 HIER
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
        jdbcTemplate.execute("""
        TRUNCATE TABLE
                        teilnehmer,
                        mitglied,
                        person,
                        verein,
                        veranstaltung,
                        planung,
                        planung_position,
                        abrechnung,
                        abrechnung_beleg,
                        abrechnung_buchung,
                        erhebungsbogen,
                        finanz_gruppe,
                        foerdersatz,
                        kik_zuschlag
                    RESTART IDENTITY CASCADE
        """);
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    /* =========================================================
       Für direkte Repository-Zugriffe im Test
       ========================================================= */

    protected void ensureTenantSchema() {
        String tenant = tenant();

        TenantContext.setCurrentTenant(tenant);
        jdbcTemplate.execute("SET search_path TO " + tenant);
    }

    protected org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder req(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder
    ) {
        return builder.with(
                com.kcserver.support.security.TestSecurity.tenantUser(tenant())
        );
    }
}