package com.kcserver.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.PersonDTO;
import com.kcserver.enumtype.Sex;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("person-crud")
class PersonValidationTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    /* =========================================================
       CREATE – VALIDATION
       ========================================================= */

    @Test
    void createPerson_withEmptyVorname_returns400() throws Exception {

        PersonDTO dto = validPerson();
        dto.setVorname(" ");

        mockMvc.perform(
                        post("/api/person")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPerson_withEmptyName_returns400() throws Exception {

        PersonDTO dto = validPerson();
        dto.setName("");

        mockMvc.perform(
                        post("/api/person")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPerson_withNullSex_returns400() throws Exception {

        PersonDTO dto = validPerson();
        dto.setSex(null);

        mockMvc.perform(
                        post("/api/person")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPerson_withFutureGeburtsdatum_returns400() throws Exception {

        PersonDTO dto = validPerson();
        dto.setGeburtsdatum(LocalDate.now().plusDays(1));

        mockMvc.perform(
                        post("/api/person")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());
    }

    /* =========================================================
       Helper
       ========================================================= */

    private PersonDTO validPerson() {
        PersonDTO dto = new PersonDTO();
        dto.setVorname("Max");
        dto.setName("Mustermann");
        dto.setSex(Sex.MAENNLICH);
        dto.setGeburtsdatum(LocalDate.of(1990, 1, 1));
        dto.setAktiv(true);
        return dto;
    }
}