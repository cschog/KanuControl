package com.kcserver.mitglied;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("mitglied-crud")
class MitgliedDeleteTest extends AbstractIntegrationTest {

    Long personId;
    Long vereinId;
    Long mitgliedId;

    @BeforeEach
    void setup() throws Exception {
        personId = createPerson("Max", "Mustermann");
        vereinId = createVerein("Eschweiler Kanu Club", "EKC");

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);
        dto.setHauptVerein(true);

        String response = mockMvc.perform(
                        post("/api/mitglied")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mitgliedId = objectMapper.readValue(response, MitgliedDTO.class).getId();
    }

    /* =========================================================
       ✅ HAPPY PATH
       ========================================================= */

    @Test
    void deleteMitglied_existing_returns204() throws Exception {

        mockMvc.perform(
                        delete("/api/mitglied/{id}", mitgliedId)
                                .with(jwt())
                )
                .andExpect(status().isNoContent());
    }

    /* =========================================================
       ❌ NOT FOUND
       ========================================================= */

    @Test
    void deleteMitglied_notExisting_returns404() throws Exception {

        mockMvc.perform(
                        delete("/api/mitglied/{id}", 99999L)
                                .with(jwt())
                )
                .andExpect(status().isNotFound());
    }
}