package com.kcserver.vermietung.service;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
@EnabledIfEnvironmentVariable(named="KC_WORKFLOW_TEST_PG",matches="jdbc:postgresql://127\\.0\\.0\\.1:55448/postgres\\?user=kc_workflow_test")
class WorkflowMigrationPostgresTest {
 @Test void migrationZweiMandantenIstIsoliertIdempotentUndErhaeltAltdaten()throws Exception {
  String url=System.getenv("KC_WORKFLOW_TEST_PG"),a="workflow_a_"+UUID.randomUUID().toString().replace("-",""),b="workflow_b_"+UUID.randomUUID().toString().replace("-","");
  try(var c=DriverManager.getConnection(url)){
   try{
    for(String schema:List.of(a,b)){var s=c.createStatement();s.execute("CREATE SCHEMA "+schema);s.execute("SET search_path TO "+schema);s.execute("CREATE TABLE mieter(id BIGINT PRIMARY KEY)");s.execute("CREATE TABLE verein(id BIGINT PRIMARY KEY)");s.execute("CREATE TABLE buchung(id BIGINT PRIMARY KEY,status VARCHAR(20))");s.execute("INSERT INTO buchung VALUES(1,'ANFRAGE')");s.execute("CREATE TABLE vermietung_vorlage(id BIGSERIAL PRIMARY KEY,mietobjekt_id BIGINT,vorgang VARCHAR(30),inhalt TEXT,version BIGINT)");s.execute("INSERT INTO vermietung_vorlage(mietobjekt_id,vorgang,inhalt,version) VALUES(7,'RESERVIERUNG','Reservierungsbestätigung mit Zahlungsmodalitäten',2),(7,'BESTAETIGUNG','alte Bestätigung',1)");}
    c.createStatement().execute("SET search_path TO "+a);
    var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));db.setDefaultSchemaName(a);db.setLiquibaseSchemaName(a);
    var lb=new Liquibase("db/changelog/changes/20261119-01-vermietung-workflow.xml",new ClassLoaderResourceAccessor(),db);lb.update(new Contexts());lb.update(new Contexts());
    try(var rs=c.createStatement().executeQuery("SELECT inhalt,version FROM "+a+".vermietung_vorlage WHERE vorgang='BESTAETIGUNG'")){assertTrue(rs.next());assertEquals("Buchungsbestätigung mit Zahlungsmodalitäten",rs.getString(1));assertEquals(2,rs.getLong(2));}
    try(var rs=c.createStatement().executeQuery("SELECT inhalt FROM "+b+".vermietung_vorlage WHERE vorgang='BESTAETIGUNG'")){rs.next();assertEquals("alte Bestätigung",rs.getString(1));}
    try(var rs=c.createStatement().executeQuery("SELECT status FROM "+a+".buchung")){rs.next();assertEquals("ANFRAGE",rs.getString(1));}
    assertTrue(c.getMetaData().getColumns(null,a,"mieter","zusammengefuehrt_in").next());assertFalse(c.getMetaData().getColumns(null,b,"mieter","zusammengefuehrt_in").next());
   }finally{c.createStatement().execute("DROP SCHEMA IF EXISTS "+a+" CASCADE");c.createStatement().execute("DROP SCHEMA IF EXISTS "+b+" CASCADE");}
  }
 }
}
