package com.kcserver.integration.support;

import com.kcserver.tenancy.TenantContext;
import com.kcserver.tenancy.TenantSchemaProvisioner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public abstract class AbstractTenantIntegrationTest {

    protected static final String TENANT = "ekc_test";

    @Autowired
    protected TenantSchemaProvisioner tenantSchemaProvisioner;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setupTenant() {
        tenantSchemaProvisioner.createFromBaseline(TENANT);
        TenantContext.setCurrentTenant(TENANT);

        // 🔥 DAS ist entscheidend
        jdbcTemplate.execute("TRUNCATE TABLE teilnehmer CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE mitglied CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE person CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE verein CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE veranstaltung CASCADE");
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    protected MockHttpServletRequestBuilder tenantRequest(
            MockHttpServletRequestBuilder builder
    ) {
        return builder
                .header("X-Tenant", TENANT)
                .with(jwt());
    }
}