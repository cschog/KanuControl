package com.kcserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.KC_Server;
import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.service.PersonService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = KC_Server.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PersonControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllPersons() throws Exception {
        // Sample data
        List<PersonDTO> samplePersons = List.of(
                new PersonDTO(1L, "Schog", "Chris", "Ardennenstr. 82", "52076", "Aachen", "02408-81549",
                        "Commerzbank Aachen", "DE671234567890", "DRESGENOW",
                        List.of(new MitgliedDTO(1L, 1L, "Eschweiler Kanu Club", "EKC", "Geschäftsführer", true))),
                new PersonDTO(2L, "Schog", "Hildegard", "Ardennenstr. 82", "52076", "Aachen", "02408-81549",
                        "Commerzbank Aachen", "DE671234567890", "DRESGENOW", null)
        );

        when(personService.getAllPersonsWithDetails()).thenReturn(samplePersons);

        // Perform GET request with mock JWT authentication
        mockMvc.perform(get("/api/person")
                        .with(jwt().jwt(builder -> builder.claim("groups", List.of("Eschweiler Kanu Club"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Schog"));

        verify(personService, times(1)).getAllPersonsWithDetails();
    }

    @Test
    void shouldCreateNewPerson() throws Exception {
        PersonDTO inputPerson = new PersonDTO(
                null, "Smith", "Jane", "High Street 100", "67890", "New Town",
                "987-654-3210", "Other Bank", "IBAN987", "BIC987", null
        );

        PersonDTO createdPerson = new PersonDTO(
                1L, "Smith", "Jane", "High Street 100", "67890", "New Town",
                "987-654-3210", "Other Bank", "IBAN987", "BIC987", null
        );

        when(personService.createPerson(Mockito.any(PersonDTO.class))).thenReturn(createdPerson);

        mockMvc.perform(post("/api/person")
                        .with(jwt().jwt(builder -> builder.claim("groups", List.of("Eschweiler Kanu Club"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputPerson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Smith"));

        verify(personService, times(1)).createPerson(Mockito.any(PersonDTO.class));
    }
}