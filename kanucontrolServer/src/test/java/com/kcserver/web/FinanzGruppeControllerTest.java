package com.kcserver.finanz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.finanz.FinanzGruppeCreateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FinanzGruppeControllerTest extends AbstractFinanzIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long veranstaltungId;

    /* =========================================================
       TEST USER (JWT + Tenant)
       ========================================================= */

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor testUser() {
        return jwt().jwt(jwt -> jwt
                .subject("test-user")
                .claim("groups", List.of("Eschweiler Kanu Club"))
        );
    }

    /* ========================================================= */

    @BeforeEach
    void setup() {
        veranstaltungId = createTestVeranstaltung();
    }

    /* =========================================================
       CREATE + LIST
       ========================================================= */

    @Test
    void shouldCreateAndListGroup() throws Exception {

        var dto = new FinanzGruppeCreateDTO("MS");

        mockMvc.perform(
                        post("/api/veranstaltungen/{vid}/finanzgruppen", veranstaltungId)
                                .with(testUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kuerzel").value("MS"));

        mockMvc.perform(
                        get("/api/veranstaltungen/{vid}/finanzgruppen", veranstaltungId)
                                .with(testUser())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kuerzel").value("MS"));
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @Test
    void shouldUpdateGroup() throws Exception {

        var create = new FinanzGruppeCreateDTO("MS");

        String response =
                mockMvc.perform(
                                post("/api/veranstaltungen/{vid}/finanzgruppen", veranstaltungId)
                                        .with(testUser())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(create))
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        Long id = node.get("id").asLong();

        var update = new FinanzGruppeCreateDTO("CS");

        mockMvc.perform(
                        put("/api/veranstaltungen/{vid}/finanzgruppen/{gid}",
                                veranstaltungId, id)
                                .with(testUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kuerzel").value("CS"));
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Test
    void shouldDeleteGroup() throws Exception {

        var create = new FinanzGruppeCreateDTO("MS");

        String response =
                mockMvc.perform(
                                post("/api/veranstaltungen/{vid}/finanzgruppen", veranstaltungId)
                                        .with(testUser())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(create))
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        Long id = node.get("id").asLong();

        mockMvc.perform(
                        delete("/api/veranstaltungen/{vid}/finanzgruppen/{gid}",
                                veranstaltungId, id)
                                .with(testUser())
                )
                .andExpect(status().isNoContent());
    }
}