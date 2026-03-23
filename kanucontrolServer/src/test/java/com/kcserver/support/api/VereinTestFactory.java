package com.kcserver.support.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import com.kcserver.dto.verein.VereinDTO;
import com.kcserver.support.web.AbstractApiTestFactory;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Optional;

/**
 * Tenant-agnostische Test-Factory für Vereine.
 * Erwartet einen RequestPostProcessor (z.B. jwtWithTenant),
 * damit keine Abhängigkeit zu Test-Basisklassen entsteht.
 */
public class VereinTestFactory extends AbstractApiTestFactory {

    public VereinTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper
    ) {
        super(mockMvc, objectMapper, null);
    }

    public Long create(String abk, String name) throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setAbk(abk);
        dto.setName(name);

        var result =
                mockMvc.perform(
                                post("/api/verein")
                                        .contentType(MediaType.APPLICATION_JSON) // 🔥 DAS FEHLT
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                        .andExpect(status().isCreated())
                        .andReturn();

        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }
    public Long createIfNotExists(String abk, String name) throws Exception {

        Optional<Long> existing = findVereinId(abk, name);
        if (existing.isPresent()) {
            return existing.get();
        }

        return create(abk, name);
    }

    private Optional<Long> findVereinId(String abk, String name) throws Exception {

        var result =
                mockMvc.perform(
                        (get("/api/verein/search")
                                .param("abk", abk)
                                .param("name", name))
                ).andReturn();

        if (result.getResponse().getStatus() != 200) {
            return Optional.empty();
        }

        var tree = objectMapper.readTree(result.getResponse().getContentAsString());

        if (tree.isArray() && !tree.isEmpty()) {
            return Optional.of(tree.get(0).get("id").asLong());
        }

        return Optional.empty();
    }
}