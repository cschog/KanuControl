package com.kcserver.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.person.PersonDTO;
import com.kcserver.enumtype.Sex;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("person-crud")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PersonDeleteTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    /* =========================================================
       Helper
       ========================================================= */

    private Long createPerson() throws Exception {

        PersonDTO dto = new PersonDTO();
        dto.setVorname("Max");
        dto.setName("Mustermann");
        dto.setGeburtsdatum(LocalDate.of(1990, 1, 1));
        dto.setSex(Sex.MAENNLICH);

        String response =
                mockMvc.perform(
                                post("/api/person")
                                        .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                                        .with(jwt())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    /* =========================================================
       TESTS
       ========================================================= */

    @Test
    void deleteExistingPerson_returns204() throws Exception {

        Long id = createPerson();

        mockMvc.perform(
                        delete("/api/person/{id}", id)
                                .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                                .with(jwt())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePerson_thenGetById_returns404() throws Exception {

        Long id = createPerson();

        mockMvc.perform(
                        delete("/api/person/{id}", id)
                                .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                                .with(jwt())
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/person/{id}", id)
                                .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                                .with(jwt())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNonExistingPerson_returns404() throws Exception {

        mockMvc.perform(
                        delete("/api/person/{id}", 99999L)
                                .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                                .with(jwt())
                )
                .andExpect(status().isNotFound());
    }
}