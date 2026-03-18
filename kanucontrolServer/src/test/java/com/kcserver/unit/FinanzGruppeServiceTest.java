package com.kcserver.finanz;

import com.kcserver.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.repository.FinanzGruppeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FinanzGruppeServiceTest extends AbstractFinanzIntegrationTest {

    @Autowired
    FinanzGruppeService finanzGruppeService;

    @Autowired
    AbrechnungBelegService belegService;

    @Autowired
    FinanzGruppeRepository finanzGruppeRepository;

    Long veranstaltungId;
    Long teilnehmerId;

    @BeforeEach
    void setup() {

        veranstaltungId = createTestVeranstaltung();

        Teilnehmer teilnehmer = createTeilnehmer(
                veranstaltungRepository
                        .findById(veranstaltungId)
                        .orElseThrow(),
                null
        );

        teilnehmerId = teilnehmer.getId();
    }

    @Test
    void shouldCreateGroup() {

        FinanzGruppe g =
                finanzGruppeService.create(veranstaltungId, "MS");

        assertThat(g.getId()).isNotNull();
        assertThat(g.getKuerzel()).isEqualTo("MS");
    }

    @Test
    void shouldNotAllowDuplicate() {

        finanzGruppeService.create(veranstaltungId, "MS");

        assertThatThrownBy(() ->
                finanzGruppeService.create(veranstaltungId, "MS"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldUpdateGroup() {

        FinanzGruppe g =
                finanzGruppeService.create(veranstaltungId, "MS");

        FinanzGruppe updated =
                finanzGruppeService.update(veranstaltungId, g.getId(), "CS");

        assertThat(updated.getKuerzel()).isEqualTo("CS");
    }

    @Test
    void shouldDeleteGroup() {

        FinanzGruppe g =
                finanzGruppeService.create(veranstaltungId, "MS");

        finanzGruppeService.delete(veranstaltungId, g.getId());

        assertThatThrownBy(() ->
                finanzGruppeService.delete(veranstaltungId, g.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }
    @Test
    void shouldAllowChangingKuerzelIfNoBookingsExist() {

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "A1"
        );

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "B1"
        );

        assertThat(true).isTrue();
    }

    @Test
    void shouldAllowMultipleParticipantsWithSameKuerzel() {

        Long teilnehmer2 = createTeilnehmer(
                veranstaltungRepository.findById(veranstaltungId).orElseThrow(),
                null
        ).getId();

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "A1"
        );

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmer2,
                "A1"
        );

        assertThat(true).isTrue();
    }

    @Test
    void shouldPreventChangingKuerzelAfterBookingExists() {

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "A1"
        );

        createBelegMitBuchung();

        assertThatThrownBy(() ->
                finanzGruppeService.assignKuerzel(
                        veranstaltungId,
                        teilnehmerId,
                        "B1"
                )
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldAllowChangingKuerzelIfNoBelegeExist() {

        FinanzGruppe gruppe = finanzGruppeService.create(
                veranstaltungId,
                "A1"
        );

        finanzGruppeService.update(
                veranstaltungId,
                gruppe.getId(),
                "B1"
        );

        assertThat(gruppe.getKuerzel()).isEqualTo("B1");
    }

    @Test
    void shouldPreventChangingKuerzelIfBelegeExist() {

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "A1"
        );

        createBelegMitBuchung(); // helper

        FinanzGruppe gruppe =
                finanzGruppeRepository
                        .findByVeranstaltungIdAndKuerzel(
                                veranstaltungId, "A1")
                        .orElseThrow();

        assertThatThrownBy(() ->
                finanzGruppeService.update(
                        veranstaltungId,
                        gruppe.getId(),
                        "B1"
                )
        ).isInstanceOf(ResponseStatusException.class);
    }

    private void createBelegMitBuchung() {

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "A1"
        );

        createOpenAbrechnung(veranstaltungId);

        AbrechnungBelegCreateDTO belegDTO = new AbrechnungBelegCreateDTO();
        belegDTO.setBelegnummer("T1");
        belegDTO.setDatum(LocalDate.now());
        belegDTO.setKuerzel("A1"); // 🔥 WICHTIG

        AbrechnungBelegDTO beleg =
                belegService.createBeleg(veranstaltungId, belegDTO);

        AbrechnungBuchungCreateDTO posDTO =
                new AbrechnungBuchungCreateDTO();

        posDTO.setTeilnehmerId(teilnehmerId);
        posDTO.setKategorie(FinanzKategorie.UNTERKUNFT);
        posDTO.setBetrag(new BigDecimal("100"));
        posDTO.setDatum(LocalDate.now());

        belegService.addPosition(
                veranstaltungId,
                beleg.getId(),
                posDTO
        );
    }
}
