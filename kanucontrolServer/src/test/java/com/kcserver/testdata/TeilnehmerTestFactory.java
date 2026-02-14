package com.kcserver.testdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeilnehmerTestFactory extends AbstractApiTestFactory {

    public TeilnehmerTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            RequestPostProcessor auth
    ) {
        super(mockMvc, objectMapper, auth);
    }

    public Long createActiveVeranstaltung(Long vereinId, Long leiterId) throws Exception {

        var dto = new java.util.HashMap<String, Object>();
        dto.put("name", "Test Veranstaltung");
        dto.put("typ", "JEM");
        dto.put("aktiv", true);
        dto.put("vereinId", vereinId);
        dto.put("leiterId", leiterId);
        dto.put("beginnDatum", java.time.LocalDate.now().toString());
        dto.put("beginnZeit", "10:00:00");
        dto.put("endeDatum", java.time.LocalDate.now().plusDays(5).toString());
        dto.put("endeZeit", "18:00:00");

        MvcResult result = mockMvc.perform(
                        req(post("/api/veranstaltung"))
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    public Long addTeilnehmer(Long veranstaltungId, Long personId) throws Exception {

        MvcResult result = mockMvc.perform(
                        req(post("/api/veranstaltung/" + veranstaltungId + "/teilnehmer/" + personId))
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    public void removeTeilnehmer(Long veranstaltungId, Long teilnehmerId) throws Exception {

        mockMvc.perform(
                req(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/veranstaltung/" + veranstaltungId + "/teilnehmer/" + teilnehmerId))
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());
    }
}