package com.kcserver.person;

import com.kcserver.dto.PersonDTO;
import com.kcserver.enumtype.Sex;
import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("person-security")
class PersonSecurityTest extends AbstractIntegrationTest {

    @Test
    void createPerson_withoutJwt_returns401() throws Exception {

        PersonDTO dto = new PersonDTO();
        dto.setVorname("Max");
        dto.setName("Mustermann");
        dto.setSex(Sex.MAENNLICH);
        dto.setGeburtsdatum(LocalDate.of(1990, 1, 1));

        mockMvc.perform(
                        post("/api/person")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPerson_withoutJwt_returns401() throws Exception {

        mockMvc.perform(
                        get("/api/person")
                )
                .andExpect(status().isUnauthorized());
    }
}