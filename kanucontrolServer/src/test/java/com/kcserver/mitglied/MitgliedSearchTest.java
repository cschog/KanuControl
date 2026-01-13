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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("mitglied-search")
class MitgliedSearchTest extends AbstractIntegrationTest {

    Long personId;
    Long verein1Id;
    Long verein2Id;

    Long mitglied1Id;
    Long mitglied2Id;

    @BeforeEach
    void setup() throws Exception {
        personId = createPerson("Anna", "Müller");
        verein1Id = createVerein("Eschweiler Kanu Club", "EKC");
        verein2Id = createVerein("Bonner Kanu Verein", "BKV");

        mitglied1Id = createMitglied(personId, verein1Id, true, MitgliedFunktion.BOOTSHAUSWART);
        mitglied2Id = createMitglied(personId, verein2Id, false, MitgliedFunktion.JUGENDWART);
    }

    /* =========================================================
       🔎 BY PERSON
       ========================================================= */

    @Test
    void getMitgliederByPerson_returnsAllMemberships() throws Exception {

        mockMvc.perform(
                        get("/api/mitglied/person/{personId}", personId)
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].personId").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is(personId.intValue())
                )));
    }

    /* =========================================================
       🔎 BY VEREIN
       ========================================================= */

    @Test
    void getMitgliederByVerein_returnsMembers() throws Exception {

        mockMvc.perform(
                        get("/api/mitglied/verein/{vereinId}", verein1Id)
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vereinId").value(verein1Id))
                .andExpect(jsonPath("$[0].hauptVerein").value(true));
    }

    /* =========================================================
       🔎 HAUPTVEREIN
       ========================================================= */

    @Test
    void getHauptvereinByPerson_returnsSingleMembership() throws Exception {

        mockMvc.perform(
                        get("/api/mitglied/person/{personId}/hauptverein", personId)
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personId").value(personId))
                .andExpect(jsonPath("$.vereinId").value(verein1Id))
                .andExpect(jsonPath("$.hauptVerein").value(true));
    }

    /* =========================================================
       ❌ KEIN HAUPTVEREIN
       ========================================================= */

    @Test
    void getHauptvereinByPerson_whenNoneExists_returns404() throws Exception {

        Long otherPersonId = createPerson("Max", "Mustermann");

        mockMvc.perform(
                        get("/api/mitglied/person/{personId}/hauptverein", otherPersonId)
                                .with(jwt())
                )
                .andExpect(status().isNotFound());
    }

    /* =========================================================
       Helper
       ========================================================= */

    private Long createMitglied(
            Long personId,
            Long vereinId,
            boolean hauptverein,
            MitgliedFunktion funktion
    ) throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);
        dto.setHauptVerein(hauptverein);
        dto.setFunktion(funktion);

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

        return objectMapper.readValue(response, MitgliedDTO.class).getId();
    }
}