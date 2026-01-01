package com.kcserver.tenancy;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Profile("!test")
@Component
public class TenantSchemaInitializer {

    private static final Logger logger =
            LoggerFactory.getLogger(TenantSchemaInitializer.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 🔥 Schema-Cache (thread-safe)
     */
    private final Set<String> initializedSchemas =
            ConcurrentHashMap.newKeySet();

    public TenantSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Entry point – wird vom Filter / Resolver aufgerufen
     */
    public void initializeIfNeeded(String tenant) {
        if (initializedSchemas.contains(tenant)) {
            logger.debug("Schema {} already initialized (cache hit)", tenant);
            return;
        }

        synchronized (this) {
            if (initializedSchemas.contains(tenant)) {
                return;
            }

            createSchemaIfNotExists(tenant);
            runLiquibase(tenant);

            initializedSchemas.add(tenant);
            logger.info("Schema {} initialized and cached", tenant);
        }
    }

    private void createSchemaIfNotExists(String schema) {
        logger.info("Ensuring schema {} exists", schema);
        jdbcTemplate.execute(
                "CREATE SCHEMA IF NOT EXISTS \"" + schema + "\""
        );
    }

    private void runLiquibase(String schema) {
        logger.info("Running Liquibase for schema {}", schema);

        try (Connection connection = dataSource.getConnection()) {

            connection.setSchema(schema);

            Database database =
                    DatabaseFactory.getInstance()
                            .findCorrectDatabaseImplementation(
                                    new JdbcConnection(connection)
                            );

            // 🔥🔥🔥 DAS FEHLT BEI DIR 🔥🔥🔥
            database.setDefaultSchemaName(schema);

            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.yaml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update(new Contexts(), new LabelExpression());

        } catch (Exception ex) {
            logger.error("Liquibase failed for schema {}", schema, ex);
            throw new IllegalStateException(
                    "Failed to initialize schema " + schema, ex
            );
        }
    }
}