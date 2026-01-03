package com.kcserver.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.enumtype.MitgliedFunktion;
import com.kcserver.enumtype.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PersonSearchIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    Long vereinId;
    private String tenant;

    @BeforeEach
    void setupData() throws Exception {

        tenant = "test_" + System.currentTimeMillis();

        // Verein anlegen
        String vereinResponse =
                mockMvc.perform(
                                post("/api/verein")
                                        .header("X-Tenant", tenant)
                                        .with(jwt())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                                            {
                                              "name": "EKC",
                                              "abk": "EKC"
                                            }
                                            """)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        vereinId = objectMapper.readTree(vereinResponse).get("id").asLong();

        createPerson("Max", "Mustermann", LocalDate.of(2005, 1, 1), "Köln", "50667");
        createPerson("Erika", "Mustermann", LocalDate.of(1990, 6, 1), "Bonn", "53111");
    }

    private void createPerson(
            String vorname,
            String name,
            LocalDate geburtsdatum,
            String ort,
            String plz
    ) throws Exception {

        PersonDTO dto = new PersonDTO();
        dto.setVorname(vorname);
        dto.setName(name);
        dto.setGeburtsdatum(geburtsdatum);
        dto.setSex(Sex.WEIBLICH);
        dto.setOrt(ort);
        dto.setPlz(plz);

        MitgliedDTO mitglied = new MitgliedDTO();
        mitglied.setVereinId(vereinId);
        mitglied.setFunktion(MitgliedFunktion.JUGENDWART);
        mitglied.setHauptVerein(true);

        dto.setMitgliedschaften(List.of(mitglied));

        mockMvc.perform(
                        post("/api/person")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    /* =========================================================
       TESTS
       ========================================================= */

    @Test
    void search_byName_returnsMatchingPersons() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .param("name", "Muster")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
    @Test
    void search_aktivFalse_returnsEmptyResult() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .param("aktiv", "false")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_bySex_filtersCorrectly() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .param("sex", "WEIBLICH")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void search_byVerein_filtersCorrectly() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .param("vereinId", vereinId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
    @Test
    void search_byAlterRange_filtersCorrectly() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .param("alterMin", "18")
                                .param("alterMax", "25")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vorname").value("Max"));
    }
    @Test
    void search_multipleFilters_AND_combined() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .param("name", "Muster")
                                .param("alterMin", "18")
                                .param("alterMax", "30")
                                .param("vereinId", vereinId.toString())
                                .param("sex", "WEIBLICH")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vorname").value("Max"));
    }

    @Test
    void search_byPlzAndOrt_combined() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .param("plz", "50667")
                                .param("ort", "Köln")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vorname").value("Max"));
    }

    @Test
    void search_withFilterAndPaging() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .param("name", "Muster")
                                .param("page", "0")
                                .param("size", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_withPaging_stillWorks() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}