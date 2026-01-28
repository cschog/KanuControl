package com.kcserver.mitglied;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.MitgliedDTO;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MitgliedHauptvereinTest extends AbstractTenantIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    Long personId;
    Long vereinAId;
    Long vereinBId;
    Long mitgliedAId;
    Long mitgliedBId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        // Person
        personId = personen.createPerson(
                "Max",
                "Mustermann",
                java.time.LocalDate.of(2000, 1, 1),
                null
        );

        // Vereine
        vereinAId = vereine.createIfNotExists("VA", "Verein A");
        vereinBId = vereine.createIfNotExists("VB", "Verein B");

        // Mitglied A = Hauptverein
        mitgliedAId = createMitglied(personId, vereinAId, true);

        // Mitglied B = kein Hauptverein
        mitgliedBId = createMitglied(personId, vereinBId, false);
    }

    @Test
    void setHauptverein_switchesFromOldToNew() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                put("/api/mitglied/{id}/hauptverein", mitgliedBId)
                        )
                )
                .andExpect(status().isNoContent());

        MitgliedDTO a = getMitglied(mitgliedAId);
        MitgliedDTO b = getMitglied(mitgliedBId);

        assertFalse(a.getHauptVerein(), "alter Hauptverein muss false sein");
        assertTrue(b.getHauptVerein(), "neuer Hauptverein muss true sein");
    }

    /* =========================================================
       Helper
       ========================================================= */

    private Long createMitglied(Long personId, Long vereinId, boolean hauptverein) throws Exception {
        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);
        dto.setHauptVerein(hauptverein);

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

        return objectMapper.readTree(response).get("id").asLong();
    }

    private MitgliedDTO getMitglied(Long mitgliedId) throws Exception {
        String json =
                mockMvc.perform(
                                tenantRequest(get("/api/mitglied/{id}", mitgliedId))
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readValue(json, new TypeReference<>() {});
    }
}