package com.kcserver.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public abstract class AbstractApiTestFactory {

    protected final MockMvc mockMvc;
    protected final ObjectMapper objectMapper;
    protected final RequestPostProcessor auth;

    protected AbstractApiTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            RequestPostProcessor auth
    ) {
        if (auth == null) {
            throw new IllegalStateException("Auth RequestPostProcessor is NULL");
        }
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.auth = auth;
    }

    /* =========================================================
       🔑 Zentrale Request-Methode (AUTH + JSON)
       Tenant kommt aus JWT → Filter → TenantContext
       ========================================================= */

    protected MockHttpServletRequestBuilder req(
            MockHttpServletRequestBuilder builder
    ) {
        return builder
                .with(auth)   // ⭐ enthält Tenant im JWT
                .contentType(MediaType.APPLICATION_JSON);
    }
}