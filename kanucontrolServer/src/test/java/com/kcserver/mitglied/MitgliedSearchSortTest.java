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
class MitgliedSearchSortTest extends AbstractIntegrationTest {

    Long personId;
    Long vereinId;

    @BeforeEach
    void setup() throws Exception {
        personId = createPerson("Anna", "Müller");

        Long verein1 = createVerein("Eschweiler KC", "EKC");
        Long verein2 = createVerein("Oberhausener KC", "OKC");
        Long verein3 = createVerein("Bonner KC", "BKC");

        createMitglied(personId, verein1, MitgliedFunktion.BOOTSHAUSWART, true);
        createMitglied(personId, verein2, MitgliedFunktion.JUGENDWART, false);
        createMitglied(personId, verein3, MitgliedFunktion.KASSENWART, false);
    }

    /* =========================================================
       SORT: funktion ASC
       ========================================================= */

    @Test
    void getMitgliederByPerson_sortByFunktionAsc() throws Exception {

        mockMvc.perform(
                        get("/api/mitglied/person/{personId}", personId)
                                .param("sort", "funktion,asc")
                                .with(jwt())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].funktion").value("BOOTSHAUSWART"))
                .andExpect(jsonPath("$[1].funktion").value("JUGENDWART"))
                .andExpect(jsonPath("$[2].funktion").value("KASSENWART"));
    }

    /* =========================================================
       SORT: hauptVerein DESC
       ========================================================= */

    @Test
    void getMitgliederByPerson_sortByHauptvereinDesc() throws Exception {

        mockMvc.perform(
                        get("/api/mitglied/person/{personId}", personId)
                                .param("sort", "hauptVerein,desc")
                                .with(jwt())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hauptVerein").value(true))
                .andExpect(jsonPath("$[1].hauptVerein").value(false))
                .andExpect(jsonPath("$[2].hauptVerein").value(false));
    }

    /* =========================================================
       Test-Helfer
       ========================================================= */

    private void createMitglied(
            Long personId,
            Long vereinId,
            MitgliedFunktion funktion,
            boolean hauptverein
    ) throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);
        dto.setFunktion(funktion);
        dto.setHauptVerein(hauptverein);

        mockMvc.perform(
                        post("/api/mitglied")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }
}