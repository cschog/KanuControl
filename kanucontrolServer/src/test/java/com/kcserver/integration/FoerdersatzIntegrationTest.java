package com.kcserver.integration;

import com.kcserver.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO;
import com.kcserver.dto.foerder.FoerdersatzDTO;
import com.kcserver.entity.Abrechnung;
import com.kcserver.entity.Foerdersatz;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.finanz.AbrechnungBelegService;
import com.kcserver.finanz.AbrechnungService;
import com.kcserver.finanz.FinanzGruppeService;
import com.kcserver.repository.AbrechnungRepository;
import com.kcserver.repository.FoerdersatzRepository;
import com.kcserver.service.FoerdersatzService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FoerdersatzIntegrationTest extends AbstractFinanzIntegrationTest {

    @Autowired
    FoerdersatzService foerdersatzService;

    @Autowired
    AbrechnungService abrechnungService;

    @Autowired
    AbrechnungRepository abrechnungRepository;

    @Autowired
    FinanzGruppeService finanzGruppeService;

    @Autowired
    FoerdersatzRepository foerdersatzRepository;

    @Autowired
    AbrechnungBelegService belegService;

    Long veranstaltungId;
    Long teilnehmerId;

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 5, 1);

    @BeforeEach
    void setup() {

        foerdersatzRepository.findAll().forEach(f ->

                System.out.println(
                        f.getTyp() + " "
                                + f.getGueltigVon() + " "
                                + f.getFoerdersatz()
                )
        );

        // Veranstaltung beginnt fix in 2026 → deterministisch
        veranstaltungId = createTestVeranstaltung(
                VeranstaltungTyp.JEM,
                TEST_DATE
        );

        createOpenAbrechnung(veranstaltungId);

        var veranstaltung = veranstaltungRepository
                .findById(veranstaltungId)
                .orElseThrow();

        teilnehmerId = createTeilnehmer(veranstaltung, null).getId();
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @Test
    void shouldCreateFoerdersatz() {

        FoerdersatzCreateUpdateDTO dto = new FoerdersatzCreateUpdateDTO();

        LocalDate start = LocalDate.now().plusYears(10);

        dto.setGueltigVon(start);

        dto.setTyp(VeranstaltungTyp.JEM);

        dto.setGueltigBis(null);
        dto.setFoerdersatz(new BigDecimal("42.00"));
        dto.setBeschluss("Beschluss 2026");

        FoerdersatzDTO created = foerdersatzService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTyp()).isEqualTo(VeranstaltungTyp.JEM);
        assertThat(created.getFoerdersatz()).isEqualByComparingTo("42.00");
    }

    /* =========================================================
       OVERLAP PREVENTION (typabhängig!)
       ========================================================= */

    @Test
    void shouldPreventOverlappingFoerdersatzSameTyp() {

        FoerdersatzCreateUpdateDTO dto1 = new FoerdersatzCreateUpdateDTO();

        LocalDate start = LocalDate.now().plusYears(10);

        dto1.setGueltigVon(start);
        dto1.setTyp(VeranstaltungTyp.FM);
        dto1.setFoerdersatz(new BigDecimal("40.00"));

        foerdersatzService.create(dto1);

        FoerdersatzCreateUpdateDTO dto2 = new FoerdersatzCreateUpdateDTO();
        dto2.setTyp(VeranstaltungTyp.FM);
        dto2.setGueltigVon(start);
        dto2.setFoerdersatz(new BigDecimal("50.00"));

        assertThatThrownBy(() ->
                foerdersatzService.create(dto2)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* =========================================================
       OVERLAP ERLAUBT BEI ANDEREM TYP
       ========================================================= */

    @Test
    void shouldAllowSamePeriodForDifferentTyp() {

        FoerdersatzCreateUpdateDTO dto1 = new FoerdersatzCreateUpdateDTO();

        LocalDate start = LocalDate.now().plusYears(10);
        dto1.setTyp(VeranstaltungTyp.FM);
        dto1.setGueltigVon(start);
        dto1.setFoerdersatz(new BigDecimal("40.00"));

        foerdersatzService.create(dto1);

        FoerdersatzCreateUpdateDTO dto2 = new FoerdersatzCreateUpdateDTO();
        dto2.setTyp(VeranstaltungTyp.BM);
        dto2.setGueltigVon(start);
        dto2.setFoerdersatz(new BigDecimal("30.00"));

        FoerdersatzDTO created = foerdersatzService.create(dto2);

        assertThat(created.getTyp()).isEqualTo(VeranstaltungTyp.BM);
    }

    /* =========================================================
       FIND VALID RATE
       ========================================================= */

    @Test
    void shouldFindValidFoerdersatzForDate() {

        FoerdersatzCreateUpdateDTO dto = new FoerdersatzCreateUpdateDTO();
        dto.setTyp(VeranstaltungTyp.FM);
        dto.setGueltigVon(LocalDate.of(2031,1,1));
        dto.setGueltigBis(LocalDate.of(2031,12,31));
        dto.setFoerdersatz(new BigDecimal("55.00"));

        foerdersatzService.create(dto);

        Foerdersatz entity =
                foerdersatzService.findEntityGueltigFuerTypAm(
                        VeranstaltungTyp.FM,
                        LocalDate.of(2031,5,1)
                );

        assertThat(entity.getFoerdersatz())
                .isEqualByComparingTo("55.00");
    }

    /* =========================================================
       SNAPSHOT ON ABSCHLIESSEN
       ========================================================= */

    @Test
    void shouldStoreFoerdersatzSnapshotWhenClosing() {

        // GIVEN
        // Aktuell gültigen Fördersatz für den Veranstaltungsbeginn ermitteln
        Foerdersatz erwarteterFoerdersatz =
                foerdersatzService.findEntityGueltigFuerTypAm(
                        VeranstaltungTyp.JEM,
                        TEST_DATE
                );

        addPosition(FinanzKategorie.UNTERKUNFT, "100.00");
        addPosition(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

        // WHEN
        abrechnungService.abschliessen(veranstaltungId);

        // THEN
        Abrechnung abrechnung = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow();

        assertThat(abrechnung.getVerwendeterFoerdersatz())
                .isEqualByComparingTo(
                        erwarteterFoerdersatz.getFoerdersatz()
                );
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private void addPosition(FinanzKategorie kat, String betrag) {

        // 🔥 Erst Kürzel vergeben
        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "X1"
        );

        AbrechnungBelegCreateDTO belegDTO = new AbrechnungBelegCreateDTO();
        belegDTO.setDatum(TEST_DATE);
        belegDTO.setBeschreibung("Test");
        belegDTO.setKuerzel("X1");   // 🔥 NEU

        AbrechnungBelegDTO beleg =
                belegService.createBeleg(veranstaltungId, belegDTO);

        AbrechnungBuchungCreateDTO posDTO =
                new AbrechnungBuchungCreateDTO();

        posDTO.setKategorie(kat);
        posDTO.setBetrag(new BigDecimal(betrag));

        belegService.addPosition(
                veranstaltungId,
                beleg.getId(),
                posDTO
        );
    }
}