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
class JahresabrechnungMigrationPostgresTest {
 @Test void trenntJahreUndSchlussrechnungenOhneAltdatenverlust()throws Exception {
  String schema="jahresabschluss_"+UUID.randomUUID().toString().replace("-","");
  try(var c=DriverManager.getConnection(System.getenv("KC_WORKFLOW_TEST_PG"))){
   try{
    var s=c.createStatement();s.execute("CREATE SCHEMA "+schema);s.execute("SET search_path TO "+schema);s.execute("CREATE TABLE buchung(id BIGINT PRIMARY KEY)");s.execute("INSERT INTO buchung VALUES(1)");
    var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));db.setDefaultSchemaName(schema);db.setLiquibaseSchemaName(schema);
    new Liquibase("db/changelog/changes/20261107-04-vermietung-abrechnung.xml",new ClassLoaderResourceAccessor(),db).update(new Contexts());
    s.execute("INSERT INTO vermietung_abrechnung(buchung_id,revision,erstellt,korrekturgrund,pdf) VALUES(1,1,now(),'alt',decode('aabb','hex'))");
    var lb=new Liquibase("db/changelog/changes/20261119-02-jahresabrechnung.xml",new ClassLoaderResourceAccessor(),db);lb.update(new Contexts());lb.update(new Contexts());c.setAutoCommit(true);
    s.execute("INSERT INTO vermietung_abrechnung(buchung_id,revision,erstellt,korrekturgrund,pdf,abrechnungs_jahr) VALUES(1,1,now(),'',decode('11','hex'),2025),(1,1,now(),'',decode('22','hex'),2026)");
    assertThrows(SQLException.class,()->s.execute("INSERT INTO vermietung_abrechnung(buchung_id,revision,erstellt,korrekturgrund,pdf,abrechnungs_jahr) VALUES(1,2,now(),'',decode('33','hex'),2026)"));
    assertThrows(SQLException.class,()->s.execute("INSERT INTO vermietung_abrechnung(buchung_id,revision,erstellt,korrekturgrund,pdf) VALUES(1,1,now(),'',decode('33','hex'))"));
    s.execute("UPDATE vermietung_abrechnung SET revision=2,pdf=decode('44','hex') WHERE abrechnungs_jahr=2026");
    try(var rs=s.executeQuery("SELECT encode(pdf,'hex') FROM vermietung_abrechnung WHERE abrechnungs_jahr IS NULL")){assertTrue(rs.next());assertEquals("aabb",rs.getString(1));}
    s.execute("INSERT INTO vermietung_abrechnung(buchung_id,revision,erstellt,korrekturgrund,pdf) VALUES(1,2,now(),'',decode('55','hex'))");
    s.execute("UPDATE vermietung_abrechnung SET pdf=decode('','hex') WHERE buchung_id=1 AND revision!=2 AND abrechnungs_jahr IS NULL");
    try(var rs=s.executeQuery("SELECT count(*) FROM vermietung_abrechnung WHERE abrechnungs_jahr IS NOT NULL AND octet_length(pdf)>0")){rs.next();assertEquals(2,rs.getInt(1));}
   }finally{c.createStatement().execute("DROP SCHEMA IF EXISTS "+schema+" CASCADE");}
  }
 }
}
