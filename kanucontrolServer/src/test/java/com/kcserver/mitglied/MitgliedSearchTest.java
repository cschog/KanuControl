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

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("mitglied-search")
class MitgliedSearchTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long personId;
    Long verein1Id;
    Long verein2Id;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        verein1Id = vereine.createIfNotExists("EKC_M", "Eschweiler Kanu Club");
        verein2Id = vereine.createIfNotExists("BKV_M", "Bonner Kanu Verein");

        personId = personen.createPerson(
                "Anna",
                "Müller",
                LocalDate.of(1995, 1, 1),
                null
        );

        createMitglied(personId, verein1Id,  MitgliedFunktion.BOOTSHAUSWART);
        createMitglied(personId, verein2Id,  MitgliedFunktion.JUGENDWART);
    }

    /* =========================================================
       🔎 BY PERSON
       ========================================================= */

    @Test
    void getMitgliederByPerson_returnsAllMemberships() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/mitglied/person/{personId}", personId)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].personId")
                        .value(everyItem(is(personId.intValue()))));
    }

    /* =========================================================
       🔎 BY VEREIN
       ========================================================= */

    @Test
    void getMitgliederByVerein_returnsMembers() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/mitglied/verein/{vereinId}", verein1Id)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vereinId").value(verein1Id));

    }

    /* =========================================================
       🔎 HAUPTVEREIN
       ========================================================= */

    @Test
    void getHauptvereinByPerson_returnsSingleMembership() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/mitglied/person/{personId}/hauptverein", personId)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vereinId").value(verein2Id));
    }

    /* =========================================================
       ❌ KEIN HAUPTVEREIN
       ========================================================= */



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