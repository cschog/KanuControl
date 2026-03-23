package com.kcserver.support.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public abstract class AbstractApiTestFactory {

    protected final MockMvc mockMvc;
    protected final ObjectMapper objectMapper;

    protected final RequestPostProcessor security;

    public AbstractApiTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            RequestPostProcessor security
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.security = security;
    }
}