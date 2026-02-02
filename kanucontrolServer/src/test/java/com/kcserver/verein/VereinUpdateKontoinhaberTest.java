package com.kcserver.verein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.VereinDTO;
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
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        vereinId = vereine.createIfNotExists(
                "EKC",
                "Eschweiler Kanu Club"
        );

        personId = personen.createOrReuse(
                "Chris",
                "Schog",
                LocalDate.of(1980, 1, 1),
                null
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
                        tenantRequest(
                                put("/api/verein/{id}", vereinId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(update))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vereinId))
                .andExpect(jsonPath("$.kontoinhaber").exists())
                .andExpect(jsonPath("$.kontoinhaber.id").value(personId))
                .andExpect(jsonPath("$.kontoinhaber.name").value("Schog"));
    }
}