package com.kcserver.unit;

import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class BankverbindungMigrationTest {
    private Liquibase migrate(Connection c, String schema) throws Exception {
        c.createStatement().execute("CREATE SCHEMA " + schema); c.setSchema(schema);
        c.createStatement().execute("CREATE TABLE mietobjekt (id BIGINT PRIMARY KEY)");
        c.createStatement().execute("CREATE TABLE buchung (id BIGINT PRIMARY KEY)");
        c.createStatement().execute("INSERT INTO mietobjekt VALUES (7)");
        c.createStatement().execute("INSERT INTO buchung VALUES (1)");
        var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
        db.setDefaultSchemaName(schema);
        var lb=new Liquibase("db/changelog/changes/20261108-01-mietobjekt-bankverbindungen.xml",new ClassLoaderResourceAccessor(),db);
        lb.update(new Contexts(),new LabelExpression()); return lb;
    }
    @Test void bestandStandardBeideKontenMandantentrennungUndRollback() throws Exception {
        try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")) {
            var lb=migrate(c,"CLUB_A");
            try(var rs=c.createStatement().executeQuery("SELECT bankverbindung_auswahl FROM buchung")) { assertTrue(rs.next()); assertEquals("PRIMAER",rs.getString(1)); }
            for(String slot : new String[]{"primaer","alternativ"}) {
                c.createStatement().execute("UPDATE mietobjekt SET bank_"+slot+"_kontoinhaber='Verein', bank_"+slot+"_name='Bank', bank_"+slot+"_iban='DE89370400440532013000', bank_"+slot+"_bic='COBADEFFXXX'");
                try(var rs=c.createStatement().executeQuery("SELECT bank_"+slot+"_kontoinhaber,bank_"+slot+"_name,bank_"+slot+"_iban,bank_"+slot+"_bic FROM mietobjekt")) {
                    assertTrue(rs.next());assertEquals("Verein",rs.getString(1));assertEquals("Bank",rs.getString(2));assertEquals("DE89370400440532013000",rs.getString(3));assertEquals("COBADEFFXXX",rs.getString(4));
                }
            }
            c.createStatement().execute("UPDATE buchung SET bankverbindung_auswahl='ALTERNATIV'");
            migrate(c,"CLUB_B");
            try(var rs=c.createStatement().executeQuery("SELECT bank_alternativ_iban FROM mietobjekt")) {assertTrue(rs.next());assertNull(rs.getString(1));}
            try(var rs=c.createStatement().executeQuery("SELECT bankverbindung_auswahl FROM CLUB_A.buchung")) {assertTrue(rs.next());assertEquals("ALTERNATIV",rs.getString(1));}
            c.setSchema("CLUB_A");lb.rollback(1,new Contexts(),new LabelExpression());
            assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT bankverbindung_auswahl FROM buchung"));
            try(var rs=c.createStatement().executeQuery("SELECT id FROM mietobjekt")) {assertTrue(rs.next());assertEquals(7,rs.getLong(1));}
        }
    }
}
