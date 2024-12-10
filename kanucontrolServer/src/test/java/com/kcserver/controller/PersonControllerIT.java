package com.kcserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.entity.Person;
import com.kcserver.repository.PersonRepository;
import com.kcserver.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PersonControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService; // This mocks the service layer

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private PersonDTO samplePersonDTO;

    @BeforeEach
    void setup() {
        // Ensure the database contains expected test data
        personRepository.save(new Person(
                "Doe",
                "John",
                "Main Street 123",
                "12345",
                "Test City",
                "123-456-7890",
                "Bank",
                "IBAN123",
                "BIC123"
        ));
    }

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

        // Mock the service call
        when(personService.getAllPersonsWithDetails()).thenReturn(samplePersons);

        // Perform GET request
        mockMvc.perform(get("/api/person")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Schog"))
                .andExpect(jsonPath("$[0].mitgliedschaften[0].vereinName").value("Eschweiler Kanu Club"))
                .andExpect(jsonPath("$[1].vorname").value("Hildegard"));

        // Verify service call
        verify(personService, times(1)).getAllPersonsWithDetails();
    }

    @Test
    void shouldReturnPersonById() throws Exception {
        // Create a mock PersonDTO
        PersonDTO mockPerson = new PersonDTO(
                1L,
                "Smith",
                "John",
                "123 Main Street",
                "12345",
                "City",
                "123-456-7890",
                "Bank",
                "IBAN123",
                "BIC123",
                List.of(new MitgliedDTO(1L, 1L, "Test Verein", "TV", "Member", true))
        );

        // Mock the service behavior
        when(personService.getPersonWithDetails(1L)).thenReturn(mockPerson);

        // Perform GET request
        mockMvc.perform(get("/api/person/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk()) // Check for HTTP 200
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Smith"))
                .andExpect(jsonPath("$.vorname").value("John"))
                .andExpect(jsonPath("$.strasse").value("123 Main Street"));
    }

    @Test
    void shouldCreateNewPerson() throws Exception {
        // Arrange
        PersonDTO inputPerson = new PersonDTO(
                null, "Smith", "Jane", "High Street 100", "67890", "New Town",
                "987-654-3210", "Other Bank", "IBAN987", "BIC987", null
        );

        PersonDTO createdPerson = new PersonDTO(
                1L, "Smith", "Jane", "High Street 100", "67890", "New Town",
                "987-654-3210", "Other Bank", "IBAN987", "BIC987", null
        );

        when(personService.createPerson(Mockito.any(PersonDTO.class))).thenReturn(createdPerson);

        // Act & Assert
        mockMvc.perform(post("/api/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputPerson)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Smith"))
                .andExpect(jsonPath("$.vorname").value("Jane"));

        verify(personService, times(1)).createPerson(Mockito.any(PersonDTO.class));
    }

    @Test
    void shouldUpdatePerson() throws Exception {
        // Mock the updated PersonDTO
        PersonDTO updatedPerson = new PersonDTO(
                1L,
                "Doe",
                "John",
                "Updated Street 123",
                "54321",
                "Updated City",
                "123-456-0000",
                "Updated Bank",
                "IBANUPDATED",
                "BICUPDATED",
                null
        );

        // Mock the service behavior
        when(personService.updatePerson(eq(1L), any(PersonDTO.class))).thenReturn(updatedPerson);

        // Perform PUT request
        mockMvc.perform(put("/api/person/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "name": "Doe",
                    "vorname": "John",
                    "strasse": "Updated Street 123",
                    "plz": "54321",
                    "ort": "Updated City",
                    "telefon": "123-456-0000",
                    "bank": "Updated Bank",
                    "iban": "IBANUPDATED",
                    "bic": "BICUPDATED"
                }
            """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Ensure status 200
                .andExpect(jsonPath("$.name").value("Doe"))
                .andExpect(jsonPath("$.vorname").value("John"))
                .andExpect(jsonPath("$.strasse").value("Updated Street 123"))
                .andExpect(jsonPath("$.plz").value("54321"))
                .andExpect(jsonPath("$.ort").value("Updated City"))
                .andExpect(jsonPath("$.telefon").value("123-456-0000"))
                .andExpect(jsonPath("$.bankName").value("Updated Bank"))
                .andExpect(jsonPath("$.iban").value("IBANUPDATED"))
                .andExpect(jsonPath("$.bic").value("BICUPDATED"));
    }

    @Test
    void shouldDeletePerson() throws Exception {
        when(personService.deletePerson(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/person/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldUpdatePersonWithMitgliedschaften() throws Exception {
        PersonDTO updatedPersonDTO = new PersonDTO(
                1L, "Updated Name", "Updated Vorname", "Updated Strasse", "54321", "Updated Ort",
                "987654321", "Updated Bank", "Updated IBAN", "Updated BIC",
                List.of(new MitgliedDTO(1L, 1L, "Updated Verein", "Updated Abk", "Updated Funktion", true))
        );

        when(personService.updatePerson(eq(1L), any(PersonDTO.class))).thenReturn(updatedPersonDTO);

        mockMvc.perform(put("/api/person/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPersonDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mitgliedschaften[0].vereinName").value("Updated Verein"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingPerson() throws Exception {
        when(personService.deletePerson(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/person/999"))
                .andExpect(status().isNotFound());
    }
}