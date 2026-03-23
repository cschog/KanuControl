package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class VeranstaltungControllerTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long vereinId;
    Long leiterId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper);
        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);

        vereinId = vereine.createIfNotExists("TV", "Testverein");

        leiterId = personen.create(b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(LocalDate.of(1990, 1, 1))
        );

        // 🔹 Veranstaltung anlegen
        VeranstaltungCreateDTO dto = new VeranstaltungCreateDTO();
        dto.setName("Sommerlager 2026");
        dto.setTyp(VeranstaltungTyp.JEM);
        dto.setVereinId(vereinId);
        dto.setLeiterId(leiterId);
        dto.setBeginnDatum(LocalDate.now().plusDays(5));
        dto.setBeginnZeit(LocalTime.of(10, 0));
        dto.setEndeDatum(LocalDate.now().plusDays(10));
        dto.setEndeZeit(LocalTime.of(18, 0));

        mockMvc.perform(
                post("/api/veranstaltungen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isCreated());
    }

    /* =========================================================
       RETURN ALL (Page → content[])
       ========================================================= */

    @Test
    void shouldReturnAllVeranstaltungen() throws Exception {

        mockMvc.perform(get("/api/veranstaltungen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Sommerlager 2026"));
    }

    /* =========================================================
       FILTER
       ========================================================= */

    @Test
    void shouldFilterByName() throws Exception {

        mockMvc.perform(get("/api/veranstaltungen")
                        .param("name", "Sommerlager"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Sommerlager 2026"));
    }

    /* =========================================================
       JSON STRUCTURE (ObjectMapper)
       ========================================================= */

    @Test
    void shouldReturnValidJsonStructure() throws Exception {

        String json = mockMvc.perform(get("/api/veranstaltungen"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);

        // 🔹 wichtig: Page → content
        JsonNode content = root.get("content");

        assertThat(content).isNotNull();
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isGreaterThan(0);

        JsonNode first = content.get(0);

        assertThat(first.get("name").asText()).isEqualTo("Sommerlager 2026");
        assertThat(first.get("id")).isNotNull();
    }
}