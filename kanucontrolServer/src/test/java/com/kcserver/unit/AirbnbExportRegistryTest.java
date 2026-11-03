package com.kcserver.unit;
import com.kcserver.vermietung.ical.*;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AirbnbExportRegistryTest {
    DriverManagerDataSource ds; AirbnbExportRegistry registry; Liquibase lb;
    @BeforeEach void setup() throws Exception {
        ds=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL;DB_CLOSE_DELAY=-1","sa","");
        var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(ds.getConnection()));db.setDefaultSchemaName("PUBLIC");
        lb=new Liquibase("db/changelog/changes-global/2026-11-03-airbnb-kalender-export.xml",new ClassLoaderResourceAccessor(),db);lb.update(new Contexts(),new LabelExpression());
        registry=new AirbnbExportRegistry(new JdbcTemplate(ds));
    }
    @AfterEach void close() throws Exception{lb.close();}
    @Test void linkIstNachNeustartLesbarUndNurAlsHashGespeichert(){String token=AirbnbExportRegistry.newToken();assertEquals(43,token.length());registry.create("club_a",7,true,Set.of(10L,11L),token);assertEquals("club_a",registry.lookup(token).orElseThrow().tenant());assertEquals(7,registry.lookup(token).orElseThrow().objektId());var jdbc=new JdbcTemplate(ds);assertNotEquals(token,jdbc.queryForObject("SELECT token_hash FROM public.airbnb_kalender_export",String.class));assertTrue(new AirbnbExportRegistry(jdbc).lookup(token).isPresent());}
    @Test void mandantenMitGleicherObjektIdBleibenGetrennt(){registry.create("club_a",7,true,Set.of(10L),AirbnbExportRegistry.newToken());registry.create("club_b",7,false,Set.of(11L),AirbnbExportRegistry.newToken());assertEquals(Set.of(10L),registry.get("club_a",7).orElseThrow().ids());assertEquals(Set.of(11L),registry.get("club_b",7).orElseThrow().ids());}
    @Test void rotationSperrtAltenLinkUndErkenntVersionskonflikt(){String old=AirbnbExportRegistry.newToken(),next=AirbnbExportRegistry.newToken();registry.create("club_a",7,true,Set.of(10L),old);var config=registry.get("club_a",7).orElseThrow();assertTrue(registry.update(config,true,Set.of(10L),next));assertTrue(registry.lookup(old).isEmpty());assertTrue(registry.lookup(next).isPresent());assertFalse(registry.update(config,true,Set.of(10L),null));}
    @Test void deaktiverenBehaeltLinkFuerLeerenKalender(){String token=AirbnbExportRegistry.newToken();registry.create("club_a",7,true,Set.of(10L),token);assertTrue(registry.update(registry.get("club_a",7).orElseThrow(),false,Set.of(),null));assertFalse(registry.lookup(token).orElseThrow().aktiv());}
    @Test void ungueltigeSchluesselWerdenAbgelehnt(){for(String token:List.of("", "a", "../club_a", "x".repeat(43)))assertTrue(registry.lookup(token).isEmpty());}
    @Test void migrationIstRuecknehmbar() throws Exception{lb.rollback(1,new Contexts(),new LabelExpression());assertThrows(org.springframework.jdbc.BadSqlGrammarException.class,()->registry.get("club_a",7));}
}
