package com.kcserver.unit;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class VorlagensystemMigrationTest {
 @Test void briefpapierVorlagenArchivSindTenantLokalEindeutigUndRueckrollbar()throws Exception{
  try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")){
   for(String schema:new String[]{"CLUB_A","CLUB_B"}){
    c.createStatement().execute("CREATE SCHEMA "+schema);c.setSchema(schema);
    c.createStatement().execute("CREATE TABLE mietobjekt(id BIGINT PRIMARY KEY)");c.createStatement().execute("INSERT INTO mietobjekt VALUES(7)");
    var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));db.setDefaultSchemaName(schema);
    var lb=new Liquibase("db/changelog/changes/20261109-01-vorlagensystem.xml",new ClassLoaderResourceAccessor(),db);lb.update(new Contexts(),new LabelExpression());
    try(var insert=c.prepareStatement("INSERT INTO vermietung_briefpapier(id,version,pdf,gruss) VALUES(1,0,?,?)")){insert.setBytes(1,new byte[]{1,2,3});insert.setString(2,schema);insert.executeUpdate();}
    c.createStatement().execute("INSERT INTO vermietung_vorlage(version,mietobjekt_id,vorgang,inhalt) VALUES(0,7,'RESERVIERUNG','{}')");
    assertThrows(SQLException.class,()->c.createStatement().execute("INSERT INTO vermietung_vorlage(version,mietobjekt_id,vorgang,inhalt) VALUES(0,7,'RESERVIERUNG','{}')"));
    if(schema.equals("CLUB_B")){
     try(var rs=c.createStatement().executeQuery("SELECT gruss,pdf FROM CLUB_A.vermietung_briefpapier")){assertTrue(rs.next());assertEquals("CLUB_A",rs.getString(1));assertArrayEquals(new byte[]{1,2,3},rs.getBytes(2));}
     lb.rollback(1,new Contexts(),new LabelExpression());assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT * FROM vermietung_briefpapier"));
     try(var rs=c.createStatement().executeQuery("SELECT gruss FROM CLUB_A.vermietung_briefpapier")){assertTrue(rs.next());assertEquals("CLUB_A",rs.getString(1));}
    }
   }
  }
 }
}
