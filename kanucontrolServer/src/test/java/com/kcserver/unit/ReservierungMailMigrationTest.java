package com.kcserver.unit;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class ReservierungMailMigrationTest {
 @Test void mailzustandIstMandantenbezogenUndRueckrollbar() throws Exception {
  try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")) {
   for(String schema:new String[]{"CLUB_A","CLUB_B"}) {
    c.createStatement().execute("CREATE SCHEMA "+schema);c.setSchema(schema);
    c.createStatement().execute("CREATE TABLE buchung(id BIGINT PRIMARY KEY)");
    c.createStatement().execute("INSERT INTO buchung VALUES(1)");
    var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));db.setDefaultSchemaName(schema);
    var lb=new Liquibase("db/changelog/changes/20261108-02-reservierung-mail.xml",new ClassLoaderResourceAccessor(),db);
    lb.update(new Contexts(),new LabelExpression());
    try(var rs=c.createStatement().executeQuery("SELECT reservierung_mail_gesendet_am,reservierung_mail_fehler FROM buchung")) {
     assertTrue(rs.next());assertNull(rs.getObject(1));assertNull(rs.getString(2));
    }
    c.createStatement().execute("UPDATE buchung SET reservierung_mail_fehler='SMTP fehlgeschlagen'");
    c.createStatement().execute("UPDATE buchung SET reservierung_mail_fehler=NULL,reservierung_mail_gesendet_am=CURRENT_TIMESTAMP");
    if(schema.equals("CLUB_B")) {
     lb.rollback(1,new Contexts(),new LabelExpression());
     assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT reservierung_mail_gesendet_am FROM buchung"));
     try(var rs=c.createStatement().executeQuery("SELECT reservierung_mail_gesendet_am FROM CLUB_A.buchung")) {assertTrue(rs.next());assertNotNull(rs.getObject(1));}
    }
   }
  }
 }
}
