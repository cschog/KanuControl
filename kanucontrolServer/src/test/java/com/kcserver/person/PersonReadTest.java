package com.kcserver.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.testdata.PersonTestFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("person-crud")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PersonReadTest extends AbstractTenantIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper; // ✅ DAS fehlte


    @Test
    void getAllPersons_returnsList() throws Exception {

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        personen.createOrReuse(
                "Max",
                "Mustermann",
                LocalDate.of(1990, 1, 1),
                null   // 👈 kein Verein nötig für diesen Test
        );

        mockMvc.perform(
                        tenantRequest(get("/api/person"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].vorname").value(hasItem("Max")))
                .andExpect(jsonPath("$[*].name").value(hasItem("Mustermann")));
    }

    @Test
    void getPersonById_returnsPerson() throws Exception {

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        Long personId =
                personen.createOrReuse(
                        "Erika",
                        "Mustermann",
                        LocalDate.of(1995, 5, 5),
                        null
                );

        mockMvc.perform(
                        tenantRequest(get("/api/person/{id}", personId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId))
                .andExpect(jsonPath("$.vorname").value("Erika"))
                .andExpect(jsonPath("$.name").value("Mustermann"));
    }
}