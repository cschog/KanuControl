package com.kcserver.unit;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class HomepageAnbindungMigrationTest {
    private Liquibase migrate(Connection c, String schema) throws Exception {
        c.createStatement().execute("CREATE SCHEMA " + schema);
        c.setSchema(schema);
        try (Statement sql = c.createStatement()) {
            sql.execute("CREATE TABLE mietobjekt (id BIGINT PRIMARY KEY)");
            sql.execute("CREATE TABLE mietbereich (id BIGINT PRIMARY KEY, mietobjekt_id BIGINT)");
            sql.execute("INSERT INTO mietobjekt VALUES (7)");
            sql.execute("INSERT INTO mietbereich VALUES (10, 7)");
        }
        var database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(c));
        database.setDefaultSchemaName(schema);
        var liquibase = new Liquibase("db/changelog/changes/20261102-06-homepage-anbindung.xml",
                new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
        return liquibase;
    }
    private String text(Connection c, String query) throws SQLException {
        try (var sql = c.createStatement(); var rows = sql.executeQuery(query)) {
            assertTrue(rows.next()); return rows.getString(1);
        }
    }
    @Test void migrationTrenntVereinsschemataUndSichertReferenzen() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL")) {
            migrate(c, "CLUB_A");
            c.createStatement().execute("INSERT INTO homepage_anbindung (mietobjekt_id, bezeichnung, created_at) VALUES (7, 'Verein A', CURRENT_TIMESTAMP)");
            c.createStatement().execute("INSERT INTO homepage_anbindung_mietbereich VALUES (7, 10)");
            assertThrows(SQLException.class, () -> c.createStatement().execute("INSERT INTO homepage_anbindung_mietbereich VALUES (7, 999)"));
            migrate(c, "CLUB_B");
            c.createStatement().execute("INSERT INTO homepage_anbindung (mietobjekt_id, bezeichnung, created_at) VALUES (7, 'Verein B', CURRENT_TIMESTAMP)");
            assertEquals("Verein A", text(c, "SELECT bezeichnung FROM CLUB_A.homepage_anbindung WHERE mietobjekt_id = 7"));
            assertEquals("Verein B", text(c, "SELECT bezeichnung FROM CLUB_B.homepage_anbindung WHERE mietobjekt_id = 7"));
            c.createStatement().execute("DELETE FROM CLUB_A.mietobjekt WHERE id = 7");
            assertEquals("0", text(c, "SELECT COUNT(*) FROM CLUB_A.homepage_anbindung"));
            assertEquals("0", text(c, "SELECT COUNT(*) FROM CLUB_A.homepage_anbindung_mietbereich"));
            assertEquals("1", text(c, "SELECT COUNT(*) FROM CLUB_B.homepage_anbindung"));
        }
    }
    @Test void integrationsMigrationErgaenztFreischaltungUndEindeutigeVorgaenge() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL")) {
            migrate(c, "CLUB_D");
            c.createStatement().execute("CREATE TABLE buchung (id BIGINT PRIMARY KEY)");
            c.createStatement().execute("INSERT INTO homepage_anbindung (mietobjekt_id,bezeichnung,created_at) VALUES (7,'Homepage',CURRENT_TIMESTAMP)");
            var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            database.setDefaultSchemaName("CLUB_D");
            var lb = new Liquibase("db/changelog/changes/20261102-07-homepage-integration.xml", new ClassLoaderResourceAccessor(), database);
            lb.update(new Contexts(), new LabelExpression());
            assertEquals("FALSE", text(c, "SELECT aktiv FROM homepage_anbindung").toUpperCase());
            String insert = "INSERT INTO homepage_anfrage (mietobjekt_id,anfrage_id,payload_hash,antwort_json,hinweis_stand,created_at) VALUES (7,'test-id','hash','{}','stand',CURRENT_TIMESTAMP)";
            c.createStatement().execute(insert);
            assertThrows(SQLException.class, () -> c.createStatement().execute(insert));
            lb.rollback(1, new Contexts(), new LabelExpression());
            assertThrows(SQLException.class, () -> c.createStatement().executeQuery("SELECT * FROM homepage_anfrage"));
            assertEquals("Homepage", text(c, "SELECT bezeichnung FROM homepage_anbindung"));
        }
    }
    @Test void statusmailsBleibenNachBuchungsloeschungErhaltenUndSindRuecknehmbar() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL")) {
            migrate(c,"CLUB_STATUS");
            c.createStatement().execute("CREATE TABLE buchung (id BIGINT PRIMARY KEY)");
            c.createStatement().execute("INSERT INTO buchung VALUES (1)");
            var database=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            database.setDefaultSchemaName("CLUB_STATUS");
            var lb=new Liquibase("db/changelog/changes/20261102-08-homepage-status-mails.xml",new ClassLoaderResourceAccessor(),database);
            lb.update(new Contexts(),new LabelExpression());
            c.createStatement().execute("INSERT INTO homepage_status_mail (id,mietobjekt_id,buchung_id,buchungsnummer,status,empfaenger,betreff,text,created_at) VALUES ('test',7,1,'2027-001','RESERVIERT','gast@example.org','Test','Text',CURRENT_TIMESTAMP)");
            c.createStatement().execute("DELETE FROM buchung WHERE id=1");
            assertEquals("1",text(c,"SELECT COUNT(*) FROM homepage_status_mail WHERE buchung_id IS NULL"));
            lb.rollback(1,new Contexts(),new LabelExpression());
            assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT * FROM homepage_status_mail"));
            assertEquals("1",text(c,"SELECT COUNT(*) FROM mietobjekt"));
        }
    }
    @Test void migrationKannZurueckgerolltWerden() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL")) {
            Liquibase liquibase = migrate(c, "CLUB_C");
            liquibase.rollback(1, new Contexts(), new LabelExpression());
            assertThrows(SQLException.class, () -> c.createStatement().executeQuery("SELECT * FROM homepage_anbindung"));
            assertEquals("1", text(c, "SELECT COUNT(*) FROM mietobjekt"));
        }
    }
}
