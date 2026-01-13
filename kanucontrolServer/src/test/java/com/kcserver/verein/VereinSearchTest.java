package com.kcserver.verein;

import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinSearchTest extends AbstractIntegrationTest {

    @BeforeEach
    void setup() throws Exception {
        createVerein("Eschweiler Kanu Club", "EKC");
        createVerein("Oberhausener Kanu Club", "OKC");
        createVerein("Dürener Kanu Verein", "DKV");
    }

    /* =========================================================
       TESTS
       ========================================================= */

    @Test
    void search_byName_contains_returnsMatchingVereine() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search")
                                .with(jwt())
                                .param("name", "Kanu")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void search_byAbk_returnsSingleVerein() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search")
                                .with(jwt())
                                .param("abk", "EKC")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Eschweiler Kanu Club"));
    }

    @Test
    void search_combinedFilters_AND_applied() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search")
                                .with(jwt())
                                .param("name", "Kanu")
                                .param("abk", "DKV")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Dürener Kanu Verein"));
    }

    @Test
    void search_withPaging_limitsResults() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search")
                                .with(jwt())
                                .param("page", "0")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
