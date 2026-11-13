package com.kcserver.vermietung.zaehlerimport;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import liquibase.integration.spring.SpringLiquibase;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Only creates and drops its own randomly named schema in a local PostgreSQL database. */
@EnabledIfEnvironmentVariable(named="KC_IMPORT_TEST_JDBC", matches=".+")
class MeterImportPostgresTest {
    DriverManagerDataSource ds; JdbcTemplate jdbc; MeterImportConfig config; MeterImportService service; String schema;
    @BeforeEach void setup() throws Exception {
        ds = new DriverManagerDataSource(System.getenv("KC_IMPORT_TEST_JDBC"),System.getenv("KC_IMPORT_TEST_USER"),System.getenv("KC_IMPORT_TEST_PASSWORD"));
        jdbc = new JdbcTemplate(ds); schema = "ha_test_"+UUID.randomUUID().toString().replace("-", "");
        jdbc.execute("CREATE SCHEMA "+schema);
        jdbc.execute("CREATE TABLE "+schema+".zaehler (id bigint PRIMARY KEY, zaehlernummer text, aktiv boolean, verbrauchsart text, mengeneinheit text)");
        jdbc.execute("CREATE TABLE "+schema+".zaehlerstand (id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, zaehler_id bigint REFERENCES "+schema+".zaehler(id), ablesedatum timestamp NOT NULL, zaehlerstand numeric(15,3) NOT NULL, bemerkung varchar(1000), created_at timestamptz NOT NULL, created_by text, last_modified_at timestamptz, last_modified_by text)");
        var lb = new SpringLiquibase(); lb.setDataSource(ds); lb.setDefaultSchema(schema);
        lb.setChangeLog("classpath:db/changelog/changes/20261112-04-meter-import.xml"); lb.afterPropertiesSet();
        jdbc.execute("INSERT INTO "+schema+".zaehler VALUES (1,'9728276',true,'STROM','kWh'),(2,'other',true,'STROM','kWh')");
        config = new MeterImportConfig(); config.setEnabled(true); config.setTenant(schema); config.setMeterId(1L);
        config.setMeterNumber("9728276"); config.setKeyHash("a".repeat(64));
        service = new MeterImportService(ds,config);
    }
    @AfterEach void close() { if (jdbc != null && schema != null) jdbc.execute("DROP SCHEMA "+schema+" CASCADE"); }
    MeterImportController.Reading reading(String time,String value) {
        return new MeterImportController.Reading(schema,1L,OffsetDateTime.parse(time),new BigDecimal(value),"kWh","Automatischer Import von Home Assistant");
    }
    @Test void storesValueTimeAndRemarkAndDeduplicatesEquivalentOffsets() throws Exception {
        var first = service.ingest(reading("2026-10-25T02:00:00+02:00","123.456"));
        var retry = service.ingest(reading("2026-10-25T00:00:00Z","123.456"));
        assertTrue(first.created()); assertFalse(retry.created()); assertEquals(first.id(),retry.id());
        assertTrue(service.ingest(reading("2026-10-25T02:00:00+01:00","123.457")).created());
        assertEquals(2,jdbc.queryForObject("SELECT count(*) FROM "+schema+".zaehlerstand",Integer.class));
        var row = jdbc.queryForMap("SELECT * FROM "+schema+".zaehlerstand WHERE id=?",first.id());
        assertEquals(new BigDecimal("123.456"),row.get("zaehlerstand"));
        assertEquals("Automatischer Import von Home Assistant",row.get("bemerkung"));
        assertEquals(java.time.LocalDateTime.parse("2026-10-25T02:00:00"),((java.sql.Timestamp)row.get("ablesedatum")).toLocalDateTime());
        assertEquals(java.time.Instant.parse("2026-10-25T00:00:00Z"),((java.sql.Timestamp)row.get("import_messzeit")).toInstant());
    }
    @Test void concurrentRetriesCreateOneRowAndChangedValueConflicts() throws Exception {
        var r = reading("2026-10-01T12:00:00Z","42");
        try (var pool = Executors.newFixedThreadPool(8)) {
            var tasks = new ArrayList<Callable<MeterImportController.Result>>();
            for (int i=0;i<16;i++) tasks.add(()->service.ingest(r));
            var results = pool.invokeAll(tasks); int created=0; Set<Long> ids = new HashSet<>();
            for (var result:results) { var value=result.get(); if(value.created()) created++; ids.add(value.id()); }
            assertEquals(1,created); assertEquals(1,ids.size());
        }
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.ingest(reading("2026-10-01T12:00:00Z","43"))).getStatusCode().value());
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM "+schema+".zaehlerstand",Integer.class));
    }
    @Test void scopeAndPhysicalIdentityCannotBeChangedByCaller() throws Exception {
        var r=reading("2026-10-01T12:00:00Z","42");
        var otherTenant=new MeterImportController.Reading("other",1L,r.measuredAt(),r.value(),r.unit(),r.remark());
        assertEquals(403,assertThrows(ResponseStatusException.class,()->service.ingest(otherTenant)).getStatusCode().value());
        var otherMeter=new MeterImportController.Reading(schema,2L,r.measuredAt(),r.value(),r.unit(),r.remark());
        assertEquals(403,assertThrows(ResponseStatusException.class,()->service.ingest(otherMeter)).getStatusCode().value());
        config.setMeterNumber("wrong");
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.ingest(r)).getStatusCode().value());
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM "+schema+".zaehlerstand",Integer.class));
    }
    @Test void zeroIsValidButFutureAndSubsecondTimesAreNot() throws Exception {
        assertTrue(service.ingest(reading("2026-10-01T12:00:00Z","0")).created());
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service.ingest(reading("2099-10-01T12:00:00Z","1"))).getStatusCode().value());
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service.ingest(reading("2026-10-01T12:00:00.123Z","1"))).getStatusCode().value());
    }
}
