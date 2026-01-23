package com.kcserver.mitglied;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.MitgliedDTO;
import com.kcserver.enumtype.MitgliedFunktion;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.testdata.PersonTestFactory;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MitgliedReadTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long personId;
    Long vereinId;
    Long mitgliedId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        vereinId = vereine.createIfNotExists(
                "EKC_READ",
                "Eschweiler Kanu Club"
        );

        personId = personen.createOrReuse(
                "Max",
                "Mustermann",
                java.time.LocalDate.of(2000, 1, 1),
                null
        );

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);
        dto.setHauptVerein(true);
        dto.setFunktion(MitgliedFunktion.JUGENDWART);

        String response =
                mockMvc.perform(
                                tenantRequest(post("/api/mitglied"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        mitgliedId =
                objectMapper.readTree(response).get("id").asLong();
    }

    /* =========================================================
       READ
       ========================================================= */

    @Test
    void getMitgliedById_returns200() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/mitglied/{id}", mitgliedId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mitgliedId))
                .andExpect(jsonPath("$.personId").value(personId))
                .andExpect(jsonPath("$.vereinId").value(vereinId));
    }

    @Test
    void getMitgliederByPerson_returnsList() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/mitglied/person/{id}", personId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getMitgliederByVerein_returnsList() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/mitglied/verein/{id}", vereinId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}