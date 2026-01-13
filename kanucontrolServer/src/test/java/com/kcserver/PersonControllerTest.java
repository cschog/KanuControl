package com.kcserver;

import com.kcserver.config.TestAuditorAware;
import com.kcserver.controller.PersonController;
import com.kcserver.exception.GlobalExceptionHandler;
import com.kcserver.service.LoginService;
import com.kcserver.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PersonController.class)
@Import(TestAuditorAware.class)
@ContextConfiguration(classes = {
        PersonController.class,
        GlobalExceptionHandler.class
})
class PersonControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean PersonService personService;
    @MockBean LoginService loginService;

    @Test
    void postPerson_withInvalidPayload_returns400() throws Exception {
        mockMvc.perform(
                post("/api/person")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isBadRequest());
    }
}