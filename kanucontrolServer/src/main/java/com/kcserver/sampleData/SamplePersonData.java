package com.kcserver.sampleData;

import com.kcserver.entity.Person;
import com.kcserver.repository.PersonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(2)
public class SamplePersonData {
    @Bean
    public CommandLineRunner samplePerson (PersonRepository personRepository)
    {
        return (args) -> {
            personRepository.save(new Person(
                    "Schog",
                    "Chris",
                    "Ardennenstr. 82",
                    "52076",
                    "Aachen",
                    "02408-81549",
                    "Commerzbank Aachen",
                    "DE67 1234567890",
                    "DRESGENOW"
            ));
            personRepository.save(new Person(
                    "Schog",
                    "Hildegard",
                    "Ardennenstr. 82",
                    "52076",
                    "Aachen",
                    "02408-81549",
                    "Commerzbank Aachen",
                    "DE67 1234567890",
                    "DRESGENOW"
            ));
        };
    }
}
