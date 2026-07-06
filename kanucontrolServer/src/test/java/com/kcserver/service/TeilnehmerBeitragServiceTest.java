package com.kcserver.service;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.service.beitrag.BeitragsregelService;
import com.kcserver.service.beitrag.TeilnehmerBeitragService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@Disabled("Nach Refactoring auf Mockito umstellen")
class TeilnehmerBeitragServiceTest {

    private TeilnehmerBeitragService service;

    @BeforeEach
    void setup() {

        service = new TeilnehmerBeitragService(
                new BeitragsregelService(null),
                new AltersService()
        );
    }

    /* =========================================================
       STANDARDGEBÜHR
       ========================================================= */

    @Test
    void shouldReturnStandardGebuehr() {

        Veranstaltung v = new Veranstaltung();
        v.setIndividuelleGebuehren(false);
        v.setStandardGebuehr(
                BigDecimal.valueOf(250)
        );

        Teilnehmer t = new Teilnehmer();

        BigDecimal result =
                service.getEffektiverBeitrag(v, t);

        assertEquals(
                BigDecimal.valueOf(250),
                result
        );
    }

    /* =========================================================
       INDIVIDUELLER BEITRAG
       ========================================================= */

    @Test
    void shouldReturnIndividuellerBeitrag() {

        Veranstaltung v = new Veranstaltung();
        v.setIndividuelleGebuehren(true);

        Teilnehmer t = new Teilnehmer();

        t.setIndividuellerBeitrag(
                BigDecimal.valueOf(125)
        );

        BigDecimal result =
                service.getEffektiverBeitrag(v, t);

        assertEquals(
                BigDecimal.valueOf(125),
                result
        );
    }

    /* =========================================================
       NULL STANDARDGEBÜHR
       ========================================================= */

    @Test
    void shouldReturnZeroWhenStandardGebuehrIsNull() {

        Veranstaltung v = new Veranstaltung();
        v.setIndividuelleGebuehren(false);

        Teilnehmer t = new Teilnehmer();

        BigDecimal result =
                service.getEffektiverBeitrag(v, t);

        assertEquals(
                BigDecimal.ZERO,
                result
        );
    }

    /* =========================================================
       BEZAHLT
       ========================================================= */

