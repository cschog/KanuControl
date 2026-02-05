package com.kcserver.mitglied;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.mitglied.MitgliedDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("mitglied-crud")
class MitgliedCreateTest extends AbstractTenantIntegrationTest {

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

        verein1Id = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");
        verein2Id = vereine.createIfNotExists("OKC", "Oberhausener Kanu Club");

        personId = personen.createOrReuse(
                "Max",
                "Mustermann",
                java.time.LocalDate.of(2000, 1, 1),
                null
        );
    }

    /* =========================================================
       ✅ HAPPY PATH
       ========================================================= */

    @Test
    void createMitglied_valid_returns201() throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(verein1Id);
        dto.setHauptVerein(true);
        dto.setFunktion(MitgliedFunktion.JUGENDWART);

        mockMvc.perform(
                        tenantRequest(post("/api/mitglied"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.hauptVerein").value(true))
                .andExpect(jsonPath("$.funktion").value("JUGENDWART"))
                .andExpect(jsonPath("$.verein.id").value(verein1Id))
                .andExpect(jsonPath("$.verein.name").value("Eschweiler Kanu Club"));
    }

    /* =========================================================
       ❌ REFERENZFEHLER
       ========================================================= */

    @Test
    void createMitglied_personNotFound_returns404() throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(99999L);
        dto.setVereinId(verein1Id);
        dto.setHauptVerein(true);

        mockMvc.perform(
                        tenantRequest(post("/api/mitglied"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void createMitglied_vereinNotFound_returns404() throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(99999L);
        dto.setHauptVerein(true);

        mockMvc.perform(
                        tenantRequest(post("/api/mitglied"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }

    /* =========================================================
       ❌ DUPLIKATE
       ========================================================= */

    @Test
    void createMitglied_samePersonAndVerein_twice_returns409() throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(verein1Id);
        dto.setHauptVerein(true);

        mockMvc.perform(
                tenantRequest(post("/api/mitglied"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                tenantRequest(post("/api/mitglied"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isConflict());
    }

    /* =========================================================
       ❌ HAUPTVEREIN-REGEL
       ========================================================= */

    @Test
    void createSecondMitglied_switchesHauptverein() throws Exception {

        MitgliedDTO first = new MitgliedDTO();
        first.setPersonId(personId);
        first.setVereinId(verein1Id);
        first.setHauptVerein(true);

        MitgliedDTO second = new MitgliedDTO();
        second.setPersonId(personId);
        second.setVereinId(verein2Id);
        second.setHauptVerein(true);

        mockMvc.perform(
                tenantRequest(post("/api/mitglied"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                tenantRequest(post("/api/mitglied"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        tenantRequest(get("/api/mitglied/person/{personId}", personId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.vereinId==" + verein1Id + ")].hauptVerein").value(false))
                .andExpect(jsonPath("$[?(@.vereinId==" + verein2Id + ")].hauptVerein").value(true));
    }
}