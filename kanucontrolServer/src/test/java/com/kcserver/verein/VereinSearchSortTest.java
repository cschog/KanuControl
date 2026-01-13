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
class VereinSearchSortTest extends AbstractIntegrationTest {

    @BeforeEach
    void setup() throws Exception {
        createVerein("Eschweiler Kanu Club", "EKC");
        createVerein("Oberhausener Kanu Club", "OKC");
        createVerein("Aachener Kanu Verein", "AKV");
        createVerein("Bonner Kanu Freunde", "BKF");
    }

    /* =========================================================
       SORT
       ========================================================= */

    @Test
    void search_sortedByNameAsc() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search")
                                .with(jwt())
                                .param("sort", "name,asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aachener Kanu Verein"))
                .andExpect(jsonPath("$[3].name").value("Oberhausener Kanu Club"));
    }

    @Test
    void search_sortedByNameDesc() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search")
                                .with(jwt())
                                .param("sort", "name,desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Oberhausener Kanu Club"))
                .andExpect(jsonPath("$[3].name").value("Aachener Kanu Verein"));
    }

    @Test
    void search_sortedByAbkAsc() throws Exception {

        mockMvc.perform(
                        get("/api/verein/search")
                                .with(jwt())
                                .param("sort", "abk,asc")
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
                        get("/api/verein/search")
                                .with(jwt())
                                .param("sort", "name,asc")
                                .param("page", "1")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Eschweiler Kanu Club"))
                .andExpect(jsonPath("$[1].name").value("Oberhausener Kanu Club"));
    }
}