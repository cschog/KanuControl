package com.kcserver.repository;

import com.kcserver.entity.Person;
import com.kcserver.service.PersonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@SpringJUnitConfig
public class PersonRepositoryTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    @Test
    public void testFindByName() {
        String nameToFind = "Schog";
        List<Person> expectedPersons = new ArrayList<>();

        // Create some mock Person objects with the name "Schog"
        Person person1 = new Person("Schog", "Chris", "Address1", "12345", "City1", "123-456", "Bank1", "DE123", "BIC1");
        Person person2 = new Person("Schog", "Hildegard", "Address2", "67890", "City2", "789-012", "Bank2", "DE456", "BIC2");
        expectedPersons.add(person1);
        expectedPersons.add(person2);

        // Mock the repository behavior
        Mockito.when(personRepository.findByName(nameToFind)).thenReturn(expectedPersons);

        // Call the service method that uses the repository
        List<Person> actualPersons = personService.getPersonsByName(nameToFind);

        // Verify that the expected and actual lists of Persons match
        assertEquals(expectedPersons, actualPersons);
    }

    @Test
    public void testFindByNameIs() {
        String nameToFind = "Schog";
        Person expectedPerson = new Person("Schog", "Chris", "Address1", "12345", "City1", "123-456", "Bank1", "DE123", "BIC1");

        // Mock the repository behavior
        Mockito.when(personRepository.findByNameIs(nameToFind)).thenReturn(expectedPerson);

        // Call the service method that uses the repository
        Person actualPerson = personService.getPersonByName(nameToFind);

        // Verify that the expected and actual Persons match
        assertEquals(expectedPerson, actualPerson);
    }
}

