package com.kcserver.service;

import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TeilnehmerBeitragServiceTest {

    private final TeilnehmerBeitragService service =
            new TeilnehmerBeitragService();

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
       NULL INDIVIDUELLER BEITRAG
       ========================================================= */

    @Test
    void shouldReturnZeroWhenIndividuellerBeitragIsNull() {

        Veranstaltung v = new Veranstaltung();
        v.setIndividuelleGebuehren(true);

        Teilnehmer t = new Teilnehmer();

        BigDecimal result =
                service.getEffektiverBeitrag(v, t);

        assertEquals(
                BigDecimal.ZERO,
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
}