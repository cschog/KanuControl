package com.kcserver.unit;

import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class VeranstalterMigrationTest {
    @Test void bestehendeVereineUndZuordnungenBleibenInBeidenMandantenErhalten() throws Exception {
        try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")){
            for(String schema:new String[]{"CLUB_A","CLUB_B"}){
                c.createStatement().execute("CREATE SCHEMA "+schema);c.setSchema(schema);
                c.createStatement().execute("CREATE TABLE verein(id BIGINT PRIMARY KEY, name VARCHAR(100))");
                c.createStatement().execute("INSERT INTO verein VALUES(7,'Bestandsverein')");
                c.createStatement().execute("CREATE TABLE buchung(id BIGINT PRIMARY KEY, veranstalter_verein_id BIGINT REFERENCES verein(id))");
                c.createStatement().execute("INSERT INTO buchung VALUES(1,7),(2,NULL)");
                var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));db.setDefaultSchemaName(schema);
                var lb=new Liquibase("db/changelog/changes/20261111-03-veranstalter.xml",new ClassLoaderResourceAccessor(),db);
                lb.update(new Contexts(),new LabelExpression());
                try(var rs=c.createStatement().executeQuery("SELECT v.id,v.kjfp_verein FROM buchung b JOIN verein v ON b.veranstalter_verein_id=v.id")){
                    assertTrue(rs.next());assertEquals(7,rs.getLong(1));assertTrue(rs.getBoolean(2));assertFalse(rs.next());
                }
                c.createStatement().execute("INSERT INTO verein(id,name,kjfp_verein) VALUES(8,'Schule',FALSE)");
                try(var rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM verein WHERE kjfp_verein=TRUE")){rs.next();assertEquals(1,rs.getInt(1));}
                if(schema.equals("CLUB_B")){
                    c.createStatement().execute("UPDATE verein SET name='Nur B' WHERE id=7");
                    try(var rs=c.createStatement().executeQuery("SELECT name FROM CLUB_A.verein WHERE id=7")){rs.next();assertEquals("Bestandsverein",rs.getString(1));}
                    lb.rollback(1,new Contexts(),new LabelExpression());
                    assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT kjfp_verein FROM verein"));
                    try(var rs=c.createStatement().executeQuery("SELECT veranstalter_verein_id FROM buchung WHERE id=1")){rs.next();assertEquals(7,rs.getLong(1));}
                }
            }
        }
    }
}
