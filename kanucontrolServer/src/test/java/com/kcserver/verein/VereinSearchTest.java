package com.kcserver.verein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.support.api.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinSearchTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper);

        vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");
        vereine.createIfNotExists("OKC", "Oberhausener Kanu Club");
        vereine.createIfNotExists("DKV", "Dürener Kanu Verein");
    }

    /* =========================================================
       TESTS
       ========================================================= */

    @Test
    void search_byName_contains_returnsMatchingVereine() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search/all")
                                .param("name", "Kanu")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void search_byAbk_returnsSingleVerein() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search")
                                .param("abk", "EKC")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Eschweiler Kanu Club"));
    }

    @Test
    void search_combinedFilters_AND_applied() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search")
                                .param("name", "Kanu")
                                .param("abk", "DKV")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Dürener Kanu Verein"));
    }

    @Test
    void search_withPaging_limitsResults() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search")
                                .param("page", "0")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }
}