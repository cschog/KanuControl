package com.kcserver.service;

import com.kcserver.dto.PersonDTO;
import com.kcserver.entity.Person;
import com.kcserver.mapper.EntityMapper;
import com.kcserver.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private PersonService personService;

    @Test
    void shouldReturnPersonDTOWhenPersonExists() {
        // Arrange
        Person person = new Person(1L, "Doe", "John", "Street 1", "12345", "City", "123456789", "Bank", "IBAN", "BIC", null);
        PersonDTO personDTO = new PersonDTO(1L, "Doe", "John", "Street 1", "12345", "City", "123456789", "Bank", "IBAN", "BIC", null);

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(mapper.toPersonDTO(person)).thenReturn(personDTO);

        // Act
        PersonDTO result = personService.getPerson(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Doe", result.getName());
        verify(personRepository).findById(1L);
        verify(mapper).toPersonDTO(person);
    }

    @Test
    void shouldThrowExceptionWhenPersonNotFound() {
        // Arrange
        when(personRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> personService.getPerson(1L));
    }
}