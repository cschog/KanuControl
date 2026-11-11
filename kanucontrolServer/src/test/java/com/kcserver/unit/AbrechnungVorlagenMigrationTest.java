package com.kcserver.unit;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class AbrechnungVorlagenMigrationTest {
 @Test void alteBuchungenBehaltenAnzahlungsplanUndNeueFelderSindTenantlokal()throws Exception {
  try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")){
   for(var schema:new String[]{"CLUB_A","CLUB_B"}){
    c.createStatement().execute("CREATE SCHEMA "+schema);c.setSchema(schema);
    for(var table:new String[]{"buchung","mietobjekt","person"}){c.createStatement().execute("CREATE TABLE "+table+"(id BIGINT PRIMARY KEY)");c.createStatement().execute("INSERT INTO "+table+" VALUES(1)");}
    var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));db.setDefaultSchemaName(schema);
    var lb=new Liquibase("db/changelog/changes/20261111-01-abrechnung-vorlagen.xml",new ClassLoaderResourceAccessor(),db);lb.update(new Contexts(),new LabelExpression());
    try(var rs=c.createStatement().executeQuery("SELECT zahlungsvariante FROM buchung")){assertTrue(rs.next());assertEquals("ANZAHLUNG",rs.getString(1));}
    try(var rs=c.createStatement().executeQuery("SELECT kurzfristgrenze_tage FROM mietobjekt")){assertTrue(rs.next());assertEquals(42,rs.getInt(1));}
    c.createStatement().execute("UPDATE person SET organisation='Verein',kontoinhaber='Verein'");
    lb.rollback(1,new Contexts(),new LabelExpression());assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT organisation FROM person"));
   }
  }
 }
}
