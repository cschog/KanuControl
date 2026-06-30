package com.kcserver.service;

import com.kcserver.entity.Veranstaltung;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class VeranstaltungBerechnungsServiceTest {

    private final VeranstaltungBerechnungsService service =
            new VeranstaltungBerechnungsService();

    @Test
    void ermittleGeplanteTeilnehmer() {

        Veranstaltung v = new Veranstaltung();

        v.setGeplanteTeilnehmerMaennlich(10);
        v.setGeplanteTeilnehmerWeiblich(8);
        v.setGeplanteTeilnehmerDivers(2);

        assertEquals(
                20,
                service.ermittleGeplanteTeilnehmer(v)
        );
    }

    @Test
    void ermittleGeplanteMitarbeiter() {

        Veranstaltung v = new Veranstaltung();

        v.setGeplanteMitarbeiterMaennlich(3);
        v.setGeplanteMitarbeiterWeiblich(2);
        v.setGeplanteMitarbeiterDivers(1);

        assertEquals(
                6,
                service.ermittleGeplanteMitarbeiter(v)
        );
    }

    @Test
    void ermittleGeplanteGesamtPersonen() {

        Veranstaltung v = new Veranstaltung();

        v.setGeplanteTeilnehmerMaennlich(10);
        v.setGeplanteTeilnehmerWeiblich(8);
        v.setGeplanteTeilnehmerDivers(2);

        v.setGeplanteMitarbeiterMaennlich(3);
        v.setGeplanteMitarbeiterWeiblich(2);
        v.setGeplanteMitarbeiterDivers(1);

        assertEquals(
                26,
                service.ermittleGeplanteGesamtPersonen(v)
        );
    }

    @Test
    void ermittleTage() {

        Veranstaltung v = new Veranstaltung();

        v.setBeginnDatum(LocalDate.of(2026, 7, 10));
        v.setEndeDatum(LocalDate.of(2026, 7, 15));

        assertEquals(
                6,
                service.ermittleTage(v)
        );
    }

    @Test
    void ermittleNaechte() {

        Veranstaltung v = new Veranstaltung();

        v.setBeginnDatum(LocalDate.of(2026, 7, 10));
        v.setEndeDatum(LocalDate.of(2026, 7, 15));

        assertEquals(
                5,
                service.ermittleNaechte(v)
        );
    }

    @Test
    void ermittleTageBeiEinerTagesveranstaltung() {

        Veranstaltung v = new Veranstaltung();

        v.setBeginnDatum(LocalDate.of(2026, 7, 10));
        v.setEndeDatum(LocalDate.of(2026, 7, 10));

        assertEquals(
                1,
                service.ermittleTage(v)
        );
    }

    @Test
    void ermittleNaechteBeiEinerTagesveranstaltung() {

        Veranstaltung v = new Veranstaltung();

        v.setBeginnDatum(LocalDate.of(2026, 7, 10));
        v.setEndeDatum(LocalDate.of(2026, 7, 10));

        assertEquals(
                0,
                service.ermittleNaechte(v)
        );
    }

    @Test
    void nullWerteWerdenAlsNullPersonenBehandelt() {

        Veranstaltung v = new Veranstaltung();

        assertEquals(
                0,
                service.ermittleGeplanteTeilnehmer(v)
        );

        assertEquals(
                0,
                service.ermittleGeplanteMitarbeiter(v)
        );

        assertEquals(
                0,
                service.ermittleGeplanteGesamtPersonen(v)
        );
    }

    @Test
    void ermittleTageBeiNullVeranstaltung() {

        assertEquals(
                0,
                service.ermittleTage(null)
        );
    }

    @Test
    void ermittleNaechteBeiNullVeranstaltung() {

        assertEquals(
                0,
                service.ermittleNaechte(null)
        );
    }
}