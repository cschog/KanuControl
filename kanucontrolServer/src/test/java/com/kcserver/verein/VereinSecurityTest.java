package com.kcserver.verein;

import com.kcserver.dto.VereinDTO;
import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-security")
class VereinSecurityTest extends AbstractIntegrationTest {

    @Test
    void getAll_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/verein"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/verein/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withoutJwt_returns401() throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setName("Test Verein");
        dto.setAbk("TV");

        mockMvc.perform(
                        post("/api/verein")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_withoutJwt_returns401() throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setName("Updated Verein");
        dto.setAbk("UV");

        mockMvc.perform(
                        put("/api/verein/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_withoutJwt_returns401() throws Exception {
        mockMvc.perform(delete("/api/verein/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }
}