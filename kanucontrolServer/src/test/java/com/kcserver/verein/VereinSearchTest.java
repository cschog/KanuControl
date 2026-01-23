package com.kcserver.verein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

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
                        tenantRequest(
                                get("/api/verein/search")
                                        .param("name", "Kanu")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void search_byAbk_returnsSingleVerein() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/verein/search")
                                        .param("abk", "EKC")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Eschweiler Kanu Club"));
    }

    @Test
    void search_combinedFilters_AND_applied() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/verein/search")
                                        .param("name", "Kanu")
                                        .param("abk", "DKV")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Dürener Kanu Verein"));
    }

    @Test
    void search_withPaging_limitsResults() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/verein/search")
                                        .param("page", "0")
                                        .param("size", "2")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}