package com.kcserver.config;

import com.kcserver.tenancy.TenantContext;
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
import java.sql.*;

@Component
public class SchemaMultiTenantConnectionProvider
        extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private static final Logger logger = LoggerFactory.getLogger(SchemaMultiTenantConnectionProvider.class);
    private static final String DEFAULT_SCHEMA = "kanu";

    @Autowired
    private DataSource dataSource;

    @Override
    protected DataSource selectAnyDataSource() {
        return dataSource;
    }

    public SchemaMultiTenantConnectionProvider() {
        logger.info("SchemaMultiTenantConnectionProvider initialized.");
    }

    @Override
    protected DataSource selectDataSource(Object tenantIdentifier) {
        String tenantId = TenantContext.getTenant();
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

    private boolean isSchemaInitialized(String tenantId) {
        String query = "SELECT initialized FROM schema_status WHERE tenant_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean("initialized");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check schema initialization status", e);
        }
    }

    private void markSchemaAsInitialized(String tenantId) {
        String query = "INSERT INTO schema_status (tenant_id, initialized) VALUES (?, TRUE) " +
                "ON DUPLICATE KEY UPDATE initialized = TRUE, last_updated = CURRENT_TIMESTAMP";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, tenantId);
            statement.executeUpdate();
            logger.info("Marked schema as initialized for tenant: {}", tenantId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark schema as initialized", e);
        }
    }

    private void switchToSchema(Connection connection, String schema) throws SQLException {
        logger.info("Switching to schema: {}", schema);
        try (Statement statement = connection.createStatement()) {
            statement.execute("USE `" + schema + "`");
            logger.info("Switched to schema: {}", schema);
        }
    }

    public void ensureSchemaExists(Connection connection, String tenantIdentifier) throws SQLException {
        logger.info("Ensuring schema exists for tenant: {}", tenantIdentifier);

        // Create schema if it doesn't exist
        try (Statement statement = connection.createStatement()) {
            String createSchemaSQL = "CREATE SCHEMA IF NOT EXISTS `" + tenantIdentifier + "`";
            statement.execute(createSchemaSQL);
            logger.info("Schema ensured for tenant: {}", tenantIdentifier);
        }

        // Run migrations only if schema is not initialized
        if (!isSchemaInitialized(tenantIdentifier)) {
            runLiquibaseMigrations(tenantIdentifier);
            markSchemaAsInitialized(tenantIdentifier);
        } else {
            logger.info("Schema already initialized for tenant: {}", tenantIdentifier);
        }
    }

    private void runLiquibaseMigrations(String tenantIdentifier) throws SQLException {
        logger.info("Running Liquibase migrations for schema: {}", tenantIdentifier);

        try (Connection connection = dataSource.getConnection()) {
            switchToSchema(connection, tenantIdentifier);

            // Configure Liquibase
            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.yaml",
                    new ClassLoaderResourceAccessor(),
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
            );

            // Set Liquibase parameters
            liquibase.setChangeLogParameter("schemaName", tenantIdentifier);
            logger.info("Liquibase schemaName set to: {}", tenantIdentifier);

            // Run migrations
            liquibase.update((String) null); // Using the current context
            logger.info("Liquibase migrations completed for schema: {}", tenantIdentifier);
        } catch (Exception e) {
            logger.error("Liquibase migration failed for schema: {}", tenantIdentifier, e);
            throw new SQLException("Failed to run Liquibase migrations for schema: " + tenantIdentifier, e);
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }
}