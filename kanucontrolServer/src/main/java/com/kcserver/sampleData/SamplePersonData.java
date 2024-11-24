package com.kcserver.sampleData;

import com.kcserver.dto.PersonDTO;
import com.kcserver.entity.Person;
import com.kcserver.repository.PersonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Order(2)
public class SamplePersonData {

    private static final Logger logger = LoggerFactory.getLogger(SamplePersonData.class);

    @Bean
    public CommandLineRunner samplePerson(PersonRepository personRepository) {
        return (args) -> {
            if (personRepository.count() == 0) { // Check if data already exists
                logger.info("Loading sample Person data...");

                List<PersonDTO> samplePersonDTOs = List.of(
                        new PersonDTO(
                                null, // ID will be auto-generated
                                "Schog",
                                "Chris",
                                "Ardennenstr. 82",
                                "52076",
                                "Aachen",
                                "02408-81549",
                                "Commerzbank Aachen",
                                "DE671234567890",
                                "DRESGENOW"
                        ),
                        new PersonDTO(
                                null,
                                "Schog",
                                "Hildegard",
                                "Ardennenstr. 82",
                                "52076",
                                "Aachen",
                                "02408-81549",
                                "Commerzbank Aachen",
                                "DE671234567890",
                                "DRESGENOW"
                        )
                );

                List<Person> samplePersons = samplePersonDTOs.stream()
                        .map(this::convertToEntity) // Convert DTOs to entities
                        .collect(Collectors.toList());

                personRepository.saveAll(samplePersons);

                logger.info("Sample Person data loaded successfully: {} records added.", samplePersons.size());
            } else {
                logger.info("Sample Person data already exists. Skipping initialization.");
            }
        };
    }

    /**
     * Converts a PersonDTO to a Person entity.
     *
     * @param personDTO The PersonDTO to convert.
     * @return The corresponding Person entity.
     */
    private Person convertToEntity(PersonDTO personDTO) {
        return new Person(
                personDTO.getName(),
                personDTO.getVorname(),
                personDTO.getStrasse(),
                personDTO.getPlz(),
                personDTO.getOrt(),
                personDTO.getTelefon(),
                personDTO.getBankName(),
                personDTO.getIban(),
                personDTO.getBic()
        );
    }
}
