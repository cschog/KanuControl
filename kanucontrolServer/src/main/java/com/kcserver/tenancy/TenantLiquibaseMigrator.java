package com.kcserver.tenancy;

import jakarta.annotation.PostConstruct;
import liquibase.Contexts;
import liquibase.LabelExpression;
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

                    // ⭐ kanu immer zuerst
                    if (a.equals("kanu")) return -1;
                    if (b.equals("kanu")) return 1;

                    return a.compareTo(b);
                })
                .forEach(this::migrateSchema);
    }

    private void migrateSchema(String schema) {

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
                            "db/changelog/db.changelog-master.yaml",
                            new ClassLoaderResourceAccessor(),
                            database
                    );

            boolean hasPersonTable =
                    tableExists(schema, "person");

            boolean hasLiquibaseEntries =
                    hasLiquibaseEntries(schema);

            /*
             * =====================================================
             * ALTSCHEMAS:
             * Tabellen existieren bereits,
             * Liquibase kennt sie aber noch nicht
             * =====================================================
             */
            if (hasPersonTable && !hasLiquibaseEntries) {

                log.warn("""
                        
                        Existing schema detected without DATABASECHANGELOG.
                        Bootstrapping Liquibase history for schema '{}'
                        
                        """, schema);

                liquibase.changeLogSync(
                        new Contexts(),
                        new LabelExpression()
                );
            }

            /*
             * =====================================================
             * NORMALE MIGRATION
             * =====================================================
             */
            liquibase.update(
                    new Contexts(),
                    new LabelExpression()
            );
            System.out.println("Liquibase migration finished for schema: " + schema);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Liquibase migration failed for schema: " + schema,
                    e
            );
        }
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

        if (SYSTEM_SCHEMAS.contains(schema)) {
            return false;
        }

        return schema.equals("kanu")
                || schema.startsWith("ekc_")
                || schema.startsWith("okc_")
                || schema.startsWith("svbt_")
                || schema.startsWith("wkc_");
    }
}