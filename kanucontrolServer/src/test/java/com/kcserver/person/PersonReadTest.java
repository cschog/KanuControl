package com.kcserver.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.mitglied.MitgliedDTO;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("person-crud")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PersonReadTest extends AbstractTenantIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getAllPersons_returnsList() throws Exception {

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);

        personen.create(b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(LocalDate.of(1990, 1, 1))
        );

        mockMvc.perform(
                        get("/api/person")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].vorname").value(hasItem("Max")))
                .andExpect(jsonPath("$.content[*].name").value(hasItem("Mustermann")));
    }

    @Test
    void getPersonById_returnsPerson() throws Exception {

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);


        Long personId = personen.create(b ->
                b.withVorname("Erika")
                        .withName("Mustermann")
                        .withGeburtsdatum(java.time.LocalDate.of(1995, 5, 5))
        );

        mockMvc.perform(
                        get("/api/person/{id}", personId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId))
                .andExpect(jsonPath("$.vorname").value("Erika"))
                .andExpect(jsonPath("$.name").value("Mustermann"));
    }

    @Test
    void getAllPersons_returnsPersonListDTO_withoutMitgliedschaften() throws Exception {

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);

        personen.create(b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(LocalDate.of(1990, 1, 1))
        );

        mockMvc.perform(
                        get("/api/person")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].vorname").exists())
                .andExpect(jsonPath("$.content[0].name").exists())
                .andExpect(jsonPath("$.content[0].mitgliedschaften").doesNotExist())
                .andExpect(jsonPath("$.content[0].mitgliedschaftenCount").exists());
    }

    @Test
    void getPersonById_returnsPersonDetail_withVerein() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper);

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);

        Long vereinId = vereine.createIfNotExists(
                "EKC_TEST",
                "Eschweiler Kanu Club"
        );

        Long personId = personen.create(b ->
                b.withVorname("Anna")
                        .withName("Müller")
                        .withGeburtsdatum(java.time.LocalDate.of(1995, 1, 1))
        );

        MitgliedDTO m = new MitgliedDTO();
        m.setPersonId(personId);
        m.setVereinId(vereinId);
        m.setHauptVerein(true);

        mockMvc.perform(
                post("/api/mitglied")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(m))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/person/{id}", personId))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId))
                .andExpect(jsonPath("$.mitgliedschaften").isArray())
                .andExpect(jsonPath("$.mitgliedschaften[0].verein.id").value(vereinId))
                .andExpect(jsonPath("$.mitgliedschaften[0].verein.name")
                        .value("Eschweiler Kanu Club"));
    }

    @Test
    void personList_mustNeverContainVereinIdNull() throws Exception {

        mockMvc.perform(
                        get("/api/person"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].vereinId").doesNotExist());
    }
}