package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VereinTestFactory;
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
                new VereinTestFactory(mockMvc, objectMapper);

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper);

        vereinId = vereine.createIfNotExists(
                "EKC",
                "Eschweiler Kanu Club"
        );

        leiterId = personen.create(b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(java.time.LocalDate.of(1990, 1, 1))
        );

        // Veranstaltung anlegen
        String json = mockMvc.perform(

                                post("/api/veranstaltungen")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                createDto()
                                        ))

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

                                get("/api/veranstaltungen/{id}", veranstaltungId)

                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(veranstaltungId))
                .andExpect(jsonPath("$.name").value("Sommerfreizeit 2026"))
                .andExpect(jsonPath("$.typ").value("JEM"))
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
            public final VeranstaltungTyp typ = VeranstaltungTyp.JEM;
            public final Long vereinId = VeranstaltungReadTest.this.vereinId;
            public final Long leiterId = VeranstaltungReadTest.this.leiterId;
            public final LocalDate beginnDatum = LocalDate.now().plusDays(10);
            public final LocalDate endeDatum = LocalDate.now().plusDays(20);
            public final LocalTime beginnZeit = LocalTime.of(10, 0);
            public final LocalTime endeZeit = LocalTime.of(18, 0);
        };
    }
}