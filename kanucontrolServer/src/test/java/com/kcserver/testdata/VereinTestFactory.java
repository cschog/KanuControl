package com.kcserver.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.VereinDTO;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tenant-agnostische Test-Factory für Vereine.
 * Erwartet einen RequestPostProcessor (z.B. jwtWithTenant),
 * damit keine Abhängigkeit zu Test-Basisklassen entsteht.
 */
public class VereinTestFactory {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final RequestPostProcessor tenant;

    public VereinTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            RequestPostProcessor tenant
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.tenant = tenant;
    }

    /* =========================================================
       Öffentliche API
       ========================================================= */

    /**
     * Legt einen Verein an, falls er noch nicht existiert
     * (Suche über abk + name).
     */
    public Long createIfNotExists(String abk, String name) throws Exception {

        Optional<Long> existing = findVereinId(abk, name);
        if (existing.isPresent()) {
            return existing.get();
        }

        return create(abk, name);
    }

    /**
     * Legt immer einen neuen Verein an.
     */
    public Long create(String abk, String name) throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setAbk(abk);
        dto.setName(name);

        var result =
                mockMvc.perform(
                                post("/api/verein")
                                        .with(tenant)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                        .andExpect(status().isCreated())
                        .andReturn();

        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    /* =========================================================
       Intern
       ========================================================= */

    private Optional<Long> findVereinId(String abk, String name) throws Exception {

        var result =
                mockMvc.perform(
                                get("/api/verein/search")
                                        .with(tenant)
                                        .param("abk", abk)
                                        .param("name", name)
                        )
                        .andReturn();

        if (result.getResponse().getStatus() != 200) {
            return Optional.empty();
        }

        var tree = objectMapper.readTree(result.getResponse().getContentAsString());

        if (tree.isArray() && tree.size() > 0) {
            return Optional.of(tree.get(0).get("id").asLong());
        }

        return Optional.empty();
    }
}