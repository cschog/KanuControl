package com.kcserver.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.person.PersonSaveDTO;
import com.kcserver.enumtype.Sex;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("person-crud")
class PersonCreateTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    /* =========================================================
       1. HAPPY PATH
       ========================================================= */

    @Test
    void createPerson_valid_returns201() throws Exception {

        PersonSaveDTO dto = basePerson("Max", "Mustermann", LocalDate.of(1990,1,1));

        mockMvc.perform(

                                post("/api/person")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))

                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.vorname").value("Max"));
    }

    /* =========================================================
       2. DUPLICATE BLOCK
       ========================================================= */

    @Test
    void createPerson_duplicate_shouldReturn409() throws Exception {

        PersonSaveDTO dto = basePerson("Max", "Mustermann", LocalDate.of(1990,1,1));

        // first
        mockMvc.perform(post("/api/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // duplicate
        mockMvc.perform(post("/api/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    /* =========================================================
       3. DUPLICATE ALLOWED (VARIATION)
       ========================================================= */

    @Test
    void createPerson_sameNameDifferentBirthdate_shouldWork() throws Exception {

        PersonSaveDTO p1 = basePerson("Max", "Mustermann", LocalDate.of(1990,1,1));
        PersonSaveDTO p2 = basePerson("Max", "Mustermann", LocalDate.of(1995,1,1));

        mockMvc.perform(post("/api/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p2)))
                .andExpect(status().isCreated());
    }

    /* =========================================================
       4. RESPONSE STRUCTURE
       ========================================================= */

    @Test
    void createPerson_shouldReturnDetailDTO() throws Exception {

        PersonSaveDTO dto = basePerson("Lisa", "Schmidt", LocalDate.of(2001,3,3));

        mockMvc.perform(
                        post("/api/person")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.mitgliedschaften").isArray());
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private PersonSaveDTO basePerson(String vorname, String name, LocalDate geburt) {
        PersonSaveDTO dto = new PersonSaveDTO();
        dto.setVorname(vorname);
        dto.setName(name);
        dto.setSex(Sex.MAENNLICH);
        dto.setGeburtsdatum(geburt);
        return dto;
    }
}