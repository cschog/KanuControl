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
class VereinSearchSortTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");
        vereine.createIfNotExists("OKC", "Oberhausener Kanu Club");
        vereine.createIfNotExists("AKV", "Aachener Kanu Verein");
        vereine.createIfNotExists("BKF", "Bonner Kanu Freunde");
    }

    /* =========================================================
       SORT
       ========================================================= */

    @Test
    void search_sortedByNameAsc() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/verein/search")
                                        .param("sort", "name,asc")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aachener Kanu Verein"))
                .andExpect(jsonPath("$[3].name").value("Oberhausener Kanu Club"));
    }

    @Test
    void search_sortedByNameDesc() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/verein/search")
                                        .param("sort", "name,desc")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Oberhausener Kanu Club"))
                .andExpect(jsonPath("$[3].name").value("Aachener Kanu Verein"));
    }

    @Test
    void search_sortedByAbkAsc() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/verein/search")
                                        .param("sort", "abk,asc")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].abk").value("AKV"))
                .andExpect(jsonPath("$[3].abk").value("OKC"));
    }

    /* =========================================================
       SORT + PAGING
       ========================================================= */

    @Test
    void search_withPagingAndSorting_returnsSecondPage() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/verein/search")
                                        .param("sort", "name,asc")
                                        .param("page", "1")
                                        .param("size", "2")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Eschweiler Kanu Club"))
                .andExpect(jsonPath("$[1].name").value("Oberhausener Kanu Club"));
    }
}