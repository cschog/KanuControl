package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungUpdateDTO;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.support.data.PersonTestFactory;
import com.kcserver.support.data.VeranstaltungTestFactory;
import com.kcserver.support.data.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class VeranstaltungUpdateTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    VeranstaltungRepository veranstaltungRepository;

    VeranstaltungTestFactory veranstaltungFactory;

    Long veranstaltungId;
    Long vereinId;
    Long leiterId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());
        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        veranstaltungFactory =
                new VeranstaltungTestFactory(mockMvc, objectMapper, tenantAuth());

        vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.createOrReuse(
                "Max",
                "Mustermann",
                LocalDate.of(1990, 1, 1),
                null
        );

        // 🔹 CREATE erste Veranstaltung
        VeranstaltungCreateDTO createDTO = new VeranstaltungCreateDTO();
        createDTO.setName("Sommerfreizeit");
        createDTO.setTyp(VeranstaltungTyp.JEM);
        createDTO.setVereinId(vereinId);
        createDTO.setLeiterId(leiterId);
        createDTO.setBeginnDatum(LocalDate.now().plusDays(5));
        createDTO.setBeginnZeit(LocalTime.of(10, 0));
        createDTO.setEndeDatum(LocalDate.now().plusDays(10));
        createDTO.setEndeZeit(LocalTime.of(18, 0));

        String json = mockMvc.perform(
                        tenantRequest(
                                post("/api/veranstaltungen")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createDTO))
                        )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        veranstaltungId =
                objectMapper.readTree(json).get("id").asLong();
    }

    /* =========================================================
       UPDATE (partial)
       ========================================================= */

    @Test
    void updateVeranstaltung_updatesNameAndDates() throws Exception {

        VeranstaltungUpdateDTO updateDTO = new VeranstaltungUpdateDTO();
        updateDTO.setName("Sommerfreizeit 2026");
        updateDTO.setEndeDatum(LocalDate.now().plusDays(12));

        mockMvc.perform(
                        tenantRequest(
                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateDTO))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sommerfreizeit 2026"))
                .andExpect(jsonPath("$.endeDatum").exists());
    }

    /* =========================================================
       ACTIVATE (exklusiv)
       ========================================================= */

    @Test
    void activateVeranstaltung_deactivatesOtherOnes() throws Exception {

        Long firstId  = veranstaltungFactory.create(vereinId, leiterId, "Sommer");
        Long secondId = veranstaltungFactory.create(vereinId, leiterId, "Herbst");

        mockMvc.perform(
                        tenantRequest(get("/api/veranstaltungen/active"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(secondId));
    }
    @Test
    void shouldUpdateName() throws Exception {

        VeranstaltungUpdateDTO dto = new VeranstaltungUpdateDTO();
        dto.setName("Neue Sommerfreizeit");

        mockMvc.perform(
                        tenantRequest(
                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neue Sommerfreizeit"));
    }

    @Test
    void shouldChangeLeiter() throws Exception {

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        Long newLeiterId = personen.createOrReuse(
                "Anna",
                "Leiterin",
                LocalDate.of(1992, 1, 1),
                null
        );

        VeranstaltungUpdateDTO dto = new VeranstaltungUpdateDTO();
        dto.setLeiterId(newLeiterId);

        mockMvc.perform(
                        tenantRequest(
                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leiterId").value(newLeiterId));
    }
    @Test
    void shouldChangeVerein() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        Long newVereinId = vereine.createIfNotExists(
                "KSC",
                "Kanu Sport Club"
        );

        VeranstaltungUpdateDTO dto = new VeranstaltungUpdateDTO();
        dto.setVereinId(newVereinId);

        mockMvc.perform(
                        tenantRequest(
                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vereinId").value(newVereinId));
    }
    @Test
    void shouldFailInvalidLeiterAge() throws Exception {

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        // 12 Jahre alt → ungültig
        Long tooYoungId = personen.createOrReuse(
                "Tim",
                "ZuJung",
                LocalDate.now().minusYears(12),
                null
        );

        VeranstaltungUpdateDTO dto = new VeranstaltungUpdateDTO();
        dto.setLeiterId(tooYoungId);

        mockMvc.perform(
                        tenantRequest(
                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                )
                .andExpect(status().isBadRequest());
    }

}