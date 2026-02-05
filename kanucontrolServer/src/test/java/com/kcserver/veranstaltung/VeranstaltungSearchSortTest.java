package com.kcserver.veranstaltung;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.testdata.PersonTestFactory;
import com.kcserver.testdata.VeranstaltungTestFactory;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VeranstaltungSearchSortTest
        extends AbstractTenantIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private VeranstaltungTestFactory veranstaltungFactory;
    private Long vereinId;
    private Long leiterId;

    @BeforeEach
    void setup() throws Exception {

        PersonTestFactory personFactory =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        VereinTestFactory vereinFactory =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        veranstaltungFactory =
                new VeranstaltungTestFactory(mockMvc, objectMapper, tenantAuth());

        leiterId = personFactory.createPerson(
                "Leiter",
                "Test",
                LocalDate.of(2000, 1, 1),
                null
        );

        vereinId = vereinFactory.create("TV", "Test Verein");
    }

    @Test
    void sortByBeginnDatumAsc_returnsCorrectOrder() throws Exception {

        // GIVEN
        Long v1 = veranstaltungFactory.createWithDate(
                vereinId,
                leiterId,
                "Früh",
                LocalDate.now().plusDays(5)
        );

        Long v2 = veranstaltungFactory.createWithDate(
                vereinId,
                leiterId,
                "Mittel",
                LocalDate.now().plusDays(10)
        );

        Long v3 = veranstaltungFactory.createWithDate(
                vereinId,
                leiterId,
                "Spät",
                LocalDate.now().plusDays(20)
        );

        // WHEN
        String response = mockMvc.perform(
                        tenantRequest(
                                get("/api/veranstaltung")
                                        .param("sort", "beginnDatum,asc")
                                        .accept(MediaType.APPLICATION_JSON)
                        )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        JsonNode content = json.get("content");

        // THEN
        assertThat(content).hasSize(3);

        Long firstId  = content.get(0).get("id").asLong();
        Long secondId = content.get(1).get("id").asLong();
        Long thirdId  = content.get(2).get("id").asLong();

        assertThat(firstId).isEqualTo(v1);
        assertThat(secondId).isEqualTo(v2);
        assertThat(thirdId).isEqualTo(v3);
    }
    @Test
    void sortByBeginnDatumDesc_returnsCorrectOrder() throws Exception {

        Long v1 = veranstaltungFactory.createWithDate(vereinId, leiterId, "Früh",
                LocalDate.now().plusDays(5));

        Long v2 = veranstaltungFactory.createWithDate(vereinId, leiterId, "Mittel",
                LocalDate.now().plusDays(10));

        Long v3 = veranstaltungFactory.createWithDate(vereinId, leiterId, "Spät",
                LocalDate.now().plusDays(20));

        String response = mockMvc.perform(
                        tenantRequest(
                                get("/api/veranstaltung")
                                        .param("sort", "beginnDatum,desc")
                                        .accept(MediaType.APPLICATION_JSON)
                        )
                ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode content = objectMapper.readTree(response).get("content");

        assertThat(content.get(0).get("id").asLong()).isEqualTo(v3);
        assertThat(content.get(1).get("id").asLong()).isEqualTo(v2);
        assertThat(content.get(2).get("id").asLong()).isEqualTo(v1);
    }

    @Test
    void pagination_returnsCorrectPage() throws Exception {

        for (int i = 0; i < 5; i++) {
            veranstaltungFactory.createWithDate(
                    vereinId, leiterId, "Event " + i,
                    LocalDate.now().plusDays(i)
            );
        }

        String response = mockMvc.perform(
                        tenantRequest(
                                get("/api/veranstaltung")
                                        .param("page", "1")
                                        .param("size", "2")
                                        .param("sort", "beginnDatum,asc")
                                        .accept(MediaType.APPLICATION_JSON)
                        )
                ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode content = objectMapper.readTree(response).get("content");

        assertThat(content).hasSize(2);
    }

    @Test
    void multiSort_returnsStableSortedOrder() throws Exception {

        veranstaltungFactory.createWithDate(vereinId, leiterId, "B",
                LocalDate.now().plusDays(10));

        veranstaltungFactory.createWithDate(vereinId, leiterId, "A",
                LocalDate.now().plusDays(10));

        String response = mockMvc.perform(
                        tenantRequest(
                                get("/api/veranstaltung")
                                        .param("sort", "beginnDatum,asc")
                                        .param("sort", "name,asc")
                                        .accept(MediaType.APPLICATION_JSON)
                        )
                ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode content = objectMapper.readTree(response).get("content");

        assertThat(content.get(0).get("name").asText()).isEqualTo("A");
        assertThat(content.get(1).get("name").asText()).isEqualTo("B");
    }

    @Test
    void filterByName_returnsMatchingOnly() throws Exception {

        veranstaltungFactory.create(vereinId, leiterId, "Sommerlager");
        veranstaltungFactory.create(vereinId, leiterId, "Winterlager");

        String response = mockMvc.perform(
                        tenantRequest(
                                get("/api/veranstaltung")
                                        .param("name", "Sommer")
                                        .accept(MediaType.APPLICATION_JSON)
                        )
                ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode content = objectMapper.readTree(response).get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("name").asText()).contains("Sommer");
    }

    @Test
    void filterByAktiv_returnsOnlyActive() throws Exception {

        Long active = veranstaltungFactory.create(vereinId, leiterId, "Aktiv");
        Long inactive = veranstaltungFactory.createInactive(
                vereinId, leiterId, "Inaktiv", active);

        String response = mockMvc.perform(
                        tenantRequest(
                                get("/api/veranstaltung")
                                        .param("aktiv", "true")
                                        .accept(MediaType.APPLICATION_JSON)
                        )
                ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode content = objectMapper.readTree(response).get("content");

        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("id").asLong()).isEqualTo(active);
    }

    @Test
    void filter_returnsEmpty_whenNoMatch() throws Exception {

        veranstaltungFactory.create(vereinId, leiterId, "Sommerlager");

        String response = mockMvc.perform(
                        tenantRequest(
                                get("/api/veranstaltung")
                                        .param("name", "XYZ_NOT_FOUND")
                                        .accept(MediaType.APPLICATION_JSON)
                        )
                ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode content = objectMapper.readTree(response).get("content");

        assertThat(content).isEmpty();
    }

    @Test
    void stableOrder_whenSameBeginnDatum() throws Exception {

        LocalDate sameDate = LocalDate.now().plusDays(10);

        Long v1 = veranstaltungFactory.createWithDate(vereinId, leiterId, "A", sameDate);
        Long v2 = veranstaltungFactory.createWithDate(vereinId, leiterId, "B", sameDate);

        String response = mockMvc.perform(
                        tenantRequest(
                                get("/api/veranstaltung")
                                        .param("sort", "beginnDatum,asc")
                                        .param("sort", "id,asc")
                                        .accept(MediaType.APPLICATION_JSON)
                        )
                ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode content = objectMapper.readTree(response).get("content");

        assertThat(content.get(0).get("id").asLong()).isEqualTo(v1);
        assertThat(content.get(1).get("id").asLong()).isEqualTo(v2);
    }
}

