package com.kcserver.web;

import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("person-crud")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonControllerTest extends AbstractTenantIntegrationTest {

    @Test
    void postPerson_withInvalidPayload_returns400() throws Exception {

        // absichtlich leeres / ungültiges JSON
        mockMvc.perform(
                        tenantRequest(
                                post("/api/person")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{}")
                        )
                )
                .andExpect(status().isBadRequest());
    }
}