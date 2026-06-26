package com.kcserver.mitglied;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.mitglied.MitgliedDTO;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VereinTestFactory;
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

        PersonTestFactory personFactory = new PersonTestFactory(mockMvc, objectMapper);

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper);

        vereinId = vereine.createIfNotExists(
                "EKC_DELETE",
                "Eschweiler Kanu Club"
        );

        vereinAId = vereine.createIfNotExists("V_A", "Verein A");
        vereinBId = vereine.createIfNotExists("V_B", "Verein B");

        personId = personFactory.createWithVerein(vereinId, b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(java.time.LocalDate.of(2000, 1, 1))
        );

        List<MitgliedDTO> mitglieder = getMitgliedByPerson(personId);

        mitgliedId = mitglieder.stream()
                .filter(m -> m.getVereinId().equals(vereinId))
                .findFirst()
                .orElseThrow()
                .getId();

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
                (delete("/api/mitglied/{id}", mitgliedId))
        ).andExpect(status().isNoContent());
    }

    /* =========================================================
       ❌ NOT FOUND
       ========================================================= */

    @Test
    void deleteMitglied_notExisting_returns404() throws Exception {

        mockMvc.perform(
                        delete("/api/mitglied/{id}", 99999L)
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void deleteNormalMitglied_keepsExistingHauptverein() throws Exception {

        // B ist Hauptverein → A ist normal
        mockMvc.perform(
                (delete("/api/mitglied/{id}", mitgliedAId))
        ).andExpect(status().isNoContent());

        List<MitgliedDTO> remaining =
                getMitgliedByPerson(personId);

        MitgliedDTO hauptverein = findHauptverein(remaining);

        assertEquals(vereinBId, hauptverein.getVereinId());
    }

    @Test
    void deleteHauptverein_assignsAnyRemainingAsHauptverein() throws Exception {

        mockMvc.perform(
                delete("/api/mitglied/{id}", mitgliedBId)
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
                        get("/api/mitglied/person/{personId}", personId)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(json).path("data");

        return objectMapper.convertValue(
                data,
                new TypeReference<>() {
                }
        );
    }

    private Long createMitglied(Long personId, Long vereinId) throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);

        String response =
                mockMvc.perform(
                                post("/api/mitglied")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private MitgliedDTO findHauptverein(List<MitgliedDTO> list) {
        return list.stream()
                .filter(MitgliedDTO::getHauptVerein)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Kein Hauptverein gefunden"));
    }
}