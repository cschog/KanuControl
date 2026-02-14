package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.teilnehmer.TeilnehmerBulkDeleteDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungUpdateDTO;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.testdata.PersonTestFactory;
import com.kcserver.testdata.VeranstaltungTestFactory;
import com.kcserver.testdata.VereinTestFactory;
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
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        veranstaltungFactory =
                new VeranstaltungTestFactory(mockMvc, objectMapper, tenantAuth());

        vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.createOrReuse(
                "Max", "Mustermann",
                LocalDate.of(1990, 1, 1),
                null
        );

        teilnehmerId = personen.createOrReuse(
                "Erika", "Musterfrau",
                LocalDate.of(2001, 5, 5),
                null
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
                                tenantRequest(post("/api/veranstaltung"))
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
                        tenantRequest(
                                put("/api/veranstaltung/{id}", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateDTO))
                        )
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
                        tenantRequest(get("/api/veranstaltung/active"))
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
                        tenantRequest(delete("/api/veranstaltung/{id}", veranstaltungId))
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        tenantRequest(get("/api/veranstaltung/{id}", veranstaltungId))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteActiveVeranstaltung_resultsInNoActiveVeranstaltung() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/veranstaltung/active"))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        tenantRequest(delete("/api/veranstaltung/{id}", veranstaltungId))
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        tenantRequest(get("/api/veranstaltung/active"))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNonExistingVeranstaltung_returns404() throws Exception {

        mockMvc.perform(
                        tenantRequest(delete("/api/veranstaltung/{id}", 999999L))
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
                        tenantRequest(delete("/api/veranstaltung/{id}", veranstaltungId))
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
                        tenantRequest(
                                delete("/api/veranstaltung/{id}/teilnehmer", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
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
                tenantRequest(delete("/api/veranstaltung/{id}", veranstaltungId))
        ).andExpect(status().isNoContent());
    }

    /* =========================================================
       shouldReturn404AfterDelete
       ========================================================= */

    @Test
    void shouldReturn404AfterDelete() throws Exception {

        mockMvc.perform(
                tenantRequest(delete("/api/veranstaltung/{id}", veranstaltungId))
        ).andExpect(status().isNoContent());

        mockMvc.perform(
                tenantRequest(get("/api/veranstaltung/{id}", veranstaltungId))
        ).andExpect(status().isNotFound());
    }

    /* =========================================================
       shouldFailDeleteNonExisting
       ========================================================= */

    @Test
    void shouldFailDeleteNonExisting() throws Exception {

        mockMvc.perform(
                tenantRequest(delete("/api/veranstaltung/{id}", 999999L))
        ).andExpect(status().isNotFound());
    }
}