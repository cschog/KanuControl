package com.kcserver.tenancy;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Service
public class TenantDatabaseService {

    @Autowired
    private DataSource dataSource;

    public void createSchemaAndApplyMigrations(String schemaName) {
        createSchemaIfNotExists(schemaName);

        // Apply Liquibase migrations
        try (Connection connection = dataSource.getConnection()) {
            connection.setCatalog(schemaName); // Switch database
            Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
            liquibase.update(new Contexts());
            System.out.println("Liquibase migrations applied to schema: " + schemaName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply migrations to schema: " + schemaName, e);
        }
    }

    public void createSchemaIfNotExists(String schemaName) {
        String createSchemaSql = String.format("CREATE DATABASE IF NOT EXISTS `%s`", schemaName);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createSchemaSql);
            System.out.println("Schema created or already exists: " + schemaName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create schema: " + schemaName, e);
        }
    }
}