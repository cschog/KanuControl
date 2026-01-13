package com.kcserver.verein;

import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinDeleteTest extends AbstractIntegrationTest {

    Long vereinId;

    @BeforeEach
    void setup() throws Exception {
        vereinId = createVerein("Eschweiler Kanu Club", "EKC");
    }

    /* =========================================================
       TESTS
       ========================================================= */

    @Test
    void deleteVerein_existing_returns204_andRemovesVerein() throws Exception {

        // DELETE
        mockMvc.perform(
                        delete("/api/verein/{id}", vereinId)
                                .with(jwt())
                )
                .andExpect(status().isNoContent());

        // VERIFY: Verein ist weg
        mockMvc.perform(
                        get("/api/verein/{id}", vereinId)
                                .with(jwt())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteVerein_notFound_returns404() throws Exception {

        mockMvc.perform(
                        delete("/api/verein/{id}", 99999L)
                                .with(jwt())
                )
                .andExpect(status().isNotFound());
    }
}