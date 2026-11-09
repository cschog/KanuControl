package com.kcserver.unit;

import com.kcserver.vermietung.entity.MandantenBriefpapier;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import java.sql.DriverManager;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class BriefpapierPersistenzTest {
    @Test void briefpapierBildUndGrussUeberlebenNeueDatenbankverbindung() throws Exception {
        String url="jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        byte[] pdf=getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes();
        byte[] bild={1,2,3};
        try(var c=DriverManager.getConnection(url)) {
            c.createStatement().execute("CREATE TABLE mietobjekt(id BIGINT PRIMARY KEY)");
            var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            new Liquibase("db/changelog/changes/20261109-01-vorlagensystem.xml",new ClassLoaderResourceAccessor(),db).update(new Contexts(),new LabelExpression());
        }
        for(int durchlauf=0;durchlauf<2;durchlauf++) {
            try(var factory=new Configuration().addAnnotatedClass(MandantenBriefpapier.class)
                    .setProperty("hibernate.connection.url",url).setProperty("hibernate.hbm2ddl.auto","validate").buildSessionFactory();
                var session=factory.openSession()) {
                var repo=new SimpleJpaRepository<MandantenBriefpapier,Long>(MandantenBriefpapier.class,session);
                var tx=session.beginTransaction();
                if(durchlauf==0) {
                    var b=new MandantenBriefpapier();b.setPdf(pdf);b.setBild(bild);b.setGruss("Viele Grüße\nErika Mustermann");
                    var saved=repo.saveAndFlush(b);assertNotNull(saved.getVersion());
                } else {
                    var saved=repo.findById(1L).orElseThrow();
                    assertArrayEquals(pdf,saved.getPdf());assertArrayEquals(bild,saved.getBild());assertEquals("Viele Grüße\nErika Mustermann",saved.getGruss());
                    saved.setGruss("Mit freundlichen Grüßen");repo.saveAndFlush(saved);
                    session.clear();
                    saved=repo.findById(1L).orElseThrow();assertArrayEquals(pdf,saved.getPdf());assertArrayEquals(bild,saved.getBild());
                    assertEquals("Mit freundlichen Grüßen",saved.getGruss());
                }
                tx.commit();
            }
        }
    }
}
