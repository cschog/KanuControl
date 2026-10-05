package com.kcserver.unit;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.entity.Unterkunftsart;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Verpflegungsmodell;
import com.kcserver.entity.beitraege.Beitragsregel;
import com.kcserver.entity.beitraege.Beitragsstruktur;
import com.kcserver.enumtype.PlanungsStatus;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.repository.UnterkunftsartRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.VerpflegungsmodellRepository;
import com.kcserver.repository.beitrag.BeitragsstrukturRepository;
import com.kcserver.service.planung.PlanungService;
import com.kcserver.service.simulation.SimulationFacade;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class PlanungServiceTest extends AbstractTenantIntegrationTest {

    @Autowired
    private PlanungService planungService;

    @Autowired
    private VeranstaltungRepository veranstaltungRepository;

    @Autowired
    private SimulationFacade simulationFacade;

    @Autowired
    private UnterkunftsartRepository unterkunftsartRepository;

    @Autowired
    private VerpflegungsmodellRepository verpflegungsmodellRepository;

    @Autowired
    private BeitragsstrukturRepository beitragsstrukturRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private Long veranstaltungId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereinFactory =
                new VereinTestFactory(mockMvc, objectMapper);

        PersonTestFactory personFactory =
                new PersonTestFactory(mockMvc, objectMapper);

        VeranstaltungTestFactory veranstaltungFactory =
                new VeranstaltungTestFactory(mockMvc, objectMapper);

        Long vereinId =
                vereinFactory.create("TV", "Testverein");

        Long leiterId =
                personFactory.createWithVerein(
                        vereinId,
                        b -> b.withVorname("Max")
                                .withName("Mustermann")
                                .withGeburtsdatum(LocalDate.of(1990, 1, 1))
                );

        veranstaltungId =
                veranstaltungFactory.create(
                        vereinId,
                        leiterId,
                        "Test Planung"
                );
    }

    @Test
    void shouldSubmitPlanung() {

        createValidPlanung();


        planungService.einreichen(veranstaltungId);

        assertThat(planungService.get(veranstaltungId).getStatus())
                .isEqualTo(PlanungsStatus.EINGEREICHT);
    }

    @Test
    void shouldRejectAlreadySubmittedPlanung() {

        createValidPlanung();

        planungService.einreichen(veranstaltungId);

        assertThatThrownBy(() ->
                planungService.einreichen(veranstaltungId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("bereits eingereicht");
    }

    @Test
    void shouldReopenPlanung() {

        createValidPlanung();

        planungService.einreichen(veranstaltungId);

        planungService.wiederOeffnen(veranstaltungId);

        assertThat(planungService.get(veranstaltungId).getStatus())
                .isEqualTo(PlanungsStatus.IN_BEARBEITUNG);
    }

    @Test
    void shouldReturn404WhenPlanungDoesNotExist() {

        assertThatThrownBy(() ->
                planungService.get(veranstaltungId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("keine Planung");
    }

    @Test
    void shouldRejectSubmitWithoutPlanung() {

        assertThatThrownBy(() ->
                planungService.einreichen(veranstaltungId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Planung nicht gefunden");
    }

    private void createValidPlanung() {

        Unterkunftsart unterkunftsart =
                unterkunftsartRepository.save(
                        Unterkunftsart.builder()
                                .bezeichnung("Test Unterkunft")
                                .preisProPersonUndNacht(new BigDecimal("20"))
                                .aktiv(true)
                                .build()
                );

        Verpflegungsmodell verpflegungsmodell =
                verpflegungsmodellRepository.save(
                        Verpflegungsmodell.builder()
                                .bezeichnung("Test Verpflegung")
                                .preisProPersonUndTag(new BigDecimal("12"))
                                .aktiv(true)
                                .build()
                );

        Beitragsstruktur beitragsstruktur = new Beitragsstruktur();
        beitragsstruktur.setName("Test Beitragsstruktur");
        beitragsstruktur.setAktiv(true);
        beitragsstruktur.setTemplate(false);
        beitragsstruktur.setSystem(false);

        Beitragsregel regel = new Beitragsregel();
        regel.setSortierung(1);
        regel.setAlterBis(18);
        regel.setRolle(TeilnehmerRolle.MITARBEITER);
        regel.setBeitrag(new BigDecimal("100"));
        regel.setStruktur(beitragsstruktur);

        beitragsstruktur.setRegeln(List.of(regel));

        beitragsstruktur =
                beitragsstrukturRepository.save(beitragsstruktur);

        Veranstaltung veranstaltung =
                veranstaltungRepository.findById(veranstaltungId)
                        .orElseThrow();

        veranstaltung.setUnterkunftsart(unterkunftsart);
        veranstaltung.setVerpflegungsmodell(verpflegungsmodell);
        veranstaltung.setBeitragsstruktur(beitragsstruktur);

        veranstaltungRepository.save(veranstaltung);

        PlanungsSimulation simulation =
                simulationFacade.getSimulation(veranstaltungId);

        simulation.setTeilnehmer(20);
        simulation.setMitarbeiter(4);

        simulation.setUnterkunftPreisProPersonUndNacht(
                new BigDecimal("20"));

        simulation.setVerpflegungPreisProPersonUndTag(
                new BigDecimal("12"));

        simulationFacade.saveSimulation(
                veranstaltungId,
                simulation
        );
    }
}