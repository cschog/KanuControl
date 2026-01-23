package com.kcserver.mitglied;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.MitgliedDTO;
import com.kcserver.enumtype.MitgliedFunktion;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("mitglied-search")
class MitgliedSearchSortTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long personId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        Long verein1 = vereine.createIfNotExists("EKC_SORT", "Eschweiler KC");
        Long verein2 = vereine.createIfNotExists("OKC_SORT", "Oberhausener KC");
        Long verein3 = vereine.createIfNotExists("BKC_SORT", "Bonner KC");

        personId = personen.createPerson(
                "Anna",
                "Müller",
                LocalDate.of(1995, 1, 1),
                null
        );

        createMitglied(personId, verein1, MitgliedFunktion.BOOTSHAUSWART);
        createMitglied(personId, verein2, MitgliedFunktion.JUGENDWART);
        createMitglied(personId, verein3, MitgliedFunktion.KASSENWART);
    }

    /* =========================================================
       SORT: funktion ASC
       ========================================================= */

    @Test
    void getMitgliederByPerson_sortByFunktionAsc() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/mitglied/person/{personId}", personId)
                                        .param("sort", "funktion,asc")
                                        .accept(MediaType.APPLICATION_JSON)
                        )
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
                        tenantRequest(
                                get("/api/mitglied/person/{personId}", personId)
                                        .param("sort", "hauptVerein,desc")
                                        .accept(MediaType.APPLICATION_JSON)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hauptVerein").value(true))
                .andExpect(jsonPath("$[1].hauptVerein").value(false))
                .andExpect(jsonPath("$[2].hauptVerein").value(false));

    }

    /* =========================================================
       Helper
       ========================================================= */

    private void createMitglied(
            Long personId,
            Long vereinId,
            MitgliedFunktion funktion
    ) throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);
        dto.setFunktion(funktion);

        mockMvc.perform(
                        tenantRequest(post("/api/mitglied"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }
}