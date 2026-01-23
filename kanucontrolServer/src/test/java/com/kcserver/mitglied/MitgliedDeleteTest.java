package com.kcserver.mitglied;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.MitgliedDTO;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("mitglied-crud")
class MitgliedDeleteTest extends AbstractTenantIntegrationTest {

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
                "EKC_DELETE",
                "Eschweiler Kanu Club"
        );

        personId = personen.createPerson(
                "Max",
                "Mustermann",
                java.time.LocalDate.of(2000, 1, 1),
                null
        );

        // Mitglied anlegen (Setup)
        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);
        dto.setHauptVerein(true);

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
       ✅ HAPPY PATH
       ========================================================= */

    @Test
    void deleteMitglied_existing_returns204() throws Exception {

        mockMvc.perform(
                        tenantRequest(delete("/api/mitglied/{id}", mitgliedId))
                )
                .andExpect(status().isNoContent());
    }

    /* =========================================================
       ❌ NOT FOUND
       ========================================================= */

    @Test
    void deleteMitglied_notExisting_returns404() throws Exception {

        mockMvc.perform(
                        tenantRequest(delete("/api/mitglied/{id}", 99999L))
                )
                .andExpect(status().isNotFound());
    }
}