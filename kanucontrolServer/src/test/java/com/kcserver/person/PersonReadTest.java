package com.kcserver.person;

import com.kcserver.entity.Person;
import com.kcserver.enumtype.Sex;
import com.kcserver.repository.PersonRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for reading Person entities.
 * Ensures correct retrieval via REST API
 * including list and single-entity access.
 */

@Tag("person-crud")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PersonReadTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PersonRepository personRepository;

    @Test
    void getAllPersons_returnsList() throws Exception {

        // given – minimal, valide Person DIREKT in DB
        Person person = new Person();
        person.setVorname("Max");
        person.setName("Mustermann");
        person.setSex(Sex.MAENNLICH);

        personRepository.save(person);

        // when / then
        mockMvc.perform(
                        get("/api/person")
                                .header("X-Tenant", "test")
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vorname").value("Max"))
                .andExpect(jsonPath("$[0].name").value("Mustermann"));
    }

    @Test
    void getPersonById_returnsPerson() throws Exception {

        // given
        Person person = new Person();
        person.setVorname("Erika");
        person.setName("Mustermann");
        person.setSex(Sex.WEIBLICH);

        Person saved = personRepository.save(person);

        // when / then
        mockMvc.perform(
                        get("/api/person/{id}", saved.getId())
                                .header("X-Tenant", "test")
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.vorname").value("Erika"))
                .andExpect(jsonPath("$.name").value("Mustermann"));
    }
}