package com.kcserver.tenancy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Service
public class TenantDatabaseService {

    @Autowired
    private DataSource dataSource;

    public void ensureSchemaExists(String schemaName) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // Create the schema if it doesn't exist
            statement.execute(String.format("CREATE SCHEMA IF NOT EXISTS `%s`", schemaName));
            System.out.println("Schema ensured: " + schemaName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure schema: " + schemaName, e);
        }
    }
}