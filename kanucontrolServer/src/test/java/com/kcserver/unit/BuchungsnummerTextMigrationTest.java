package com.kcserver.unit;

import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class BuchungsnummerTextMigrationTest {
    private void altbestand(Connection c) throws SQLException {
        c.createStatement().execute("CREATE TABLE buchung(id BIGINT PRIMARY KEY,buchungsjahr INT NOT NULL,laufende_nummer INT NOT NULL,CONSTRAINT uk_buchung_jahr_laufende_nummer UNIQUE(buchungsjahr,laufende_nummer))");
        c.createStatement().execute("INSERT INTO buchung VALUES(1,2026,1),(2,2026,10),(3,2026,999),(4,2026,1000)");
    }
    private Liquibase migration(Connection c) throws Exception {
        var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
        return new Liquibase("db/changelog/changes/20261109-02-buchungsnummer-text.xml",new ClassLoaderResourceAccessor(),db);
    }
    private String nummer(Connection c,long id) throws SQLException {
        try(var r=c.createStatement().executeQuery("SELECT buchungsnummer FROM buchung WHERE id="+id)) { assertTrue(r.next());return r.getString(1); }
    }
    @Test void alteNummernBleibenErhaltenUndVariantenKoennenImGleichenMandantenKoexistieren() throws Exception {
        try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")) {
            for(String schema:new String[]{"CLUB_A","CLUB_B"}) {
                c.createStatement().execute("CREATE SCHEMA "+schema);c.setSchema(schema);altbestand(c);
                var lb=migration(c);lb.getDatabase().setDefaultSchemaName(schema);lb.update(new Contexts(),new LabelExpression());
                assertEquals("2026-001",nummer(c,1));assertEquals("2026-010",nummer(c,2));
                assertEquals("2026-999",nummer(c,3));assertEquals("2026-1000",nummer(c,4));
                c.createStatement().execute("INSERT INTO buchung VALUES(5,2026,1,'2026-01')");
                c.createStatement().execute("INSERT INTO buchung VALUES(6,2026,1,'2026-1')");
                assertEquals("2026-01",nummer(c,5));assertEquals("2026-1",nummer(c,6));
                assertThrows(SQLException.class,()->c.createStatement().execute("INSERT INTO buchung VALUES(7,2026,1,'2026-01')"));
                assertThrows(SQLException.class,()->c.createStatement().execute("INSERT INTO buchung VALUES(8,2026,2,NULL)"));
            }
        }
    }
    @Test void rollbackOhneVariantenIstMoeglich() throws Exception {
        try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")) {
            altbestand(c);var lb=migration(c);lb.update(new Contexts(),new LabelExpression());
            lb.rollback(1,new Contexts(),new LabelExpression());
            assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT buchungsnummer FROM buchung"));
            assertThrows(SQLException.class,()->c.createStatement().execute("INSERT INTO buchung VALUES(5,2026,1)"));
        }
    }
    @Test void rollbackVerhindertVerlustUnterscheidbarerTextnummern() throws Exception {
        try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")) {
            altbestand(c);var lb=migration(c);lb.update(new Contexts(),new LabelExpression());
            c.createStatement().execute("INSERT INTO buchung VALUES(5,2026,1,'2026-01')");
            c.commit();
            assertThrows(Exception.class,()->lb.rollback(1,new Contexts(),new LabelExpression()));
            assertEquals("2026-001",nummer(c,1));assertEquals("2026-01",nummer(c,5));
        }
    }
    @Test void rollbackVerhindertAuchDenVerlustEinerEinzigenGeaendertenSchreibweise() throws Exception {
        try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")) {
            altbestand(c);var lb=migration(c);lb.update(new Contexts(),new LabelExpression());
            c.createStatement().execute("UPDATE buchung SET buchungsnummer='2026-01' WHERE id=1");c.commit();
            assertThrows(Exception.class,()->lb.rollback(1,new Contexts(),new LabelExpression()));
            assertEquals("2026-01",nummer(c,1));
        }
    }

}
