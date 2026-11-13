package testfixtures.finanzausgleich;

import jakarta.persistence.*;
import com.kcserver.kjfp.enumtype.*;
import java.math.BigDecimal;

/** Isoliertes H2-Modell außerhalb des Spring-Boot-Entity-Scans von com.kcserver. */
public final class FinanzausgleichQueryModel {
    private FinanzausgleichQueryModel() {}
    @Entity(name="TestEvent") public static class Event { @Id public Long id; }
    @Entity(name="TestAccount") public static class Account { @Id public Long id; }
    @Entity(name="TestAbrechnung") public static class Abrechnung { @Id public Long id; @ManyToOne public Event veranstaltung; }
    @Entity(name="TestBeleg") public static class Beleg { @Id public Long id; @ManyToOne public Abrechnung abrechnung; @ManyToOne public Account finanzGruppe; }
    @Entity(name="AbrechnungBuchung") public static class Buchung {
        @Id public Long id; @ManyToOne public Beleg beleg;
        @Enumerated(EnumType.STRING) public FinanzKategorie kategorie;
        @Enumerated(EnumType.STRING) public BuchungsHerkunft herkunft;
        public BigDecimal betrag;
    }
}
