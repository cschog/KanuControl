package com.kcserver.unit;

import com.kcserver.kjfp.enumtype.*;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungBuchungRepository;
import jakarta.persistence.*;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

/** Führt die produktiven HQL-Aggregationen auf einem isolierten H2-Datenmodell aus. */
class FinanzausgleichKategorienQueryTest {
    @Entity(name="TestEvent") static class Event { @Id Long id; }
    @Entity(name="TestAccount") static class Account { @Id Long id; }
    @Entity(name="TestAbrechnung") static class Abrechnung { @Id Long id; @ManyToOne Event veranstaltung; }
    @Entity(name="TestBeleg") static class Beleg { @Id Long id; @ManyToOne Abrechnung abrechnung; @ManyToOne Account finanzGruppe; }
    @Entity(name="AbrechnungBuchung") static class Buchung {
        @Id Long id; @ManyToOne Beleg beleg;
        @Enumerated(EnumType.STRING) FinanzKategorie kategorie;
        @Enumerated(EnumType.STRING) BuchungsHerkunft herkunft;
        BigDecimal betrag;
    }
    String query(String name) throws Exception {
        return AbrechnungBuchungRepository.class.getMethod(name, Long.class).getAnnotation(Query.class).value();
    }
    @Test void everyCategoryIsCountedWithCorrectDirectionAndAutomaticContributionsAreNotCountedTwice() throws Exception {
        var cfg = new Configuration().setProperty("hibernate.connection.url", "jdbc:h2:mem:categories;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop");
        for (var type : List.of(Event.class, Account.class, Abrechnung.class, Beleg.class, Buchung.class)) cfg.addAnnotatedClass(type);
        try (var factory = cfg.buildSessionFactory(); var session = factory.openSession()) {
            var tx = session.beginTransaction();
            var event = new Event(); event.id=1L; session.persist(event);
            var abrechnung = new Abrechnung(); abrechnung.id=1L; abrechnung.veranstaltung=event; session.persist(abrechnung);
            for (var category : FinanzKategorie.values()) {
                long id = category.ordinal()+1L;
                var account = new Account(); account.id=id; session.persist(account);
                var beleg = new Beleg(); beleg.id=id; beleg.abrechnung=abrechnung; beleg.finanzGruppe=account; session.persist(beleg);
                var b = new Buchung(); b.id=id; b.beleg=beleg; b.kategorie=category; b.herkunft=BuchungsHerkunft.MANUELL; b.betrag=BigDecimal.TEN; session.persist(b);
                if (category==FinanzKategorie.TEILNEHMERBEITRAG) {
                    var auto = new Buchung(); auto.id=100L; auto.beleg=beleg; auto.kategorie=category;
                    auto.herkunft=BuchungsHerkunft.TEILNEHMERBEITRAG; auto.betrag=new BigDecimal("100"); session.persist(auto);
                }
            }
            session.flush();
            var rows = session.createQuery(query("sumFinanzenByVeranstaltungGrouped"), Object[].class).setParameter("veranstaltungId",1L).getResultList();
            for (Object[] row : rows) {
                var category=FinanzKategorie.values()[((Number)row[0]).intValue()-1];
                assertThat((BigDecimal)row[1]).as(category+" Einnahmen").isEqualByComparingTo(category.isEinnahme()?BigDecimal.TEN:BigDecimal.ZERO);
                assertThat((BigDecimal)row[2]).as(category+" Ausgaben").isEqualByComparingTo(category.isKosten()?BigDecimal.TEN:BigDecimal.ZERO);
            }
            var saldo = session.createQuery(query("sumAbrechnungSaldoByFinanzGruppeGrouped"),Object[].class).setParameter("veranstaltungId",1L).getResultList();
            for(Object[] row:saldo) {
                var category=FinanzKategorie.values()[((Number)row[0]).intValue()-1];
                assertThat((BigDecimal)row[1]).as(category+" Saldo").isEqualByComparingTo(category.isKosten()?BigDecimal.TEN:category.isEinnahme()?BigDecimal.TEN.negate():BigDecimal.ZERO);
            }
            tx.rollback();
        }
    }
}
