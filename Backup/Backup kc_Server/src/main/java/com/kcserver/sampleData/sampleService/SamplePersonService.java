package com.kcserver.sampleData.sampleService;

import com.kcserver.entity.Person;
import com.kcserver.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SamplePersonService {
    private static final Logger logger = LoggerFactory.getLogger(SamplePersonService.class);

    private final PersonRepository personRepository;

    public SamplePersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void initialize() {
        if (personRepository.count() == 0) {
            logger.info("Loading sample Person data...");
            List<Person> samplePersons = List.of(
                    new Person("Schog", "Chris", "Ardennenstr. 82", "52076", "Aachen", "02408-81549", "Commerzbank Aachen", "DE671234567890", "DRESGENOW"),
                    new Person("Schog", "Hildegard", "Ardennenstr. 82", "52076", "Aachen", "02408-81549", "Commerzbank Aachen", "DE671234567890", "DRESGENOW")
            );
            personRepository.saveAll(samplePersons);
            logger.info("Sample Person data loaded successfully: {} records added.", samplePersons.size());
        } else {
            logger.info("Sample Person data already exists. Skipping initialization.");
        }
    }
}
