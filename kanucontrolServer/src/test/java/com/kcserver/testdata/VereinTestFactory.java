package com.kcserver.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.VereinDTO;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VereinTestFactory {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final String tenant;

    public VereinTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            String tenant
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.tenant = tenant;
    }

    /** Öffentliche, fachliche API */
    public Long create(String abk, String name) throws Exception {
        VereinDTO dto = new VereinDTO();
        dto.setAbk(abk);
        dto.setName(name);
        return create(dto);
    }

    public Long createIfNotExists(String abk, String name) throws Exception {

        System.out.println("🔎 createIfNotExists aufgerufen mit:");
        System.out.println("   abk = " + abk);
        System.out.println("   name = " + name);

        Optional<Long> existing = findVereinId(abk, name);

        if (existing.isPresent()) {
            System.out.println("✅ Verein EXISTIERT bereits, id = " + existing.get());
            return existing.get();
        }

        System.out.println("➕ Verein EXISTIERT NICHT → wird angelegt");
        return createUnique(abk, name);
    }

    /** Öffentliche Convenience-Methode */
    public Long createUnique(String abk, String name) throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setAbk(abk);
        dto.setName(name);

        var result =
                mockMvc.perform(
                                post("/api/verein")
                                        .header("X-Tenant", tenant)
                                        .with(jwt())
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

    /** Interne technische Methode */
    private Long create(VereinDTO dto) throws Exception {

        var result =
                mockMvc.perform(
                                post("/api/verein")
                                        .header("X-Tenant", tenant)
                                        .with(jwt())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                        .andDo(r -> {
                            if (r.getResponse().getStatus() != 201) {
                                System.err.println("❌ Verein fehlgeschlagen");
                                System.err.println("Status: " + r.getResponse().getStatus());
                                System.err.println("Body:   " + r.getResponse().getContentAsString());
                            }
                        })
                        .andExpect(status().isCreated())
                        .andReturn();

        String body = result.getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }
    public Optional<Long> findVereinId(String abk, String name) throws Exception {

        var result =
                mockMvc.perform(
                                get("/api/verein/search")
                                        .header("X-Tenant", tenant)
                                        .with(jwt())
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