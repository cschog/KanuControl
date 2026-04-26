package com.kcserver.integration;

import com.kcserver.entity.*;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.repository.KikZuschlagRepository;
import com.kcserver.service.VeranstaltungFoerderService;
import com.kcserver.service.FoerdersatzService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class FoerderBerechnungIntegrationTest extends AbstractFinanzIntegrationTest {

    @Autowired
    VeranstaltungFoerderService foerderService;

    @Autowired
    FoerdersatzService foerdersatzService;

    @Autowired
    KikZuschlagRepository kikZuschlagRepository;

    private static final LocalDate START = LocalDate.of(2026, 5, 1);

    Long veranstaltungId;

    @BeforeEach
    void setup() {
        veranstaltungId = createTestVeranstaltung(
                VeranstaltungTyp.FM,
                START
        );
        createOpenAbrechnung(veranstaltungId);
    }

    /* =========================================================
       BASIS: 2 Tage × 10 Teilnehmer × 14 €
       ========================================================= */

    @Test
    void shouldCalculateBasicFoerderung() {

        createFoerdersatz(VeranstaltungTyp.FM, "14.00", "20.00");

        createTeilnehmerMitAlter(10, 10); // 10 Teilnehmer à 10 Jahre

        BigDecimal result = foerderService.berechneFoerderung(veranstaltungId);

        // 2 Tage × 10 × 14
        assertThat(result).isEqualByComparingTo("280.00");
    }

    /* =========================================================
       ALTERSGRENZEN
       ========================================================= */

    @Test
    void shouldOnlyCountAgeBetween6And20() {

        createFoerdersatz(VeranstaltungTyp.FM, "10.00", "20.00");

        createTeilnehmerMitGeburtsjahr(START.minusYears(5));   // 5 Jahre
        createTeilnehmerMitGeburtsjahr(START.minusYears(6));
        createTeilnehmerMitGeburtsjahr(START.minusYears(7));
        createTeilnehmerMitGeburtsjahr(START.minusYears(8));
        createTeilnehmerMitGeburtsjahr(START.minusYears(9));
        createTeilnehmerMitGeburtsjahr(START.minusYears(10));
        createTeilnehmerMitGeburtsjahr(START.minusYears(15));
        createTeilnehmerMitGeburtsjahr(START.minusYears(20));
        createTeilnehmerMitGeburtsjahr(START.minusYears(21));  // 21 Jahre

        BigDecimal result = foerderService.berechneFoerderung(veranstaltungId);

        // Förderfähig: 6, 10, 20 = 7 Personen
        // 2 Tage × 7 × 10 €
        assertThat(result).isEqualByComparingTo("140.00");
    }

     /* =========================================================
       MINDESTANZAHL
       ========================================================= */

    @Test

    void shouldReturnZeroIfLessThan7FoerderfaehigeTeilnehmer() {

        createFoerdersatz(

                VeranstaltungTyp.FM,

                "10.00",

                "20.00"

        );

        createTeilnehmerMitAlter(6, 10);

        BigDecimal result =

                foerderService

                        .berechneFoerderung(veranstaltungId);

        assertThat(result)

                .isEqualByComparingTo("0.00");

    }

    @Test

    void shouldAllowFoerderungAt7Teilnehmer() {

        createFoerdersatz(

                VeranstaltungTyp.FM,

                "10.00",

                "20.00"

        );

        createTeilnehmerMitAlter(7, 10);

        BigDecimal result =

                foerderService

                        .berechneFoerderung(veranstaltungId);

        // 2 Tage × 7 × 10 €

        assertThat(result)

                .isEqualByComparingTo("140.00");

    }

    /* =========================================================
       KiK-ZUSCHLAG
       ========================================================= */

    @Test
    void shouldAddKikZuschlag() {

        createFoerdersatz(VeranstaltungTyp.FM, "14.00", "30.00");
        createKikZuschlag("3.00");

        setVereinKikZertifiziert(true);

        createTeilnehmerMitAlter(7, 10);

        BigDecimal result = foerderService.berechneFoerderung(veranstaltungId);

        // 14 + 3 = 17
        // 2 Tage × 5 × 17
        assertThat(result).isEqualByComparingTo("238.00");
    }

    /* =========================================================
       DECKEL GREIFT
       ========================================================= */

    @Test
    void shouldApplyFoerderdeckel() {

        createFoerdersatz(VeranstaltungTyp.FM, "14.00", "15.00");
        createKikZuschlag("3.00");

        setVereinKikZertifiziert(true);

        createTeilnehmerMitAlter(10, 12);

        BigDecimal result = foerderService.berechneFoerderung(veranstaltungId);

        // 14 + 3 = 17 → gedeckelt auf 15
        // 2 Tage × 10 × 15
        assertThat(result).isEqualByComparingTo("300.00");
    }

     /* =========================================================
       MAXIMAL 21 TAGE
       ========================================================= */

    @Test

    void shouldLimitFoerderungTo21Days() {

        veranstaltungId =

                createTestVeranstaltung(

                        VeranstaltungTyp.FM,

                        LocalDate.of(2026, 1, 1),

                        LocalDate.of(2026, 1, 30)

                );

        createOpenAbrechnung(veranstaltungId);

        createFoerdersatz(

                VeranstaltungTyp.FM,

                "10.00",

                "20.00"

        );

        createTeilnehmerMitAlter(10, 10);

        BigDecimal result =

                foerderService

                        .berechneFoerderung(veranstaltungId);

        // max. 21 Tage!

        // 21 × 10 × 10 €

        assertThat(result)

                .isEqualByComparingTo("2100.00");

    }

    /* =========================================================
       NICHT UNTERSTÜTZTER TYP
       ========================================================= */

    @Test
    void shouldReturnZeroForUnsupportedTyp() {

        veranstaltungId = createTestVeranstaltung(
                VeranstaltungTyp.BM,
                START
        );
        createOpenAbrechnung(veranstaltungId);

        createFoerdersatz(VeranstaltungTyp.BM, "20.00", "50.00");

        createTeilnehmerMitAlter(10, 12);

        BigDecimal result = foerderService.berechneFoerderung(veranstaltungId);

        assertThat(result).isEqualByComparingTo("0.00");
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private void createFoerdersatz(VeranstaltungTyp typ, String satz, String deckel) {

        var dto = new com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO();
        dto.setTyp(typ);
        dto.setGueltigVon(LocalDate.of(2026, 1, 1));
        dto.setFoerdersatz(new BigDecimal(satz));
        dto.setFoerderdeckel(new BigDecimal(deckel));

        foerdersatzService.create(dto);
    }

    private void createKikZuschlag(String betrag) {

        KikZuschlag kz = new KikZuschlag();
        kz.setGueltigVon(LocalDate.of(2026, 1, 1));
        kz.setGueltigBis(null);
        kz.setKikZuschlag(new BigDecimal(betrag));
        kz.setBeschluss("Test");

        kikZuschlagRepository.save(kz);
    }

    private void createTeilnehmerMitAlter(int anzahl, int alter) {
        for (int i = 0; i < anzahl; i++) {
            createTeilnehmerMitGeburtsjahr(START.minusYears(alter));
        }
    }

    private void createTeilnehmerMitGeburtsjahr(LocalDate geburt) {

        var veranstaltung = veranstaltungRepository
                .findById(veranstaltungId)
                .orElseThrow();

        Person p = new Person();
        p.setVorname("Test");
        p.setName("Teilnehmer" + System.nanoTime());
        p.setGeburtsdatum(geburt);
        p.setSex(Sex.WEIBLICH);
        p.setAktiv(true);

        p = personRepository.save(p);

        Teilnehmer t = new Teilnehmer();
        t.setVeranstaltung(veranstaltung);
        t.setPerson(p);

        teilnehmerRepository.save(t);
    }

    private void setVereinKikZertifiziert(boolean aktiv) {

        var veranstaltung = veranstaltungRepository
                .findById(veranstaltungId)
                .orElseThrow();

        if (aktiv) {
            veranstaltung.getVerein()
                    .setKikZertifiziertSeit(LocalDate.of(2025, 1, 1));
        }

        vereinRepository.save(veranstaltung.getVerein());
    }
    @Test
    void shouldReturnZeroIfNoFoerdersatzExists() {

        createTeilnehmerMitAlter(5, 12);

        BigDecimal result =
                foerderService.berechneFoerderung(veranstaltungId);

        assertThat(result).isEqualByComparingTo("0.00");
    }
    @Test
    void shouldCalculateWithoutKikIfNoKikZuschlagExists() {

        createFoerdersatz(VeranstaltungTyp.FM, "14.00", "30.00");

        setVereinKikZertifiziert(true);
        // KEIN createKikZuschlag()

        createTeilnehmerMitAlter(7, 10);

        BigDecimal result =
                foerderService.berechneFoerderung(veranstaltungId);

        // 2 Tage × 5 × 14 €
        assertThat(result).isEqualByComparingTo("196.00");
    }
    @Test
    void shouldIgnoreKikIfVereinNotCertified() {

        createFoerdersatz(VeranstaltungTyp.FM, "14.00", "30.00");
        createKikZuschlag("3.00");

        // kein setVereinKikZertifiziert(true)

        createTeilnehmerMitAlter(7, 10);

        BigDecimal result =
                foerderService.berechneFoerderung(veranstaltungId);

        // nur 14 €
        assertThat(result).isEqualByComparingTo("196.00");
    }
    @Test
    void shouldNotReduceIfExactlyOnFoerderdeckel() {

        createFoerdersatz(VeranstaltungTyp.FM, "15.00", "15.00");

        createTeilnehmerMitAlter(7, 10);

        BigDecimal result =
                foerderService.berechneFoerderung(veranstaltungId);

        // 2 Tage × 5 × 15 €
        assertThat(result).isEqualByComparingTo("210.00");
    }
}