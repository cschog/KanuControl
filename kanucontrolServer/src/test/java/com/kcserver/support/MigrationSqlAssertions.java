package com.kcserver.support;

import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

public final class MigrationSqlAssertions {
    private MigrationSqlAssertions() {}

    public static void assertSqlState(Connection connection, String sql, String expectedState) throws SQLException {
        // Creation must fail the test outright, never satisfy its expected SQL error.
        try (var statement = connection.createStatement()) {
            SQLException failure = assertThrows(SQLException.class, () -> statement.execute(sql));
            assertEquals(expectedState, failure.getSQLState(), failure::getMessage);
            // Statement.close also closes an unexpected ResultSet if execute succeeds.
        }
    }
}
