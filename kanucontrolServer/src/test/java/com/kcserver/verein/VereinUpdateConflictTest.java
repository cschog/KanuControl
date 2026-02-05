package com.kcserver.verein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.verein.VereinDTO;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinUpdateConflictTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long verein1Id;
    Long verein2Id;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        verein1Id = vereine.createIfNotExists(
                "EKC",
                "Eschweiler Kanu Club"
        );

        verein2Id = vereine.createIfNotExists(
                "OKC",
                "Oberhausener Kanu Club"
        );
    }

    /* =========================================================
       KONFLIKT-TESTS
       ========================================================= */

    @Test
    void updateVerein_toExistingAbkAndName_returns409() throws Exception {

        VereinDTO update = new VereinDTO();
        update.setName("Eschweiler Kanu Club");
        update.setAbk("EKC");

        mockMvc.perform(
                        tenantRequest(
                                put("/api/verein/{id}", verein2Id)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(update))
                        )
                )
                .andExpect(status().isConflict());
    }

    @Test
    void updateVerein_withSameValues_onSameEntity_isAllowed() throws Exception {

        VereinDTO update = new VereinDTO();
        update.setName("Eschweiler Kanu Club");
        update.setAbk("EKC");

        mockMvc.perform(
                        tenantRequest(
                                put("/api/verein/{id}", verein1Id)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(update))
                        )
                )
                .andExpect(status().isOk());
    }
}