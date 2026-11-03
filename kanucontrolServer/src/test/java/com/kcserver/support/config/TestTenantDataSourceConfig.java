package com.kcserver.support.config;

import com.kcserver.core.tenancy.TenantContext;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** Wählt das Testmandantenschema bei jeder Verbindungsausleihe statt nur im Setup. */
@Configuration
@Profile("junit")
@EnableConfigurationProperties(DataSourceProperties.class)
public class TestTenantDataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    HikariDataSource testConnectionPool(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    DataSource dataSource(@Qualifier("testConnectionPool") DataSource pool, Environment environment) {
        return new TenantDataSource(pool, environment.getRequiredProperty("spring.liquibase.default-schema"));
    }

    public static class TenantDataSource extends DelegatingDataSource {
        private final String defaultSchema;
        public TenantDataSource(DataSource pool, String defaultSchema) {
            super(pool);
            this.defaultSchema = defaultSchema;
        }
        @Override public Connection getConnection() throws SQLException {
            return selectSchema(super.getConnection());
        }
        @Override public Connection getConnection(String username, String password) throws SQLException {
            return selectSchema(super.getConnection(username, password));
        }
        private Connection selectSchema(Connection connection) throws SQLException {
            try {
                String tenant = TenantContext.getCurrentTenant();
                connection.setSchema(tenant == null ? defaultSchema : tenant);
                return connection;
            } catch (SQLException | RuntimeException ex) {
                try { connection.close(); } catch (SQLException closeError) { ex.addSuppressed(closeError); }
                throw ex;
            }
        }
    }
}
