package com.kcserver.unit;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class SperrzeitMigrationTest {
    private Liquibase migrate(Connection c,String schema) throws Exception {
        c.createStatement().execute("CREATE SCHEMA " + schema); c.setSchema(schema);
        c.createStatement().execute("CREATE TABLE mietobjekt (id BIGINT PRIMARY KEY)");
        c.createStatement().execute("INSERT INTO mietobjekt VALUES (7)");
        var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));db.setDefaultSchemaName(schema);
        var lb=new Liquibase("db/changelog/changes/20261102-09-mietobjekt-sperrzeiten.xml",new ClassLoaderResourceAccessor(),db);
        lb.update(new Contexts(),new LabelExpression()); return lb;
    }
    @Test void mandantenTrennungReferenzenUndRollback() throws Exception {
        try (Connection c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")) {
            var lb=migrate(c,"CLUB_A");
            String insert="INSERT INTO mietobjekt_sperrzeit (mietobjekt_id,von,bis,ganztaegig,grund,created_at) VALUES (%s,'2027-05-21 00:00:00','2027-05-22 00:00:00',true,'Wartung',CURRENT_TIMESTAMP)";
            c.createStatement().execute(insert.formatted(7));
            assertThrows(SQLException.class,()->c.createStatement().execute(insert.formatted(999)));
            migrate(c,"CLUB_B");
            try(var rs=c.createStatement().executeQuery("SELECT count(*) FROM mietobjekt_sperrzeit")){assertTrue(rs.next());assertEquals(0,rs.getInt(1));}
            c.createStatement().execute("DELETE FROM CLUB_A.mietobjekt WHERE id=7");
            try(var rs=c.createStatement().executeQuery("SELECT count(*) FROM CLUB_A.mietobjekt_sperrzeit")){assertTrue(rs.next());assertEquals(0,rs.getInt(1));}
            c.setSchema("CLUB_A");lb.rollback(1,new Contexts(),new LabelExpression());
            assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT * FROM CLUB_A.mietobjekt_sperrzeit"));
        }
    }
}
