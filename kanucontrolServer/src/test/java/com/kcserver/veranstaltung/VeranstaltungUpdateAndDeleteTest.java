package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.teilnehmer.TeilnehmerBulkDeleteDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungUpdateDTO;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class VeranstaltungUpdateAndDeleteTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    VeranstaltungTestFactory veranstaltungFactory;

    Long veranstaltungId;
    Long vereinId;
    Long leiterId;
    Long teilnehmerId;

    /* =========================================================
       SETUP
       ========================================================= */

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper);

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);

        veranstaltungFactory =
                new VeranstaltungTestFactory(mockMvc, objectMapper);

        vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.create(b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(LocalDate.of(1990, 1, 1))
        );

        teilnehmerId = personen.create(b ->
                b.withVorname("Erika")
                        .withName("Musterfrau")
                        .withGeburtsdatum(LocalDate.of(2001, 5, 5))
        );

        VeranstaltungCreateDTO createDTO = new VeranstaltungCreateDTO();
        createDTO.setName("Sommerfreizeit");
        createDTO.setTyp(VeranstaltungTyp.JEM);
        createDTO.setVereinId(vereinId);
        createDTO.setLeiterId(leiterId);
        createDTO.setBeginnDatum(LocalDate.now().plusDays(5));
        createDTO.setBeginnZeit(LocalTime.of(10, 0));
        createDTO.setEndeDatum(LocalDate.now().plusDays(10));
        createDTO.setEndeZeit(LocalTime.of(18, 0));

        String json =
                mockMvc.perform(
                                post("/api/veranstaltungen")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createDTO))
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        veranstaltungId = objectMapper.readTree(json).get("id").asLong();
    }

    /* =========================================================
       UPDATE (bestehend)
       ========================================================= */

    @Test
    void updateVeranstaltung_updatesNameAndEndDate() throws Exception {

        VeranstaltungUpdateDTO updateDTO = new VeranstaltungUpdateDTO();
        updateDTO.setName("Sommerfreizeit 2026");
        updateDTO.setEndeDatum(LocalDate.now().plusDays(12));

        mockMvc.perform(

                                put("/api/veranstaltungen/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateDTO))

                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sommerfreizeit 2026"))
                .andExpect(jsonPath("$.endeDatum").exists());
    }

    /* =========================================================
       ACTIVATE (bestehend)
       ========================================================= */

    @Test
    void activateVeranstaltung_switchesActiveVeranstaltung() throws Exception {

        Long firstId = veranstaltungFactory.create(vereinId, leiterId, "Sommer");
        Long secondId = veranstaltungFactory.create(vereinId, leiterId, "Herbst");

        mockMvc.perform(
                        get("/api/veranstaltungen/aktiv")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(secondId));
    }

    /* =========================================================
       DELETE – BASIS (bestehend)
       ========================================================= */

    @Test
    void deleteVeranstaltung_onlyLeiter_allowsDelete() throws Exception {

        mockMvc.perform(
                        delete("/api/veranstaltungen/{id}", veranstaltungId)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/veranstaltungen/{id}", veranstaltungId)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteActiveVeranstaltung_resultsInNoActiveVeranstaltung() throws Exception {

        mockMvc.perform(
                        get("/api/veranstaltungen/aktiv")
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/api/veranstaltungen/{id}", veranstaltungId)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/veranstaltungen/aktiv")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNonExistingVeranstaltung_returns404() throws Exception {

        mockMvc.perform(
                        delete("/api/veranstaltungen/{id}", 999999L)
                )
                .andExpect(status().isNotFound());
    }

    /* =========================================================
       DELETE – MIT TEILNEHMERN (neu)
       ========================================================= */

    @Test
    void deleteVeranstaltung_withAdditionalTeilnehmer_returns409() throws Exception {

        // 🔒 Sicherheitscheck im Test
        assertNotEquals(leiterId, teilnehmerId);

        veranstaltungFactory.addTeilnehmer(
                veranstaltungId,
                teilnehmerId
        );

        mockMvc.perform(
                        delete("/api/veranstaltungen/{id}", veranstaltungId)
                )
                .andExpect(status().isConflict());
    }

    /* =========================================================
       BULK DELETE TEILNEHMER (neu)
       ========================================================= */

    @Test
    void bulkDeleteTeilnehmer_removesOnlyNonLeiter_thenDeleteAllowed() throws Exception {

        // 🔒 Sicherheitscheck im Test
        assertNotEquals(leiterId, teilnehmerId);

        veranstaltungFactory.addTeilnehmer(
                veranstaltungId,
                teilnehmerId
        );

        TeilnehmerBulkDeleteDTO dto = new TeilnehmerBulkDeleteDTO();
        dto.setPersonIds(List.of(leiterId, teilnehmerId));

        mockMvc.perform(

                                delete("/api/veranstaltungen/{id}/teilnehmer", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)
                        )
                )
                .andExpect(status().isNoContent());
    }

      /* =========================================================
       shouldDelete
       ========================================================= */

    @Test
    void shouldDelete() throws Exception {

        mockMvc.perform(
                delete("/api/veranstaltungen/{id}", veranstaltungId)
        ).andExpect(status().isNoContent());
    }

    /* =========================================================
       shouldReturn404AfterDelete
       ========================================================= */

    @Test
    void shouldReturn404AfterDelete() throws Exception {

        mockMvc.perform(
                delete("/api/veranstaltungen/{id}", veranstaltungId)
        ).andExpect(status().isNoContent());

        mockMvc.perform(
                get("/api/veranstaltungen/{id}", veranstaltungId)
        ).andExpect(status().isNotFound());
    }

    /* =========================================================
       shouldFailDeleteNonExisting
       ========================================================= */

    @Test
    void shouldFailDeleteNonExisting() throws Exception {

        mockMvc.perform(
                delete("/api/veranstaltungen/{id}", 999999L)
        ).andExpect(status().isNotFound());
    }
}