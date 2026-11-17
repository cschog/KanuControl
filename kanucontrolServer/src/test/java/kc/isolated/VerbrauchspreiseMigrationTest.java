package kc.isolated;

import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class VerbrauchspreiseMigrationTest {
 @Test void uebernimmtBestandEinmaligUndErhaeltSnapshots() throws Exception {
  try (var c = DriverManager.getConnection(System.getProperty("kc.test.verbrauchspreise.jdbcUrl", "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL"))) {
   var sql = c.createStatement();
   sql.execute("CREATE TABLE verbrauchskosten(id BIGINT PRIMARY KEY, preis NUMERIC(10,2) NOT NULL)");
   sql.execute("INSERT INTO verbrauchskosten VALUES(1,0.40),(2,0),(3,99999999.99)");
   sql.execute("CREATE TABLE buchung_verbrauchskosten(id BIGINT PRIMARY KEY, preis NUMERIC(10,2), kosten NUMERIC(10,2))");
   sql.execute("INSERT INTO buchung_verbrauchskosten VALUES(9,0.25,5.00)");
   var db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
   try (var lb = new Liquibase("db/changelog/changes/20261117-01-verbrauchspreise.xml", new ClassLoaderResourceAccessor(), db)) {
    lb.validate();
    lb.update(new Contexts(), new LabelExpression());
    lb.update(new Contexts(), new LabelExpression());
    try (var rows = sql.executeQuery("SELECT p.*, v.preis AS alt FROM verbrauchskosten_preis p JOIN verbrauchskosten v ON v.id=p.verbrauchskosten_id ORDER BY v.id")) {
     for (int i=0;i<3;i++) {
      assertTrue(rows.next()); assertEquals(java.time.LocalDate.of(1,1,1), rows.getObject("gueltig_von", java.time.LocalDate.class));
      assertNull(rows.getObject("gueltig_bis")); assertEquals(rows.getBigDecimal("alt"),rows.getBigDecimal("preis"));
     }
     assertFalse(rows.next());
    }
    try (var rows=sql.executeQuery("SELECT preis,kosten FROM buchung_verbrauchskosten WHERE id=9")) {
     assertTrue(rows.next()); assertEquals(new java.math.BigDecimal("0.25"),rows.getBigDecimal(1)); assertEquals(new java.math.BigDecimal("5.00"),rows.getBigDecimal(2));
    }
    c.setAutoCommit(true);
    assertThrows(SQLException.class,()->sql.execute("INSERT INTO verbrauchskosten_preis VALUES(1, DATE '2026-01-01',NULL,-1)"));
    assertThrows(SQLException.class,()->sql.execute("INSERT INTO verbrauchskosten_preis VALUES(1, DATE '2026-01-02',DATE '2026-01-01',1)"));
    assertThrows(SQLException.class,()->sql.execute("INSERT INTO verbrauchskosten_preis VALUES(99, DATE '2026-01-01',NULL,1)"));
    sql.execute("DELETE FROM verbrauchskosten WHERE id=1");
    try(var rows=sql.executeQuery("SELECT COUNT(*) FROM verbrauchskosten_preis WHERE verbrauchskosten_id=1")){assertTrue(rows.next());assertEquals(0,rows.getInt(1));}
   }
  }
 }
}
