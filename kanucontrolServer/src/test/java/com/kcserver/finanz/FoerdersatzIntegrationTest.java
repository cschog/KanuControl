package com.kcserver.finanz;

import com.kcserver.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO;
import com.kcserver.dto.foerder.FoerdersatzDTO;
import com.kcserver.entity.Abrechnung;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.repository.AbrechnungRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    AbrechnungBelegService belegService;

    Long veranstaltungId;
    Long teilnehmerId;

    @BeforeEach
    void setup() {
        veranstaltungId = createTestVeranstaltung();
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
        dto.setGueltigVon(LocalDate.of(2026, 1, 1));
        dto.setGueltigBis(null);
        dto.setBetragProTeilnehmer(new BigDecimal("42.00"));
        dto.setBeschluss("Beschluss 2026");

        FoerdersatzDTO created = foerdersatzService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getBetragProTeilnehmer())
                .isEqualByComparingTo("42.00");
    }

    /* =========================================================
       OVERLAP PREVENTION
       ========================================================= */

    @Test
    void shouldPreventOverlappingFoerdersatz() {

        FoerdersatzCreateUpdateDTO dto1 = new FoerdersatzCreateUpdateDTO();
        dto1.setGueltigVon(LocalDate.of(2026, 1, 1));
        dto1.setGueltigBis(null);
        dto1.setBetragProTeilnehmer(new BigDecimal("40.00"));

        foerdersatzService.create(dto1);

        FoerdersatzCreateUpdateDTO dto2 = new FoerdersatzCreateUpdateDTO();
        dto2.setGueltigVon(LocalDate.of(2026, 6, 1));
        dto2.setGueltigBis(null);
        dto2.setBetragProTeilnehmer(new BigDecimal("50.00"));

        assertThatThrownBy(() ->
                foerdersatzService.create(dto2)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* =========================================================
       FIND VALID RATE
       ========================================================= */

    @Test
    void shouldFindValidFoerdersatzForDate() {

        FoerdersatzCreateUpdateDTO dto = new FoerdersatzCreateUpdateDTO();
        dto.setGueltigVon(LocalDate.of(2026, 1, 1));
        dto.setGueltigBis(LocalDate.of(2026, 12, 31));
        dto.setBetragProTeilnehmer(new BigDecimal("55.00"));

        foerdersatzService.create(dto);

        FoerdersatzDTO found =
                foerdersatzService.findGueltigAm(LocalDate.of(2026, 5, 1));

        assertThat(found.getBetragProTeilnehmer())
                .isEqualByComparingTo("55.00");
    }

    /* =========================================================
       SNAPSHOT ON ABSCHLIESSEN
       ========================================================= */

    @Test
    void shouldStoreFoerdersatzSnapshotWhenClosing() {

        // Fördersatz anlegen
        FoerdersatzCreateUpdateDTO dto = new FoerdersatzCreateUpdateDTO();
        dto.setGueltigVon(LocalDate.of(2026, 1, 1));
        dto.setGueltigBis(null);
        dto.setBetragProTeilnehmer(new BigDecimal("60.00"));

        foerdersatzService.create(dto);

        // Abrechnung ausgleichen
        addPosition(FinanzKategorie.UNTERKUNFT, "100.00");
        addPosition(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

        // Abschließen
        abrechnungService.abschliessen(veranstaltungId);

        Abrechnung a = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow();

        assertThat(a.getVerwendeterFoerdersatz())
                .isEqualByComparingTo("60.00");
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private void addPosition(FinanzKategorie kat, String betrag) {

        AbrechnungBelegCreateDTO belegDTO = new AbrechnungBelegCreateDTO();
        belegDTO.setBelegnummer("FS-TEST");
        belegDTO.setDatum(LocalDate.now());
        belegDTO.setBeschreibung("Test");

        AbrechnungBelegDTO beleg =
                belegService.createBeleg(veranstaltungId, belegDTO);

        AbrechnungBuchungCreateDTO posDTO =
                new AbrechnungBuchungCreateDTO();

        posDTO.setTeilnehmerId(teilnehmerId);
        posDTO.setKuerzel("X1");
        posDTO.setKategorie(kat);
        posDTO.setBetrag(new BigDecimal(betrag));
        posDTO.setDatum(LocalDate.now());

        belegService.addPosition(
                veranstaltungId,
                beleg.getId(),
                posDTO
        );
    }
}