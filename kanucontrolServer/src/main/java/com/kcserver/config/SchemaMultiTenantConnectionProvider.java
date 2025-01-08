package com.kcserver.config;

import com.kcserver.tenancy.TenantContext;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class SchemaMultiTenantConnectionProvider
        extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private static final Logger logger = LoggerFactory.getLogger(SchemaMultiTenantConnectionProvider.class);
    private static final String DEFAULT_SCHEMA = "kanu";

    @Autowired
    private DataSource dataSource;

    public SchemaMultiTenantConnectionProvider() {
        logger.info("SchemaMultiTenantConnectionProvider initialized.");
    }

    @Override
    protected DataSource selectAnyDataSource() {
        logger.debug("Selecting default data source.");
        return dataSource;
    }

    @Override
    protected DataSource selectDataSource(Object tenantIdentifier) {
        String tenantId = TenantContext.getTenantId(); // Ensure this is set after login
        if (tenantId == null) {
            throw new IllegalStateException("Tenant identifier is not set. Ensure login is complete.");
        }
        return dataSource;
    }

    public Connection getTenantConnection(String tenantIdentifier) throws SQLException {
        logger.info("Getting connection for tenant: {}", tenantIdentifier);
        Connection connection = dataSource.getConnection();
        ensureSchemaExists(connection, tenantIdentifier);
        switchToSchema(connection, tenantIdentifier);
        return connection;
    }

    public void ensureSchemaExists(Connection connection, String tenantIdentifier) throws SQLException {
        String createSchemaSQL = "CREATE SCHEMA `" + tenantIdentifier + "`";
        try (Statement statement = connection.createStatement()) {
            logger.info("Attempting to create schema for tenant: {}", tenantIdentifier);
            statement.execute(createSchemaSQL);
            logger.info("Schema created for tenant: {}", tenantIdentifier);
        } catch (SQLException e) {
            if ("42000".equals(e.getSQLState()) || e.getMessage().contains("already exists")) {
                logger.info("Schema already exists for tenant: {}", tenantIdentifier);
            } else {
                logger.error("Unexpected error creating schema for tenant: {}", tenantIdentifier, e);
                // throw e; // Rethrow unexpected exceptions
            }
        }
        runLiquibaseMigrations(tenantIdentifier);
    }

    private void switchToSchema(Connection connection, String schema) throws SQLException {
        logger.info("Switching connection to schema: {}", schema);
        try (Statement statement = connection.createStatement()) {
            statement.execute("USE `" + schema + "`");
            logger.info("Successfully switched to schema: {}", schema);
        } catch (SQLException e) {
            logger.error("Error switching to schema: {}", schema);
            //throw e;
        }
    }

    private void runLiquibaseMigrations(String tenantIdentifier) throws SQLException {
        logger.info("Running Liquibase migrations for schema: {}", tenantIdentifier);

        try (Connection connection = dataSource.getConnection()) {

            // Ensure the connection switches to the correct schema
            try (Statement statement = connection.createStatement()) {
                statement.execute("USE `" + tenantIdentifier + "`");
            }
            // Set up Liquibase
            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.yaml",
                    new ClassLoaderResourceAccessor(),
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
            );
            // Pass the tenant schema as a parameter
            liquibase.setChangeLogParameter("schemaName", tenantIdentifier);
            logger.info("Liquibase schemaName set to: {}", tenantIdentifier);

            // Explicitly set the default schema for Liquibase
            liquibase.getDatabase().setDefaultSchemaName(tenantIdentifier);
            liquibase.getDatabase().setOutputDefaultSchema(false);

            // Run the migrations
            liquibase.update(new Contexts(), new LabelExpression());

            logger.info("Liquibase migrations completed for schema: {}", tenantIdentifier);
        } catch (Exception e) {
            logger.error("Liquibase migration failed for schema: {}", tenantIdentifier);
            // throw new SQLException("Failed to run Liquibase migrations for schema: " + tenantIdentifier, e);
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        logger.debug("supportsAggressiveRelease: false");
        return false;
    }
}