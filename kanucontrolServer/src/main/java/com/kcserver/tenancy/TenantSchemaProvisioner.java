package com.kcserver.tenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TenantSchemaProvisioner {

    private static final Logger log =
            LoggerFactory.getLogger(TenantSchemaProvisioner.class);

    private final JdbcTemplate jdbcTemplate;

    /**
     * Merkt sich bereits initialisierte Tenants (Runtime)
     */
    private final Set<String> initializedTenants =
            ConcurrentHashMap.newKeySet();

    /**
     * Baseline-Schema (Liquibase!)
     */
    private static final String BASELINE_SCHEMA = "kanu";

    /**
     * Reihenfolge ist WICHTIG wegen Foreign Keys
     */
    private static final List<String> TABLES_IN_ORDER = List.of(
            "person",
            "verein",
            "veranstaltung",
            "teilnehmer",
            "erhebungsbogen",
            "mitglied"
    );

    public TenantSchemaProvisioner(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* =================================================
       Öffentliche API
       ================================================= */

    /**
     * Für Runtime (TenantFilter → Service)
     * → idempotent + performant
     */
    public void createFromBaselineIfNeeded(String tenantSchema) {

        if (initializedTenants.contains(tenantSchema)) {
            return;
        }

        synchronized (this) {
            if (initializedTenants.contains(tenantSchema)) {
                return;
            }

            createFromBaseline(tenantSchema);
            initializedTenants.add(tenantSchema);
        }
    }

    /**
     * Für Tests (explizit, deterministisch)
     */
    public void createFromBaseline(String tenantSchema) {

        log.info("Provisioning tenant schema '{}'", tenantSchema);

        createSchemaIfNotExists(tenantSchema);
        createTables(tenantSchema);

        log.info("Tenant schema '{}' successfully provisioned", tenantSchema);
    }

    /* =================================================
       Internals
       ================================================= */

    private void createSchemaIfNotExists(String schema) {
        jdbcTemplate.execute(
                "CREATE SCHEMA IF NOT EXISTS \"" + schema + "\""
        );
    }

    private void createTables(String tenantSchema) {
        for (String table : TABLES_IN_ORDER) {

            String sql = """
                CREATE TABLE IF NOT EXISTS %s.%s
                (LIKE %s.%s INCLUDING ALL)
                """.formatted(
                    tenantSchema,
                    table,
                    BASELINE_SCHEMA,
                    table
            );

            log.debug("Creating table {}.{}", tenantSchema, table);
            jdbcTemplate.execute(sql);
        }
    }
}