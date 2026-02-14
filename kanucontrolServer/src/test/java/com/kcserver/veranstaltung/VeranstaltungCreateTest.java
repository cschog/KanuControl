package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.testdata.PersonTestFactory;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("veranstaltung-crud")
class VeranstaltungCreateTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired TeilnehmerRepository teilnehmerRepository;
    @Autowired VeranstaltungRepository veranstaltungRepository;

    Long vereinId;
    Long leiterId;

    /* =========================================================
       SETUP
       ========================================================= */

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        vereinId = vereine.createIfNotExists(
                "EKC",
                "Eschweiler Kanu Club"
        );

        leiterId = personen.createOrReuse(
                "Max",
                "Mustermann",
                LocalDate.of(1990, 1, 1),
                null
        );
    }

    /* =========================================================
       TEST 1 — shouldCreateVeranstaltung
       ========================================================= */

    @Test
    void shouldCreateVeranstaltung() throws Exception {

        String json = createValidVeranstaltung("Sommerfreizeit 2026");

        Long veranstaltungId =
                objectMapper.readTree(json).get("id").asLong();

        assertThat(veranstaltungRepository.findById(veranstaltungId)).isPresent();
    }

    /* =========================================================
       TEST 2 — shouldFailMissingFields
       ========================================================= */

    @Test
    void shouldFailMissingFields() throws Exception {

        VeranstaltungCreateDTO dto = new VeranstaltungCreateDTO(); // leer

        mockMvc.perform(
                tenantRequest(
                        post("/api/veranstaltung")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
        ).andExpect(status().isBadRequest());
    }

    /* =========================================================
       TEST 3 — shouldSetActiveAutomatically
       ========================================================= */

    @Test
    void shouldSetActiveAutomatically() throws Exception {

        String json = createValidVeranstaltung("Auto Active Test");

        boolean aktiv =
                objectMapper.readTree(json).get("aktiv").asBoolean();

        assertThat(aktiv).isTrue();
    }

    /* =========================================================
       TEST 4 — shouldSwitchActiveOnSecondCreate
       ========================================================= */

    @Test
    void shouldSwitchActiveOnSecondCreate() throws Exception {

        createValidVeranstaltung("V1");
        createValidVeranstaltung("V2");

        long activeCount = veranstaltungRepository
                .findAll()
                .stream()
                .filter(v -> v.isAktiv())
                .count();

        assertThat(activeCount).isEqualTo(1);
    }

    /* =========================================================
       BONUS — Leiter wird Teilnehmer
       ========================================================= */

    @Test
    void shouldCreateLeiterTeilnehmerAutomatically() throws Exception {

        String json = createValidVeranstaltung("Leiter Auto Teilnehmer");

        Long veranstaltungId =
                objectMapper.readTree(json).get("id").asLong();

        var teilnehmer = teilnehmerRepository
                .findByVeranstaltungIdAndPersonId(veranstaltungId, leiterId)
                .orElseThrow();

        assertThat(teilnehmer.getRolle())
                .isEqualTo(TeilnehmerRolle.LEITER);
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private String createValidVeranstaltung(String name) throws Exception {

        VeranstaltungCreateDTO dto = new VeranstaltungCreateDTO();
        dto.setName(name);
        dto.setTyp(VeranstaltungTyp.JEM);

        dto.setVereinId(vereinId);
        dto.setLeiterId(leiterId);

        dto.setBeginnDatum(LocalDate.now().plusDays(10));
        dto.setEndeDatum(LocalDate.now().plusDays(20));
        dto.setBeginnZeit(LocalTime.of(10, 0));
        dto.setEndeZeit(LocalTime.of(18, 0));

        return mockMvc.perform(
                        tenantRequest(
                                post("/api/veranstaltung")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}