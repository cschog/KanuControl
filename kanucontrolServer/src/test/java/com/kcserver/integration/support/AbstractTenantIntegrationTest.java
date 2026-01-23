package com.kcserver.integration.support;

import com.kcserver.tenancy.TenantContext;
import com.kcserver.tenancy.TenantSchemaProvisioner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static com.kcserver.integration.support.TestJwtUtils.jwtWithTenant;

public abstract class AbstractTenantIntegrationTest {

    /**
     * 🔑 Standard-Test-Tenant
     * (pro Testklasse überschreibbar)
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

    @BeforeEach
    void setupTenant() {
        String tenant = tenant();

        tenantSchemaProvisioner.createFromBaseline(tenant);
        TenantContext.setCurrentTenant(tenant);

        jdbcTemplate.execute("SET search_path TO " + tenant);

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

    /**
     * ✅ EINZIGE Request-Methode
     */

    protected RequestPostProcessor tenantAuth() {
        return jwtWithTenant(tenant());
    }

    protected MockHttpServletRequestBuilder tenantRequest(
            MockHttpServletRequestBuilder builder
    ) {
        return builder.with(tenantAuth());
    }
}