package com.kcserver.verein;

import com.kcserver.dto.VereinDTO;
import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinUpdateConflictTest extends AbstractIntegrationTest {

    Long verein1Id;
    Long verein2Id;

    @BeforeEach
    void setup() throws Exception {
        verein1Id = createVerein("Eschweiler Kanu Club", "EKC");
        verein2Id = createVerein("Oberhausener Kanu Club", "OKC");
    }

    @Test
    void updateVerein_toExistingAbkAndName_returns409() throws Exception {

        VereinDTO update = new VereinDTO();
        update.setName("Eschweiler Kanu Club");
        update.setAbk("EKC");

        mockMvc.perform(
                        put("/api/verein/{id}", verein2Id)
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isConflict());
    }

    @Test
    void updateVerein_withSameValues_onSameEntity_isAllowed() throws Exception {

        VereinDTO update = new VereinDTO();
        update.setName("Eschweiler Kanu Club");
        update.setAbk("EKC");

        mockMvc.perform(
                        put("/api/verein/{id}", verein1Id)
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk());
    }
}