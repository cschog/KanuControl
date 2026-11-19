package com.kcserver.support;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MigrationSqlAssertionsTest {
    @Test void statementCreationFailureCannotSatisfyExpectedSqlFailure() throws Exception {
        var connection = mock(Connection.class);
        var original = new SQLException("connection failed before SQL", "08006");
        when(connection.createStatement()).thenThrow(original);
        assertSame(original, assertThrows(SQLException.class,
                () -> MigrationSqlAssertions.assertSqlState(connection, "SELECT missing", "08006")));
    }

    @Test void wrongSqlStateFailsAndClosesStatement() throws Exception {
        var connection = mock(Connection.class);
        var statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.execute("SELECT missing")).thenThrow(new SQLException("connection lost", "08006"));
        assertThrows(AssertionError.class,
                () -> MigrationSqlAssertions.assertSqlState(connection, "SELECT missing", "42P01"));
        verify(statement).close();
    }

    @Test void expectedSqlStatePassesAndClosesStatement() throws Exception {
        var connection = mock(Connection.class);
        var statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.execute("SELECT missing")).thenThrow(new SQLException("table absent", "42P01"));
        MigrationSqlAssertions.assertSqlState(connection, "SELECT missing", "42P01");
        verify(statement).close();
    }

    @Test void unexpectedSuccessFailsAndStillClosesStatement() throws Exception {
        var connection = mock(Connection.class);
        var statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.execute("SELECT present")).thenReturn(true);
        assertThrows(AssertionError.class,
                () -> MigrationSqlAssertions.assertSqlState(connection, "SELECT present", "42P01"));
        verify(statement).close();
    }
}
