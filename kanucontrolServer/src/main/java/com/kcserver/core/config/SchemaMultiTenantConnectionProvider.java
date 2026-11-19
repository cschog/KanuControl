package com.kcserver.core.config;

import com.kcserver.core.tenancy.TenantSchemaName;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Profile("!junit")
@Component
public class SchemaMultiTenantConnectionProvider
        implements MultiTenantConnectionProvider {

    private transient final DataSource dataSource;

    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection)
            throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        String schema = TenantSchemaName.requireValid(
                tenantIdentifier == null ? null : tenantIdentifier.toString());

        Connection connection = getAnyConnection();

        try {
            connection.setSchema(schema);
            return connection;
        } catch (SQLException | RuntimeException ex) {
            discard(connection, ex);
            throw ex;
        }
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection)
            throws SQLException {

        try {
            connection.setSchema("kanu");
        } catch (SQLException | RuntimeException ex) {
            discard(connection, ex);
            throw ex;
        }

        connection.close();
    }

    private void discard(Connection connection, Exception failure) {
        // A failed reset must not return an unknown search_path to the pool.
        try {
            connection.abort(Runnable::run);
        } catch (SQLException | RuntimeException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
        try {
            connection.close();
        } catch (SQLException | RuntimeException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException();
    }
}
