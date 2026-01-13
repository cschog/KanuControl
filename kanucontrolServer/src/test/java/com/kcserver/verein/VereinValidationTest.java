package com.kcserver.verein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.VereinDTO;
import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinValidationTest extends AbstractIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    /* =========================================================
       VALIDATION TESTS
       ========================================================= */

    @Test
    void createVerein_withoutName_returns400() throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setAbk("EKC");

        mockMvc.perform(
                        post("/api/verein")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVerein_withoutAbk_returns400() throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setName("Eschweiler Kanu Club");

        mockMvc.perform(
                        post("/api/verein")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVerein_withEmptyName_returns400() throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setName("   ");
        dto.setAbk("EKC");

        mockMvc.perform(
                        post("/api/verein")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVerein_withTooLongAbk_returns400() throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setName("Eschweiler Kanu Club");
        dto.setAbk("ABCDEFGHIKLMNOP"); // > @Size(max=10)

        mockMvc.perform(
                        post("/api/verein")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());
    }
}