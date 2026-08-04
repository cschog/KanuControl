package com.kcserver.unit;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.enumtype.PlanungsStatus;
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

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class PlanungServiceTest extends AbstractTenantIntegrationTest {

    @Autowired
    private PlanungService planungService;

    @Autowired
    private SimulationFacade simulationFacade;

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