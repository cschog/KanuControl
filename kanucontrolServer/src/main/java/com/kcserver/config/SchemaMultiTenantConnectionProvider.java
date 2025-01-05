package com.kcserver.config;

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

    public SchemaMultiTenantConnectionProvider() {
        logger.info("SchemaMultiTenantConnectionProvider initialized.");
    }

    @Autowired
    private DataSource dataSource;

    @Override
    protected DataSource selectAnyDataSource() {
        logger.info("Selecting default data source.");
        return dataSource;
    }

    @Override
    protected DataSource selectDataSource(Object tenantIdentifier) {
        logger.info("Selecting data source for tenant identifier: {}", tenantIdentifier);
        if (!(tenantIdentifier instanceof String)) {
            throw new IllegalArgumentException("Tenant identifier must be a string.");
        }
        return dataSource;
    }

    public Connection getTenantConnection(String tenantIdentifier) throws SQLException {
        logger.info("Getting connection for tenant: {}", tenantIdentifier);
        try (Connection connection = dataSource.getConnection()) {
            ensureSchemaExists(connection, tenantIdentifier);
            switchToSchema(connection, tenantIdentifier);
            return connection;
        }
    }

    private void ensureSchemaExists(Connection connection, String tenantIdentifier) throws SQLException {
        logger.info("Checking if schema exists for tenant: {}", tenantIdentifier);
        try (Statement statement = connection.createStatement()) {
            logger.debug("Executing schema creation statement for tenant: {}", tenantIdentifier);
            statement.execute("CREATE SCHEMA IF NOT EXISTS `" + tenantIdentifier + "`");
            logger.info("Schema ensured for tenant: {}", tenantIdentifier);
        } catch (SQLException e) {
            logger.error("Error ensuring schema for tenant: {}", tenantIdentifier, e);
            throw e;
        }
    }

    private void switchToSchema(Connection connection, String schema) throws SQLException {
        logger.info("Switching connection to schema: {}", schema);
        try (Statement statement = connection.createStatement()) {
            logger.debug("Executing 'USE' statement for schema: {}", schema);
            statement.execute("USE `" + schema + "`");
            logger.info("Successfully switched to schema: {}", schema);
        } catch (SQLException e) {
            logger.error("Error switching to schema: {}", schema, e);
            throw e;
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        logger.debug("supportsAggressiveRelease: false");
        return false;
    }
}