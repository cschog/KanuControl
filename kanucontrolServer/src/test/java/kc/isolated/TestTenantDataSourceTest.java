package kc.isolated;

import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.support.config.TestTenantDataSourceConfig.TenantDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestTenantDataSourceTest {
    @Test void jedePoolVerbindungWaehltDenAktuellenMandantenUndSetztIhnDanachZurueck() throws Exception {
        try (var pool = new HikariDataSource()) {
            pool.setJdbcUrl("jdbc:h2:mem:" + UUID.randomUUID());
            pool.setMaximumPoolSize(2);
            try (var connection = pool.getConnection(); var sql = connection.createStatement()) {
                sql.execute("CREATE SCHEMA KANU");
                sql.execute("CREATE SCHEMA CLUB_A");
                sql.execute("CREATE SCHEMA CLUB_B");
            }
            var dataSource = new TenantDataSource(pool, "KANU");
            TenantContext.setCurrentTenant("CLUB_A");
            try (var first = dataSource.getConnection(); var second = dataSource.getConnection()) {
                assertSchema(first, "CLUB_A");
                assertSchema(second, "CLUB_A");
            }
            TenantContext.setCurrentTenant("CLUB_B");
            try (var first = dataSource.getConnection(); var second = dataSource.getConnection()) {
                assertSchema(first, "CLUB_B");
                assertSchema(second, "CLUB_B");
            }
            TenantContext.clear();
            try (var first = dataSource.getConnection(); var second = dataSource.getConnection()) {
                assertSchema(first, "KANU");
                assertSchema(second, "KANU");
            }
        } finally {
            TenantContext.clear();
        }
    }
    private void assertSchema(Connection connection, String expected) throws SQLException {
        try (var statement = connection.createStatement(); var rows = statement.executeQuery("SELECT CURRENT_SCHEMA")) {
            rows.next();
            assertEquals(expected, rows.getString(1));
        }
    }
}
