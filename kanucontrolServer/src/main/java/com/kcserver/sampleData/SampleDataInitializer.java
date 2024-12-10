package com.kcserver.sampleData;

import com.kcserver.repository.MitgliedRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

public class LoadingSampleData {

    private static final Logger logger = LoggerFactory.getLogger(SamplePersonData.class);

    @Bean
    public CommandLineRunner initializeData(
            PersonRepository personRepository,
            VereinRepository vereinRepository,
            MitgliedRepository mitgliedRepository) {

        return (args) -> {
            if (vereinRepository.count() == 0) {
                logger.info("Loading sample Verein data...");
                // Add Verein logic here

            }
            if (personRepository.count() == 0) {
                logger.info("Loading sample Person data...");
                // Add Person logic here
            }
            if (mitgliedRepository.count() == 0) {
                logger.info("Loading sample Mitglied data...");
                // Add Mitglied logic here
            }
        };
    }
}
