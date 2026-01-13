package com.kcserver.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.enumtype.MitgliedFunktion;
import com.kcserver.enumtype.Sex;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PersonCreateIntegrationTest extends AbstractTenantIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createPerson_createsDataInTenantSchema() throws Exception {

        // 1️⃣ Verein anlegen
        String vereinResponse =
                mockMvc.perform(
                                tenantRequest(post("/api/verein"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                                                {
                                                   "name": "EKC",
                                                   "abk": "EKC",
                                                   "iban": "DE89370400440532013000",
                                                   "bankName": "Testbank"
                                                 }
                                            """)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long vereinId =
                objectMapper.readTree(vereinResponse).get("id").asLong();

        // 2️⃣ Person anlegen
        PersonDTO dto = validPerson();
        dto.getMitgliedschaften().get(0).setVereinId(vereinId);

        mockMvc.perform(
                        tenantRequest(post("/api/person"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    /* -------------------------------------------------
       Helpers
       ------------------------------------------------- */

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

    protected MockHttpServletRequestBuilder tenantRequest(
            MockHttpServletRequestBuilder builder
    ) {
        return builder
                .header("X-Tenant", TENANT)
                .with(jwt());
    }
}