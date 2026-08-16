package com.kcserver.tenancy;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TenantSchemaProvisioner {

    private static final String BASELINE_CHANGELOG =
            "db/changelog/db.changelog-tenant.yaml";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Merkt sich bereits initialisierte Tenants während der Laufzeit.
     */
    private final Set<String> initializedTenants =
            ConcurrentHashMap.newKeySet();

    public TenantSchemaProvisioner(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Für Runtime:
     *
     * - bereits initialisierten Tenant sofort durchlassen
     * - fehlendes Schema anlegen
     * - komplettes Tenant-Schema über Liquibase aufbauen
     *
     * Idempotent und thread-sicher.
     */
    public void createFromBaselineIfNeeded(String tenantSchema) {

        validateSchemaName(tenantSchema);

        if (initializedTenants.contains(tenantSchema)) {
            return;
        }

        synchronized (this) {

            if (initializedTenants.contains(tenantSchema)) {
                return;
            }

            if (!schemaExists(tenantSchema)) {

                log.info(
                        "Tenant schema '{}' does not exist. Creating and running Liquibase.",
                        tenantSchema
                );

                createSchema(tenantSchema);
                runLiquibase(tenantSchema);

                log.info(
                        "Tenant schema '{}' successfully provisioned.",
                        tenantSchema
                );

            } else {

                log.debug(
                        "Tenant schema '{}' already exists.",
                        tenantSchema
                );
            }

            initializedTenants.add(tenantSchema);
        }
    }

    /**
     * Explizites Provisioning.
     *
     * Wird z. B. für Tests oder eine gezielte Neuanlage verwendet.
     *
     * Das Schema muss nicht vorher existieren.
     */
    public void createFromBaseline(String tenantSchema) {

        validateSchemaName(tenantSchema);

        log.info(
                "Provisioning tenant schema '{}'",
                tenantSchema
        );

        createSchema(tenantSchema);
        runLiquibase(tenantSchema);

        initializedTenants.add(tenantSchema);

        log.info(
                "Tenant schema '{}' successfully provisioned.",
                tenantSchema
        );
    }

    /**
     * Legt das Schema an, falls es noch nicht existiert.
     */
    private void createSchema(String schema) {

        jdbcTemplate.execute(
                "CREATE SCHEMA IF NOT EXISTS \"" + schema + "\""
        );
    }

    /**
     * Führt den vollständigen Tenant-Liquibase-Changelog
     * gegen das angegebene Schema aus.
     */
    private void runLiquibase(String schema) {

        log.info(
                "Running Liquibase for tenant schema '{}'",
                schema
        );

        try (Connection connection = dataSource.getConnection()) {

            connection.setSchema(schema);

            Database database =
                    DatabaseFactory.getInstance()
                            .findCorrectDatabaseImplementation(
                                    new JdbcConnection(connection)
                            );

            database.setDefaultSchemaName(schema);

            Liquibase liquibase =
                    new Liquibase(
                            BASELINE_CHANGELOG,
                            new ClassLoaderResourceAccessor(),
                            database
                    );

            liquibase.update(
                    new Contexts(),
                    new LabelExpression()
            );

            log.info(
                    "Liquibase finished successfully for tenant schema '{}'",
                    schema
            );

        } catch (Exception ex) {

            log.error(
                    "Liquibase provisioning failed for tenant schema '{}'",
                    schema,
                    ex
            );

            throw new IllegalStateException(
                    "Tenant-Schema '" + schema +
                            "' konnte nicht über Liquibase aufgebaut werden.",
                    ex
            );
        }
    }

    /**
     * Prüft, ob das PostgreSQL-Schema existiert.
     */
    private boolean schemaExists(String schema) {

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM information_schema.schemata
                WHERE schema_name = ?
                """,
                Integer.class,
                schema
        );

        return count != null && count > 0;
    }

    /**
     * Tenant-Schema kommt letztlich aus dem Tenant-Kontext.
     *
     * Da Schema-Namen als SQL-Identifier verwendet werden,
     * akzeptieren wir hier nur das erwartete Format.
     */
    private void validateSchemaName(String schema) {

        if (schema == null || !schema.matches("[a-z][a-z0-9_]*")) {

            throw new IllegalArgumentException(
                    "Ungültiger Tenant-Schema-Name: " + schema
            );
        }
    }
}