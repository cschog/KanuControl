package com.kcserver.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.TeilnehmerDTO;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.VereinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Disabled("Requires Veranstaltung + Teilnehmer fully implemented")
class TeilnehmerAddIntegrationTest extends AbstractTenantIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired VereinRepository vereinRepository;
    @Autowired PersonRepository personRepository;
    @Autowired VeranstaltungRepository veranstaltungRepository;

    private Long personId;
    private Long secondPersonId;

    @BeforeEach
    void setup() throws Exception {

        // Verein
        String vereinResponse = mockMvc.perform(
                        tenantRequest(post("/api/..."))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                                               "name": "EKC",
                                               "abk": "EKC",
                                               "iban": "DE89370400440532013000",
                                               "bankName": "Testbank"
                                             }
                    """)
                ).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long vereinId = objectMapper.readTree(vereinResponse).get("id").asLong();

        // Person
        String personResponse = mockMvc.perform(
                        tenantRequest(post("/api/..."))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "vorname": "Max",
                          "name": "Mustermann",
                          "sex": "MAENNLICH",
                          "geburtsdatum": "2000-01-01"
                        }
                    """)
                ).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        personId = objectMapper.readTree(personResponse).get("id").asLong();

        // zweite Person
        String person2Response = mockMvc.perform(
                        tenantRequest(post("/api/..."))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                {
                  "vorname": "Erika",
                  "name": "Mustermann",
                  "sex": "WEIBLICH",
                  "geburtsdatum": "2001-01-01"
                }
            """)
                ).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        secondPersonId =
                objectMapper.readTree(person2Response).get("id").asLong();

        // Veranstaltung
        mockMvc.perform(
                tenantRequest(post("/api/..."))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Sommerfahrt",
                          "typ": "JUGENDERHOLUNGSMASSNAHME",
                          "vereinId": %d,
                          "leiterId": %d,
                          "aktiv": true
                        }
                    """.formatted(vereinId, personId))
        ).andExpect(status().isCreated());
    }
    @Test
    void UC_T1_addTeilnehmer_twice_returnsConflict() throws Exception {

        TeilnehmerDTO dto = new TeilnehmerDTO();
        dto.setPersonId(secondPersonId);

        // erstes Hinzufügen → OK
        mockMvc.perform(
                        tenantRequest(post("/api/..."))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());

        // zweites Hinzufügen → Conflict
        mockMvc.perform(
                        tenantRequest(post("/api/..."))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isConflict());
    }

    @Test
    void UC_T1_addTeilnehmer_toActiveVeranstaltung() throws Exception {

        TeilnehmerDTO dto = new TeilnehmerDTO();
        dto.setPersonId(secondPersonId);

        mockMvc.perform(
                        tenantRequest(post("/api/..."))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.personId").value(secondPersonId))
                .andExpect(jsonPath("$.rolle").value("TEILNEHMER"));
    }
}