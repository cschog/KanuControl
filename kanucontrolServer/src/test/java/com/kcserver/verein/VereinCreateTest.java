package com.kcserver.verein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.VereinDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("verein-crud")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class VereinCreateTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createVerein_minimalValid_returns201() throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setAbk("EKC");
        dto.setName("Eschweiler Kanu Club");

        mockMvc.perform(
                        post("/api/verein")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.abk").value("EKC"))
                .andExpect(jsonPath("$.name").value("Eschweiler Kanu Club"));
    }

    @Test
    void createVerein_sameAbkAndName_twice_returns409() throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setAbk("EKC");
        dto.setName("Eschweiler Kanu Club");

        mockMvc.perform(post("/api/verein")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/verein")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }
}