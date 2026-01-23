package com.kcserver.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.testdata.PersonTestFactory;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PersonSearchIntegrationTest extends AbstractTenantIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    Long vereinId;

    private static final String TEST_ABK  = "EKC_SEARCH";
    private static final String TEST_NAME = "Eschweiler KC Search";

    @BeforeEach
    void setupData() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        vereinId = vereine.createIfNotExists(TEST_ABK, TEST_NAME);

        personen.createOrReuse(
                "Max",
                "Mustermann",
                LocalDate.of(2005, 1, 1),
                vereinId,
                "Köln",
                "50667"
        );

        personen.createOrReuse(
                "Erika",
                "Mustermann",
                LocalDate.of(1990, 6, 1),
                vereinId,
                "Bonn",
                "53111"
        );
    }

    /* =========================================================
       BASISFILTER
       ========================================================= */

    @Test
    void search_byName_returnsMatchingPersons() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/person/search")
                                        .param("name", "Muster")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void search_aktivFalse_returnsEmptyResult() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/person/search")
                                        .param("aktiv", "false")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /* =========================================================
       EINZELFILTER
       ========================================================= */

    @Test
    void search_byVerein_filtersCorrectly() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/person/search")
                                        .param("vereinId", vereinId.toString())
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void search_byAlterRange_filtersCorrectly() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/person/search")
                                        .param("alterMin", "18")
                                        .param("alterMax", "25")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vorname").value("Max"));
    }

    /* =========================================================
       KOMBINIERTE FILTER
       ========================================================= */

    @Test
    void search_multipleFilters_AND_combined() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/person/search")
                                        .param("name", "Muster")
                                        .param("alterMin", "18")
                                        .param("alterMax", "30")
                                        .param("vereinId", vereinId.toString())
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vorname").value("Max"));
    }

    @Test
    void search_byPlzAndOrt_combined() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/person/search")
                                        .param("plz", "50667")
                                        .param("ort", "Köln")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vorname").value("Max"));
    }

    /* =========================================================
       PAGING
       ========================================================= */

    @Test
    void search_withFilterAndPaging() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/person/search")
                                        .param("name", "Muster")
                                        .param("page", "0")
                                        .param("size", "1")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_withPaging_stillWorks() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/person/search")
                                        .param("page", "0")
                                        .param("size", "10")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}