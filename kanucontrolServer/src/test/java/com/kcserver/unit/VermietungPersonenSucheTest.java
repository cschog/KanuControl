package com.kcserver.unit;

import com.kcserver.kjfp.repository.PersonRepository;
import jakarta.persistence.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.springframework.data.jpa.repository.Query;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Führt die unveränderte Repository-JPQL gegen ein isoliertes Suchmodell aus. */
class VermietungPersonenSucheTest {
    private SessionFactory factory;
    @Entity(name="Person") @Table(name="such_person")
    public static class SuchPerson {
        @Id Long id; String name; String vorname; boolean aktiv; boolean kjfpSichtbar;
        @OneToMany(mappedBy="person",cascade=CascadeType.ALL) List<Mitgliedschaft> mitgliedschaften=new ArrayList<>();
        public SuchPerson() {}
        SuchPerson(long id,String name,boolean aktiv,boolean kjfp) {this.id=id;this.name=name;this.vorname="Test";this.aktiv=aktiv;this.kjfpSichtbar=kjfp;}
    }
    @Entity(name="Mieter") @Table(name="such_mieter")
    public static class SuchMieter {
        @Id Long id; @ManyToOne SuchPerson person; boolean aktiv;
        public SuchMieter() {}
        SuchMieter(long id,SuchPerson person,boolean aktiv) {this.id=id;this.person=person;this.aktiv=aktiv;}
    }
    @Entity(name="Mitgliedschaft") @Table(name="such_mitgliedschaft")
    public static class Mitgliedschaft {
        @Id Long id; @ManyToOne SuchPerson person; @ManyToOne SuchVerein verein;
        public Mitgliedschaft() {}
    }
    @Entity(name="Verein") @Table(name="such_verein")
    public static class SuchVerein { @Id Long id; public SuchVerein() {} }
    @BeforeEach void setup() {
        factory=new Configuration().addAnnotatedClass(SuchPerson.class).addAnnotatedClass(SuchMieter.class)
            .addAnnotatedClass(Mitgliedschaft.class).addAnnotatedClass(SuchVerein.class)
            .setProperty("hibernate.connection.url","jdbc:h2:mem:"+UUID.randomUUID())
            .setProperty("hibernate.hbm2ddl.auto","create-drop").buildSessionFactory();
        try(var session=factory.openSession()) {
            var tx=session.beginTransaction();
            var kjfp=new SuchPerson(1,"KJFP",true,true);
            var mieter=new SuchPerson(2,"Mieter",true,false);
            var beides=new SuchPerson(3,"Beides",true,true);
            var inaktiv=new SuchPerson(4,"Inaktiv",false,true);
            var verborgen=new SuchPerson(5,"Verborgen",true,false);
            var deaktivierterMieter=new SuchPerson(6,"Deaktiviert",true,false);
            for(var p:List.of(kjfp,mieter,beides,inaktiv,verborgen,deaktivierterMieter))session.persist(p);
            session.persist(new SuchMieter(1,mieter,true));session.persist(new SuchMieter(2,beides,true));
            session.persist(new SuchMieter(3,deaktivierterMieter,false));
            tx.commit();
        }
    }
    @AfterEach void close() {factory.close();}
    private List<Long> suche(String search) throws Exception {
        String query=PersonRepository.class.getMethod("searchVermietungRefList",String.class).getAnnotation(Query.class).value();
        try(var session=factory.openSession()) {
            return session.createQuery(query,SuchPerson.class).setParameter("search",search).getResultList().stream().map(p->p.id).toList();
        }
    }
    @Test void kombiniertAktiveKjfpPersonenUndMieterOhneDuplikate() throws Exception {
        assertEquals(List.of(3L,1L,2L),suche(null));
        assertEquals(List.of(3L,1L,2L),suche(""));
    }
    @Test void sucheFiltertBeideQuellenUndIstUnabhaengigVonGrossschreibung() throws Exception {
        assertEquals(List.of(1L),suche("kjfp"));assertEquals(List.of(2L),suche("MIETER"));
        assertEquals(List.of(3L,1L,2L),suche("test"));assertTrue(suche("unbekannt").isEmpty());
    }
    @Test void verborgeneUndInaktivePersonenOhneAktivenMieterBleibenAusgeschlossen() throws Exception {
        assertTrue(suche("Inaktiv").isEmpty());assertTrue(suche("Verborgen").isEmpty());assertTrue(suche("Deaktiviert").isEmpty());
    }
}
