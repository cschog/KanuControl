package com.kcserver.tenancy;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.Stoppable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider, Stoppable {

    private final DataSource dataSource;

    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        if (tenantIdentifier == null) {
            throw new IllegalArgumentException("Tenant identifier cannot be null");
        }
        Connection connection = dataSource.getConnection();
        connection.setSchema(tenantIdentifier.toString()); // Switch schema
        return connection;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return MultiTenantConnectionProvider.class.isAssignableFrom(unwrapType);
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return (T) this;
    }

    @Override
    public void stop() {
        // Cleanup resources if needed
    }
}