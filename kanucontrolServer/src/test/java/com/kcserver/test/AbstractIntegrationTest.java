package com.kcserver.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.dto.VereinDTO;
import com.kcserver.enumtype.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractIntegrationTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void resetDb() {
        jdbcTemplate.execute("TRUNCATE TABLE mitglied CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE person CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE verein CASCADE");
    }

    protected Long createVerein(String name, String abk) throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setName(name);
        dto.setAbk(abk);

        String response = mockMvc.perform(
                        post("/api/verein")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, VereinDTO.class).getId();
    }
    protected Long createPerson(String vorname, String name) throws Exception {

        PersonDTO dto = new PersonDTO();
        dto.setVorname(vorname);
        dto.setName(name);
        dto.setSex(Sex.MAENNLICH);          // Pflichtfeld
        dto.setAktiv(true);         // sinnvoller Default

        String response = mockMvc.perform(
                        post("/api/person")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, PersonDTO.class).getId();
    }

    protected Long createMitglied(Long personId, Long vereinId, boolean hauptVerein) throws Exception {

        MitgliedDTO dto = new MitgliedDTO();
        dto.setPersonId(personId);
        dto.setVereinId(vereinId);
        dto.setHauptVerein(hauptVerein);

        String response = mockMvc.perform(
                        post("/api/mitglied")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, MitgliedDTO.class).getId();
    }
}