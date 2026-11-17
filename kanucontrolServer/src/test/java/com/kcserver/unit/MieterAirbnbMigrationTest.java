package com.kcserver.unit;

import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MieterAirbnbMigrationTest {
 @Test void uebernimmtNurReineAirbnbMieterUndErhaeltPersonenBeimRollback()throws Exception{
  try(var c=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL")){
   var sql=c.createStatement();
   sql.execute("CREATE TABLE mieter(id BIGINT PRIMARY KEY,person_id BIGINT)");
   sql.execute("CREATE TABLE buchung(mieter_id BIGINT,buchungsquelle VARCHAR(20))");
   sql.execute("INSERT INTO mieter VALUES(1,101),(2,102),(3,103),(4,104)");
   sql.execute("INSERT INTO buchung VALUES(101,'AIRBNB'),(102,'DIREKT'),(103,'AIRBNB'),(103,'DIREKT')");
   var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
   var lb=new Liquibase("db/changelog/changes/20261116-02-mieter-airbnb.xml",new ClassLoaderResourceAccessor(),db);
   lb.update(new Contexts(),new LabelExpression());
   try(var rows=sql.executeQuery("SELECT id,airbnb FROM mieter ORDER BY id")){
    assertTrue(rows.next());assertTrue(rows.getBoolean("airbnb"));
    for(int i=0;i<3;i++){assertTrue(rows.next());assertFalse(rows.getBoolean("airbnb"));}
   }
   sql.execute("INSERT INTO mieter(id,person_id) VALUES(5,105)");
   try(var rows=sql.executeQuery("SELECT airbnb FROM mieter WHERE id=5")){assertTrue(rows.next());assertFalse(rows.getBoolean(1));}
   c.commit();
   lb.rollback(1,new Contexts(),new LabelExpression());
   try(var rows=sql.executeQuery("SELECT COUNT(*) FROM mieter")){assertTrue(rows.next());assertEquals(5,rows.getInt(1));}
  }
 }
}
