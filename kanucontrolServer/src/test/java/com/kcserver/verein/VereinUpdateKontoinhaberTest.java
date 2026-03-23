package com.kcserver.verein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.verein.VereinDTO;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinUpdateKontoinhaberTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long vereinId;
    Long personId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper);

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);

        vereinId = vereine.createIfNotExists(
                "EKC",
                "Eschweiler Kanu Club"
        );

        personId = personen.create(b ->
                b.withVorname("Chris")
                        .withName("Schog")
                        .withGeburtsdatum(LocalDate.of(1990, 1, 1))
        );

    }

    /* =========================================================
       TEST
       ========================================================= */

    @Test
    void updateVerein_setsKontoinhaberSuccessfully() throws Exception {

        VereinDTO update = new VereinDTO();
        update.setName("Eschweiler Kanu Club");
        update.setAbk("EKC");
        update.setKontoinhaberId(personId);

        mockMvc.perform(

                                put("/api/verein/{id}", vereinId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(update))

                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vereinId))
                .andExpect(jsonPath("$.kontoinhaber").exists())
                .andExpect(jsonPath("$.kontoinhaber.id").value(personId))
                .andExpect(jsonPath("$.kontoinhaber.name").value("Schog"));
    }
}