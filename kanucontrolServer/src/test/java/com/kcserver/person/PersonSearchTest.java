package com.kcserver.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.PersonSaveDTO;
import com.kcserver.enumtype.Sex;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("person-crud")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "kcserver.test-tenant=tenant_test_2"
})
class PersonSearchTest extends AbstractTenantIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        createPerson("Max", "Mustermann", Sex.MAENNLICH, true, LocalDate.of(1990, 1, 1));
        createPerson("Erika", "Mustermann", Sex.WEIBLICH, true, LocalDate.of(1991, 1, 1));
        createPerson("Paul", "Meier", Sex.MAENNLICH, false, LocalDate.of(1992, 1, 1));
    }

    /* =========================================================
       Helper
       ========================================================= */

    private void createPerson(
            String vorname,
            String name,
            Sex sex,
            boolean aktiv,
            LocalDate geburtsdatum
    ) throws Exception {

        PersonSaveDTO dto = new PersonSaveDTO();
        dto.setVorname(vorname);
        dto.setName(name);
        dto.setSex(sex);
        dto.setAktiv(aktiv);
        dto.setGeburtsdatum(geburtsdatum);

        mockMvc.perform(
                tenantRequest(post("/api/person"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isCreated());
    }

    /* =========================================================
       TESTS
       ========================================================= */

    @Test
    void search_byName_returnsMatchingPersons() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/person/search"))
                                .param("name", "Muster")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void search_bySex_filtersCorrectly() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/person/search"))
                                .param("sex", "WEIBLICH")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].vorname").value("Erika"));
    }

    @Test
    void search_byAktivFalse_returnsOnlyInactive() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/person/search"))
                                .param("aktiv", "false")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Meier"));
    }

    @Test
    void search_withPaging_limitsResults() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/person/search"))
                                .param("page", "0")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                // 🔑 totalElements MUSS mindestens so groß sein
                .andExpect(jsonPath("$.totalElements").value(
                        org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                ));
    }

    @Test
    void search_combinedFilters_AND_applied() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/person/search"))
                                .param("name", "Muster")
                                .param("sex", "MAENNLICH")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].vorname").value("Max"));
    }

    @Test
    void searchPersons_returnsPagedPersonListDTO() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/person/search"))
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "name,asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].mitgliedschaften").doesNotExist());
    }
}