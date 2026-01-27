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

import static com.kcserver.testsupport.ResultMatchersExt.hasExactlyOneHauptverein;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("mitglied-crud")
class MitgliedUpdateTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long personId;
    Long verein1Id;
    Long verein2Id;
    Long mitglied1Id;
    Long mitglied2Id;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        verein1Id = vereine.createIfNotExists("EKC_U", "Eschweiler Kanu Club");
        verein2Id = vereine.createIfNotExists("OKC_U", "Oberhausener Kanu Club");

        personId = personen.createOrReuse(
                "Max",
                "Mustermann",
                LocalDate.of(1990, 1, 1),
                null
        );

        mitglied1Id = createMitglied(personId, verein1Id);
        mitglied2Id = createMitglied(personId, verein2Id);
    }

    /* =========================================================
       ✔ FUNKTION ÄNDERN
       ========================================================= */

    @Test
    void updateMitglied_changeFunktion_returns200() throws Exception {

        MitgliedDTO update = new MitgliedDTO();
        update.setFunktion(MitgliedFunktion.BOOTSHAUSWART);

        mockMvc.perform(
                        tenantRequest(
                                put("/api/mitglied/{id}", mitglied1Id)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(update))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.funktion").value("BOOTSHAUSWART"));
    }

    /* =========================================================
       ✔ HAUPTVEREIN WECHSELN
       ========================================================= */

    @Test
    void updateMitglied_switchHauptverein_unsetsPrevious() throws Exception {

        MitgliedDTO update = new MitgliedDTO();
        update.setHauptVerein(true);

        // zweiter Mitglied wird Hauptverein
        mockMvc.perform(
                        tenantRequest(
                                put("/api/mitglied/{id}", mitglied2Id)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(update))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hauptVerein").value(true));

        // erster ist jetzt kein Hauptverein mehr
        mockMvc.perform(
                        tenantRequest(
                                put("/api/mitglied/{id}", mitglied1Id)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{}")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hauptVerein").value(false));
    }

    /* =========================================================
       ❌ IDENTITÄT ÄNDERN NICHT ERLAUBT
       ========================================================= */

    @Test
    void updateMitglied_changePerson_returns400() throws Exception {

        MitgliedDTO update = new MitgliedDTO();
        update.setPersonId(999L);

        mockMvc.perform(
                        tenantRequest(
                                put("/api/mitglied/{id}", mitglied1Id)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(update))
                        )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMitglied_changeVerein_returns400() throws Exception {

        MitgliedDTO update = new MitgliedDTO();
        update.setVereinId(999L);

        mockMvc.perform(
                        tenantRequest(
                                put("/api/mitglied/{id}", mitglied1Id)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(update))
                        )
                )
                .andExpect(status().isBadRequest());
    }

    /* =========================================================
       Helper
       ========================================================= */

    private Long createMitglied(Long personId, Long vereinId) throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);

        String response =
                mockMvc.perform(
                                tenantRequest(
                                        post("/api/mitglied")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(dto))
                                )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readValue(response, MitgliedDTO.class).getId();
    }

    @Test
    void updatePerson_updatesMitgliedschaftenCorrectly() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/person/{id}", personId))
                )
                .andExpect(status().isOk())
                .andExpect(hasExactlyOneHauptverein());
    }
}