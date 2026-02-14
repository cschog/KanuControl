package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("veranstaltung-active")
class VeranstaltungActiveTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;
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

        vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.createOrReuse(
                "Max",
                "Mustermann",
                LocalDate.of(1990, 1, 1),
                null
        );
    }

    /* =========================================================
       TEST 1 — shouldReturnSingleActive
       ========================================================= */

    @Test
    void shouldReturnSingleActive() throws Exception {

        createVeranstaltung("V1");

        String json = mockMvc.perform(
                        tenantRequest(get("/api/veranstaltung/active"))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        boolean aktiv = objectMapper.readTree(json).get("aktiv").asBoolean();

        assertThat(aktiv).isTrue();
    }

    /* =========================================================
       TEST 2 — shouldSwitchActive
       ========================================================= */

    @Test
    void shouldSwitchActive() throws Exception {

        Long v1 = createVeranstaltung("V1");
        Long v2 = createVeranstaltung("V2"); // muss automatisch aktiv werden

        var first = veranstaltungRepository.findById(v1).orElseThrow();
        var second = veranstaltungRepository.findById(v2).orElseThrow();

        assertThat(first.isAktiv()).isFalse();
        assertThat(second.isAktiv()).isTrue();
    }

    /* =========================================================
       TEST 3 — shouldEnforceSingleActiveDBConstraint
       ========================================================= */

    @Test
    void shouldEnforceSingleActiveDBConstraint() throws Exception {

        createVeranstaltung("V1");

        // zweites Create darf nicht 2 aktive erzeugen
        createVeranstaltung("V2");

        long activeCount = veranstaltungRepository.findAll()
                .stream()
                .filter(v -> v.isAktiv())
                .count();

        assertThat(activeCount).isEqualTo(1);
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private Long createVeranstaltung(String name) throws Exception {

        VeranstaltungDetailDTO dto = new VeranstaltungDetailDTO();
        dto.setName(name);
        dto.setTyp(VeranstaltungTyp.JEM);

        dto.setVereinId(vereinId);
        dto.setLeiterId(leiterId);

        dto.setBeginnDatum(LocalDate.now().plusDays(5));
        dto.setEndeDatum(LocalDate.now().plusDays(10));
        dto.setBeginnZeit(LocalTime.of(10, 0));
        dto.setEndeZeit(LocalTime.of(18, 0));

        String json = mockMvc.perform(
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

        return objectMapper.readTree(json).get("id").asLong();
    }
}