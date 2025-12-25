package com.kcserver.SampleData;

import com.kcserver.repository.MitgliedRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Use a separate test profile to avoid affecting production data
public class SampleMitgliedDataGesamt {

    @Autowired
    private MitgliedRepository mitgliedRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private VereinRepository vereinRepository;

    @BeforeEach
    void setUp() {
        // Clean up repositories before each test
        mitgliedRepository.deleteAll();
        personRepository.deleteAll();
        vereinRepository.deleteAll();
    }
}