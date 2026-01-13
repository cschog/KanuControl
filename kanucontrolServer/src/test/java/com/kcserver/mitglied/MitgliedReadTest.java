package com.kcserver.mitglied;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.enumtype.MitgliedFunktion;
import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MitgliedReadTest extends AbstractIntegrationTest {

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
        dto.setFunktion(MitgliedFunktion.JUGENDWART);

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

    @Test
    void getMitgliedById_returns200() throws Exception {
        mockMvc.perform(
                        get("/api/mitglied/{id}", mitgliedId)
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mitgliedId))
                .andExpect(jsonPath("$.personId").value(personId))
                .andExpect(jsonPath("$.vereinId").value(vereinId));
    }

    @Test
    void getMitgliederByPerson_returnsList() throws Exception {
        mockMvc.perform(
                        get("/api/mitglied/person/{id}", personId)
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getMitgliederByVerein_returnsList() throws Exception {
        mockMvc.perform(
                        get("/api/mitglied/verein/{id}", vereinId)
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}