package com.kcserver.core.tenancy;

import com.kcserver.core.config.SchemaMultiTenantConnectionProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Dedicated disposable database only. One pool connection exercises actual reuse. */
@EnabledIfEnvironmentVariable(named = "KC_TENANT_TEST_JDBC",
        matches = "jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/kc_tenant_boundary_test")
class TenantBoundaryPostgresTest {
    private HikariDataSource pool;
    private JdbcTemplate jdbc;
    private SchemaMultiTenantConnectionProvider provider;
    private String a;
    private String b;

    @BeforeEach void setup() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv("KC_TENANT_TEST_JDBC"));
        config.setUsername(System.getenv("KC_TENANT_TEST_USER"));
        config.setPassword(System.getenv("KC_TENANT_TEST_PASSWORD"));
        config.setMaximumPoolSize(1);
        config.setConnectionTimeout(3000);
        pool = new HikariDataSource(config);
        jdbc = new JdbcTemplate(pool);
        provider = new SchemaMultiTenantConnectionProvider(pool);
        a = "test_a_" + UUID.randomUUID().toString().replace("-", "");
        b = "test_b_" + UUID.randomUUID().toString().replace("-", "");
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS kanu");
        for (String schema : new String[]{a, b}) {
            jdbc.execute("CREATE SCHEMA " + TenantSchemaName.quoted(schema));
            jdbc.execute("CREATE TABLE " + TenantSchemaName.quoted(schema) + ".tenant_probe(id int primary key, value text)");
            jdbc.update("INSERT INTO " + TenantSchemaName.quoted(schema) + ".tenant_probe VALUES (1, ?)", schema);
        }
    }

    @AfterEach void close() {
        TenantContext.clear();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        if (pool != null) {
            try {
                if (a != null) jdbc.execute("DROP SCHEMA IF EXISTS " + TenantSchemaName.quoted(a) + " CASCADE");
                if (b != null) jdbc.execute("DROP SCHEMA IF EXISTS " + TenantSchemaName.quoted(b) + " CASCADE");
            } finally {
                pool.close();
            }
        }
    }

    @Test void sameIdsStaySeparatedAcrossReusedConnection() throws Exception {
        Connection connection = provider.getConnection(a);
        int backend;
        try {
            assertEquals(a, value(connection));
            backend = backend(connection);
            try (var statement = connection.createStatement()) {
                assertEquals(1, statement.executeUpdate("UPDATE tenant_probe SET value='changed-a' WHERE id=1"));
            }
        } finally {
            provider.releaseConnection(a, connection);
        }
        connection = provider.getConnection(b);
        try {
            assertEquals(backend, backend(connection));
            assertEquals(b, value(connection));
        } finally {
            provider.releaseConnection(b, connection);
        }
        connection = provider.getConnection(a);
        try {
            assertEquals("changed-a", value(connection));
        } finally {
            provider.releaseConnection(a, connection);
        }
        try (var bootstrap = provider.getAnyConnection()) {
            assertEquals("kanu", bootstrap.getSchema());
        }
    }

    @Test void failedTransactionResetDiscardsPhysicalConnectionAndPoolRecovers() throws Exception {
        Connection connection = provider.getConnection(a);
        int oldBackend = backend(connection);
        connection.setAutoCommit(false);
        try (var statement = connection.createStatement()) {
            assertThrows(SQLException.class, () -> statement.execute("SELECT 1/0"));
        }
        assertThrows(SQLException.class, () -> provider.releaseConnection(a, connection));
        assertTrue(connection.isClosed());
        Connection next = provider.getConnection(b);
        try {
            assertNotEquals(oldBackend, backend(next));
            assertEquals(b, value(next));
        } finally {
            provider.releaseConnection(b, next);
        }
    }

    @Test void invalidIdentifiersNeverBorrowOrChangeTenant() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> provider.getConnection(a + "," + b));
        assertThrows(IllegalArgumentException.class, () -> provider.getConnection("a".repeat(64)));
        assertEquals(0, pool.getHikariPoolMXBean().getActiveConnections());
        Connection next = provider.getConnection(b);
        try {
            assertEquals(b, value(next));
        } finally {
            provider.releaseConnection(b, next);
        }
    }

    @Test void jwtRequestsRouteToOwnSchemaAndClearContextAfterDatabaseFailure() throws Exception {
        var schemas = new TenantSchemaServiceImpl(new TenantSchemaProvisioner(pool));
        var filter = new JwtTenantFilter(schemas);
        for (String schema : new String[]{a, b}) {
            var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("verified-by-security-chain")
                    .header("alg", "none").subject("user").claim("tenant", schema).build();
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt));
            var request = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/probe");
            assertThrows(jakarta.servlet.ServletException.class, () -> filter.doFilter(request,
                    new org.springframework.mock.web.MockHttpServletResponse(), (req, res) -> {
                        Connection connection = null;
                        try {
                            connection = provider.getConnection(new TenantIdentifierResolver().resolveCurrentTenantIdentifier());
                            assertEquals(schema, value(connection));
                            try (var statement = connection.createStatement()) {
                                statement.execute("SELECT 1/0");
                            }
                        } catch (SQLException failure) {
                            throw new jakarta.servlet.ServletException(failure);
                        } finally {
                            if (connection != null) {
                                try { provider.releaseConnection(schema, connection); }
                                catch (SQLException failure) { throw new jakarta.servlet.ServletException(failure); }
                            }
                        }
                    }));
            assertNull(TenantContext.getCurrentTenant());
            assertEquals(0, pool.getHikariPoolMXBean().getActiveConnections());
        }
    }

    @Test void onboardingCreatesOnlySchemaAndRemainsIdempotent() {
        jdbc.execute("DROP SCHEMA " + TenantSchemaName.quoted(a) + " CASCADE");
        var database = new TenantDatabaseService();
        ReflectionTestUtils.setField(database, "dataSource", pool);
        var onboarding = new TenantOnboardingService();
        ReflectionTestUtils.setField(onboarding, "tenantDatabaseService", database);
        onboarding.onboardTenant(a);
        onboarding.onboardTenant(a);
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM information_schema.schemata WHERE schema_name=?", Integer.class, a));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema=?", Integer.class, a));
        assertThrows(IllegalArgumentException.class, () -> onboarding.onboardTenant(a + ";DROP SCHEMA " + b));
        assertEquals(b, jdbc.queryForObject("SELECT value FROM " + TenantSchemaName.quoted(b) + ".tenant_probe", String.class));
    }

    @Test void existingSchemasStayUntouchedByRuntimeProvisioning() {
        var provisioner = new TenantSchemaProvisioner(pool);
        provisioner.createFromBaselineIfNeeded(a);
        assertEquals(a, jdbc.queryForObject("SELECT value FROM " + TenantSchemaName.quoted(a) + ".tenant_probe", String.class));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema=?", Integer.class, a));
        assertThrows(IllegalArgumentException.class, () -> provisioner.createFromBaselineIfNeeded("a".repeat(64)));
    }

    @Test void changelogLookupHandlesMissingEmptyAndPopulatedTables() {
        var migrator = new TenantLiquibaseMigrator(pool, jdbc);
        assertEquals(false, ReflectionTestUtils.invokeMethod(migrator, "hasLiquibaseEntries", a));
        jdbc.execute("CREATE TABLE " + TenantSchemaName.quoted(a) + ".databasechangelog(id text)");
        assertEquals(false, ReflectionTestUtils.invokeMethod(migrator, "hasLiquibaseEntries", a));
        jdbc.execute("INSERT INTO " + TenantSchemaName.quoted(a) + ".databasechangelog VALUES ('done')");
        assertEquals(true, ReflectionTestUtils.invokeMethod(migrator, "hasLiquibaseEntries", a));
        assertEquals(false, ReflectionTestUtils.invokeMethod(migrator, "hasLiquibaseEntries", b));
        assertEquals(false, ReflectionTestUtils.invokeMethod(migrator, "isManagedSchema", "club_bad\"name"));
        assertEquals(true, ReflectionTestUtils.invokeMethod(migrator, "isManagedSchema", "kanu"));
        assertEquals(true, ReflectionTestUtils.invokeMethod(migrator, "isManagedSchema", "ekc_eschweilerkanuclub"));
    }

    @Test void permissionDeniedOnExistingChangelogIsNotReportedAsAbsent() {
        String role = "test_role_" + UUID.randomUUID().toString().replace("-", "");
        jdbc.execute("CREATE TABLE " + TenantSchemaName.quoted(a) + ".databasechangelog(id text)");
        jdbc.execute("CREATE ROLE " + TenantSchemaName.quoted(role) + " LOGIN");
        try {
            jdbc.execute("GRANT USAGE ON SCHEMA " + TenantSchemaName.quoted(a) + " TO " + TenantSchemaName.quoted(role));
            var restricted = new org.springframework.jdbc.datasource.DriverManagerDataSource(
                    System.getenv("KC_TENANT_TEST_JDBC"), role, "");
            var migrator = new TenantLiquibaseMigrator(restricted, new JdbcTemplate(restricted));
            var failure = assertThrows(org.springframework.dao.DataAccessException.class,
                    () -> ReflectionTestUtils.invokeMethod(migrator, "hasLiquibaseEntries", a));
            assertInstanceOf(SQLException.class, failure.getMostSpecificCause());
            assertEquals("42501", ((SQLException) failure.getMostSpecificCause()).getSQLState());
        } finally {
            jdbc.execute("REVOKE USAGE ON SCHEMA " + TenantSchemaName.quoted(a) + " FROM " + TenantSchemaName.quoted(role));
            jdbc.execute("DROP ROLE " + TenantSchemaName.quoted(role));
        }
    }

    private static String value(Connection connection) throws SQLException {
        try (var statement = connection.createStatement(); var result = statement.executeQuery("SELECT value FROM tenant_probe WHERE id=1")) {
            assertTrue(result.next());
            return result.getString(1);
        }
    }

    private static int backend(Connection connection) throws SQLException {
        try (var statement = connection.createStatement(); var result = statement.executeQuery("SELECT pg_backend_pid()")) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }
}
