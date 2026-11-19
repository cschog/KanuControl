package com.kcserver.core.tenancy;

import com.kcserver.core.config.SchemaMultiTenantConnectionProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantBoundaryTest {
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "club_a,club_b", "club_a;DROP SCHEMA club_b", "club_\"escape", "CLUB_A", "club_ä",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
    void allSchemaDdlAndChangelogEntrypointsRejectUnsafeIdentifiersBeforeSql(String schema) {
        DataSource ds = mock(DataSource.class);
        var jdbc = mock(org.springframework.jdbc.core.JdbcTemplate.class);
        var database = new TenantDatabaseService();
        org.springframework.test.util.ReflectionTestUtils.setField(database, "dataSource", ds);
        var provisioner = new TenantSchemaProvisioner(ds);
        var migrator = new TenantLiquibaseMigrator(ds, jdbc);
        assertThrows(IllegalArgumentException.class, () -> database.ensureSchemaExists(schema));
        assertThrows(IllegalArgumentException.class, () -> provisioner.createFromBaseline(schema));
        assertThrows(IllegalArgumentException.class, () -> provisioner.createFromBaselineIfNeeded(schema));
        assertThrows(IllegalArgumentException.class, () -> org.springframework.test.util.ReflectionTestUtils
                .invokeMethod(migrator, "hasLiquibaseEntries", schema));
        verifyNoInteractions(ds, jdbc);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "club_a,club_b", "club_a;DROP SCHEMA club_b", "club_\"escape", "CLUB_A", "club_ä",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
    void meterImportCannotReachAnyOfItsThreeQueriesWithUnsafeConfiguredSchema(String schema) throws Exception {
        DataSource ds = mock(DataSource.class);
        var config = new com.kcserver.vermietung.zaehlerimport.MeterImportConfig();
        config.setEnabled(true);
        config.setKeyHash("a".repeat(64));
        config.setTenant(schema);
        config.setMeterId(1L);
        config.setMeterNumber("bound-meter");
        var service = new com.kcserver.vermietung.zaehlerimport.MeterImportService(ds, config);
        var reading = new com.kcserver.vermietung.zaehlerimport.MeterImportController.Reading(schema, 1L,
                java.time.OffsetDateTime.parse("2026-10-01T12:00:00Z"), java.math.BigDecimal.ONE, "kWh", "test");
        var failure = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.ingest(reading));
        assertEquals(403, failure.getStatusCode().value());
        verifyNoInteractions(ds);
    }

    @AfterEach void clear() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test void rejectsIdentifiersBeforeBorrowingConnection() throws Exception {
        DataSource ds = mock(DataSource.class);
        var provider = new SchemaMultiTenantConnectionProvider(ds);
        for (String invalid : new String[]{"", "Club_a", "club_a,club_b", "club_a;select 1", "a\"b", "a".repeat(64), "pg_ä"}) {
            assertThrows(IllegalArgumentException.class, () -> provider.getConnection(invalid));
        }
        assertThrows(IllegalArgumentException.class, () -> provider.getConnection(null));
        verifyNoInteractions(ds);
        assertEquals("a".repeat(63), TenantSchemaName.requireValid("a".repeat(63)));
        assertEquals("kanu", new TenantIdentifierResolver().resolveCurrentTenantIdentifier());
    }

    @Test void acquisitionFailurePreservesCleanupFailures() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(ds.getConnection()).thenReturn(connection);
        SQLException original = new SQLException("set schema");
        doThrow(original).when(connection).setSchema("club_a");
        doThrow(new SQLException("abort")).when(connection).abort(any());
        doThrow(new SQLException("close")).when(connection).close();
        assertSame(original, assertThrows(SQLException.class,
                () -> new SchemaMultiTenantConnectionProvider(ds).getConnection("club_a")));
        assertEquals(2, original.getSuppressed().length);
        verify(connection).close();
    }

    @Test void resetFailureAbortsBeforeReturningToPool() throws Exception {
        Connection connection = mock(Connection.class);
        SQLException original = new SQLException("reset");
        doThrow(original).when(connection).setSchema("kanu");
        assertSame(original, assertThrows(SQLException.class,
                () -> new SchemaMultiTenantConnectionProvider(mock(DataSource.class)).releaseConnection("club_a", connection)));
        var order = inOrder(connection);
        order.verify(connection).setSchema("kanu");
        order.verify(connection).abort(any());
        order.verify(connection).close();
    }

    @Test void successfulReleaseKeepsBootstrapReset() throws Exception {
        Connection connection = mock(Connection.class);
        new SchemaMultiTenantConnectionProvider(mock(DataSource.class)).releaseConnection("club_a", connection);
        var order = inOrder(connection);
        order.verify(connection).setSchema("kanu");
        order.verify(connection).close();
        verify(connection, never()).abort(any());
    }

    @Test void initializationFailureClearsContextAndNextRequestUsesOwnTenant() throws Exception {
        TenantSchemaService schemas = mock(TenantSchemaService.class);
        var filter = new JwtTenantFilter(schemas);
        doThrow(new IllegalStateException("migration failed")).when(schemas).initializeTenant("club_a");
        authenticate("club_a", null);
        assertThrows(IllegalStateException.class, () -> filter.doFilter(request(), new MockHttpServletResponse(),
                (req, res) -> fail("must not reach application")));
        assertNull(TenantContext.getCurrentTenant());
        authenticate("club_b", null);
        filter.doFilter(request(), new MockHttpServletResponse(),
                (req, res) -> assertEquals("club_b", TenantContext.getCurrentTenant()));
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test void downstreamFailureClearsContextAndKeycloakGroupFallbackWorks() throws Exception {
        var filter = new JwtTenantFilter(mock(TenantSchemaService.class));
        authenticate(null, List.of("/CLUB_A"));
        assertThrows(IllegalStateException.class, () -> filter.doFilter(request(), new MockHttpServletResponse(),
                (req, res) -> {
                    assertEquals("club_a", TenantContext.getCurrentTenant());
                    throw new IllegalStateException("application failed");
                }));
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test void missingJwtAndMissingClaimNeverProvision() throws Exception {
        TenantSchemaService schemas = mock(TenantSchemaService.class);
        var filter = new JwtTenantFilter(schemas);
        var response = new MockHttpServletResponse();
        filter.doFilter(request(), response, (req, res) -> fail());
        assertEquals(401, response.getStatus());
        authenticate(null, null);
        response = new MockHttpServletResponse();
        filter.doFilter(request(), response, (req, res) -> fail());
        assertEquals(400, response.getStatus());
        verifyNoInteractions(schemas);
    }

    @Test void unsafeClaimNeverProvisionsAndClearsContext() {
        TenantSchemaService schemas = mock(TenantSchemaService.class);
        authenticate("club_a,club_b", null);
        assertThrows(IllegalArgumentException.class, () -> new JwtTenantFilter(schemas)
                .doFilter(request(), new MockHttpServletResponse(), (req, res) -> fail()));
        verifyNoInteractions(schemas);
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test void migrationReadErrorsDoNotMasqueradeAsMissingChangelog() {
        var jdbc = mock(org.springframework.jdbc.core.JdbcTemplate.class);
        var migrator = new TenantLiquibaseMigrator(mock(DataSource.class), jdbc);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("club_a"), eq("databasechangelog"))).thenReturn(1);
        var failure = new org.springframework.dao.PermissionDeniedDataAccessException("denied", new SQLException());
        when(jdbc.queryForObject("SELECT count(*) FROM \"club_a\".databasechangelog", Integer.class)).thenThrow(failure);
        assertSame(failure, assertThrows(org.springframework.dao.PermissionDeniedDataAccessException.class,
                () -> org.springframework.test.util.ReflectionTestUtils.invokeMethod(migrator, "hasLiquibaseEntries", "club_a")));
    }

    private static MockHttpServletRequest request() {
        return new MockHttpServletRequest("GET", "/api/probe");
    }

    private static void authenticate(String tenant, List<String> groups) {
        var builder = Jwt.withTokenValue("test").header("alg", "none").subject("test-user");
        if (tenant != null) builder.claim("tenant", tenant);
        if (groups != null) builder.claim("groups", groups);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(builder.build()));
    }
}
