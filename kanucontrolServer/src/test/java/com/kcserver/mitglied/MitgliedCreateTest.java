package com.kcserver.mitglied;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.enumtype.MitgliedFunktion;
import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("mitglied-crud")
class MitgliedCreateTest extends AbstractIntegrationTest {

    Long personId;
    Long verein1Id;
    Long verein2Id;

    @BeforeEach
    void setup() throws Exception {
        personId = createPerson("Max", "Mustermann");
        verein1Id = createVerein("Eschweiler Kanu Club", "EKC");
        verein2Id = createVerein("Oberhausener Kanu Club", "OKC");
    }

    /* =========================================================
       ✅ HAPPY PATH
       ========================================================= */

    @Test
    void createMitglied_valid_returns201() throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(verein1Id);
        dto.setHauptVerein(true);
        dto.setFunktion(MitgliedFunktion.JUGENDWART);

        mockMvc.perform(
                        post("/api/mitglied")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.personId").value(personId))
                .andExpect(jsonPath("$.vereinId").value(verein1Id))
                .andExpect(jsonPath("$.hauptVerein").value(true));
    }

    /* =========================================================
       ❌ REFERENZFEHLER
       ========================================================= */

    @Test
    void createMitglied_personNotFound_returns404() throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(99999L);
        dto.setVereinId(verein1Id);
        dto.setHauptVerein(true);

        mockMvc.perform(
                        post("/api/mitglied")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void createMitglied_vereinNotFound_returns404() throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(99999L);
        dto.setHauptVerein(true);

        mockMvc.perform(
                        post("/api/mitglied")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    /* =========================================================
       ❌ DUPLIKATE
       ========================================================= */

    @Test
    void createMitglied_samePersonAndVerein_twice_returns409() throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(verein1Id);
        dto.setHauptVerein(true);

        mockMvc.perform(
                post("/api/mitglied")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/mitglied")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isConflict());
    }

    /* =========================================================
       ❌ HAUPTVEREIN-REGEL
       ========================================================= */

    @Test
    void createSecondHauptverein_forSamePerson_returns409() throws Exception {

        MitgliedDTO first = new MitgliedDTO();
        first.setPersonId(personId);
        first.setVereinId(verein1Id);
        first.setHauptVerein(true);

        MitgliedDTO second = new MitgliedDTO();
        second.setPersonId(personId);
        second.setVereinId(verein2Id);
        second.setHauptVerein(true);

        mockMvc.perform(
                post("/api/mitglied")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/mitglied")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second))
        ).andExpect(status().isConflict());
    }
}
