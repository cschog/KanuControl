package com.kcserver;

import com.kcserver.tenancy.TenantSchemaInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true"
})
class TenantSchemaIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    DataSource dataSource;

    @Autowired
    TenantSchemaInitializer tenantSchemaInitializer;

    @Test
    void initializingTenant_createsSchema() {

        String tenant = "ekc_test";

        tenantSchemaInitializer.initializeIfNeeded(tenant);

        assertThat(schemaExists(tenant)).isTrue();
    }

    /**
     * 🔥 Integration test:
     * - first request with tenant header
     * - creates schema if not existing
     * - request succeeds
     */
    @Test
    void firstRequest_createsTenantSchema() throws Exception {

        String tenant = "ekc_test";

        mockMvc.perform(
                        get("/api/person")
                                .header("X-Tenant", tenant)
                                .with(SecurityMockMvcRequestPostProcessors.jwt())
                )
                .andExpect(status().isOk());

        assertThat(schemaExists(tenant)).isTrue();
    }

    /**
     * 🔍 Checks INFORMATION_SCHEMA if schema exists
     */
    private boolean schemaExists(String schemaName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.SCHEMATA
                WHERE SCHEMA_NAME = ?
                """,
                Integer.class,
                schemaName
        );

        return count != null && count > 0;
    }
}