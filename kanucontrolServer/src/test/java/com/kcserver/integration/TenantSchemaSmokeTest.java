package com.kcserver.integration;

import com.kcserver.tenancy.TenantSchemaProvisioner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TenantSchemaSmokeTest {

    @Autowired
    TenantSchemaProvisioner provisioner;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void tenantSchemaContainsBaselineTables() {

        String tenant = "tenant_test_1";

        // WHEN
        provisioner.createFromBaseline(tenant);

        // THEN
        assertTableExists(tenant, "person");
        assertTableExists(tenant, "veranstaltung");
        assertTableExists(tenant, "teilnehmer");
    }

    private void assertTableExists(String schema, String table) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = ?
              AND table_name = ?
            """,
                Integer.class,
                schema,
                table
        );

        assertThat(count)
                .as("Table %s.%s should exist", schema, table)
                .isEqualTo(1);
    }
}