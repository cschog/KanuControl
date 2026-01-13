package com.kcserver.verein;

import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinDeleteNotFoundTest extends AbstractIntegrationTest {

    @Test
    void deleteVerein_notFound_returns404() throws Exception {

        mockMvc.perform(
                        delete("/api/verein/{id}", 99999L)
                                .with(jwt())
                )
                .andExpect(status().isNotFound());
    }
}