    @Test
    void shouldReturnTrueWhenBezahlt() {

        Teilnehmer t = new Teilnehmer();
        t.setBezahlt(true);

        boolean result =
                service.isBezahlt(t);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenNichtBezahlt() {

        Teilnehmer t = new Teilnehmer();
        t.setBezahlt(false);

        boolean result =
                service.isBezahlt(t);

        assertFalse(result);
    }

    /* =========================================================
       OFFENER BETRAG
       ========================================================= */

    @Test
    void shouldReturnZeroWhenAlreadyBezahlt() {

        Veranstaltung v = new Veranstaltung();

        v.setIndividuelleGebuehren(false);

        v.setStandardGebuehr(
                BigDecimal.valueOf(300)
        );

        Teilnehmer t = new Teilnehmer();
        t.setBezahlt(true);

        BigDecimal result =
                service.getOffenerBetrag(v, t);

        assertEquals(
                BigDecimal.ZERO,
                result
        );
    }

    @Test
    void shouldReturnOpenAmountWhenNotBezahlt() {

        Veranstaltung v = new Veranstaltung();

        v.setIndividuelleGebuehren(false);

        v.setStandardGebuehr(
                BigDecimal.valueOf(300)
        );

        Teilnehmer t = new Teilnehmer();
        t.setBezahlt(false);

        BigDecimal result =
                service.getOffenerBetrag(v, t);

        assertEquals(
                BigDecimal.valueOf(300),
                result
        );
    }

    /* =========================================================
       NULL SAFETY
       ========================================================= */

    @Test
    void shouldReturnZeroWhenVeranstaltungIsNull() {

        Teilnehmer t = new Teilnehmer();

        BigDecimal result =
                service.getEffektiverBeitrag(
                        null,
                        t
                );

        assertEquals(
                BigDecimal.ZERO,
                result
        );
    }

    @Test
    void shouldReturnZeroWhenTeilnehmerIsNull() {

        Veranstaltung v = new Veranstaltung();

        BigDecimal result =
                service.getEffektiverBeitrag(
                        v,
                        null
                );

        assertEquals(
                BigDecimal.ZERO,
                result
        );
    }

    /* =========================================================
       BEITRAGSSTRUKTUR
       ========================================================= */

    @Test
    void shouldUseBeitragsstrukturWhenNoIndividualBeitrag() {

        Veranstaltung v = new Veranstaltung();

        v.setIndividuelleGebuehren(true);

        v.setBeginnDatum(
                LocalDate.of(2024, 1, 1)
        );

        Beitragsstruktur struktur =
                new Beitragsstruktur();

        Beitragsregel regel =
                new Beitragsregel();


        regel.setAlterBis(12);

        regel.setBeitrag(
                BigDecimal.valueOf(100)
        );

        regel.setStruktur(struktur);

        struktur.setRegeln(
                List.of(regel)
        );

        v.setBeitragsstruktur(struktur);

        Person p = new Person();

        p.setGeburtsdatum(
                LocalDate.of(2014, 1, 1)
        );

        Teilnehmer t = new Teilnehmer();

        t.setPerson(p);

        t.setVeranstaltung(v);

        BigDecimal result =
                service.getEffektiverBeitrag(v, t);

        assertEquals(
                BigDecimal.valueOf(100),
                result
        );
    }

    @Test
    void shouldMatchRolleSpecificRegel() {

        Veranstaltung v = new Veranstaltung();

        v.setIndividuelleGebuehren(true);

        v.setBeginnDatum(
                LocalDate.of(2024, 1, 1)
        );

        Beitragsstruktur struktur =
                new Beitragsstruktur();

        Beitragsregel standard =
                new Beitragsregel();


        standard.setAlterBis(null);

        standard.setBeitrag(
                BigDecimal.valueOf(200)
        );

        standard.setStruktur(struktur);

        Beitragsregel mitarbeiter =
                new Beitragsregel();


        mitarbeiter.setAlterBis(null);

        mitarbeiter.setRolle(
                TeilnehmerRolle.MITARBEITER
        );

        mitarbeiter.setBeitrag(
                BigDecimal.valueOf(50)
        );

        mitarbeiter.setStruktur(struktur);

        struktur.setRegeln(
                List.of(standard, mitarbeiter)
        );

        v.setBeitragsstruktur(struktur);

        Person p = new Person();

        p.setGeburtsdatum(
                LocalDate.of(2000, 1, 1)
        );

        Teilnehmer t = new Teilnehmer();

        t.setPerson(p);

        t.setVeranstaltung(v);

        t.setRolle(
                TeilnehmerRolle.MITARBEITER
        );

        BigDecimal result =
                service.getEffektiverBeitrag(v, t);

        assertEquals(
                BigDecimal.valueOf(50),
                result
        );
    }

    @Test
    void shouldPreferIndividuellerBeitragOverStruktur() {

        Veranstaltung v = new Veranstaltung();

        v.setIndividuelleGebuehren(true);

        v.setBeginnDatum(
                LocalDate.of(2024, 1, 1)
        );

        Beitragsstruktur struktur =
                new Beitragsstruktur();

        Beitragsregel regel =
                new Beitragsregel();


        regel.setAlterBis(null);

        regel.setBeitrag(
                BigDecimal.valueOf(100)
        );

        regel.setStruktur(struktur);

        struktur.setRegeln(
                List.of(regel)
        );

        v.setBeitragsstruktur(struktur);

        Person p = new Person();

        p.setGeburtsdatum(
                LocalDate.of(2000, 1, 1)
        );

        Teilnehmer t = new Teilnehmer();

        t.setPerson(p);

        t.setVeranstaltung(v);

        t.setIndividuellerBeitrag(
                BigDecimal.valueOf(999)
        );

        BigDecimal result =
                service.getEffektiverBeitrag(v, t);

        assertEquals(
                BigDecimal.valueOf(999),
                result
        );
    }

    @Test
    void shouldReturnZeroWhenNoRegelMatches() {

        Veranstaltung v = new Veranstaltung();

        v.setIndividuelleGebuehren(true);

        v.setBeginnDatum(
                LocalDate.of(2024, 1, 1)
        );

        Beitragsstruktur struktur =
                new Beitragsstruktur();

        Beitragsregel regel =
                new Beitragsregel();


        regel.setAlterBis(5);

        regel.setBeitrag(
                BigDecimal.valueOf(50)
        );

        regel.setStruktur(struktur);

        struktur.setRegeln(
                List.of(regel)
        );

        v.setBeitragsstruktur(struktur);

        Person p = new Person();

        p.setGeburtsdatum(
                LocalDate.of(2000, 1, 1)
        );

        Teilnehmer t = new Teilnehmer();

        t.setPerson(p);

        t.setVeranstaltung(v);

        BigDecimal result =
                service.getEffektiverBeitrag(v, t);

        assertEquals(
                BigDecimal.ZERO,
                result
        );
    }

    @Test
    void shouldUseOpenEndedRule() {

        Veranstaltung v = new Veranstaltung();

        v.setIndividuelleGebuehren(true);

        v.setBeginnDatum(
                LocalDate.of(2024, 1, 1)
        );

        Beitragsstruktur struktur =
                new Beitragsstruktur();

        Beitragsregel regel =
                new Beitragsregel();


        regel.setAlterBis(null);

        regel.setBeitrag(
                BigDecimal.valueOf(400)
        );

        regel.setStruktur(struktur);

        struktur.setRegeln(
                List.of(regel)
        );

        v.setBeitragsstruktur(struktur);

        Person p = new Person();

        p.setGeburtsdatum(
                LocalDate.of(1980, 1, 1)
        );

        Teilnehmer t = new Teilnehmer();

        t.setPerson(p);

        t.setVeranstaltung(v);

        BigDecimal result =
                service.getEffektiverBeitrag(v, t);

        assertEquals(
                BigDecimal.valueOf(400),
                result
        );
    }
}