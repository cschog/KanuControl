package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.testdata.PersonTestFactory;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("veranstaltung-read")
class VeranstaltungReadTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long veranstaltungId;
    Long vereinId;
    Long leiterId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        vereinId = vereine.createIfNotExists(
                "EKC",
                "Eschweiler Kanu Club"
        );

        leiterId = personen.createOrReuse(
                "Max",
                "Mustermann",
                LocalDate.of(1990, 1, 1),
                null
        );

        // Veranstaltung anlegen
        String json = mockMvc.perform(
                        tenantRequest(
                                post("/api/veranstaltung")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                createDto()
                                        ))
                        )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        veranstaltungId =
                objectMapper.readTree(json).get("id").asLong();
    }

    /* =========================================================
       TEST
       ========================================================= */

    @Test
    void getVeranstaltungById_returnsDetail() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                get("/api/veranstaltung/{id}", veranstaltungId)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(veranstaltungId))
                .andExpect(jsonPath("$.name").value("Sommerfreizeit 2026"))
                .andExpect(jsonPath("$.typ").value("JUGENDERHOLUNGSMASSNAHME"))
                .andExpect(jsonPath("$.aktiv").value(true))

                // Beziehungen (IDs)
                .andExpect(jsonPath("$.vereinId").value(vereinId))
                .andExpect(jsonPath("$.leiterId").value(leiterId))

                // Anzeigeobjekte
                .andExpect(jsonPath("$.verein.name").value("Eschweiler Kanu Club"))
                .andExpect(jsonPath("$.leiter.vorname").value("Max"))
                .andExpect(jsonPath("$.leiter.name").value("Mustermann"));
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private Object createDto() {
        return new Object() {
            public final String name = "Sommerfreizeit 2026";
            public final VeranstaltungTyp typ = VeranstaltungTyp.JUGENDERHOLUNGSMASSNAHME;
            public final Long vereinId = VeranstaltungReadTest.this.vereinId;
            public final Long leiterId = VeranstaltungReadTest.this.leiterId;
            public final LocalDate beginnDatum = LocalDate.now().plusDays(10);
            public final LocalDate endeDatum = LocalDate.now().plusDays(20);
            public final LocalTime beginnZeit = LocalTime.of(10, 0);
            public final LocalTime endeZeit = LocalTime.of(18, 0);
        };
    }
}