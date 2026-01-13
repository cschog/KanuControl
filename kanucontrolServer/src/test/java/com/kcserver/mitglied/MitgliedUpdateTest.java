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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("mitglied-crud")
class MitgliedUpdateTest extends AbstractIntegrationTest {

    Long personId;
    Long verein1Id;
    Long verein2Id;
    Long mitglied1Id;
    Long mitglied2Id;

    @BeforeEach
    void setup() throws Exception {
        personId = createPerson("Max", "Mustermann");
        verein1Id = createVerein("Eschweiler Kanu Club", "EKC");
        verein2Id = createVerein("Oberhausener Kanu Club", "OKC");

        mitglied1Id = createMitglied(personId, verein1Id, true);
        mitglied2Id = createMitglied(personId, verein2Id, false);
    }

    /* =========================================================
       ✔ FUNKTION ÄNDERN
       ========================================================= */

    @Test
    void updateMitglied_changeFunktion_returns200() throws Exception {

        MitgliedDTO update = new MitgliedDTO();
        update.setFunktion(MitgliedFunktion.BOOTSHAUSWART);

        mockMvc.perform(
                        put("/api/mitglied/{id}", mitglied1Id)
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
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

        mockMvc.perform(
                        put("/api/mitglied/{id}", mitglied2Id)
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hauptVerein").value(true));

        // alter Hauptverein ist jetzt false
        mockMvc.perform(
                        put("/api/mitglied/{id}", mitglied1Id)
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
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
                        put("/api/mitglied/{id}", mitglied1Id)
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMitglied_changeVerein_returns400() throws Exception {

        MitgliedDTO update = new MitgliedDTO();
        update.setVereinId(999L);

        mockMvc.perform(
                        put("/api/mitglied/{id}", mitglied1Id)
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isBadRequest());
    }
}