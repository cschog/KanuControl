package com.kcserver.tenancy;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class TenantSchemaInitializer {

    private final DataSource dataSource;

    public TenantSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates the tenant schema if it does not exist
     * and runs Liquibase migrations for it.
     */
    public void createSchemaIfNotExists(String schema) {

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schema);

            connection.setSchema(schema);

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            database.setDefaultSchemaName(schema);

            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.yaml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update("");

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize schema " + schema, e);
        }
    }
}