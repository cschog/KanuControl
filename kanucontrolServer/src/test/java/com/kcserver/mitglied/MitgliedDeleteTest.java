package com.kcserver.mitglied;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.mitglied.MitgliedDTO;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        vereinId = vereine.createIfNotExists(
                "EKC_DELETE",
                "Eschweiler Kanu Club"
        );

        vereinAId = vereine.createIfNotExists("V_A", "Verein A");
        vereinBId = vereine.createIfNotExists("V_B", "Verein B");

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

        // Mitglied A = Hauptverein
        mitgliedAId = createMitglied(personId, vereinAId);

        // Mitglied B = normal
        mitgliedBId = createMitglied(personId, vereinBId);
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
    @Test
    void deleteNormalMitglied_keepsExistingHauptverein() throws Exception {

        // B ist Hauptverein → A ist normal
        mockMvc.perform(
                tenantRequest(delete("/api/mitglied/{id}", mitgliedAId))
        ).andExpect(status().isNoContent());

        List<MitgliedDTO> remaining =
                getMitgliedByPerson(personId);

        MitgliedDTO hauptverein = findHauptverein(remaining);

        assertEquals(vereinBId, hauptverein.getVereinId());
    }

    @Test
    void deleteHauptverein_assignsAnyRemainingAsHauptverein() throws Exception {

        mockMvc.perform(
                tenantRequest(delete("/api/mitglied/{id}", mitgliedBId))
        ).andExpect(status().isNoContent());

        List<MitgliedDTO> remaining =
                getMitgliedByPerson(personId);

        MitgliedDTO hauptverein = findHauptverein(remaining);

        // MUSS einer der verbleibenden sein
        assertTrue(
                hauptverein.getVereinId().equals(vereinAId)
                        || hauptverein.getVereinId().equals(vereinId)
        );
    }

    private List<MitgliedDTO> getMitgliedByPerson(Long personId) throws Exception {
        String json = mockMvc.perform(
                        tenantRequest(
                                get("/api/mitglied/person/{id}", personId)
                        )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                json,
                new TypeReference<List<MitgliedDTO>>() {}
        );
    }

    private Long createMitglied(Long personId, Long vereinId) throws Exception {
        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);

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

    private MitgliedDTO findHauptverein(List<MitgliedDTO> list) {
        return list.stream()
                .filter(MitgliedDTO::getHauptVerein)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Kein Hauptverein gefunden"));
    }
}