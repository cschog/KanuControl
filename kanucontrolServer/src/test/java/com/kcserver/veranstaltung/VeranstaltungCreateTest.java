package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.repository.TeilnehmerRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("veranstaltung-crud")
class VeranstaltungCreateTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TeilnehmerRepository teilnehmerRepository;

    Long vereinId;
    Long leiterId;

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
       TEST
       ========================================================= */

    @Test
    void createVeranstaltung_setsActiveAndCreatesLeiterTeilnehmer() throws Exception {

        VeranstaltungDetailDTO dto = new VeranstaltungDetailDTO();
        dto.setName("Sommerfreizeit 2026");
        dto.setTyp(VeranstaltungTyp.JUGENDERHOLUNGSMASSNAHME);

        dto.setVereinId(vereinId);
        dto.setLeiterId(leiterId);

        dto.setBeginnDatum(LocalDate.now().plusDays(10));
        dto.setEndeDatum(LocalDate.now().plusDays(20));
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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Sommerfreizeit 2026"))
                .andExpect(jsonPath("$.aktiv").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long veranstaltungId =
                objectMapper.readTree(json).get("id").asLong();

        // 🔍 Leiter wurde automatisch als Teilnehmer angelegt
        var teilnehmer = teilnehmerRepository
                .findByVeranstaltungIdAndPersonId(veranstaltungId, leiterId)
                .orElseThrow();

        // 🔑 Leiter-Rolle korrekt gesetzt
        assert teilnehmer.getRolle() == TeilnehmerRolle.LEITER;
    }
}