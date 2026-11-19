package com.kcserver.vermietung.service;
import com.kcserver.core.entity.*;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.core.repository.VereinRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.*;
import java.math.BigDecimal;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@EnabledIfEnvironmentVariable(named="KC_WORKFLOW_TEST_PG",matches="jdbc:postgresql://127\\.0\\.0\\.1:55448/postgres\\?user=kc_workflow_test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkflowTransaktionPostgresTest {
 static String schema,url;static VermietungAenderungsprotokoll rawLog;
 @Configuration @EnableTransactionManagement @org.springframework.data.jpa.repository.config.EnableJpaAuditing
 @EnableJpaRepositories(basePackageClasses={BuchungRepository.class,PersonRepository.class,VereinRepository.class})
 static class Config {
  @Bean DataSource ds(){return new DriverManagerDataSource(url+"&currentSchema="+schema);}
  @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds){var f=new LocalContainerEntityManagerFactoryBean();f.setDataSource(ds);f.setPackagesToScan("com.kcserver");f.setJpaVendorAdapter(new HibernateJpaVendorAdapter());f.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","create","hibernate.hbm2ddl.create_namespaces","true","hibernate.show_sql","false"));return f;}
  @Bean PlatformTransactionManager transactionManager(EntityManagerFactory f){return new JpaTransactionManager(f);}
  @Bean ObjectMapper mapper(){return new ObjectMapper().findAndRegisterModules();}
  @Bean jakarta.validation.Validator validator(){return Validation.buildDefaultValidatorFactory().getValidator();}
  @Bean VermietungAenderungsprotokoll log(VermietungAenderungRepository r,ObjectMapper m){rawLog=spy(new VermietungAenderungsprotokoll(r,m));return rawLog;}
  @Bean VerbrauchsabrechnungService verbrauch(){return mock(VerbrauchsabrechnungService.class);}
  @Bean EntschaedigungPositionService entschaedigung(){return mock(EntschaedigungPositionService.class);}
  @Bean ReservierungsversandService reservierung(){return mock(ReservierungsversandService.class);}
  @Bean VorlagenArchivService archiv(){return mock(VorlagenArchivService.class);}
  @Bean BuchungLoeschService loeschen(){return mock(BuchungLoeschService.class);}
  @Bean HomepageStatusMailService mails(){return mock(HomepageStatusMailService.class);}
  @Bean VorlagenService vorlagen(){return mock(VorlagenService.class);}
  @Bean ReservierungsbestaetigungPdf pdf(){return mock(ReservierungsbestaetigungPdf.class);}
  @Bean MieterService mieter(MieterRepository m,PersonRepository p,BuchungRepository b){return new MieterService(m,p,b,mock(com.kcserver.core.mapper.PersonMapper.class));}
  @Bean VeranstalterService veranstalter(){return mock(VeranstalterService.class);}
  @Bean StammdatenMergeService merge(MieterRepository m,PersonRepository p,VereinRepository v,BuchungRepository b,MieterService ms,VeranstalterService vs,VermietungAenderungsprotokoll log,ObjectMapper mapper,jakarta.validation.Validator validator){return new StammdatenMergeService(m,p,v,b,ms,vs,log,mapper,validator);}
  @Bean AnfrageMigrationService altanfragen(BuchungRepository b,BuchungService s,PlatformTransactionManager t,VermietungAenderungsprotokoll log){return new AnfrageMigrationService(b,s,t,log);}
  @Bean BuchungFinanzenService finanzen(BuchungRepository b,BuchungFinanzbewegungRepository m,BuchungVerbrauchskostenRepository v,VerbrauchsabrechnungService va){return new BuchungFinanzenService(b,m,v,va);}
  @Bean BuchungService buchung(BuchungRepository b,MieterRepository m,BuchungsnummerCounterRepository n,PersonRepository p,VereinRepository v,MietobjektRepository o,MietbereichRepository a,MietpreisRepository preise,MietbereichRabattRepository rabatt,ZaehlerRepository z,ZaehlerstandRepository zs,VerbrauchsabrechnungService verbrauch,BuchungVerbrauchskostenRepository vk,BuchungFinanzbewegungRepository f,HomepageStatusMailService mail){return new BuchungService(b,m,n,p,v,o,a,preise,rabatt,z,zs,verbrauch,vk,f,mail, org.mockito.Mockito.mock(com.kcserver.vermietung.service.VermietungAenderungsprotokoll.class));}
 }
 AnnotationConfigApplicationContext ctx;TransactionTemplate tx;
 @BeforeAll void start()throws Exception{url=System.getenv("KC_WORKFLOW_TEST_PG");schema="workflow_tx_"+UUID.randomUUID().toString().replace("-","");try(var c=DriverManager.getConnection(url)){c.createStatement().execute("CREATE SCHEMA "+schema);}ctx=new AnnotationConfigApplicationContext(Config.class);tx=new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class));}
 @AfterAll void close()throws Exception{if(ctx!=null)ctx.close();try(var c=DriverManager.getConnection(url)){c.createStatement().execute("DROP SCHEMA IF EXISTS "+schema+" CASCADE");}}
 @BeforeEach void auth(){TenantContext.setCurrentTenant(schema);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test","",List.of()));}
 @AfterEach void clear(){TenantContext.clear();SecurityContextHolder.clearContext();doCallRealMethod().when(rawLog).schreiben(anyString(),any());}
 <T>T bean(Class<T> type){return ctx.getBean(type);}
 long mieter(){return tx.execute(t->{var p=new Person();p.setName("Muster");p.setVorname("Erika");p.setKjfpSichtbar(false);bean(PersonRepository.class).saveAndFlush(p);var m=new Mieter();m.setPerson(p);return bean(MieterRepository.class).saveAndFlush(m).getId();});}
 List<Long> buchungen(long mieter,int anzahl){return tx.execute(t->{var person=bean(MieterRepository.class).findById(mieter).orElseThrow().getPerson();var o=new Mietobjekt();o.setBezeichnung("Testobjekt");bean(MietobjektRepository.class).saveAndFlush(o);var area=new Mietbereich();area.setMietobjekt(o);area.setBezeichnung("EG");area.setBestand(1);bean(MietbereichRepository.class).saveAndFlush(area);var ids=new ArrayList<Long>();for(int i=0;i<anzahl;i++){var b=new Buchung();b.setMieter(person);b.setMietobjekt(o);b.setBuchungsnummer(UUID.randomUUID().toString().substring(0,12));b.setBuchungsjahr(2026);b.setLaufendeNummer(i+1);b.setBuchungsquelle(Buchungsquelle.DIREKT);b.setStatus(Buchungsstatus.STORNIERT);b.setAnreise(LocalDateTime.now().plusDays(20));b.setAbreise(b.getAnreise().plusDays(2));var pos=new BuchungMietbereich();pos.setBuchung(b);pos.setMietbereich(area);pos.setVereinbarterPreis(BigDecimal.TEN);pos.setBerechnungsart(Berechnungsart.BUCHUNG);b.getMietbereiche().add(pos);ids.add(bean(BuchungRepository.class).saveAndFlush(b).getId());}return ids;});}
 @Test void paralleleRuecknahmenKoennenKeinenBestandDoppeltBelegen()throws Exception{var ids=buchungen(mieter(),2);var start=new CountDownLatch(1);try(var pool=Executors.newFixedThreadPool(2)){var futures=new ArrayList<Future<Boolean>>();for(long id:ids)futures.add(pool.submit(()->{auth();start.await();try{bean(BuchungService.class).stornoZuruecknehmen(id);return true;}catch(IllegalArgumentException e){return false;}finally{TenantContext.clear();SecurityContextHolder.clearContext();}}));start.countDown();int successes=0;for(var future:futures)if(future.get(30,TimeUnit.SECONDS))successes++;assertEquals(1,successes);assertEquals(Long.valueOf(1),tx.execute(t->bean(BuchungRepository.class).findAllById(ids).stream().filter(b->b.getStatus()==Buchungsstatus.BESTAETIGT).count()));}}
 @Test void fehlerAmEndeDesMergeRolltBuchungenUndStammdatenZurueck(){long q=mieter(),z=mieter();var ids=buchungen(q,2);var service=bean(StammdatenMergeService.class);var preview=service.vorschau(true,q,z);doThrow(new IllegalStateException("Audit-Ausfall nach Änderungen")).when(rawLog).schreiben(eq("MIETER_ZUSAMMENGEFUEHRT"),any());assertThrows(IllegalStateException.class,()->service.zusammenfuehren(true,new StammdatenMergeService.Auftrag(q,z,Map.of(),preview.fingerprint())));tx.executeWithoutResult(t->{assertNull(bean(MieterRepository.class).findById(q).orElseThrow().getZusammengefuehrtIn());assertTrue(bean(MieterRepository.class).findById(q).orElseThrow().isAktiv());long person=bean(MieterRepository.class).findById(q).orElseThrow().getPerson().getId();assertTrue(bean(BuchungRepository.class).findAllById(ids).stream().allMatch(b->b.getMieter().getId()==person));});}
 @Test void altanfragenWerdenNurBeiFreierVerfuegbarkeitReserviert(){var ids=buchungen(mieter(),2);tx.executeWithoutResult(t->{for(var b:bean(BuchungRepository.class).findAllById(ids))b.setStatus(Buchungsstatus.ANFRAGE);});var service=bean(AnfrageMigrationService.class);service.migrieren(ids.getFirst(),false);assertThrows(IllegalArgumentException.class,()->service.migrieren(ids.getLast(),false));tx.executeWithoutResult(t->{assertEquals(Buchungsstatus.RESERVIERT,bean(BuchungRepository.class).findById(ids.getFirst()).orElseThrow().getStatus());assertEquals(Buchungsstatus.ANFRAGE,bean(BuchungRepository.class).findById(ids.getLast()).orElseThrow().getStatus());});verifyNoInteractions(bean(HomepageStatusMailService.class));}

}
