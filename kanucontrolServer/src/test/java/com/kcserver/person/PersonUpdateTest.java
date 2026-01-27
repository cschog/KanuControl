package com.kcserver.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.PersonDTO;
import com.kcserver.dto.PersonSaveDTO;
import com.kcserver.enumtype.Sex;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("person-crud")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PersonUpdateTest extends AbstractTenantIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    /* =========================================================
       Helper
       ========================================================= */

    private Long createPerson(
            String vorname,
            String name,
            LocalDate geburtsdatum
    ) throws Exception {

        PersonDTO dto = new PersonDTO();
        dto.setVorname(vorname);
        dto.setName(name);
        dto.setGeburtsdatum(geburtsdatum);
        dto.setSex(Sex.MAENNLICH);

        String response =
                mockMvc.perform(
                                tenantRequest(post("/api/person"))
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
    void updatePerson_withValidChanges_returns200() throws Exception {

        Long id = createPerson(
                "Max",
                "Mustermann",
                LocalDate.of(1990, 1, 1)
        );

        PersonSaveDTO update = new PersonSaveDTO();
        update.setVorname("Maximilian");
        update.setName("Mustermann");
        update.setGeburtsdatum(LocalDate.of(1990, 1, 1));
        update.setSex(Sex.MAENNLICH);

        mockMvc.perform(
                        tenantRequest(put("/api/person/{id}", id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vorname").value("Maximilian"));
    }

    @Test
    void updatePerson_toExistingNameAndBirthdate_returns409() throws Exception {

        Long id1 = createPerson(
                "Anna",
                "Meyer",
                LocalDate.of(1995, 5, 5)
        );

        Long id2 = createPerson(
                "Erika",
                "Schulz",
                LocalDate.of(1992, 3, 3)
        );

        PersonSaveDTO update = new PersonSaveDTO();
        update.setVorname("Anna");
        update.setName("Meyer");
        update.setGeburtsdatum(LocalDate.of(1995, 5, 5));
        update.setSex(Sex.WEIBLICH);

        mockMvc.perform(
                        tenantRequest(put("/api/person/{id}", id2))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isConflict());
    }

    @Test
    void updatePerson_withSameIdentity_isAllowed() throws Exception {

        Long id = createPerson(
                "Paul",
                "Berg",
                LocalDate.of(1988, 8, 8)
        );

        PersonSaveDTO update = new PersonSaveDTO();
        update.setVorname("Paul");
        update.setName("Berg");
        update.setGeburtsdatum(LocalDate.of(1988, 8, 8));
        update.setSex(Sex.MAENNLICH);

        mockMvc.perform(
                        tenantRequest(put("/api/person/{id}", id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk());
    }

    @Test
    void updateNonExistingPerson_returns404() throws Exception {

        PersonDTO update = new PersonDTO();
        update.setId(9999L);
        update.setVorname("Ghost");
        update.setName("User");
        update.setSex(Sex.MAENNLICH);

        mockMvc.perform(
                        tenantRequest(put("/api/person/{id}", 9999L))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isNotFound());
    }
}