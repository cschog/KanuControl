package com.kcserver.vermietung.service;

import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Runs only against an explicitly supplied disposable PostgreSQL, never the application datasource. */
@EnabledIfEnvironmentVariable(named="KC_DOCUMENT_TEST_JDBC",matches="jdbc:postgresql://127\\.0\\.0\\.1:55473/postgres")
class BuchungsdokumentMigrationTest {
 @Test void tenantMigrationBewahrtAltbestandUndSichertAtomareErsetzungUndIdempotenz()throws Exception{
  try(var c=DriverManager.getConnection(System.getenv("KC_DOCUMENT_TEST_JDBC"),"chris","")){
   String schema="doc_test_"+UUID.randomUUID().toString().replace("-","");
   try(var sql=c.createStatement()){
    sql.execute("CREATE SCHEMA "+schema);c.setSchema(schema);
    sql.execute("CREATE TABLE buchung (id BIGINT PRIMARY KEY, buchungsnummer VARCHAR(15))");
    sql.execute("INSERT INTO buchung VALUES (1,'2026-001')");
    sql.execute("CREATE TABLE vermietung_nachrichtenentwurf (id VARCHAR(36) PRIMARY KEY, tenant VARCHAR(255), buchung_id BIGINT, ziel VARCHAR(20), pdf BYTEA)");
    sql.execute("INSERT INTO vermietung_nachrichtenentwurf VALUES ('legacy','a',1,'RESERVIERT',decode('010203','hex'))");
    sql.execute("CREATE TABLE vermietung_vorlagenversand (id VARCHAR(36) PRIMARY KEY, pdf BYTEA)");
    var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));db.setDefaultSchemaName(schema);
    new Liquibase("db/changelog/changes/20261113-03-buchungsdokumente.xml",new ClassLoaderResourceAccessor(),db).update(new Contexts(),new LabelExpression());
    c.setAutoCommit(true);
    try(var rows=sql.executeQuery("SELECT unabhaengig,aktuelle_fassung,encode(pdf,'hex') FROM vermietung_nachrichtenentwurf WHERE id='legacy'")){assertTrue(rows.next());assertFalse(rows.getBoolean(1));assertFalse(rows.getBoolean(2));assertEquals("010203",rows.getString(3));}
    sql.execute("INSERT INTO vermietung_nachrichtenentwurf (id,tenant,buchung_id,ziel,pdf,unabhaengig,aktuelle_fassung) VALUES ('current','a',1,'RESERVIERT',decode('0405','hex'),true,true)");
    assertThrows(SQLException.class,()->sql.execute("INSERT INTO vermietung_nachrichtenentwurf (id,tenant,buchung_id,ziel,unabhaengig,aktuelle_fassung) VALUES ('conflict','a',1,'RESERVIERT',true,true)"));
    // Same booking number/type in a different tenant remains independent.
    sql.execute("INSERT INTO vermietung_nachrichtenentwurf (id,tenant,buchung_id,ziel,unabhaengig,aktuelle_fassung) VALUES ('tenant-b','b',1,'RESERVIERT',true,true)");
    c.setAutoCommit(false);
    sql.execute("UPDATE vermietung_nachrichtenentwurf SET aktuelle_fassung=false WHERE id='current'");
    try{sql.execute("INSERT INTO vermietung_nachrichtenentwurf (id) VALUES ('current')");fail("Expected replacement failure");}catch(SQLException expected){c.rollback();}
    c.setAutoCommit(true);
    try(var rows=sql.executeQuery("SELECT aktuelle_fassung,encode(pdf,'hex') FROM vermietung_nachrichtenentwurf WHERE id='current'")){assertTrue(rows.next());assertTrue(rows.getBoolean(1));assertEquals("0405",rows.getString(2));}
    sql.execute("INSERT INTO vermietung_vorlagenversand VALUES ('send-1',decode('0405','hex'),'attempt')");
    assertThrows(SQLException.class,()->sql.execute("INSERT INTO vermietung_vorlagenversand VALUES ('send-2',decode('0607','hex'),'attempt')"));
    try(var rows=sql.executeQuery("SELECT buchungsnummer FROM buchung WHERE id=1")){assertTrue(rows.next());assertEquals("2026-001",rows.getString(1));}
   }finally{c.setAutoCommit(true);try(var sql=c.createStatement()){sql.execute("DROP SCHEMA IF EXISTS "+schema+" CASCADE");}}
  }
 }
 @Test void echteJpaAbfragenTrennenEntwuerfeTenantUndAktuelleFassungen()throws Exception{
  String url=System.getenv("KC_DOCUMENT_TEST_JDBC");String schema="doc_jpa_"+UUID.randomUUID().toString().replace("-","");
  try(var c=DriverManager.getConnection(url,"chris","")){c.createStatement().execute("CREATE SCHEMA "+schema);}
  var factory=new org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean();
  try{
   factory.setDataSource(new org.springframework.jdbc.datasource.DriverManagerDataSource(url+"?currentSchema="+schema,"chris",""));
   factory.setJpaVendorAdapter(new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter());
   factory.setManagedTypes(org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes.of("com.kcserver.vermietung.entity.BuchungsnachrichtEntwurf","com.kcserver.vermietung.entity.VorlagenVersand"));
   factory.setJpaPropertyMap(java.util.Map.of("hibernate.hbm2ddl.auto","create"));factory.afterPropertiesSet();
   try(var em=factory.getObject().createEntityManager()){
    var repo=new org.springframework.data.jpa.repository.support.JpaRepositoryFactory(em).getRepository(com.kcserver.vermietung.repository.BuchungsnachrichtEntwurfRepository.class);
    var e=new com.kcserver.vermietung.entity.BuchungsnachrichtEntwurf();e.setBenutzer("alice");e.setTenant("a");e.setBuchungId(1L);e.setQuelle(com.kcserver.vermietung.enumtype.Buchungsstatus.ABGESCHLOSSEN);e.setZiel(com.kcserver.vermietung.enumtype.Buchungsstatus.RESERVIERT);e.setDatum(java.time.LocalDate.now());e.setAblauf(java.time.Instant.now().plusSeconds(3600));e.setDatenHash("hash");e.setRouting("null");e.setDateiname("test.pdf");e.setUnabhaengig(true);
    em.getTransaction().begin();em.persist(e);em.flush();
    assertTrue(repo.dokument(e.getId(),"alice","a",1L).isPresent());assertTrue(repo.dokument(e.getId(),"bob","a",1L).isEmpty());assertTrue(repo.dokument(e.getId(),"alice","b",1L).isEmpty());assertTrue(repo.dokument(e.getId(),"alice","a",2L).isEmpty());
    e.setAktuelleFassung(true);em.flush();assertTrue(repo.dokument(e.getId(),"bob","a",1L).isPresent());assertEquals(1,repo.findByBuchungIdAndTenantAndUnabhaengigTrueAndAktuelleFassungTrue(1L,"a").size());assertTrue(repo.findByBuchungIdAndTenantAndUnabhaengigTrueAndAktuelleFassungTrue(1L,"b").isEmpty());
    em.getTransaction().commit();
   }
  }finally{factory.destroy();try(var c=DriverManager.getConnection(url,"chris","")){c.createStatement().execute("DROP SCHEMA "+schema+" CASCADE");}}
 }

}
