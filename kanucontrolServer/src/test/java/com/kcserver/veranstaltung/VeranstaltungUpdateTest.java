package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungUpdateDTO;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;

import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.api.VereinTestFactory;
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

    VeranstaltungTestFactory veranstaltungFactory;

    Long veranstaltungId;
    Long vereinId;
    Long leiterId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper);
        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);

        veranstaltungFactory =
                new VeranstaltungTestFactory(mockMvc, objectMapper);

        vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.create(b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(java.time.LocalDate.of(1990, 1, 1))
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

                                post("/api/veranstaltungen")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createDTO))

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

                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateDTO))

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
                        get("/api/veranstaltungen/aktiv"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(secondId));
    }
    @Test
    void shouldUpdateName() throws Exception {

        VeranstaltungUpdateDTO dto = new VeranstaltungUpdateDTO();
        dto.setName("Neue Sommerfreizeit");

        mockMvc.perform(

                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))

                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neue Sommerfreizeit"));
    }

    @Test
    void shouldChangeLeiter() throws Exception {

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);

        Long newLeiterId = personen.create(b ->
                b.withVorname("Anna")
                        .withName("Leiterin")
                        .withGeburtsdatum(java.time.LocalDate.of(1992, 1, 1))
        );

        VeranstaltungUpdateDTO dto = new VeranstaltungUpdateDTO();
        dto.setLeiterId(newLeiterId);

        mockMvc.perform(

                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))

                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leiterId").value(newLeiterId));
    }
    @Test
    void shouldChangeVerein() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper);

        Long newVereinId = vereine.createIfNotExists(
                "KSC",
                "Kanu Sport Club"
        );

        VeranstaltungUpdateDTO dto = new VeranstaltungUpdateDTO();
        dto.setVereinId(newVereinId);

        mockMvc.perform(

                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))

                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vereinId").value(newVereinId));
    }
    @Test
    void shouldFailInvalidLeiterAge() throws Exception {

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);

        // 12 Jahre alt → ungültig


        Long tooYoungId = personen.create(b ->
                b.withVorname("Tim")
                        .withName("ZuJung")
                        .withGeburtsdatum(java.time.LocalDate.now().minusYears(12))
        );

        VeranstaltungUpdateDTO dto = new VeranstaltungUpdateDTO();
        dto.setLeiterId(tooYoungId);

        mockMvc.perform(

                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))

                )
                .andExpect(status().isBadRequest());
    }

}