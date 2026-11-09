package com.kcserver.unit;

import com.kcserver.vermietung.entity.VorlagenVersand;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class VorlagenVersandMappingTest {
    @Test void archivMappingPasstZurMigrationUndLaedtNurDieGewuenschteBuchung() throws Exception {
        String url="jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        try(var connection=DriverManager.getConnection(url)) {
            connection.createStatement().execute("CREATE TABLE mietobjekt(id BIGINT PRIMARY KEY)");
            var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            var migration=new Liquibase("db/changelog/changes/20261109-01-vorlagensystem.xml",new ClassLoaderResourceAccessor(),db);
            migration.update(new Contexts(),new LabelExpression());
            // Wie die manuelle EntityManagerFactory: keine Spring-Namenskonvention voraussetzen.
            try(var factory=new Configuration().addAnnotatedClass(VorlagenVersand.class)
                    .setProperty("hibernate.connection.url",url)
                    .setProperty("hibernate.hbm2ddl.auto","validate").buildSessionFactory()) {
                var vorbereitet=new VorlagenVersand();vorbereitet.setBuchungId(26L);vorbereitet.setMietobjektId(7L);
                vorbereitet.setVorgang("RESERVIERUNG");vorbereitet.setPdf(new byte[]{1,2,3});vorbereitet.setMetadaten("Beispiel");
                var bestaetigt=new VorlagenVersand();bestaetigt.setBuchungId(99L);bestaetigt.setMietobjektId(7L);
                bestaetigt.setVorgang("RESERVIERUNG");bestaetigt.setPdf(new byte[]{4});bestaetigt.setMetadaten("Beispiel");
                bestaetigt.setSmtpAm(Instant.parse("2026-11-09T12:00:00Z"));bestaetigt.setErgebnis("SMTP_BESTAETIGT");
                try(var session=factory.openSession()) {
                    var tx=session.beginTransaction();session.persist(vorbereitet);session.persist(bestaetigt);tx.commit();
                }
                try(var session=factory.openSession()) {
                    var rows=session.createQuery("from VorlagenVersand where buchungId=:buchung order by erstelltAm desc",VorlagenVersand.class)
                            .setParameter("buchung",26L).getResultList();
                    assertEquals(1,rows.size());assertEquals(vorbereitet.getId(),rows.getFirst().getId());
                    assertNull(rows.getFirst().getSmtpAm());assertArrayEquals(new byte[]{1,2,3},rows.getFirst().getPdf());
                    assertEquals(bestaetigt.getSmtpAm(),session.find(VorlagenVersand.class,bestaetigt.getId()).getSmtpAm());
                }
            }
        }
    }
}
