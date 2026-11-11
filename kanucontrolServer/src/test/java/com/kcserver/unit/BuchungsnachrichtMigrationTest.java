package com.kcserver.unit;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class BuchungsnachrichtMigrationTest {
 @Test void migrationErhaeltAlteBuchungenUndSpeichertMehrseitigeAnhangbytesTenantlokal()throws Exception{
  try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")){
   for(var schema:new String[]{"CLUB_A","CLUB_B"}){
    c.createStatement().execute("CREATE SCHEMA "+schema);c.setSchema(schema);c.createStatement().execute("CREATE TABLE buchung(id BIGINT PRIMARY KEY)");c.createStatement().execute("INSERT INTO buchung VALUES(1)");
    var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));db.setDefaultSchemaName(schema);var lb=new Liquibase("db/changelog/changes/20261111-02-buchungsnachrichten.xml",new ClassLoaderResourceAccessor(),db);lb.update(new Contexts(),new LabelExpression());
    try(var rs=c.createStatement().executeQuery("SELECT storno_datum,storno_einbehalt FROM buchung")){assertTrue(rs.next());assertNull(rs.getDate(1));assertEquals(0,rs.getBigDecimal(2).signum());}
    try(var st=c.prepareStatement("INSERT INTO vermietung_nachrichtenentwurf(id,version,benutzer,tenant,buchung_id,quelle,ziel,datum,ablauf,daten_hash,betreff,mailtext,routing,pdf,dateiname,ohne_mail,ausgefuehrt,ablesungen,korrekturgrund) VALUES('a',0,'alice',?,1,'ANFRAGE','BESTAETIGT',CURRENT_DATE,CURRENT_TIMESTAMP,'hash','Betreff','Text','{}',?,'extern.pdf',false,false,'[]','')")){st.setString(1,schema);st.setBytes(2,new byte[]{1,2,3});st.executeUpdate();}
    try(var rs=c.createStatement().executeQuery("SELECT pdf FROM vermietung_nachrichtenentwurf")){assertTrue(rs.next());assertArrayEquals(new byte[]{1,2,3},rs.getBytes(1));}
    c.commit();c.setAutoCommit(false);c.createStatement().executeUpdate("UPDATE vermietung_nachrichtenentwurf SET ausgefuehrt=true");c.rollback();try(var rs=c.createStatement().executeQuery("SELECT ausgefuehrt FROM vermietung_nachrichtenentwurf")){assertTrue(rs.next());assertFalse(rs.getBoolean(1));}c.setAutoCommit(true);
    c.createStatement().executeUpdate("DELETE FROM buchung WHERE id=1");try(var rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM vermietung_nachrichtenentwurf")){assertTrue(rs.next());assertEquals(0,rs.getInt(1));}
    lb.rollback(1,new Contexts(),new LabelExpression());assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT * FROM vermietung_nachrichtenentwurf"));assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT storno_datum FROM buchung"));
   }
  }
 }
}
