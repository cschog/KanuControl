package com.kcserver.tenancy;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!test")
public class TenantSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final List<String> tenants;

    public TenantSchemaInitializer(
            JdbcTemplate jdbcTemplate,
            @Value("${kcserver.test-tenants}") List<String> tenants
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.tenants = tenants;
    }

    @PostConstruct
    public void initTenantSchemas() {
        for (String tenant : tenants) {
            createSchemaIfNotExists(tenant);
            cloneTablesFromBaseSchema(tenant);
        }
    }

    private void createSchemaIfNotExists(String schema) {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
    }

    private void cloneTablesFromBaseSchema(String tenantSchema) {
        List<String> tables = jdbcTemplate.queryForList(
                """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'kanu'
                  AND table_type = 'BASE TABLE'
                """,
                String.class
        );

        for (String table : tables) {
            jdbcTemplate.execute(
                    """
                    CREATE TABLE IF NOT EXISTS %s.%s
                    (LIKE kanu.%s INCLUDING ALL)
                    """.formatted(tenantSchema, table, table)
            );
        }
    }
}