package com.kcserver.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.PersonDTO;
import com.kcserver.enumtype.Sex;
import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("person-crud")
/*@WithTestTenant("tenant_test_2")
* aktuell wird immer Schema Kanu verwendet */
class PersonSearchSortTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        createPerson("Max", "Mustermann", Sex.MAENNLICH, true, LocalDate.of(1990,1,1));
        createPerson("Erika", "Mustermann", Sex.WEIBLICH, true, LocalDate.of(1991,1,1));
        createPerson("Paul", "Meier", Sex.MAENNLICH, true, LocalDate.of(1992,1,1));
        createPerson("Theo", "Becker", Sex.MAENNLICH, true, LocalDate.of(2019,1,1));
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

        PersonDTO dto = new PersonDTO();
        dto.setVorname(vorname);
        dto.setName(name);
        dto.setSex(sex);
        dto.setAktiv(aktiv);
        dto.setGeburtsdatum(geburtsdatum);

        mockMvc.perform(
                post("/api/person")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isCreated());
    }

    /* =========================================================
       TESTS
       ========================================================= */
    @Test
    void search_sortedByNameAsc() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .with(jwt())
                                .param("sort", "name,asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Becker"))
                .andExpect(jsonPath("$[1].name").value("Meier"))
                .andExpect(jsonPath("$[2].name").value("Mustermann"))
                .andExpect(jsonPath("$[3].name").value("Mustermann"));
    }

    @Test
    void search_sortedByNameDesc() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .with(jwt())
                                .param("sort", "name,desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mustermann"))
                .andExpect(jsonPath("$[3].name").value("Becker"));
    }

    @Test
    void search_sortedByVornameAsc() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .with(jwt())
                                .param("sort", "vorname,asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vorname").value("Erika"))
                .andExpect(jsonPath("$[3].vorname").value("Theo"));
    }

    @Test
    void search_withPaging_returnsSecondPage() throws Exception {

        mockMvc.perform(
                        get("/api/person/search")
                                .with(jwt())
                                .param("sort", "name,asc")
                                .param("page", "1")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Mustermann"));
    }
}