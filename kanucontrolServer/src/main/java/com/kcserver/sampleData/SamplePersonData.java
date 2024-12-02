package com.kcserver.sampleData;

import com.kcserver.entity.Person;
import com.kcserver.repository.PersonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Configuration
@Order(2)
public class SamplePersonData {

    private static final Logger logger = LoggerFactory.getLogger(SamplePersonData.class);

    @Bean
    public CommandLineRunner samplePerson(PersonRepository personRepository) {
        return (args) -> {
            if (personRepository.count() == 0) { // Check if data already exists
                logger.info("Loading sample Person data...");

                List<Person> samplePersons = List.of(
                        new Person(
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
                        new Person(
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

                personRepository.saveAll(samplePersons);

                logger.info("Sample Person data loaded successfully: {} records added.", samplePersons.size());
            } else {
                logger.info("Sample Person data already exists. Skipping initialization.");
            }
        };
    }
}