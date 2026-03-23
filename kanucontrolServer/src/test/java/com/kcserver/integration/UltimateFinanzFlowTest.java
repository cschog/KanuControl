package com.kcserver.integration;

import com.kcserver.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.dto.planung.PlanungPositionCreateDTO;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.finanz.AbrechnungBelegService;
import com.kcserver.finanz.FinanzGruppeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UltimateFinanzFlowTest extends AbstractFinanzIntegrationTest {

    Long veranstaltungId;
    Long teilnehmerId;

    @Autowired
    AbrechnungBelegService belegService;

    @Autowired
    FinanzGruppeService finanzGruppeService;

    @BeforeEach
    void setup() {

        veranstaltungId = createTestVeranstaltung();

        Teilnehmer teilnehmer = createTeilnehmer(
                veranstaltungRepository.findById(veranstaltungId).orElseThrow(),
                null
        );

        this.teilnehmerId = teilnehmer.getId();

        createOpenAbrechnung(veranstaltungId);

        // ✅ Kürzel einmalig vergeben
        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "FLOW1"
        );
    }

    @Test
    void shouldRunCompleteFinancialFlowSuccessfully() {

        /* =========================================================
           PLANUNG
           ========================================================= */

        PlanungPositionCreateDTO kosten = new PlanungPositionCreateDTO();
        kosten.setKategorie(FinanzKategorie.UNTERKUNFT);
        kosten.setBetrag(new BigDecimal("200"));

        PlanungPositionCreateDTO zuschuss = new PlanungPositionCreateDTO();
        zuschuss.setKategorie(FinanzKategorie.KJFP_ZUSCHUSS);
        zuschuss.setBetrag(new BigDecimal("200"));

        planungPositionService.addPosition(veranstaltungId, kosten);
        planungPositionService.addPosition(veranstaltungId, zuschuss);

        planungService.einreichen(veranstaltungId);

        /* =========================================================
           ABRECHNUNG (Beleg + Position)
           ========================================================= */

        AbrechnungBelegCreateDTO belegDTO = new AbrechnungBelegCreateDTO();
        belegDTO.setDatum(LocalDate.now());
        belegDTO.setBeschreibung("Test-Beleg");
        belegDTO.setKuerzel("FLOW1");

        AbrechnungBelegDTO beleg =
                belegService.createBeleg(veranstaltungId, belegDTO);

        belegService.addPosition(
                veranstaltungId,
                beleg.getId(),
                createPosition(FinanzKategorie.UNTERKUNFT, "200")
        );

        belegService.addPosition(
                veranstaltungId,
                beleg.getId(),
                createPosition(FinanzKategorie.KJFP_ZUSCHUSS, "200")
        );

        /* =========================================================
           ABSCHLIESSEN
           ========================================================= */

        abrechnungService.abschliessen(veranstaltungId);

        assertThat(
                abrechnungService.getOrCreate(veranstaltungId)
                        .getStatus()
                        .name()
        ).isEqualTo("ABGESCHLOSSEN");
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private AbrechnungBuchungCreateDTO createPosition(
            FinanzKategorie kat,
            String betrag
    ) {

        AbrechnungBuchungCreateDTO dto = new AbrechnungBuchungCreateDTO();

        dto.setTeilnehmerId(teilnehmerId);
        dto.setKategorie(kat);
        dto.setBetrag(new BigDecimal(betrag));
        dto.setDatum(LocalDate.now());

        return dto;
    }
}