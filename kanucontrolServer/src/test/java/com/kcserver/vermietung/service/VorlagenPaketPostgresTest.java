package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.core.tenancy.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.*;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;
import static com.kcserver.vermietung.service.VorlagenPaketService.*;

/** Opt-in local disposable PostgreSQL only. No application profile or production datasource. */
@EnabledIfEnvironmentVariable(named="KC_PAKET_TEST_PG",matches="jdbc:postgresql://127\\.0\\.0\\.1:55439/postgres\\?user=kc_paket_test")
class VorlagenPaketPostgresTest {
    @Configuration @org.springframework.data.jpa.repository.config.EnableJpaAuditing @EnableTransactionManagement @EnableJpaRepositories(basePackageClasses=MietobjektVorlageRepository.class)
    static class Config {
        @Bean DataSource dataSource(org.springframework.core.env.Environment env){return new DriverManagerDataSource(env.getRequiredProperty("test.url"));}
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds){var f=new LocalContainerEntityManagerFactoryBean();f.setDataSource(ds);f.setPackagesToScan("com.kcserver");f.setJpaVendorAdapter(new HibernateJpaVendorAdapter());f.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","create","hibernate.show_sql","false","hibernate.hbm2ddl.create_namespaces","true"));return f;}
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory emf){return new JpaTransactionManager(emf);}
        @Bean @Primary EntityManager entityManager(EntityManagerFactory emf){return SharedEntityManagerCreator.createSharedEntityManager(emf);}
        @Bean ObjectMapper mapper(){return new ObjectMapper();}
        @Bean VorlagenService service(MandantenBriefpapierRepository b,MietobjektVorlageRepository v,MietobjektRepository o,ObjectMapper m){return new VorlagenService(b,v,o,m);}
        @Bean VorlagenPaketService pakete(MandantenBriefpapierRepository b,MietobjektVorlageRepository v,MietobjektRepository o,VorlagenImportRepository i,VorlagenService s,EntityManager em){return new VorlagenPaketService(b,v,o,i,s,em);}
    }
    AnnotationConfigApplicationContext a,b;String sa,sb,url;
    @BeforeEach void setup()throws Exception{url=System.getenv("KC_PAKET_TEST_PG");sa="paket_a_"+UUID.randomUUID().toString().replace("-","");sb="paket_b_"+UUID.randomUUID().toString().replace("-","");try(var c=DriverManager.getConnection(url)){c.createStatement().execute("CREATE SCHEMA "+sa);c.createStatement().execute("CREATE SCHEMA "+sb);}a=context(sa);b=context(sb);auth("erika");TenantContext.setCurrentTenant(sa);}
    AnnotationConfigApplicationContext context(String schema){var c=new AnnotationConfigApplicationContext();c.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test",Map.of("test.url",url+"&currentSchema="+schema)));c.register(Config.class);c.refresh();return c;}
    static void auth(String name){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(name,"",List.of()));}
    @AfterEach void close()throws Exception{if(a!=null)a.close();if(b!=null)b.close();try(var c=DriverManager.getConnection(url)){c.createStatement().execute("DROP SCHEMA "+sa+" CASCADE");c.createStatement().execute("DROP SCHEMA "+sb+" CASCADE");}TenantContext.clear();SecurityContextHolder.clearContext();}
    VorlagenPaketService svc(AnnotationConfigApplicationContext c){return c.getBean(VorlagenPaketService.class);}
    Long objekt(AnnotationConfigApplicationContext c,String name){var o=new Mietobjekt();o.setBezeichnung(name);return c.getBean(MietobjektRepository.class).saveAndFlush(o).getId();}
    Plan plan(Long id,boolean paper){return new Plan(paper,List.of(new Zuordnung("objekt-001",id,false)),List.of(new Auswahl("objekt-001",Vorgang.RESERVIERUNG,Aktion.NEU)),List.of());}
    @Test void roundtripZweiTenantsEntwurfsbindungVorschauUndIdempotenz()throws Exception{
        Long source=objekt(a,"Bootshaus");a.getBean(VorlagenService.class).speichern(source,Vorgang.RESERVIERUNG,VorlagenPaketTest.vorlageMitVersion(null));
        var paper=new MandantenBriefpapier();paper.setPdf(getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes());paper.setGruss("Viele Grüße\nErika");a.getBean(MandantenBriefpapierRepository.class).saveAndFlush(paper);
        var exported=svc(a).exportieren();var draft=svc(a).hochladen(exported);
        TenantContext.setCurrentTenant(sb);assertEquals(410,assertThrows(ResponseStatusException.class,()->svc(b).lesen(draft.id())).getStatusCode().value());
        TenantContext.setCurrentTenant(sa);auth("andere");assertEquals(410,assertThrows(ResponseStatusException.class,()->svc(a).lesen(draft.id())).getStatusCode().value());auth("erika");
        TenantContext.setCurrentTenant(sb);objekt(b,"Dummy");Long target=objekt(b,"Bootshaus");assertNotEquals(source,target);
        var d=svc(b).hochladen(exported);assertEquals(0,b.getBean(MietobjektVorlageRepository.class).count());
        assertThrows(ResponseStatusException.class,()->svc(b).planen(d.id(),plan(999999L,true)));
        svc(b).planen(d.id(),plan(target,true));var probe=svc(b).vorschau(d.id());assertTrue(probe.status().stream().allMatch(Pruefstatus::erfolgreich));assertEquals(0,b.getBean(MietobjektVorlageRepository.class).count());
        svc(b).uebernehmen(d.id());var v=b.getBean(VorlagenService.class).vorlage(target,Vorgang.RESERVIERUNG);assertEquals(VorlagenPaketTest.vorlage().bloecke(),v.bloecke());assertEquals(VorlagenPaketTest.vorlage().mailtext(),v.mailtext());assertArrayEquals(paper.getPdf(),b.getBean(MandantenBriefpapierRepository.class).findById(1L).orElseThrow().getPdf());
        assertTrue(svc(b).uebernehmen(d.id()).uebernommen());
        var expired=svc(b).hochladen(exported);var ir=b.getBean(VorlagenImportRepository.class);var row=ir.findById(expired.id()).orElseThrow();row.setAblauf(java.time.Instant.now().minusSeconds(1));ir.saveAndFlush(row);assertEquals(410,assertThrows(ResponseStatusException.class,()->svc(b).uebernehmen(expired.id())).getStatusCode().value());
        var cancelled=svc(b).hochladen(exported);svc(b).abbrechen(cancelled.id());assertEquals(410,assertThrows(ResponseStatusException.class,()->svc(b).lesen(cancelled.id())).getStatusCode().value());
    }
    @Test void atomarKonflikteRollbackUndMigration()throws Exception{
        Long target=objekt(a,"Bootshaus");var s=svc(a);var d=s.hochladen(VorlagenPaketTest.paket(true));s.planen(d.id(),plan(target,true));var preview=s.vorschau(d.id());assertEquals(4,preview.vorlagen().size());assertTrue(preview.status().getFirst().erfolgreich());
        // Genuine database rollback after the service has flushed individual rows.
        var tx=new TransactionTemplate(a.getBean(PlatformTransactionManager.class));assertThrows(IllegalStateException.class,()->tx.execute(status->{s.uebernehmen(d.id());throw new IllegalStateException("Fehler nach Writes");}));
        assertEquals(0,a.getBean(MietobjektVorlageRepository.class).count());assertEquals(0,a.getBean(MandantenBriefpapierRepository.class).count());
        assertTrue(s.uebernehmen(d.id()).uebernommen());assertTrue(s.uebernehmen(d.id()).uebernommen());assertEquals(1,a.getBean(MietobjektVorlageRepository.class).count());
        var next=s.hochladen(VorlagenPaketTest.paket(false));var replace=new Plan(false,List.of(new Zuordnung("objekt-001",target,false)),List.of(new Auswahl("objekt-001",Vorgang.RESERVIERUNG,Aktion.ERSETZEN)),List.of());s.planen(next.id(),replace);s.vorschau(next.id());
        var repo=a.getBean(MandantenBriefpapierRepository.class);var paper=repo.findById(1L).orElseThrow();paper.setGruss("Geändert");repo.saveAndFlush(paper);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->s.uebernehmen(next.id())).getStatusCode().value());
        var kept=repo.findById(1L).orElseThrow();s.planen(next.id(),replace);s.vorschau(next.id());s.uebernehmen(next.id());var after=repo.findById(1L).orElseThrow();assertTrue(after.getVersion()>kept.getVersion());assertArrayEquals(kept.getPdf(),after.getPdf());assertEquals(kept.getGruss(),after.getGruss());
        try(var c=DriverManager.getConnection(url+"&currentSchema="+sb)){c.createStatement().execute("DROP TABLE vermietung_vorlagenimport");var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));db.setDefaultSchemaName(sb);var lb=new Liquibase("db/changelog/changes/20261110-01-vorlagenpakete.xml",new ClassLoaderResourceAccessor(),db);lb.update(new Contexts(),new LabelExpression());
        var migrated=new VorlagenImport();migrated.setId(UUID.randomUUID().toString());migrated.setBenutzer("migration-test");migrated.setTenant(sb);migrated.setAblauf(java.time.Instant.now().plusSeconds(60));migrated.setPaketHash("a".repeat(64));b.getBean(VorlagenImportRepository.class).saveAndFlush(migrated);assertTrue(b.getBean(VorlagenImportRepository.class).findById(migrated.getId()).isPresent());
        lb.rollback(1,new Contexts(),new LabelExpression());assertThrows(SQLException.class,()->c.createStatement().executeQuery("SELECT * FROM vermietung_vorlagenimport"));}
    }
    @Test void paralleleNeuanlagePlanwechselUndUngepruefteVerwaltung()throws Exception{
        Long target=objekt(a,"Bootshaus");var s=svc(a);var d=s.hochladen(VorlagenPaketTest.paket(false));s.planen(d.id(),plan(target,false));
        var noPaper=s.vorschau(d.id());assertFalse(noPaper.status().getFirst().erfolgreich());assertTrue(noPaper.vorlagen().isEmpty());assertEquals(409,assertThrows(ResponseStatusException.class,()->s.uebernehmen(d.id())).getStatusCode().value());
        var acknowledged=new Plan(false,plan(target,false).zuordnungen(),plan(target,false).auswahl(),List.of("objekt-001/RESERVIERUNG"));s.planen(d.id(),acknowledged);assertTrue(s.vorschau(d.id()).status().getFirst().ausdruecklichFreigegeben());
        // A different transaction and connection creates a previously absent key after preview.
        var competitor=java.util.concurrent.CompletableFuture.runAsync(()->{TenantContext.setCurrentTenant(sa);auth("zweite");try{a.getBean(VorlagenService.class).speichern(target,Vorgang.RESERVIERUNG,VorlagenPaketTest.vorlageMitVersion(null));}finally{TenantContext.clear();SecurityContextHolder.clearContext();}});competitor.get();
        assertEquals(409,assertThrows(ResponseStatusException.class,()->s.uebernehmen(d.id())).getStatusCode().value());assertEquals(0,a.getBean(MandantenBriefpapierRepository.class).count());
        var replace=new Plan(false,plan(target,false).zuordnungen(),List.of(new Auswahl("objekt-001",Vorgang.RESERVIERUNG,Aktion.ERSETZEN)),List.of("objekt-001/RESERVIERUNG"));s.planen(d.id(),replace);s.vorschau(d.id());s.planen(d.id(),replace);assertEquals(409,assertThrows(ResponseStatusException.class,()->s.uebernehmen(d.id())).getStatusCode().value());s.vorschau(d.id());s.uebernehmen(d.id());assertEquals(1,a.getBean(MietobjektVorlageRepository.class).count());assertEquals(0,a.getBean(VorlagenVersandRepository.class).count());
    }
    @Test void doppelteZieleUndBewusstesUeberspringen()throws Exception{
        Long target=objekt(a,"Gleicher Name");var m=VorlagenPaketTest.manifest(false);var manifest=new VorlagenPaket.Manifest(m.format(),1,m.exportiertAm(),m.quelle(),null,List.of(new VorlagenPaket.Objekt("objekt-001","Gleicher Name"),new VorlagenPaket.Objekt("objekt-002","Gleicher Name")),List.of(m.vorlagen().getFirst(),new VorlagenPaket.Eintrag("objekt-002",Vorgang.RESERVIERUNG,"vorlagen/objekt-002/RESERVIERUNG.md")));
        byte[] md=VorlagenMarkdown.schreiben(VorlagenPaketTest.vorlage());var bytes=VorlagenPaket.schreiben(manifest,Map.of("vorlagen/objekt-001/RESERVIERUNG.md",md,"vorlagen/objekt-002/RESERVIERUNG.md",md));var s=svc(a);var d=s.hochladen(bytes);
        var mapping=List.of(new Zuordnung("objekt-001",target,false),new Zuordnung("objekt-002",target,false));var choices=List.of(new Auswahl("objekt-001",Vorgang.RESERVIERUNG,Aktion.NEU),new Auswahl("objekt-002",Vorgang.RESERVIERUNG,Aktion.NEU));
        assertThrows(ResponseStatusException.class,()->s.planen(d.id(),new Plan(false,mapping,choices,List.of())));
        var skip=new Plan(false,List.of(mapping.getFirst(),new Zuordnung("objekt-002",null,true)),List.of(choices.getFirst(),new Auswahl("objekt-002",Vorgang.RESERVIERUNG,Aktion.UEBERSPRINGEN)),List.of("objekt-001/RESERVIERUNG"));var view=s.planen(d.id(),skip);assertEquals(1,view.uebersprungen());assertEquals(1,view.objekteUebersprungen());s.vorschau(d.id());s.uebernehmen(d.id());assertEquals(1,a.getBean(MietobjektVorlageRepository.class).count());
    }

}
