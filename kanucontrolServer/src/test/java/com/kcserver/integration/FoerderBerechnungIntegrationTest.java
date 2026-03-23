package com.kcserver.integration;

import com.kcserver.entity.*;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.repository.KikZuschlagRepository;
import com.kcserver.service.FoerderBerechnungsService;
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
    FoerderBerechnungsService foerderService;

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
        createTeilnehmerMitGeburtsjahr(START.minusYears(6));   // 6 Jahre
        createTeilnehmerMitGeburtsjahr(START.minusYears(10));  // 10 Jahre
        createTeilnehmerMitGeburtsjahr(START.minusYears(20));  // 20 Jahre
        createTeilnehmerMitGeburtsjahr(START.minusYears(21));  // 21 Jahre

        BigDecimal result = foerderService.berechneFoerderung(veranstaltungId);

        // Förderfähig: 6, 10, 20 = 3 Personen
        // 2 Tage × 3 × 10 €
        assertThat(result).isEqualByComparingTo("60.00");
    }

    /* =========================================================
       KiK-ZUSCHLAG
       ========================================================= */

    @Test
    void shouldAddKikZuschlag() {

        createFoerdersatz(VeranstaltungTyp.FM, "14.00", "30.00");
        createKikZuschlag("3.00");

        setVereinKikZertifiziert(true);

        createTeilnehmerMitAlter(5, 10);

        BigDecimal result = foerderService.berechneFoerderung(veranstaltungId);

        // 14 + 3 = 17
        // 2 Tage × 5 × 17
        assertThat(result).isEqualByComparingTo("170.00");
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

        createTeilnehmerMitAlter(5, 10);

        BigDecimal result =
                foerderService.berechneFoerderung(veranstaltungId);

        // 2 Tage × 5 × 14 €
        assertThat(result).isEqualByComparingTo("140.00");
    }
    @Test
    void shouldIgnoreKikIfVereinNotCertified() {

        createFoerdersatz(VeranstaltungTyp.FM, "14.00", "30.00");
        createKikZuschlag("3.00");

        // kein setVereinKikZertifiziert(true)

        createTeilnehmerMitAlter(5, 10);

        BigDecimal result =
                foerderService.berechneFoerderung(veranstaltungId);

        // nur 14 €
        assertThat(result).isEqualByComparingTo("140.00");
    }
    @Test
    void shouldNotReduceIfExactlyOnFoerderdeckel() {

        createFoerdersatz(VeranstaltungTyp.FM, "15.00", "15.00");

        createTeilnehmerMitAlter(5, 10);

        BigDecimal result =
                foerderService.berechneFoerderung(veranstaltungId);

        // 2 Tage × 5 × 15 €
        assertThat(result).isEqualByComparingTo("150.00");
    }
    @Test
    void shouldIgnoreTeilnehmerWithoutGeburtsdatum() {

        createFoerdersatz(VeranstaltungTyp.FM, "10.00", "20.00");

        // 2 gültige Teilnehmer
        createTeilnehmerMitAlter(2, 10);

        // 1 ohne Geburtsdatum
        createTeilnehmerOhneGeburtsdatum();

        BigDecimal result =
                foerderService.berechneFoerderung(veranstaltungId);

        // nur 2 zählen
        // 2 Tage × 2 × 10 €
        assertThat(result).isEqualByComparingTo("40.00");
    }
    private void createTeilnehmerOhneGeburtsdatum() {

        var veranstaltung = veranstaltungRepository
                .findById(veranstaltungId)
                .orElseThrow();

        Person p = new Person();
        p.setVorname("NoBirth");
        p.setName("Test" + System.nanoTime());
        p.setAktiv(true);
        p.setSex(Sex.WEIBLICH); // NOT NULL beachten

        p = personRepository.save(p);

        Teilnehmer t = new Teilnehmer();
        t.setVeranstaltung(veranstaltung);
        t.setPerson(p);

        teilnehmerRepository.save(t);
    }
}