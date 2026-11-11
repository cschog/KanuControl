package com.kcserver.unit;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.enumtype.VeranstaltungTyp;
import com.kcserver.kjfp.service.veranstaltung.KjfpVeranstaltungRegeln;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class KjfpVeranstaltungRegelnTest {
    private final KjfpVeranstaltungRegeln regeln = new KjfpVeranstaltungRegeln();
    private Veranstaltung veranstaltung(String von, String bis) {
        var v = new Veranstaltung(); v.setKjfpAktiv(true);
        v.setBeginnDatum(LocalDate.parse(von)); v.setEndeDatum(LocalDate.parse(bis));
        return v;
    }

    @ParameterizedTest
    @CsvSource({"2026-06-01,2026-06-01,FM", "2026-06-01,2026-06-04,FM", "2026-06-01,2026-06-05,JEM", "2026-03-27,2026-03-30,FM", "2026-10-23,2026-10-27,JEM"})
    void fehlenderTypWirdAusKalendertagenErmittelt(String von, String bis, VeranstaltungTyp erwartet) {
        var v = veranstaltung(von, bis);
        assertNull(v.getTyp());
        assertTrue(regeln.savedKjfpWarnings(v).isEmpty());
        assertEquals(erwartet, v.getTyp());
    }

    @Test void typWirdBeiDaueraenderungAngepasst() {
        var v = veranstaltung("2026-06-01", "2026-06-05"); v.setTyp(VeranstaltungTyp.FM);
        regeln.savedKjfpWarnings(v); assertEquals(VeranstaltungTyp.JEM, v.getTyp());
        v.setEndeDatum(LocalDate.parse("2026-06-04"));
        regeln.savedKjfpWarnings(v); assertEquals(VeranstaltungTyp.FM, v.getTyp());
    }

    @Test void langeDauerBehaeltFoerderwarnung() {
        var v = veranstaltung("2026-06-01", "2026-06-22");
        assertFalse(regeln.savedKjfpWarnings(v).isEmpty());
        assertEquals(VeranstaltungTyp.JEM, v.getTyp());
    }

    @Test void deaktiviertesKjfpUndHistorischerSportBleibenUnveraendert() {
        var v = veranstaltung("2026-06-01", "2026-06-05"); v.setKjfpAktiv(false);
        regeln.savedKjfpWarnings(v); assertNull(v.getTyp());
        v.setKjfpAktiv(true); v.setTyp(VeranstaltungTyp.SPORT);
        regeln.savedKjfpWarnings(v); assertEquals(VeranstaltungTyp.SPORT, v.getTyp());
    }
}
