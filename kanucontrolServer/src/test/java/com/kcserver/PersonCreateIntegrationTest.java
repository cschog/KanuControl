package com.kcserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.enumtype.MitgliedFunktion;
import com.kcserver.enumtype.Sex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PersonCreateIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createPerson_createsDataInTenantSchema() throws Exception {

        String tenant = "ekc_test";

        // 1️⃣ Verein über API
        String vereinResponse =
                mockMvc.perform(
                                post("/api/verein")
                                        .header("X-Tenant", tenant)
                                        .with(jwt())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                                {
                                  "name": "EKC",
                                  "abk": "EKC"
                                }
                                """)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long vereinId =
                objectMapper.readTree(vereinResponse).get("id").asLong();

        // 2️⃣ Person über API
        PersonDTO dto = validPerson();
        dto.getMitgliedschaften().get(0).setVereinId(vereinId);

        mockMvc.perform(
                        post("/api/person")
                                .header("X-Tenant", tenant)
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }


    private PersonDTO validPerson() {
        PersonDTO dto = new PersonDTO();
        dto.setName("Müller");
        dto.setVorname("Anna");
        dto.setGeburtsdatum(LocalDate.of(1995, 5, 12));
        dto.setSex(Sex.WEIBLICH);

        MitgliedDTO mitglied = new MitgliedDTO();
        mitglied.setFunktion(MitgliedFunktion.JUGENDWART);
        mitglied.setHauptVerein(true);

        dto.setMitgliedschaften(List.of(mitglied));
        return dto;
    }
}