package com.kcserver.finanz;

import com.kcserver.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.entity.Abrechnung;
import com.kcserver.enumtype.AbrechnungsStatus;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.repository.AbrechnungRepository;
import com.kcserver.testsupport.TestDataFactory;
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
class AbrechnungServiceTest extends AbstractTenantIntegrationTest {

    @Autowired
    AbrechnungService abrechnungService;

    @Autowired
    FinanzGruppeService finanzGruppeService;

    @Autowired
    AbrechnungBelegService belegService;

    @Autowired
    AbrechnungRepository abrechnungRepository;

    @Autowired
    TestDataFactory factory;

    Long veranstaltungId;
    Long teilnehmerId;

    /* ========================================================= */

    @BeforeEach
    void setup() {

        veranstaltungId = factory.createTestVeranstaltung();
        teilnehmerId = factory.createTeilnehmer(veranstaltungId);

        abrechnungService.getOrCreate(veranstaltungId);

        Abrechnung a = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow();

        a.setStatus(AbrechnungsStatus.OFFEN);
        abrechnungRepository.save(a);
    }

    /* ========================================================= */

    @Test
    void shouldCloseBalancedAbrechnung() {

        addPosition(FinanzKategorie.UNTERKUNFT, "100.00");
        addPosition(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

        abrechnungService.abschliessen(veranstaltungId);

        Abrechnung a = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow();

        assertThat(a.getStatus())
                .isEqualTo(AbrechnungsStatus.ABGESCHLOSSEN);
    }

    /* ========================================================= */

    @Test
    void shouldPreventCloseWhenNotBalanced() {

        addPosition(FinanzKategorie.UNTERKUNFT, "100.00");

        assertThatThrownBy(() ->
                abrechnungService.abschliessen(veranstaltungId)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* ========================================================= */

    @Test
    void shouldPreventDoubleClose() {

        addPosition(FinanzKategorie.UNTERKUNFT, "100.00");
        addPosition(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

        abrechnungService.abschliessen(veranstaltungId);

        assertThatThrownBy(() ->
                abrechnungService.abschliessen(veranstaltungId)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* ========================================================= */

    @Test
    void shouldNotAllowChangesWhenClosed() {

        addPosition(FinanzKategorie.UNTERKUNFT, "100.00");
        addPosition(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

        abrechnungService.abschliessen(veranstaltungId);

        AbrechnungBelegCreateDTO belegDTO = new AbrechnungBelegCreateDTO();
        belegDTO.setBelegnummer("TEST-2");
        belegDTO.setDatum(LocalDate.now());
        belegDTO.setKuerzel("X1");

        // 🔥 Bereits Beleg-Erstellung muss scheitern
        assertThatThrownBy(() ->
                belegService.createBeleg(veranstaltungId, belegDTO)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* ========================================================= */

    private void addPosition(FinanzKategorie kategorie, String betrag) {

        // 🔥 Erst Kürzel vergeben
        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "X1"
        );

        AbrechnungBelegCreateDTO belegDTO = new AbrechnungBelegCreateDTO();
        belegDTO.setBelegnummer("TEST");
        belegDTO.setDatum(LocalDate.now());
        belegDTO.setKuerzel("X1"); // 🔥 WICHTIG

        AbrechnungBelegDTO beleg =
                belegService.createBeleg(veranstaltungId, belegDTO);

        AbrechnungBuchungCreateDTO posDTO =
                new AbrechnungBuchungCreateDTO();

        posDTO.setTeilnehmerId(teilnehmerId);
        posDTO.setKategorie(kategorie);
        posDTO.setBetrag(new BigDecimal(betrag));
        posDTO.setDatum(LocalDate.now());

        belegService.addPosition(
                veranstaltungId,
                beleg.getId(),
                posDTO
        );
    }
}