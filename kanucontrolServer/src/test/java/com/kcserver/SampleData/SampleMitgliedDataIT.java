package com.kcserver.SampleData;

import com.kcserver.entity.Mitglied;
import com.kcserver.repository.MitgliedRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import com.kcserver.sampleData.sampleService.SampleMitgliedService;
import com.kcserver.sampleData.sampleService.SamplePersonService;
import com.kcserver.sampleData.sampleService.SampleVereinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // Use a separate test profile to avoid affecting production data
public class SampleMitgliedDataIT {

    @Autowired
    private MitgliedRepository mitgliedRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private VereinRepository vereinRepository;

    @Autowired
    private SamplePersonService samplePersonService;

    @Autowired
    private SampleVereinService sampleVereinService;

    @Autowired
    private SampleMitgliedService sampleMitgliedService;

    @BeforeEach
    void setUp() {
        // Clean up repositories before each test
        mitgliedRepository.deleteAll();
        personRepository.deleteAll();
        vereinRepository.deleteAll();

        // Run the services to initialize data
        samplePersonService.initialize();
        sampleVereinService.initialize();
    }

    @Test
    void shouldLoadSampleMitgliedData() {
        // Run the Mitglied service to initialize data
        sampleMitgliedService.initialize();

        // Verify Mitglied was created
        List<Mitglied> mitgliedList = mitgliedRepository.findAll();
        assertThat(mitgliedList).hasSize(1);

        Mitglied mitglied = mitgliedList.get(0);
        assertThat(mitglied.getPersonMitgliedschaft().getVorname()).isEqualTo("Chris");
        assertThat(mitglied.getVereinMitgliedschaft().getName()).isEqualTo("Eschweiler Kanu Club");
        assertThat(mitglied.getFunktion()).isEqualTo("Geschäftsführer");
        assertThat(mitglied.isHauptVerein()).isTrue();
    }

    @Test
    void shouldNotLoadSampleDataIfAlreadyExists() {
        // First run to insert data
        sampleMitgliedService.initialize();

        // Second run should not add duplicate data
        sampleMitgliedService.initialize();

        // Verify only one Mitglied exists
        List<Mitglied> mitgliedList = mitgliedRepository.findAll();
        assertThat(mitgliedList).hasSize(1);
    }
}