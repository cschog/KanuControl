package com.kcserver.tenancy;

import jakarta.annotation.PostConstruct;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import liquibase.Contexts;
import liquibase.LabelExpression;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class TenantLiquibaseMigrator {

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private static final List<String> SYSTEM_SCHEMAS = List.of(
            "pg_catalog",
            "information_schema",
            "public"
    );

    @PostConstruct
    public void migrateAllSchemas() {

        List<String> schemas = jdbcTemplate.queryForList(
                """
                SELECT schema_name
                FROM information_schema.schemata
                """,
                String.class
        );

        schemas.stream()
                .filter(this::isManagedSchema)
                .sorted((a, b) -> {

                    if (a.equals("kanu")) return -1;
                    if (b.equals("kanu")) return 1;

                    return a.compareTo(b);
                })
                .forEach(schema -> {

                    try {

                        migrateSchema(schema);

                    } catch (Exception ex) {

                        if ("kanu".equals(schema)) {

                            throw new IllegalStateException(
                                    "Migration des Hauptschemas 'kanu' fehlgeschlagen.",
                                    ex
                            );
                        }

                        log.error(
                                "Migration für Schema '{}' fehlgeschlagen. Tenant wird übersprungen.",
                                schema,
                                ex
                        );
                    }
                });

        log.info("Alle Tenant-Migrationen abgeschlossen.");
    }

    private void migrateSchema(String schema) throws Exception {

        log.info("Liquibase migration for schema '{}'", schema);

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
                            "db/changelog/db.changelog-tenant.yaml",
                            new ClassLoaderResourceAccessor(),
                            database
                    );

            boolean emptySchema = isEmptySchema(schema);
            boolean hasLiquibaseEntries = hasLiquibaseEntries(schema);

            if (!emptySchema && !hasLiquibaseEntries) {

                log.warn(
                        "Schema '{}' enthält Tabellen, aber keine DATABASECHANGELOG. Synchronisiere ChangeLog.",
                        schema
                );

                liquibase.changeLogSync(
                        new Contexts(),
                        new LabelExpression()
                );
            }

            liquibase.update();

            log.info("Liquibase migration finished for schema '{}'", schema);
        }
    }

    private boolean isEmptySchema(String schema) {

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM information_schema.tables
                WHERE table_schema = ?
                """,
                Integer.class,
                schema
        );

        return count == null || count == 0;
    }

    private boolean tableExists(String schema, String table) {

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM information_schema.tables
                WHERE table_schema = ?
                AND table_name = ?
                """,
                Integer.class,
                schema,
                table
        );

        return count != null && count > 0;
    }
    private boolean hasLiquibaseEntries(String schema) {

        try {

            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT count(*)
                    FROM %s.databasechangelog
                    """.formatted(schema),
                    Integer.class
            );

            return count != null && count > 0;

        } catch (Exception e) {

            // Tabelle existiert nicht
            return false;
        }
    }
    private boolean isManagedSchema(String schema) {

        return schema.equals("kanu")

                || schema.matches("[a-z]{3,5}_.*");

    }
}