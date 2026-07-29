package com.kcserver.service;

import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.veranstaltung.VeranstaltungBerechnungsService;
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