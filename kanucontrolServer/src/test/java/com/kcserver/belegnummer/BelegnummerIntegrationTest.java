package com.kcserver.belegnummer;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.entity.FinanzGruppe;
import com.kcserver.kjfp.service.abrechnung.BelegnummerService;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "KC_BELEG_TEST_URL", matches = ".+")
class BelegnummerIntegrationTest {
    String url;
    String schema;
    String migration;
    SessionFactory factory;

    @BeforeEach void setup() throws Exception {
        schema = "beleg_test_" + UUID.randomUUID().toString().replace("-", "");
        url = System.getenv("KC_BELEG_TEST_URL");
        try (var c = DriverManager.getConnection(url); var s = c.createStatement()) {
            s.execute("CREATE SCHEMA " + schema);
            s.execute("SET search_path TO " + schema);
            s.execute("CREATE TABLE veranstaltung(id bigint PRIMARY KEY)");
            s.execute("CREATE TABLE finanz_gruppe(id bigint PRIMARY KEY, veranstaltung_id bigint, kuerzel text)");
            s.execute("CREATE TABLE abrechnung(id bigint PRIMARY KEY, veranstaltung_id bigint)");
            s.execute("CREATE TABLE abrechnung_beleg(id bigint PRIMARY KEY, abrechnung_id bigint, beleg_nummer text, lfd_nr integer)");
            s.execute("CREATE TABLE zahlungsnachweis(id bigint PRIMARY KEY, veranstaltung_id bigint, finanz_gruppe_id bigint, zahlungsweg text, datum date)");
            s.execute("CREATE TABLE reisekostenabrechnung(id bigint PRIMARY KEY, veranstaltung_id bigint)");
            s.execute("INSERT INTO veranstaltung VALUES (1), (2)");
            s.execute("INSERT INTO finanz_gruppe VALUES (1, 1, 'RZ'), (2, 1, 'AK')");
            s.execute("INSERT INTO abrechnung VALUES (1, 1)");
            s.execute("INSERT INTO abrechnung_beleg VALUES (1, 1, 'RZ_019', 19), (2, 1, 'AK_010', 10)");
            s.execute("INSERT INTO zahlungsnachweis VALUES (1, 1, 1, 'QUITTUNG', '2026-01-01'), (2, 1, null, 'QUITTUNG', '2026-01-01'), (3, 1, 1, 'UEBERWEISUNG', '2026-01-01')");
        }
        migration = Files.readString(Path.of("src/main/resources/db/changelog/changes/20261112-01-gemeinsame-belegnummern.sql"));
        migration += Files.readString(Path.of("src/main/resources/db/changelog/changes/20261112-02-belegnummern-je-konto.sql"));
        factory = new Configuration()
                .setProperty("hibernate.connection.url", url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema)
                .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                .setProperty("hibernate.connection.pool_size", "12")
                .setProperty("hibernate.show_sql", "false")
                .buildSessionFactory();
    }
    void sql(String sql) throws Exception {
        try (var c = DriverManager.getConnection(url); var s = c.createStatement()) {
            s.execute("SET search_path TO " + schema);
            s.execute(sql);
        }
    }
    String scalar(String query) throws Exception {
        try (var c = DriverManager.getConnection(url); var s = c.createStatement()) {
            s.execute("SET search_path TO " + schema);
            try (var rows = s.executeQuery(query)) { rows.next(); return rows.getString(1); }
        }
    }
    FinanzGruppe konto() {
        var event = new Veranstaltung(); event.setId(1L);
        var konto = new FinanzGruppe(); konto.setId(1L); konto.setVeranstaltung(event); konto.setKuerzel("RZ");
        return konto;
    }
    String allocate() {
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            var nummer = new BelegnummerService(session).next(konto());
            tx.commit(); return nummer.wert();
        }
    }
    @AfterEach void cleanup() throws Exception {
        if (factory != null) factory.close();
        sql("DROP SCHEMA " + schema + " CASCADE");
    }
    @Test void migratesOnlyAssignedReceiptsAndCanRepeatBackfill() throws Exception {
        sql(migration);
        assertThat(scalar("SELECT beleg_nummer FROM abrechnung_beleg WHERE id=1")).isEqualTo("RZ_019");
        assertThat(scalar("SELECT belegnummer FROM zahlungsnachweis WHERE id=1")).isEqualTo("RZ_020");
        assertThat(scalar("SELECT belegnummer FROM zahlungsnachweis WHERE id=2")).isNull();
        assertThat(scalar("SELECT belegnummer FROM zahlungsnachweis WHERE id=3")).isNull();
        // Wiederholte Liquibase-Ausführung wird separat mit echtem Changelog geprüft.
        assertThat(scalar("SELECT letzter_wert FROM belegnummer_zaehler WHERE finanz_gruppe_id=1")).isEqualTo("20");
        assertThat(allocate()).isEqualTo("RZ_021");
        sql("DELETE FROM zahlungsnachweis WHERE id=1");
        assertThat(allocate()).isEqualTo("RZ_022");
    }
    @Test void rejectsExistingCollisionWithoutRenumbering() throws Exception {
        sql("INSERT INTO abrechnung_beleg VALUES (3, 1, 'RZ_019', 19)");
        assertThatThrownBy(() -> sql(migration)).hasMessageContaining("Belegnummernkollision");
        assertThat(scalar("SELECT count(*) FROM abrechnung_beleg WHERE beleg_nummer='RZ_019'")).isEqualTo("2");
    }
    @Test void liquibaseAcceptsDeployedChecksumAndAppliesAccountMigrationOnce() throws Exception {
        try (var connection = DriverManager.getConnection(url)) {
            connection.createStatement().execute("SET search_path TO " + schema);
            var database = liquibase.database.DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new liquibase.database.jvm.JdbcConnection(connection));
            database.setDefaultSchemaName(schema);
            database.setLiquibaseSchemaName(schema);
            try (var old = new liquibase.Liquibase(
                    "db/changelog/changes/20261112-01-gemeinsame-belegnummern.xml",
                    new liquibase.resource.ClassLoaderResourceAccessor(), database)) {
                old.update(new liquibase.Contexts());
                assertThat(scalar("SELECT md5sum FROM databasechangelog WHERE id='20261112-01-gemeinsame-belegnummern'"))
                        .isEqualTo("9:a0a448d3763e69f4feaada03055d5b54");
                assertThat(scalar("SELECT belegnummer FROM zahlungsnachweis WHERE id=1")).isEqualTo("RZ_020");
                // Die alte Version wurde bereits ausgerollt. Genau diesen Upgradepfad prüfen.
                {
                    var upgrade = new liquibase.Liquibase("db/changelog/belegnummer-upgrade-test.xml",
                        new liquibase.resource.ClassLoaderResourceAccessor(), database);
                    upgrade.update(new liquibase.Contexts());
                    upgrade.update(new liquibase.Contexts());
                    assertThat(scalar("SELECT count(*) FROM databasechangelog")).isEqualTo("2");
                    assertThat(scalar("SELECT belegnummer FROM zahlungsnachweis WHERE id=1")).isEqualTo("RZ_020");
                    assertThat(scalar("SELECT letzter_wert FROM belegnummer_zaehler WHERE finanz_gruppe_id=2")).isEqualTo("10");
                }
            }
        }
    }

    @Test void accountsHaveIndependentSequencesStartingAtOne() throws Exception {
        sql("INSERT INTO finanz_gruppe VALUES (3, 1, 'Hil')");
        sql("UPDATE abrechnung_beleg SET beleg_nummer='RZ_028', lfd_nr=28 WHERE id=1");
        sql(migration);
        assertThat(scalar("SELECT belegnummer FROM zahlungsnachweis WHERE id=1")).isEqualTo("RZ_029");
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            var service = new BelegnummerService(session);
            var hil = konto(); hil.setId(3L); hil.setKuerzel("Hil");
            var ak = konto(); ak.setId(2L); ak.setKuerzel("AK");
            assertThat(service.next(hil).wert()).isEqualTo("Hil_001");
            assertThat(service.next(ak).wert()).isEqualTo("AK_011");
            assertThat(service.next(hil).wert()).isEqualTo("Hil_002");
            tx.commit();
        }
        assertThat(allocate()).isEqualTo("RZ_030");
    }

    @Test void concurrentReservationsRemainUniqueAcrossDocumentTypes() throws Exception {
        sql(migration);
        var pool = Executors.newFixedThreadPool(8);
        try {
            var jobs = new ArrayList<Callable<String>>();
            for (int i=0; i<40; i++) jobs.add(this::allocate);
            var values = new HashSet<String>();
            for (var result : pool.invokeAll(jobs)) values.add(result.get());
            assertThat(values).hasSize(40).contains("RZ_021", "RZ_060");
            assertThat(scalar("SELECT count(*) FROM belegnummer_register WHERE veranstaltung_id=1")).isEqualTo("43");
        } finally { pool.shutdownNow(); }
    }
}
